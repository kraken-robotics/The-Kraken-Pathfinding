package obstacles;

import java.util.ArrayList;
import java.util.Collections;
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

    // Les obstacles mobiles, c'est-à-dire des obstacles de proximité et de balise
    private ArrayList<ObstacleCircular> listObstacles = new ArrayList<ObstacleCircular>();

    // Les obstacles fixes sont surtout utilisés pour savoir si un capteur détecte un ennemi ou un obstacle fixe
    private ArrayList<Obstacle> listObstaclesFixes;
  
    // Utilisé pour accélérer la copie
    private int hashObstacles;

    private int rayon_robot_adverse = 200;
    private int distanceApproximation = 100; // TODO: mettre dans config
    private long dureeAvantPeremption = 0;

    public ObstacleManager(Log log, Config config)
    {
        this.log = log;
        this.config = config;

        hashObstacles = 0;

        listObstaclesFixes = new ArrayList<Obstacle>();
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(0,100),800,200)); // plaque rouge
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(0,2000-580/2),1066,580)); // escalier
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(-1500+400/2,1200),22,400)); // bandes de bois zone de départ
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(1500-400/2,1200),22,400));
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(-1500+400/2,800),22,400));
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(1500-400/2,800),22,400));
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(-1200+50/2,2000-50/2),50,50)); // distributeurs
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(-900+50/2,2000-50/2),50,50));
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(900-50/2,2000-50/2),50,50));
        listObstaclesFixes.add(new ObstacleRectangular(new Vec2(1200-50/2,2000-50/2),50,50));
            
        updateConfig();
    }   

    /**
     * Créer un obstacle de proximité
     * @param position
     */
    public synchronized void creer_obstacle(final Vec2 position)
    {
        Vec2 position_sauv = position.clone();
        
        ObstacleProximity obstacle = new ObstacleProximity(position_sauv, rayon_robot_adverse, System.currentTimeMillis()+dureeAvantPeremption);
        log.warning("Obstacle créé, rayon = "+rayon_robot_adverse+", centre = "+position, this);
        listObstacles.add(obstacle);
        hashObstacles = indice++;
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
    public int nbObstaclesMobiles()
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
        	Collections.copy(other.listObstacles, listObstacles);
            other.hashObstacles = hashObstacles;
        }
    }
 
    /**
     * Cette méthode vérifie les obstacles fixes uniquement.
     * Elle est *bien* plus lente que obstacle_proximite_dans_segment et ne doit être utilisé qu'une seule fois
     * @param A
     * @param B
     * @return
     */
	public boolean obstacle_dans_segment(Vec2 A, Vec2 B)
	{
		int x0 = A.x, y0 = A.y;
		int x1 = B.x, y1 = B.y;

		// Bresenham's line algorithm
		
		int dx = Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
		int dy = Math.abs(y1-y0), sy = y0<y1 ? 1 : -1; 
		int err = (dx>dy ? dx : -dy)/2;
		int e2;
		 
		while(x0!=x1 || y0!=y1)
		{
			if(is_obstacle_fixe_present(new Vec2(x0, y0), 0))
				return false;
			
			e2 = err;
			if (e2 >-dx)
			{
				err -= dy;
				x0 += sx;
			}
			if (e2 < dy)
			{
				err += dx;
				y0 += sy;
			}
		}
		return true;
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
    public boolean is_obstacle_fixe_present(Vec2 position) {
    	return is_obstacle_fixe_present(position, distanceApproximation);
    }
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * Cela permet de ne pas détecter en obstacle mobile des obstacles fixes.
     * De plus, ça allège le nombre d'obstacles.
     * Utilisé pour savoir s'il y a un ennemi devant nous.
     * @param position
     * @return
     */
    public synchronized boolean is_obstacle_mobile_present(Vec2 position, int distance) {
        Iterator<ObstacleCircular> iterator2 = listObstacles.iterator();
        while(iterator2.hasNext())
        {
            Obstacle o = iterator2.next();
            if(is_obstacle_present(position, o, distance))
            {
                System.out.println("Obstacle: "+o);
                return true;
            }
        }
        return false;
    }

    /**
     * Y a-t-il un obstacle fixe près de ce point?
     * @param position
     * @param distance
     * @return
     */
    public boolean is_obstacle_fixe_present(Vec2 position, int distance) {
        Iterator<Obstacle> iterator2 = listObstaclesFixes.iterator();
        while(iterator2.hasNext())
        {
            Obstacle o = iterator2.next();
            if(is_obstacle_present(position, o, distance))
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
    private boolean is_obstacle_present(Vec2 position, Obstacle o, int distance)
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
		dureeAvantPeremption = Integer.parseInt(config.get("duree_peremption_obstacles"));		
	}
    

}
