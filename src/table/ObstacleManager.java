package table;

import java.util.ArrayList;

import buffer.IncomingData;
import obstacles.Obstacle;
import obstacles.ObstacleProximity;
import obstacles.ObstacleRectangular;
import obstacles.ObstacleTrajectoireCourbe;
import obstacles.ObstaclesFixes;
import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.astar.arc.PathfindingNodes;
import robot.Speed;
import container.Service;
import enums.Tribool;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;

/**
 * Service qui traite tout ce qui concerne la gestion des obstacles.
 * @author pf
 *
 */

public class ObstacleManager implements Service
{
    private Log log;
    private final Table table;
    private Capteurs capteurs;
    
    // Les obstacles mobiles, c'est-à-dire des obstacles de proximité et de balise
    // Comme ces obstacles ne peuvent que disparaître, on les retient tous et chaque instance aura un indice vers sur le premier obstacle non mort
    private static ArrayList<ObstacleProximity> listObstaclesMobiles = new ArrayList<ObstacleProximity>();

    // TODO: à virer? (static bon?)
//    private static ObstacleProximity<ReadWrite> hypotheticalEnemy = null;
//    private boolean isThereHypotheticalEnemy = false;
    
    private ObstacleTrajectoireCourbe obstacleTrajectoireCourbe;
    
    private int firstNotDead = 0;

    private int distanceApproximation;
    private int dureeAvantPeremption;
	private int table_x = 3000;
	private int table_y = 2000;
	private int rayonEnnemi;
	private int nbCapteurs;

    
    public ObstacleManager(Log log, Table table, Capteurs capteurs)
    {
        this.log = log;
        this.table = table;
        this.capteurs = capteurs;

        // On n'instancie hypotheticalEnnemy qu'une seule fois
//        if(hypotheticalEnemy == null)
//        	hypotheticalEnemy = new ObstacleProximity<ReadWrite>(new Vec2<ReadWrite>(), rayon_robot_adverse, -1000);
    }

    /**
     * Création d'un ennemi hypothétique. Utilisé pour les stratégies d'urgence uniquement
     */
/*    public void createHypotheticalEnnemy(Vec2<ReadOnly> position, int date_actuelle)
    {
    	isThereHypotheticalEnemy = true;
    	Obstacle.setPosition(hypotheticalEnemy, position);
    	hypotheticalEnemy.setDeathDate(date_actuelle+dureeAvantPeremption);
        checkGameElements(position);
    }
  */  
    /**
     * Créer un obstacle de proximité
     * @param position
     */
    public synchronized void creerObstacle(final Vec2<ReadOnly> position, long date_actuelle)
    {
        ObstacleProximity obstacle = new ObstacleProximity(position, rayonEnnemi, date_actuelle+dureeAvantPeremption);
//        log.warning("Obstacle créé, rayon = "+rayon_robot_adverse+", centre = "+position+", meurt à "+(date_actuelle+dureeAvantPeremption), this);
        listObstaclesMobiles.add(obstacle);
        checkGameElements(position);
    }
    
    /**
     * Supprime les éléments de jeux qui sont proches de cette position.
     * @param position
     */
    private void checkGameElements(Vec2<ReadOnly> position)
    {
        // On vérifie aussi ceux qui ont un rayon nul (distributeur, clap, ..)
        for(GameElementNames g: GameElementNames.values)
            if(table.isDone(g) == Tribool.FALSE && table.isProcheObstacle(g, position, rayonEnnemi))
            	table.setDone(g, Tribool.MAYBE);
    }

    /**
     * Supprime les obstacles périmés (maintenant)
     * @param date
     */
    public void supprimerObstaclesPerimes()
    {
    	supprimerObstaclesPerimes(System.currentTimeMillis());
    }

