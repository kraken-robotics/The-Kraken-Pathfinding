package table.obstacles;

import java.util.ArrayList;
import java.util.Iterator;

import smartMath.Vec2;
import utils.Config;
import utils.Log;

/**
 * Traite tout ce qui concerne la gestion des obstacles.
 * @author pf
 *
 */

public class ObstacleManager
{
    // On met cette variable en static afin que, dans deux instances dupliquées, elle ne redonne pas les mêmes nombres
    private static int indice = 1;
    private Log log;
    private Config config;

    private ArrayList<ObstacleCirculaire> listObstacles = new ArrayList<ObstacleCirculaire>();
    private static ArrayList<Obstacle> listObstaclesFixes = null;
  
    private int hashObstacles;

    private int rayon_robot_adverse = 200;
    private long duree = 0;

    public ObstacleManager(Log log, Config config)
    {
        this.log = log;
        this.config = config;

        hashObstacles = 0;

        // TODO: à quoi sert cette condition?
        if(listObstaclesFixes == null)
        {
            listObstaclesFixes = new ArrayList<Obstacle>();
            // TODO obstacles
        }
            
        maj_config();
    }
    
    public void maj_config()
    {
        rayon_robot_adverse = Integer.parseInt(config.get("rayon_robot_adverse"));
        duree = Integer.parseInt(config.get("duree_peremption_obstacles"));
    }
    

    /**
     * Utilisé par le pathfinding. Retourne uniquement les obstacles temporaires.
     * @return
     */
    public ArrayList<ObstacleCirculaire> getListObstacles()
    {
        return listObstacles;
    }
    
    /**
     * Utilisé par le pathfinding. Retourne uniquement les obstacles fixes.
     * @return
     */
    public ArrayList<Obstacle> getListObstaclesFixes()
    {
        return listObstaclesFixes;
    }
    
    /**
     * Créer un obstacle de proximité
     * @param position
     */
    public synchronized void creer_obstacle(final Vec2 position)
    {
        Vec2 position_sauv = position.clone();
        
        ObstacleProximite obstacle = new ObstacleProximite(position_sauv, rayon_robot_adverse, System.currentTimeMillis()+duree);
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
        Iterator<ObstacleCirculaire> iterator = listObstacles.iterator();
        while ( iterator.hasNext() )
        {
            Obstacle obstacle = iterator.next();
            if (obstacle instanceof ObstacleProximite && ((ObstacleProximite) obstacle).death_date <= date)
            {
                System.out.println("Suppression d'un obstacle de proximité: "+obstacle);
                iterator.remove();
                hashObstacles = indice++;
            }
        }   
    }    

    /**
     * Renvoie true si un obstacle est à une distance inférieur à "distance" du point "centre_detection"
     * @param centre_detection
     * @param distance
     * @return
     */
    public boolean obstaclePresent(final Vec2 centre_detection, int distance)
    {
        for(Obstacle obstacle: listObstacles)
        {
            // On regarde si l'intersection des cercles est vide
            if(obstacle instanceof ObstacleCirculaire && obstacle.position.squaredDistance(centre_detection) < (distance+((ObstacleCirculaire)obstacle).radius)*(distance+((ObstacleCirculaire)obstacle).radius))
                return true;
            else if(!(obstacle instanceof ObstacleCirculaire))
            {
                // Normalement, les obstacles non fixes sont toujours circulaires
                log.warning("Etrange, un obstacle non circulaire... actualiser \"obstaclePresent\" dans Table", this);
                if(obstacle.position.squaredDistance(centre_detection) < distance*distance)
                    return true;            
            }
        }
        return false;
    }    
    
    /**
     * Utilisé pour les tests
     * @return le nombre d'obstacles mobiles détectés
     */
    public int nb_obstacles()
    {
        return listObstacles.size();
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
            for(ObstacleCirculaire item: listObstacles)
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
        Iterator<ObstacleCirculaire> iterator = listObstacles.iterator();
        while(iterator.hasNext())
        {
            ObstacleCirculaire o = iterator.next();
            
            if(obstacle_proximite_dans_segment(A, B, o))
                return true;
        }
        return false;
    }
    

