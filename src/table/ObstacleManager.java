package table;

import java.util.ArrayList;

import buffer.IncomingData;
import obstacles.Obstacle;
import obstacles.ObstacleCircular;
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
	private int nbCouples;
    private int horizonCapteurs;
	
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
	{
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
		nbCouples = config.getInt(ConfigInfo.NB_COUPLES_CAPTEURS_PROXIMITE);
	}
    
	@Override
	public void useConfig(Config config) {
		table_x = config.getInt(ConfigInfo.TABLE_X);
		table_y = config.getInt(ConfigInfo.TABLE_Y);
		rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		dureeAvantPeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);
		horizonCapteurs = config.getInt(ConfigInfo.HORIZON_CAPTEURS);
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

	private static final boolean debug = true;
	
	/**
	 * Met à jour les obstacles mobiles
	 */
	public void updateObstaclesMobiles(IncomingData data)
	{
		boolean[] neVoitRien = new boolean[nbCapteurs];
		boolean[] dejaTraite = new boolean[nbCapteurs];
			
		/**
		 * Suppression des mesures qui sont hors-table ou qui voient un obstacle de table
		 */
		for(int i = 0; i < nbCapteurs; i++)
		{
			dejaTraite[i] = false;
			if(debug)
				log.debug("Capteur "+i);
			if(data.mesures[i] < 40 || data.mesures[i] > horizonCapteurs)
			{
				neVoitRien[i] = true;
				if(debug)
					log.debug("Capteur "+i+" trop proche ou trop loin.");
				continue;
			}
			Vec2<ReadWrite> positionBrute = new Vec2<ReadWrite>(data.mesures[i], Capteurs.orientationsRelatives[i]);
			Vec2.plus(positionBrute, capteurs.positionsRelatives[i]);
			Vec2.rotate(positionBrute, data.orientationRobot);
			Vec2.plus(positionBrute, data.positionRobot);
			if(debug)
				log.debug("Position brute: "+positionBrute);
			
			if(positionBrute.x > table_x / 2 - distanceApproximation ||
					positionBrute.x < -table_x / 2 + distanceApproximation ||
					positionBrute.y < distanceApproximation ||
					positionBrute.y > table_y - distanceApproximation/* ||
					isObstacleFixePresentCapteurs(positionBrute.getReadOnly())*/)
			{
				if(debug)
					log.debug("Capteur "+i+" ignoré car hors-table.");
				neVoitRien[i] = true; // le capteur voit un obstacle fixe: on ignore sa valeur
			}
			else
			{
				/**
				 * Si un capteur voit un obstacle de table, alors on l'ignore
				 */
				for(ObstaclesFixes o: ObstaclesFixes.values)
				{
					Obstacle obs = o.getObstacle();
					if(debug)
						log.debug("Vérification obstacle en "+obs.position);

					if(obs instanceof ObstacleCircular)
					{
						if(debug)
							log.debug("Obstacle circulaire");

						ObstacleCircular obsc = (ObstacleCircular) obs;
						
						/**
						 * positionObstacle est la position de l'obstacle dans le repère du robot
						 */
						Vec2<ReadOnly> positionObstacle = Vec2.rotate(obsc.position.minusNewVector(data.positionRobot), -data.orientationRobot).getReadOnly();
						
						if(debug)
							log.debug("Position obstacle dans le repère du robot: "+positionObstacle);

						if(capteurs.canBeSeenArriere(positionObstacle, i, ((ObstacleCircular) obs).getRadius()))
						{
							/**
							 * On voit l'obstacle circulaire de table. Reste maintenant
							 * à savoir à quelle distance on devrait le voir afin de savoir
							 * si c'est vraiment lui qu'on voit
							 */
							int distance;
							if(capteurs.canBeSeen(positionObstacle, i))
							{
								/**
								 * Cas simple: on voit le centre du cercle
								 */
								if(debug)
									log.debug("Cas simple");
								distance = (int) (positionObstacle.distance(capteurs.positionsRelatives[i]) - obsc.getRadius());
							}
							else
							{
								/**
								 * Cas plus complexe: on voit seulement un bout du cercle
								 */
								if(debug)
									log.debug("Cas complexe");
								int cote = capteurs.whichSee(positionObstacle, i);
								if(debug)
									log.debug("Côté qui voit: "+cote);
								distance = (int)ObstacleCircular.getDistance(positionObstacle, obsc.getRadius(), capteurs.positionsRelatives[i], capteurs.cones[3-i][cote]);
							}
							
							if(debug)
								log.debug("Distance prédite pour "+i+": "+distance);
							
							if(Math.abs(distance - data.mesures[i]) < distanceApproximation)
							{
								if(debug)
									log.debug("Le capteur "+i+" voit un obstacle fixe circulaire");
								dejaTraite[i] = true;
								// On a trouvé quel obstacle on voyait, pas besoin d'aller plus loin
								break;
							}
							
						}
						else if(debug)
							log.debug("Obstacle pas visible avec cone arrière");
					}
					else if(obs instanceof ObstacleRectangular)
					{
						if(debug)
							log.debug("Obstacle rectangulaire");

						ObstacleRectangular obsr = (ObstacleRectangular) obs;

						if(debug)
							log.debug("Vérification obstacle en "+obsr.position);

						/**
						 * On vérifie d'abord quel coin sont visibles ou non
						 */
						boolean coinBasDroiteVisible, coinBasGaucheVisible, coinHautDroiteVisible, coinHautGaucheVisible;
						coinBasDroiteVisible = capteurs.canBeSeen(Vec2.rotate(obsr.coinBasDroite.minusNewVector(data.positionRobot), -data.orientationRobot).getReadOnly(), i);
						coinBasGaucheVisible = capteurs.canBeSeen(Vec2.rotate(obsr.coinBasGauche.minusNewVector(data.positionRobot), -data.orientationRobot).getReadOnly(), i);
						coinHautDroiteVisible = capteurs.canBeSeen(Vec2.rotate(obsr.coinHautDroite.minusNewVector(data.positionRobot), -data.orientationRobot).getReadOnly(), i);
						coinHautGaucheVisible = capteurs.canBeSeen(Vec2.rotate(obsr.coinHautGauche.minusNewVector(data.positionRobot), -data.orientationRobot).getReadOnly(), i);
						
						int distance;
						Vec2<ReadOnly> coinPlusProcheVisible = obsr.getPlusProcheCoinVisible(data.positionRobot, coinBasDroiteVisible, coinBasGaucheVisible, coinHautDroiteVisible, coinHautGaucheVisible);
						if(coinPlusProcheVisible != null || coinPlusProcheVisible == obsr.getPlusProcheCoinVisible(data.positionRobot, true, true, true, true))
						{
							/**
							 * Le coin le plus proche du robot est visible.
							 * Alors c'est la partie du rectangle la plus proche du capteur.
							 */
							if(debug)
								log.debug("Cas simple");
							distance = (int) Vec2.rotate(coinPlusProcheVisible.minusNewVector(data.positionRobot), -data.orientationRobot).getReadOnly().distance(capteurs.positionsRelatives[i]);
						}
						else
						{
							distance = Integer.MAX_VALUE;

							/**
							 * Le point le plus proche des capteurs est sur une arête du rectangle
							 */
							if(debug)
								log.debug("Cas complexe");

							/**
							 * Cas où le plus proche point n'est pas dans un côté du cône
							 */

							Vec2<ReadWrite> coinBasDroiteRepereRobotSansRotation = obsr.coinBasDroite.minusNewVector(data.positionRobot);
							
							Vec2<ReadWrite> point;
							
							point = Vec2.rotate(new Vec2<ReadWrite>(0, coinBasDroiteRepereRobotSansRotation.y), -data.orientationRobot);
							
							if(capteurs.canBeSeen(point.getReadOnly(), i))
							{
								distance = Math.min(distance, Math.abs(coinBasDroiteRepereRobotSansRotation.y));
							}
							
							point = Vec2.rotate(new Vec2<ReadWrite>(coinBasDroiteRepereRobotSansRotation.x, 0), -data.orientationRobot);
							
							if(capteurs.canBeSeen(point.getReadOnly(), i))
							{
								distance = Math.min(distance, Math.abs(coinBasDroiteRepereRobotSansRotation.x));
							}

							Vec2<ReadWrite> coinBasGaucheRepereRobotSansRotation = obsr.coinBasGauche.minusNewVector(data.positionRobot);
							
							point = Vec2.rotate(new Vec2<ReadWrite>(coinBasGaucheRepereRobotSansRotation.x, 0), -data.orientationRobot);
							
							if(capteurs.canBeSeen(point.getReadOnly(), i))
							{
								distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
							}

							Vec2<ReadWrite> coinHautDroiteRepereRobotSansRotation = obsr.coinHautDroite.minusNewVector(data.positionRobot);
							
							point = Vec2.rotate(new Vec2<ReadWrite>(0, coinHautDroiteRepereRobotSansRotation.y), -data.orientationRobot);
							
							if(capteurs.canBeSeen(point.getReadOnly(), i))
							{
								distance = Math.min(distance, Math.abs(coinHautDroiteRepereRobotSansRotation.y));
							}

							/**
							 * Cas où le plus proche point est sur une arête du cône
							 */
							
							double tanAlpha1 = data.orientationRobot + Capteurs.orientationsRelatives[i];
							double tanAlpha2 = tanAlpha1 - capteurs.getAngleCone(i);
							tanAlpha1 += capteurs.getAngleCone(i);
							
							tanAlpha1 = Math.tan(tanAlpha1);
							tanAlpha2 = Math.tan(tanAlpha2);
							
							int x, y;
							Vec2<ReadWrite> p;

							/**
							 * Tangente positive pour le côté gauche du cône:
							 * il faut vérifier qu'on ne croise pas un côté horizontal de l'obstacle
							 */
							
							if(tanAlpha1 > 0)
							{
								y = obsr.coinBasDroite.y - data.positionRobot.y - capteurs.positionsRelatives[i].y;
								x = (int)(y/tanAlpha1);
								p = new Vec2<ReadWrite>(x, y);
								Vec2.plus(p, capteurs.positionsRelatives[i]);
								if(capteurs.canBeSeen(p.getReadOnly(), i))
									distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));

								y = obsr.coinHautDroite.y - data.positionRobot.y - capteurs.positionsRelatives[i].y;
								x = (int)(y/tanAlpha1);
								p = new Vec2<ReadWrite>(x, y);
								Vec2.plus(p, capteurs.positionsRelatives[i]);
								if(capteurs.canBeSeen(p.getReadOnly(), i))
									distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
							}
							else
							{
								x = obsr.coinBasDroite.x - data.positionRobot.x - capteurs.positionsRelatives[i].x;
								y = (int)(x*tanAlpha1);
								p = new Vec2<ReadWrite>(x, y);
								Vec2.plus(p, capteurs.positionsRelatives[i]);
								if(capteurs.canBeSeen(p.getReadOnly(), i))
									distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));

								x = obsr.coinHautGauche.x - data.positionRobot.x - capteurs.positionsRelatives[i].x;
								y = (int)(x*tanAlpha1);
								p = new Vec2<ReadWrite>(x, y);
								Vec2.plus(p, capteurs.positionsRelatives[i]);
								if(capteurs.canBeSeen(p.getReadOnly(), i))
									distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
							}
							
							if(tanAlpha2 >= 0)
							{
								x = obsr.coinBasDroite.x - data.positionRobot.x - capteurs.positionsRelatives[i].x;
								y = (int)(x*tanAlpha1);
								p = new Vec2<ReadWrite>(x, y);
								Vec2.plus(p, capteurs.positionsRelatives[i]);
								if(capteurs.canBeSeen(p.getReadOnly(), i))
									distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));

								x = obsr.coinHautGauche.x - data.positionRobot.x - capteurs.positionsRelatives[i].x;
								y = (int)(x*tanAlpha1);
								p = new Vec2<ReadWrite>(x, y);
								Vec2.plus(p, capteurs.positionsRelatives[i]);
								if(capteurs.canBeSeen(p.getReadOnly(), i))
									distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
							}
							else
							{
								y = obsr.coinBasDroite.y - data.positionRobot.y - capteurs.positionsRelatives[i].y;
								x = (int)(y/tanAlpha2);
								p = new Vec2<ReadWrite>(x, y);
								Vec2.plus(p, capteurs.positionsRelatives[i]);
								if(capteurs.canBeSeen(p.getReadOnly(), i))
									distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));

								y = obsr.coinHautDroite.y - data.positionRobot.y - capteurs.positionsRelatives[i].y;
								x = (int)(y/tanAlpha2);
								p = new Vec2<ReadWrite>(x, y);
								Vec2.plus(p, capteurs.positionsRelatives[i]);
								if(capteurs.canBeSeen(p.getReadOnly(), i))
									distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
							}
							
						}
						
						if(Math.abs(distance - data.mesures[i]) < distanceApproximation)
						{
							if(debug)
								log.debug("Le capteur "+i+" voit un obstacle fixe rectangulaire");
							dejaTraite[i] = true;
							// On a trouvé quel obstacle on voyait, pas besoin d'aller plus loin
							break;
						}

					}
				}