    /**
     * Appel fait lors de l'anticipation, supprime les obstacles périmés à une date future
     * Les obstacles étant triés du plus anciens au plus récent, le premier qui n'est pas supprimable
     * permet d'arrêter la recherche.
     * Renvoie vrai s'il y a eu une suppression, faux sinon.
     * @param date
     */
    public synchronized void supprimerObstaclesPerimes(long date)
    {
//        if(isThereHypotheticalEnemy && hypotheticalEnemy.isDestructionNecessary(date))
//        	isThereHypotheticalEnemy = false;
        int size = listObstaclesMobiles.size();
        
        int firstNotDeadOld = firstNotDead;
        for(; firstNotDead < size; firstNotDead++)
        {
        	if(!listObstaclesMobiles.get(firstNotDead).isDestructionNecessary(date))
        		break;
//        	else
//        		log.debug("Destruction avec un retard de "+(System.currentTimeMillis()-listObstaclesMobiles.get(firstNotDead).getDeathDate()));
        }
        if(firstNotDeadOld != firstNotDead)
        	synchronized(this)
        	{
        		notifyAll();
        	}
    }  
    
    /**
     * Utilisé UNIQUEMENT pour les tests!
     */
    public void clearObstaclesMobiles()
    {
//    	isThereHypotheticalEnemy = false;
    	listObstaclesMobiles.clear();
    	firstNotDead = 0;
    }

    /**
     * Utilisé pour le calcul de hash
     * @return le nombre d'obstacles mobiles détectés
     */
    public int nbObstaclesMobiles()
    {
        return listObstaclesMobiles.size() - firstNotDead;// + (isThereHypotheticalEnemy?1:0);
    }

    /**
     * Utilisé pour le calcul de hash
     * @return le nombre d'obstacles mobiles détectés
     */
    public int getHashObstaclesMobiles()
    {
        return (firstNotDead << 1);// | (isThereHypotheticalEnemy?1:0);
    }

    
    public ObstacleManager clone()
    {
    	ObstacleManager cloned_manager = new ObstacleManager(log, table.clone(), capteurs);
		copy(cloned_manager);
		return cloned_manager;
    }
    
    /**
     * Nécessaire au fonctionnement du memory manager
     * @param other
     */
    public void copy(ObstacleManager other)
    {
    	// La suppression des obstacles est déjà fait pendant la copy du gridspace
    	table.copy(other.table);
//    	other.isThereHypotheticalEnemy = isThereHypotheticalEnemy;
    	other.firstNotDead = firstNotDead;
    }
    
    /**
     * Cette méthode vérifie les obstacles fixes uniquement.
     * Elle est utilisée dans le lissage.
     * @param A
     * @param B
     * @return
     */
    public boolean obstacleFixeDansSegmentPathfinding(Vec2<ReadOnly> A, Vec2<ReadOnly> B)
    {
    	ObstacleRectangular chemin = new ObstacleRectangular(A, B);
    	for(ObstaclesFixes o: ObstaclesFixes.values)
    	{
    		if(chemin.isColliding(o.getObstacle()))
    			return true;
    	}
    	return false;
    }
 
	/**
	 * Y a-t-il un obstacle de table dans ce segment?
	 * @param A
	 * @param B
	 * @return
	 */
    public boolean obstacleTableDansSegment(Vec2<ReadOnly> A, Vec2<ReadOnly> B)
    {
    	ObstacleRectangular chemin = new ObstacleRectangular(A, B);
        for(GameElementNames g: GameElementNames.values())
        	// Si on a interprété que l'ennemi est passé sur un obstacle,
        	// on peut passer dessus par la suite.
            if(table.isDone(g) == Tribool.FALSE && table.obstacle_proximite_dans_segment(g, chemin))
            {
//            	log.debug(o.getName()+" est dans le chemin.", this);
                return true;
            }

        return false;    	
    }