    /**
     * Ce cercle croise-t-il le segment [A,B]?
     * @param sommet1
     * @param sommet2
     * @param obs
     * @return
     */
    public boolean obstacle_proximite_dans_segment(Vec2 A, Vec2 B, ObstacleCirculaire obs)
    {
    	/**
    	 * Ce code a été honteusement pompé sur http://openclassrooms.com/courses/theorie-des-collisions/formes-plus-complexes
    	 */
    	
    	Vec2 C = obs.getPosition();
    	if (collisionDroite(A,B,obs) == false)
	        return false;  // si on ne touche pas la droite, on ne touchera jamais le segment
	    Vec2 AB = new Vec2(), AC = new Vec2(), BC = new Vec2();
	    AB.x = B.x - A.x;
	    AB.y = B.y - A.y;
	    AC.x = C.x - A.x;
	    AC.y = C.y - A.y;
	    BC.x = C.x - B.x;
	    BC.y = C.y - B.y;
	    float pscal1 = AB.x*AC.x + AB.y*AC.y;  // produit scalaire
	    float pscal2 = (-AB.x)*BC.x + (-AB.y)*BC.y;  // produit scalaire
	    if (pscal1>=0 && pscal2>=0)
	       return true;   // I entre A et B, ok.
	    // dernière possibilité, A ou B dans le cercle
	    if (A.squaredDistance(C) < obs.getRadius()*obs.getRadius())
	      return true;
	    if (B.squaredDistance(C) < obs.getRadius()*obs.getRadius())
	      return true;
	    return false;
    }

    private boolean collisionDroite(Vec2 A, Vec2 B, ObstacleCirculaire obs)
    {
    	/**
    	 * Ce code a été honteusement pompé sur http://openclassrooms.com/courses/theorie-des-collisions/formes-plus-complexes
    	 */

    	Vec2 C = obs.getPosition();
        Vec2 u = new Vec2();
        u.x = B.x - A.x;
        u.y = B.y - A.y;
        Vec2 AC = new Vec2();
        AC.x = C.x - A.x;
        AC.y = C.y - A.y;
        float numerateur = u.x*AC.y - u.y*AC.x;   // norme du vecteur v
        if (numerateur <0)
           numerateur = -numerateur ;   // valeur absolue ; si c'est négatif, on prend l'opposé.
        float denominateur =u.x*u.x + u.y*u.y;  // norme de u
        float CI = numerateur*numerateur / denominateur;
        if (CI < obs.getRadius()*obs.getRadius())
           return true;
        else
           return false;
    }
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * Cela permet de ne pas détecter en obstacle mobile des obstacles fixex.
     * De plus, ça allège le nombre d'obstacles.
     * @param position
     * @return
     */
    public synchronized boolean obstacle_existe(Vec2 position) {
        Iterator<Obstacle> iterator2 = listObstaclesFixes.iterator();
        while(iterator2.hasNext())
        {
            Obstacle o = iterator2.next();
            if(obstacle_existe(position, o))
            {
                System.out.println("Obstacle: "+o);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie pour un seul obstacle.
     * @param position
     * @param o
     * @return
     */
    private boolean obstacle_existe(Vec2 position, Obstacle o)
    {
        // Obstacle circulaire
        if(o instanceof ObstacleCirculaire && position.squaredDistance(o.position) <= (1.2*((ObstacleCirculaire)o).getRadius())*1.2*((ObstacleCirculaire)o).getRadius())
            return true;
        // Obstacle rectangulaire
        else if(o instanceof ObstacleRectangulaire && ((ObstacleRectangulaire)o).SquaredDistance(position) <= 100*100)
            return true;
        // Autre obstacle
        else if(position.squaredDistance(o.position) <= 100)
            return true;
        return false;
    }

    /**
     * Vérifie si "pos" est dans "obstacle"
     * @param pos
     * @param obstacle
     * @return
     */
    public boolean dans_obstacle(Vec2 pos, Obstacle obstacle)
    {
        if(obstacle instanceof ObstacleRectangulaire)
        {
            Vec2 position_obs = obstacle.getPosition();
            return !(pos.x<((ObstacleRectangulaire)obstacle).getLongueur_en_x()+position_obs.x && position_obs.x < pos.x && position_obs.y <pos.y && pos.y < position_obs.y+((ObstacleRectangulaire)obstacle).getLongueur_en_y());

        }           
        // sinon, c'est qu'il est circulaire
        return   !(pos.distance(obstacle.getPosition()) < ((ObstacleCirculaire)obstacle).getRadius());

    }
    
    public int hash()
    {
        return hashObstacles;
    }

    public boolean equals(ObstacleManager other)
    {
        return hashObstacles == other.hashObstacles;
    }
    

}
