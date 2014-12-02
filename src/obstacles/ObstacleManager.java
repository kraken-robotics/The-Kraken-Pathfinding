package obstacles;

import java.util.ArrayList;
import java.util.Iterator;

import container.Service;
import smartMath.Vec2;
import utils.Config;
import utils.Log;

/**
 * Service qui traite tout ce qui concerne la gestion des obstacles.
 * @author pf
 *
 */

public class ObstacleManager implements Service
{
    // On met cette variable en static afin que, dans deux instances dupliquées, elle ne redonne pas les mêmes nombres
    private static int indice = 1;
    private Log log;
    private Config config;

    private ArrayList<ObstacleCircular> listObstacles = new ArrayList<ObstacleCircular>();
    private static ArrayList<Obstacle> listObstaclesFixes = null;
  
    private int hashObstacles;

    private int rayon_robot_adverse = 200;
    private int distanceApproximation = 100; // TODO: mettre dans config
    private long duree = 0;

    public ObstacleManager(Log log, Config config)
    {
        this.log = log;
        this.config = config;

        hashObstacles = 0;

        listObstaclesFixes = new ArrayList<Obstacle>();
            // TODO obstacles fixees
            
        updateConfig();
    }   

    /**
     * Créer un obstacle de proximité
     * @param position
     */
    public synchronized void creer_obstacle(final Vec2 position)
    {
        Vec2 position_sauv = position.clone();
        
        ObstacleProximity obstacle = new ObstacleProximity(position_sauv, rayon_robot_adverse, System.currentTimeMillis()+duree);
        log.warning("Obstacle créé, rayon = "+rayon_robot_adverse+", centre = "+position, this);
        listObstacles.add(obstacle);
        hashObstacles = indice++;
    }

    /**
     * Appel fait lors de l'anticipation, supprime les obstacles périmés à une date future
     * @param date
     */
    public synchronized void supprimerObstaclesPerimes(long date)
    {
        Iterator<ObstacleCircular> iterator = listObstacles.iterator();
        while(iterator.hasNext())
        {
            ObstacleCircular obstacle = iterator.next();
            if (obstacle.isDestructionNecessary(date))
            {
                System.out.println("Suppression d'un obstacle de proximité: "+obstacle);
                iterator.remove();
                hashObstacles = indice++;
            }
        }   
    }    

    /**
     * Utilisé pour les tests
     * @return le nombre d'obstacles mobiles détectés
     */
    public int nb_obstacles()
    {
        return listObstacles.size();
    }

    public ObstacleManager clone(long date)
    {
    	ObstacleManager cloned_manager = new ObstacleManager(log, config);
		copy(cloned_manager);
		cloned_manager.supprimerObstaclesPerimes(date);
		return cloned_manager;
    }
    
    /**
     * Nécessaire au fonctionnement du memory manager
     * @param other
     */
    public void copy(ObstacleManager other)
    {
        if(other.hashObstacles != hashObstacles)
        {
            other.listObstacles.clear();
            for(ObstacleCircular item: listObstacles)
                other.listObstacles.add(item.clone());
            other.hashObstacles = hashObstacles;
        }
    }
 
    /**
     * Y a-t-il un obstacle de proximité dans ce segment?
     * @param sommet1
     * @param sommet2
     * @return
     */
    public boolean obstacle_proximite_dans_segment(Vec2 A, Vec2 B)
    {
        Iterator<ObstacleCircular> iterator = listObstacles.iterator();
        while(iterator.hasNext())
        {
            ObstacleCircular o = iterator.next();
            
            if(o.obstacle_proximite_dans_segment(A, B))
                return true;
        }
        return false;
    }
    
    /**
     * Surcouche de obstacle_existe en utilisant la distance de la config.
     * Utilisé pour savoir si ce qu'on voit est un obstacle déjà connu.
     * @param position
     * @return
     */
    public boolean obstacle_existe(Vec2 position) {
    	return obstacle_existe(position, distanceApproximation);
    }
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * Cela permet de ne pas détecter en obstacle mobile des obstacles fixes.
     * De plus, ça allège le nombre d'obstacles.
     * Utilisé pour savoir s'il y a un ennemi devant nous.
     * @param position
     * @return
     */
    public synchronized boolean obstacle_existe(Vec2 position, int distance) {
        Iterator<Obstacle> iterator2 = listObstaclesFixes.iterator();
        while(iterator2.hasNext())
        {
            Obstacle o = iterator2.next();
            if(obstacle_existe(position, o, distance))
            {
                System.out.println("Obstacle: "+o);
                return true;
            }
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
    private boolean obstacle_existe(Vec2 position, Obstacle o, int distance)
    {
    	return o.isProcheObstacle(position, distance);
    }
    
    public int hash()
    {
        return hashObstacles;
    }

    public boolean equals(ObstacleManager other)
    {
        return hashObstacles == other.hashObstacles;
    }

	@Override
	public void updateConfig() {
		rayon_robot_adverse = Integer.parseInt(config.get("rayon_robot_adverse"));
		duree = Integer.parseInt(config.get("duree_peremption_obstacles"));		
	}
    

}
