package table;

import java.util.ArrayList;

import obstacles.Obstacle;
import obstacles.ObstacleProximity;
import obstacles.ObstacleRectangular;
import obstacles.ObstacleTrajectoireCourbe;
import obstacles.ObstaclesFixes;
import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.astar.arc.PathfindingNodes;
import planification.astar.arc.SegmentTrajectoireCourbe;
import robot.Speed;
import container.Service;
import enums.Tribool;
import threads.IncomingData;
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
    private Config config;
    private final Table table;
    
    // Les obstacles mobiles, c'est-à-dire des obstacles de proximité et de balise
    // Comme ces obstacles ne peuvent que disparaître, on les retient tous et chaque instance aura un indice vers sur le premier obstacle non mort
    private static ArrayList<ObstacleProximity<ReadOnly>> listObstaclesMobiles = new ArrayList<ObstacleProximity<ReadOnly>>();

    // TODO: vérifier ce static
    private static ObstacleProximity<ReadWrite> hypotheticalEnemy = null;
    private boolean isThereHypotheticalEnemy = false;
    
    private ObstacleTrajectoireCourbe<ReadOnly> obstacleTrajectoireCourbe;
    
    private int firstNotDead = 0;

    private int rayon_robot_adverse;
    private int distanceApproximation;
    private int dureeAvantPeremption;
    private int date_dernier_ajout = 0;
	private double tempo = 0;
	private int table_x = 3000;
	private int table_y = 2000;
	private int diametreEnnemi;

    
    public ObstacleManager(Log log, Config config, Table table)
    {
        this.log = log;
        this.config = config;
        this.table = table;

        // On n'instancie hypotheticalEnnemy qu'une seule fois
        if(hypotheticalEnemy == null)
        	hypotheticalEnemy = new ObstacleProximity<ReadWrite>(new Vec2<ReadWrite>(), rayon_robot_adverse, -1000);
        updateConfig();
    }

    /**
     * Création d'un ennemi hypothétique. Utilisé pour les stratégies d'urgence uniquement
     */
    public void createHypotheticalEnnemy(Vec2<ReadOnly> position, int date_actuelle)
    {
    	isThereHypotheticalEnemy = true;
    	Obstacle.setPosition(hypotheticalEnemy, position);
    	hypotheticalEnemy.setDeathDate(date_actuelle+dureeAvantPeremption);
        checkGameElements(position);
    }
    
    /**
     * Créer un obstacle de proximité
     * @param position
     */
    private synchronized void creerObstacle(final Vec2<ReadOnly> position, int date_actuelle)
    {
        ObstacleProximity<ReadOnly> obstacle = new ObstacleProximity<ReadOnly>(position, rayon_robot_adverse, date_actuelle+dureeAvantPeremption);
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
            if(table.isDone(g) == Tribool.FALSE && table.isProcheObstacle(g, position, rayon_robot_adverse))
            	table.setDone(g, Tribool.MAYBE);
    }

    /**
     * Supprime les obstacles périmés (maintenant)
     * @param date
     */
    public void supprimerObstaclesPerimes()
    {
    	supprimerObstaclesPerimes(System.currentTimeMillis() - Config.getDateDebutMatch());
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
        if(isThereHypotheticalEnemy && hypotheticalEnemy.isDestructionNecessary(date))
        	isThereHypotheticalEnemy = false;
        int size = listObstaclesMobiles.size();
        
        int firstNotDeadOld = firstNotDead;
        for(; firstNotDead < size; firstNotDead++)
        {
        	if(!listObstaclesMobiles.get(firstNotDead).isDestructionNecessary(date))
        		break;
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
    	isThereHypotheticalEnemy = false;
    	listObstaclesMobiles.clear();
    	firstNotDead = 0;
    }

    /**
     * Utilisé pour le calcul de hash
     * @return le nombre d'obstacles mobiles détectés
     */
    public int nbObstaclesMobiles()
    {
        return listObstaclesMobiles.size() - firstNotDead + (isThereHypotheticalEnemy?1:0);
    }

    /**
     * Utilisé pour le calcul de hash
     * @return le nombre d'obstacles mobiles détectés
     */
    public int getHashObstaclesMobiles()
    {
        return (firstNotDead << 1) | (isThereHypotheticalEnemy?1:0);
    }

    
    public ObstacleManager clone()
    {
    	ObstacleManager cloned_manager = new ObstacleManager(log, config, table.clone());
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
    	other.isThereHypotheticalEnemy = isThereHypotheticalEnemy;
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
    	ObstacleRectangular<ReadOnly> chemin = new ObstacleRectangular<ReadOnly>(A, B);
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
    	ObstacleRectangular<ReadOnly> chemin = new ObstacleRectangular<ReadOnly>(A, B);
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
        if(isThereHypotheticalEnemy && hypotheticalEnemy.obstacle_proximite_dans_segment(A, B, date))
        	return true;
        
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
        if(isThereHypotheticalEnemy && isObstaclePresent(position, hypotheticalEnemy.getReadOnly(), distance))
        	return true;
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
    private boolean isObstaclePresent(Vec2<ReadOnly> position, Obstacle<ReadOnly> o, int distance)
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
	public void updateConfig() {
		tempo = config.getDouble(ConfigInfo.CAPTEURS_TEMPORISATION_OBSTACLES);
		table_x = config.getInt(ConfigInfo.TABLE_X);
		table_y = config.getInt(ConfigInfo.TABLE_Y);
		diametreEnnemi = 2*config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		rayon_robot_adverse = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		dureeAvantPeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);
	}

	/**
	 * Utilisé pour l'affichage
	 * @return 
	 */
	public ArrayList<ObstacleProximity<ReadOnly>> getListObstaclesMobiles()
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
	 * Quelque chose change = un obstacle disparaît
	 * Si jamais rien ne change, renvoie Integer.MAX_VALUE
	 * @return
	 */
	public int getDateSomethingChange()
	{
	    int date1, date2;
	    
	    if(firstNotDead < listObstaclesMobiles.size())
	    	date1 = listObstaclesMobiles.get(firstNotDead).getDeathDate();
	    else
	    	date1 = Integer.MAX_VALUE;
	    if(isThereHypotheticalEnemy)
	    	date2 = hypotheticalEnemy.getDeathDate();
	    else
	    	date2 = Integer.MAX_VALUE;
	    
	    return Math.min(date1, date2);
	}

	public boolean isTraversableCourbe(PathfindingNodes objectifFinal, PathfindingNodes intersection, Vec2<ReadOnly> directionAvant, int tempsDepuisDebutMatch)
	{
		// TODO: calculer la vitesse qui permet exactement de passer?
		Speed vitesse = Speed.BETWEEN_SCRIPTS;
		
		obstacleTrajectoireCourbe = new ObstacleTrajectoireCourbe<ReadOnly>(objectifFinal, intersection, directionAvant, vitesse);

		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values)
    		if(obstacleTrajectoireCourbe.isColliding(o.getObstacle()))
    			return false;

    	// Collision avec un ennemi hypothétique?
        if(isThereHypotheticalEnemy && obstacleTrajectoireCourbe.isColliding(hypotheticalEnemy.getReadOnly()))
        	return false;

        // Collision avec un obtacle mobile?
        int size = listObstaclesMobiles.size();
        for(int tmpFirstNotDead = firstNotDead; tmpFirstNotDead < size; tmpFirstNotDead++)
        	if(obstacleTrajectoireCourbe.isColliding(listObstaclesMobiles.get(tmpFirstNotDead)))
        		return false;

        return true;
	}

	public SegmentTrajectoireCourbe getSegment()
	{
		return obstacleTrajectoireCourbe.getSegment();
	}

	public void addIfUseful(IncomingData data)
	{
		if(System.currentTimeMillis() - date_dernier_ajout > tempo &&
				data.pointBrut.x - diametreEnnemi > -table_x / 2 &&
				data.pointBrut.y > diametreEnnemi &&
				data.pointBrut.x + diametreEnnemi < table_x / 2 &&
				data.pointBrut.y + diametreEnnemi < table_y)
			if(!isObstacleFixePresentCapteurs(data.pointBrut))
			{
				date_dernier_ajout = (int)System.currentTimeMillis();
				creerObstacle(data.centreEnnemi, date_dernier_ajout);
				log.debug("Nouvel obstacle en "+data.centreEnnemi);
				synchronized(this)
				{
					notifyAll();
				}
			}
			else
			    log.debug("L'objet vu en "+data.pointBrut+" est un obstacle fixe.");
		else
			log.debug("Hors table ou trop récent");
	}
	
}
