package table.obstacles;

import java.util.ArrayList;

import smartMath.Vec2;
import utils.Log;
import utils.Config;

/**
 * Traite tout ce qui concerne la gestion des obstacles.
 * @author pf, marsu
 *
 */

public class ObstacleManager
{
    @SuppressWarnings("unused")
    private Log log;
    @SuppressWarnings("unused")
	private Config config;

    private ArrayList<ObstacleCircular> listObstacles = new ArrayList<ObstacleCircular>();
  
    public ObstacleManager(Log log, Config config)
    {
        this.log = log;
        this.config = config;
        
        maj_config();
    }
    
    public void maj_config()
    {
    }
    

    public void copy(ObstacleManager other)
    {
    }
    
    

    /**
     * Utilis� par le pathfinding. Retourne uniquement les obstacles temporaires.
     * @return
     */
    public ArrayList<ObstacleCircular> getListObstacles()
    {
        return listObstacles;
    }
    
    /**
     * Utilis� par le pathfinding. Retourne uniquement les obstacles fixes.
     * @return
     */
    public ArrayList<Obstacle> getListObstaclesFixes(int codeTorches)
    {
    	// TODO
        return new ArrayList<Obstacle>();
    }
    
    

    public synchronized void creer_obstacle(final Vec2 position)
    {
    	// TODO
    }

    /**
     * Appel fait lors de l'anticipation, supprime les obstacles p�rim�s � une date future
     * @param date
     */
    public synchronized void supprimerObstaclesPerimes(long date)
    {
    	// Et pouf !
    	// TODO
    }
    

    /**
     * Renvoie true si un obstacle est � une distance inf�rieur � "distance" du point "centre_detection"
     * @param centre_detection
     * @param distance
     * @return
     */
    public boolean obstaclePresent(final Vec2 centre_detection, int distance)
    {
    	//TODO
    	return false;
    }   

    /**
     * Change le position d'un robot adverse
     * @param i num�ro du robot
     * @param position nouvelle position du robot
     */
    public synchronized void deplacer_robot_adverse(int i, final Vec2 position)
    {
    	//TODO
    }
    
    /**
     * Utilis� par le thread de strat�gie
     * @return
     */
    public Vec2[] get_positions_ennemis()
    {
    	// TODO
        return  new Vec2[1];
    }
    
    
    /**
     * Utilis� pour les tests
     * @return le nombre ed'obstacles mobiles d�tect�s
     */
    public int nb_obstacles()
    {
        return listObstacles.size();
    }
    
    
    public boolean dans_obstacle(Vec2 pos, Obstacle obstacle)
    {

    	//TODO !
    	return true;

    }
    
    
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * @param position
     */
    public synchronized boolean obstacle_existe(Vec2 position)
    {
    	//TODO
    	boolean IDontKnow = false;
        return IDontKnow;
    	
    }
    
    /**
     *  Cette instance est elle dans le même état que other ?
     *  @param other
     */
    public boolean equals(ObstacleManager other)
    {
    	//TODO
    	boolean IDontKnow = false;
        return IDontKnow;
    }
    

}