//				log.debug("Ok");
				neVoitRien[i] = false;
				data.mesures[i] += rayonEnnemi;
			}
		}
		
		/**
		 * On cherche les détections couplées en priorité
		 */
		for(int i = 0; i < nbCouples; i++)
		{
			int nbCapteur1 = Capteurs.coupleCapteurs[i][0];
			int nbCapteur2 = Capteurs.coupleCapteurs[i][1];
			
			if(debug)
				log.debug("nbCapteur1: "+nbCapteur1);
			if(debug)
				log.debug("nbCapteur2: "+nbCapteur2);

			if((neVoitRien[nbCapteur1] || dejaTraite[nbCapteur1]) && (neVoitRien[nbCapteur2] || dejaTraite[nbCapteur2]))
			{
				if(debug)
					log.debug("Couple "+i+": déjà fait");
				continue;
			}
			else if((neVoitRien[nbCapteur1] ^ neVoitRien[nbCapteur2]) && (!dejaTraite[nbCapteur1] && !dejaTraite[nbCapteur2]))
			{
				if(debug)
					log.debug("Un capteur voit et pas l'autre");
				/**
				 * Cas où un capteur voit et pas l'autre
				 */
				int nbCapteurQuiVoit = neVoitRien[nbCapteur2]?nbCapteur1:nbCapteur2;
				Vec2<ReadWrite> pointVu = capteurs.getPositionAjustee(i, neVoitRien[nbCapteur2], data.mesures[nbCapteurQuiVoit]);
				if(pointVu == null)
				{
					if(debug)
						log.debug("Point vu: null");
					continue;
				}
				Vec2.plus(pointVu, capteurs.positionsRelatives[nbCapteurQuiVoit]);
				Vec2.rotate(pointVu, data.orientationRobot);
				Vec2.plus(pointVu, data.positionRobot);
				creerObstacle(pointVu.getReadOnly(), System.currentTimeMillis());
				neVoitRien[nbCapteurQuiVoit] = true;
			}
			else if(!dejaTraite[nbCapteur1] && !dejaTraite[nbCapteur2] && !neVoitRien[nbCapteur1] && !neVoitRien[nbCapteur2])
			{	
				if(debug)
					log.debug("Deux capteurs voient");
				/**
				 * Cas où les deux capteurs voient
				 */
				int distanceEntreCapteurs = Capteurs.coupleCapteurs[i][2];
				int mesure1 = data.mesures[nbCapteur1];
				int mesure2 = data.mesures[nbCapteur2];
				
				// Si l'inégalité triangulaire n'est pas respectée
				if(mesure1 + mesure2 <= distanceEntreCapteurs)
				{
					if(debug)
						log.debug("Inégalité triangulaire non respectée");
					continue;
				}
				
				double posX = ((double)(distanceEntreCapteurs*distanceEntreCapteurs + mesure1*mesure1 - mesure2*mesure2))/(2*distanceEntreCapteurs);
				double posY = Math.sqrt(mesure1*mesure1 - posX*posX);
				
				Vec2<ReadWrite> pointVu1 = capteurs.positionsRelatives[nbCapteur2].clone();
				Vec2.minus(pointVu1, capteurs.positionsRelatives[nbCapteur1]);
				Vec2<ReadWrite> BC = pointVu1.clone();
				Vec2.rotateAngleDroit(BC);
				Vec2.scalar(BC, posY/distanceEntreCapteurs);
				if(debug)
					log.debug("Longueur BC: "+BC.length()+", posY: "+posY);
				Vec2.scalar(pointVu1, (double)(posX)/distanceEntreCapteurs);
				Vec2.plus(pointVu1, capteurs.positionsRelatives[nbCapteur1]);
				Vec2<ReadWrite> pointVu2 = pointVu1.clone();
				Vec2.plus(pointVu1, BC);
				Vec2.minus(pointVu2, BC);
				
				/**
				 * Il y a deux points, pointVu1 et pointVu2 car l'intersection
				 * de deux cercles a deux solutions: une devant les capteurs, une derrière
				 */
				
				if(debug)
					log.debug("Point vu 1: "+pointVu1);
				if(debug)
					log.debug("Point vu 2: "+pointVu2);
				
				/**
				 * Afin de départager ces deux points, on regarde lequel est visible par les capteurs.
				 * Sauf qu'à cause du bruit, il est possible que le bon point ne soit pas visible mais légèrement en dehors...
				 * Du coup, on utilise une version allégée de "canBeSeen", qui vérifie juste que le côté est bon.
				 */
				
				boolean vu = capteurs.canBeSeenLight(pointVu1.getReadOnly(), nbCapteur1) && capteurs.canBeSeenLight(pointVu1.getReadOnly(), nbCapteur2);
				if(debug)
				{
					if(vu)
						log.debug("pointVu1 est visible");
					else
						log.debug("pointVu1 n'est pas visible!");
				}
	
				if(!vu)
				{
					vu = capteurs.canBeSeenLight(pointVu2.getReadOnly(), nbCapteur1) && capteurs.canBeSeenLight(pointVu2.getReadOnly(), nbCapteur2);
					pointVu1 = pointVu2;
					Vec2.oppose(BC);
					if(debug)
					{
						if(vu)
							log.debug("pointVu2 est visible");
						else
							log.debug("pointVu2 n'est pas visible!");
					}
				}
				
				if(vu)			
				{
					neVoitRien[nbCapteur1] = true;
					neVoitRien[nbCapteur2] = true;
					if(debug)
						log.debug("Scalaire: "+(rayonEnnemi)/posY);
//					Vec2.scalar(BC, ((double)rayonEnnemi)/posY);
//					Vec2.plus(pointVu1, BC);
					Vec2.rotate(pointVu1, data.orientationRobot);
					Vec2.plus(pointVu1, data.positionRobot);
					if(debug)
						log.debug("Longueur BC: "+BC.length());
					// TODO: supprimer tous les autres obstacles près de pointVu1
					creerObstacle(pointVu1.getReadOnly(), System.currentTimeMillis());
				}
			}
		}
		
		/**
		 * Maintenant, on récupère tous les capteurs qui n'ont pas participé à une détection couplée
		 */
		for(int i = 0; i < nbCapteurs; i++)
			if(!neVoitRien[i] && !dejaTraite[i])
			{
				Vec2<ReadWrite> positionEnnemi = new Vec2<ReadWrite>(data.mesures[i]+rayonEnnemi, Capteurs.orientationsRelatives[i]);
				Vec2.plus(positionEnnemi, capteurs.positionsRelatives[i]);
				Vec2.rotate(positionEnnemi, data.orientationRobot);
				Vec2.plus(positionEnnemi, data.positionRobot);
				if(debug)
					log.debug("Obstacle vu par un seul capteur: "+positionEnnemi);
				creerObstacle(positionEnnemi.getReadOnly(), System.currentTimeMillis());

			}
	}
	
}
