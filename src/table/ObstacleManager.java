package table;

import java.util.ArrayList;

import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import obstacles.Obstacle;
import obstacles.ObstacleProximity;
import obstacles.ObstacleRectangular;
import obstacles.ObstacleTrajectoireCourbe;
import obstacles.ObstaclesFixes;
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
    private Config config;
    private Table table;
    
    // Les obstacles mobiles, c'est-à-dire des obstacles de proximité et de balise
    // Comme ces obstacles ne peuvent que disparaître, on les retient tous et chaque instance aura un indice vers sur le premier obstacle non mort
    private static ArrayList<ObstacleProximity> listObstaclesMobiles = new ArrayList<ObstacleProximity>();

    private static ObstacleProximity hypotheticalEnemy = null;
    private boolean isThereHypotheticalEnemy = false;
    
    private ObstacleTrajectoireCourbe obstacleTrajectoireCourbe;
    
    private int firstNotDead = 0;

    private int rayon_robot_adverse = 200;
    private int distanceApproximation = 50;
    private int dureeAvantPeremption = 0;
        
    public ObstacleManager(Log log, Config config, Table table)
    {
        this.log = log;
        this.config = config;
        this.table = table;

        // On n'instancie hypotheticalEnnemy qu'une seule fois
        if(hypotheticalEnemy == null)
        	hypotheticalEnemy = new ObstacleProximity(new Vec2(), rayon_robot_adverse, -1000);
        updateConfig();
    }

    /**
     * Création d'un ennemi hypothétique. Utilisé pour les stratégies d'urgence uniquement
     */
    public void createHypotheticalEnnemy(Vec2 position, int date_actuelle)
    {
    	isThereHypotheticalEnemy = true;
    	hypotheticalEnemy.setPosition(position);
    	hypotheticalEnemy.setDeathDate(date_actuelle+dureeAvantPeremption);
        checkGameElements(position);
    }
    
    /**
     * Créer un obstacle de proximité
     * @param position
     */
    public void creerObstacle(final Vec2 position, int date_actuelle)
    {
        Vec2 position_sauv = position.clone();
        ObstacleProximity obstacle = new ObstacleProximity(position_sauv, rayon_robot_adverse, date_actuelle+dureeAvantPeremption);
//        log.warning("Obstacle créé, rayon = "+rayon_robot_adverse+", centre = "+position+", meurt à "+(date_actuelle+dureeAvantPeremption), this);
        listObstaclesMobiles.add(obstacle);
        checkGameElements(position);
    }
    
    /**
     * Supprime les éléments de jeux qui sont proches de cette position.
     * @param position
     */
    private void checkGameElements(Vec2 position)
    {
        // On vérifie aussi ceux qui ont un rayon nul (distributeur, clap, ..)
        for(GameElementNames g: GameElementNames.values())
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
    public void supprimerObstaclesPerimes(long date)
    {
        if(isThereHypotheticalEnemy && hypotheticalEnemy.isDestructionNecessary(date))
        	isThereHypotheticalEnemy = false;
        int size = listObstaclesMobiles.size();
        
        for(; firstNotDead < size; firstNotDead++)
        {
        	if(!listObstaclesMobiles.get(firstNotDead).isDestructionNecessary(date))
        		break;
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
    public boolean obstacleFixeDansSegmentPathfinding(Vec2 A, Vec2 B)
    {
    	ObstacleRectangular chemin = new ObstacleRectangular(A, B);
    	for(ObstaclesFixes o: ObstaclesFixes.values())
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
    public boolean obstacleTableDansSegment(Vec2 A, Vec2 B)
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
    public boolean obstacleProximiteDansSegment(Vec2 A, Vec2 B, int date)
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
    public boolean isObstacleFixePresentCapteurs(Vec2 position)
    {
    	for(ObstaclesFixes o: ObstaclesFixes.values())
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
    public boolean isObstacleMobilePresent(Vec2 position, int distance) 
    {
        if(isThereHypotheticalEnemy && isObstaclePresent(position, hypotheticalEnemy, distance))
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
    private boolean isObstaclePresent(Vec2 position, Obstacle o, int distance)
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
		rayon_robot_adverse = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		dureeAvantPeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);
		log.updateConfig();
		table.updateConfig();
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

	public boolean isTraversableCourbe(PathfindingNodes objectifFinal, PathfindingNodes intersection, Vec2 directionAvant, int tempsDepuisDebutMatch)
	{
		// TODO: calculer la vitesse qui permet exactement de passer?
		Speed vitesse = Speed.BETWEEN_SCRIPTS;
		
		obstacleTrajectoireCourbe = new ObstacleTrajectoireCourbe(objectifFinal, intersection, directionAvant, vitesse);

		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values())
    		if(obstacleTrajectoireCourbe.isColliding(o.getObstacle()))
    			return false;

    	// Collision avec un ennemi hypothétique?
        if(isThereHypotheticalEnemy && obstacleTrajectoireCourbe.isColliding(hypotheticalEnemy))
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
	
}