    /**
     * Y a-t-il un obstacle de proximité dans ce segment?
     * Va-t-il disparaître pendant le temps de parcours?
     * @param sommet1
     * @param sommet2
     * @return
     */
    public boolean obstacleProximiteDansSegment(Vec2<ReadOnly> A, Vec2<ReadOnly> B, int date)
    {
  //      if(isThereHypotheticalEnemy && hypotheticalEnemy.obstacle_proximite_dans_segment(A, B, date))
  //      	return true;
        
        int size = listObstaclesMobiles.size();
        for(int tmpFirstNotDead = firstNotDead; tmpFirstNotDead < size; tmpFirstNotDead++)
        	if(listObstaclesMobiles.get(tmpFirstNotDead).obstacle_proximite_dans_segment(A, B, date))
        		return true;

        return false;
    }

    /**
     * Utilisé pour savoir si ce qu'on voit est un obstacle fixe.
     * @param position
     * @return
     */
    public boolean isObstacleFixePresentCapteurs(Vec2<ReadOnly> position)
    {
    	for(ObstaclesFixes o: ObstaclesFixes.values)
            if(isObstaclePresent(position, o.getObstacle(), distanceApproximation))
                return true;
        return false;
    }
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * Cela permet de ne pas détecter en obstacle mobile des obstacles fixes.
     * De plus, ça allège le nombre d'obstacles.
     * Utilisé pour savoir s'il y a un ennemi devant nous.
     * @param position
     * @return
     */
    public boolean isObstacleMobilePresent(Vec2<ReadOnly> position, int distance) 
    {
    //    if(isThereHypotheticalEnemy && isObstaclePresent(position, hypotheticalEnemy.getReadOnly(), distance))
    //    	return true;
        int size = listObstaclesMobiles.size();
        for(int tmpFirstNotDead = firstNotDead; tmpFirstNotDead < size; tmpFirstNotDead++)
        {
        	if(isObstaclePresent(position, listObstaclesMobiles.get(tmpFirstNotDead), distance))
        		return true;
        }
        return false;
    }

    /**
     * Vérifie pour un seul obstacle.
     * Renvoie true si un obstacle est proche
     * @param position
     * @param o
     * @param distance
     * @return
     */
    private boolean isObstaclePresent(Vec2<ReadOnly> position, Obstacle o, int distance)
    {
    	return o.isProcheObstacle(position, distance);
    }
    
    /**
     * Utilisé afin de calculer la péremption du cache du gridspace
     * @param other
     * @return
     */
    public boolean equals(ObstacleManager other)
    {
        return firstNotDead == other.firstNotDead;
    }

	@Override
	public void updateConfig(Config config)
	{}
    
	@Override
	public void useConfig(Config config) {
		table_x = config.getInt(ConfigInfo.TABLE_X);
		table_y = config.getInt(ConfigInfo.TABLE_Y);
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
		rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		dureeAvantPeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);
	}

	/**
	 * Utilisé pour l'affichage
	 * @return 
	 */
	public ArrayList<ObstacleProximity> getListObstaclesMobiles()
	{
		return listObstaclesMobiles;
	}
	
	/**
	 * Utilisé pour la copie
	 * @return
	 */
	public int getFirstNotDead()
	{
		return firstNotDead;
	}

	/**
	 * Surcouche de la table
	 * @param element
	 * @param done
	 */
	public void setDone(GameElementNames element, Tribool done)
	{
		table.setDone(element, done);
	}

	/**
	 * Surcouche de la table
	 * @param element
	 * @return
	 */
	public Tribool isDone(GameElementNames element)
	{
		return table.isDone(element);
	}

	/**
	 * Surcouche de la table
	 * @return
	 */
	public long getHashTable()
	{
		return table.getHash();
	}

	/**
	 * Debug
	 */
	public void printHash()
	{
		table.printHash();
	}

	/**
	 * Quelque chose change si un obstacle disparaît
	 * Si jamais rien ne change, renvoie Long.MAX_VALUE
	 * @return
	 */
	public long getDateSomethingChange()
	{
//	    long date1, date2;
	    
	    if(firstNotDead < listObstaclesMobiles.size())
	    	return listObstaclesMobiles.get(firstNotDead).getDeathDate();
	    else
	    	return Long.MAX_VALUE;
	//    if(isThereHypotheticalEnemy)
	//    	date2 = hypotheticalEnemy.getDeathDate();
	//    else
	//    	return date1;
	    
//	    return Math.min(date1, date2);
	}

	public boolean isTraversableCourbe(PathfindingNodes objectifFinal, PathfindingNodes intersection, Vec2<ReadOnly> directionAvant, int tempsDepuisDebutMatch)
	{
		// TODO: calculer la vitesse qui permet exactement de passer?
		Speed vitesse = Speed.BETWEEN_SCRIPTS;
		
		obstacleTrajectoireCourbe = new ObstacleTrajectoireCourbe(objectifFinal, intersection, directionAvant, vitesse);

		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values)
    		if(obstacleTrajectoireCourbe.isColliding(o.getObstacle()))
    			return false;

    	// Collision avec un ennemi hypothétique?
  //      if(isThereHypotheticalEnemy && obstacleTrajectoireCourbe.isColliding(hypotheticalEnemy.getReadOnly()))
  //      	return false;

        // Collision avec un obtacle mobile?
        int size = listObstaclesMobiles.size();
        for(int tmpFirstNotDead = firstNotDead; tmpFirstNotDead < size; tmpFirstNotDead++)
        	if(obstacleTrajectoireCourbe.isColliding(listObstaclesMobiles.get(tmpFirstNotDead)))
        		return false;

        return true;
	}

	/**
	 * Met à jour les obstacles mobiles
	 */
	public void updateObstaclesMobiles(IncomingData data)
	{
		boolean[] done = new boolean[nbCapteurs];
			
		/**
		 * Suppression des mesures qui sont hors-table ou qui voit un obstacle de table
		 */
		for(int i = 0; i < nbCapteurs; i++)
		{
			if(data.mesures[i] < 40 || data.mesures[i] > 800)
			{
				done[i] = true;
//				log.debug("Capteur "+i+" trop proche ou trop loin.");
				continue;
			}
			Vec2<ReadWrite> positionBrute = new Vec2<ReadWrite>(data.mesures[i], Capteurs.orientationsRelatives[i]);
			Vec2.plus(positionBrute, capteurs.positionsRelatives[i]);
			Vec2.rotate(positionBrute, data.orientationRobot);
			Vec2.plus(positionBrute, data.positionRobot);
//			log.debug("Position brute: "+positionBrute);
			
			if(positionBrute.x > table_x / 2 - distanceApproximation ||
					positionBrute.x < -table_x / 2 + distanceApproximation ||
					positionBrute.y < distanceApproximation ||
					positionBrute.y > table_y - distanceApproximation ||
					isObstacleFixePresentCapteurs(positionBrute.getReadOnly()))
			{
//				log.debug("Capteur "+i+" ignoré.");
				done[i] = true; // le capteur voit un obstacle fixe: on ignore sa valeur
			}
			else
				done[i] = false;
		}
		
		/**
		 * On cherche les détections couplées en priorité
		 */
		for(int i = 0; i < capteurs.nbCouples; i++)
		{
			int nbCapteur1 = Capteurs.coupleCapteurs[i][0];
			int nbCapteur2 = Capteurs.coupleCapteurs[i][1];

			if(done[nbCapteur1] && done[nbCapteur2])
			{
				continue;
			}
			else if(done[nbCapteur1] || done[nbCapteur2])
			{
				int nbCapteurQuiVoit = done[nbCapteur2]?nbCapteur1:nbCapteur2;
				Vec2<ReadWrite> pointVu = capteurs.getPositionAjustee(i, rayonEnnemi, done[nbCapteur2], data.mesures[nbCapteurQuiVoit]);
//				Vec2<ReadWrite> pointVu = Capteurs.getPositionAjustee(i, 0, done[nbCapteur2], data.mesures[nbCapteurQuiVoit]);
				if(pointVu == null)
					continue;
				Vec2.plus(pointVu, capteurs.positionsRelatives[nbCapteurQuiVoit]);
				Vec2.rotate(pointVu, data.orientationRobot);
				Vec2.plus(pointVu, data.positionRobot);
				creerObstacle(pointVu.getReadOnly(), System.currentTimeMillis());
				done[nbCapteurQuiVoit] = true;
			}
			else
			{	
				int distanceEntreCapteurs = Capteurs.coupleCapteurs[i][2];
				int mesure1 = data.mesures[nbCapteur1];
				int mesure2 = data.mesures[nbCapteur2];
				
				// Si l'inégalité triangulaire n'est pas respectée
				if(mesure1 + mesure2 <= distanceEntreCapteurs)
					continue;
				
				double posX = ((double)(distanceEntreCapteurs*distanceEntreCapteurs + mesure1*mesure1 - mesure2*mesure2))/(2*distanceEntreCapteurs);
				double posY = Math.sqrt(mesure1*mesure1 - posX*posX);
				
				Vec2<ReadWrite> pointVu1 = capteurs.positionsRelatives[nbCapteur2].clone();
				Vec2.minus(pointVu1, capteurs.positionsRelatives[nbCapteur1]);
				Vec2<ReadWrite> BC = pointVu1.clone();
				Vec2.rotateAngleDroit(BC);
				Vec2.scalar(BC, posY/distanceEntreCapteurs);
//				log.debug("Longueur BC: "+BC.length()+", posY: "+posY);
				Vec2.scalar(pointVu1, (double)(posX)/distanceEntreCapteurs);
				Vec2.plus(pointVu1, capteurs.positionsRelatives[nbCapteur1]);
				Vec2<ReadWrite> pointVu2 = pointVu1.clone();
				Vec2.plus(pointVu1, BC);
				Vec2.minus(pointVu2, BC);
				
//				log.debug("Point vu 1: "+pointVu1);
//				log.debug("Point vu 2: "+pointVu2);
				
				boolean vu = capteurs.canBeSeen(pointVu1.getReadOnly(), nbCapteur1) && capteurs.canBeSeen(pointVu1.getReadOnly(), nbCapteur2);
	//			if(vu)
	//				log.debug("pointVu1 est visible!");
	
				if(!vu)
				{
					vu = capteurs.canBeSeen(pointVu2.getReadOnly(), nbCapteur1) && capteurs.canBeSeen(pointVu2.getReadOnly(), nbCapteur2);
					pointVu1 = pointVu2;
					Vec2.oppose(BC);
	//				if(vu)
	//					log.debug("pointVu2 est visible!");
				}
				if(vu)			
				{
					done[nbCapteur1] = true;
					done[nbCapteur2] = true;
	//				log.debug("Scalaire: "+(rayonEnnemi)/posY);
					Vec2.scalar(BC, ((double)rayonEnnemi)/posY);
					Vec2.plus(pointVu1, BC);
					Vec2.rotate(pointVu1, data.orientationRobot);
					Vec2.plus(pointVu1, data.positionRobot);
	//				log.debug("Longueur BC: "+BC.length());
					// TODO: supprimer tous les autres obstacles près de pointVu1
					creerObstacle(pointVu1.getReadOnly(), System.currentTimeMillis());
				}
			}
		}
		
		/**
		 * Maintenant, on récupère tous les capteurs qui n'ont pas participé à une détection couplée
		 */
		for(int i = 0; i < nbCapteurs; i++)
			if(!done[i])
			{
				Vec2<ReadWrite> positionEnnemi = new Vec2<ReadWrite>(data.mesures[i]+rayonEnnemi, Capteurs.orientationsRelatives[i]);
				Vec2.plus(positionEnnemi, capteurs.positionsRelatives[i]);
				Vec2.rotate(positionEnnemi, data.orientationRobot);
				Vec2.plus(positionEnnemi, data.positionRobot);
		//		log.debug("Obstacle vu par un seul capteur: "+positionEnnemi);
				creerObstacle(positionEnnemi.getReadOnly(), System.currentTimeMillis());

			}
	}
	
}
