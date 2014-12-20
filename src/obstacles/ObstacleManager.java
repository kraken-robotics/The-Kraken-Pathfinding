package obstacles;

import java.util.ArrayList;
import java.util.Iterator;

import container.Service;
import enums.ConfigInfo;
import enums.GameElementNames;
import enums.Tribool;
import smartMath.Vec2;
import table.Table;
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
    private Table table;
    
    // Les obstacles mobiles, c'est-à-dire des obstacles de proximité et de balise
    private ArrayList<ObstacleProximity> listObstaclesMobiles = new ArrayList<ObstacleProximity>();

    // Les obstacles fixes sont surtout utilisés pour savoir si un capteur détecte un ennemi ou un obstacle fixe
    // Commun à toutes les instances
    private static ArrayList<Obstacle> listObstaclesFixes = null;
  
    // Utilisé pour accélérer la copie
    private int hashObstacles;

    private int dilatation_obstacle = 30;
    private int rayon_robot_adverse = 200;
    private int distanceApproximation = 50;
    private int dureeAvantPeremption = 0;

    // L'initialisation a lieu une seule fois pour tous les objets.
    private void createListObstaclesFixes()
    {
        listObstaclesFixes = new ArrayList<Obstacle>();

        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(0,100),800,200)); // plaque rouge        
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(0,2000-580/2),1066,580)); // escalier
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(-1500+400/2,1200),400,22)); // bandes de bois zone de départ
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(1500-400/2,1200),400,22));
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(-1500+400/2,800),400,22));
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(1500-400/2,800),400,22));
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(-1200+50/2,2000-50/2),50,50)); // distributeurs
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(-900+50/2,2000-50/2),50,50));
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(900-50/2,2000-50/2),50,50));
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(1200-50/2,2000-50/2),50,50));

        // bords
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(0,0),3000,1));
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(-1500,1000),1,2000));
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(1500,1000),1,2000));
        listObstaclesFixes.add(new ObstacleRectangular(log, new Vec2(0,2000),3000,1));
    }
    
    public ObstacleManager(Log log, Config config, Table table)
    {
        this.log = log;
        this.config = config;
        this.table = table;

        hashObstacles = 0;

        if(listObstaclesFixes == null)
        	createListObstaclesFixes();
        
        updateConfig();
    }

    /**
     * Créer un obstacle de proximité
     * @param position
     */
    public void creer_obstacle(final Vec2 position, int date_actuelle)
    {
        Vec2 position_sauv = position.clone();
        ObstacleProximity obstacle = new ObstacleProximity(log, position_sauv, rayon_robot_adverse, date_actuelle+dureeAvantPeremption);
        log.warning("Obstacle créé, rayon = "+rayon_robot_adverse+", centre = "+position+", meurt à "+(date_actuelle+dureeAvantPeremption), this);
        listObstaclesMobiles.add(obstacle);
        check_game_element(position);
        hashObstacles = indice++;
    }
    
    /**
     * Supprime les éléments de jeux qui sont près de cette position.
     * @param position
     */
    private void check_game_element(Vec2 position)
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
    public boolean supprimerObstaclesPerimes()
    {
    	return supprimerObstaclesPerimes(System.currentTimeMillis() - Config.getDateDebutMatch());
    }

    /**
     * Appel fait lors de l'anticipation, supprime les obstacles périmés à une date future
     * Les obstacles étant triés du plus anciens au plus récent, le premier qui n'est pas supprimable
     * permet d'arrêter la recherche.
     * Renvoie vrai s'il y a eu une suppression, faux sinon.
     * @param date
     */
    public boolean supprimerObstaclesPerimes(long date)
    {
        Iterator<ObstacleProximity> iterator = listObstaclesMobiles.iterator();
        boolean out = false;
        while(iterator.hasNext())
        {
            ObstacleCircular obstacle = iterator.next();
            if (obstacle.isDestructionNecessary(date))
            {
//                log.debug("Suppression à "+date+" d'un obstacle de proximité: "+obstacle, this);
                iterator.remove();
                hashObstacles = indice++;
                out = true;
            }
            else
            	break;
        }   
        return out;
    }  
    
    /**
     * Utilisé UNIQUEMENT pour les tests!
     */
    public void clear_obstacles_mobiles()
    {
    	listObstaclesMobiles.clear();
    }

    /**
     * Utilisé pour les tests
     * @return le nombre d'obstacles mobiles détectés
     */
    public int nbObstaclesMobiles()
    {
        return listObstaclesMobiles.size();
    }

    public ObstacleManager clone(long date)
    {
    	ObstacleManager cloned_manager = new ObstacleManager(log, config, table.clone());
		copy(cloned_manager, date);
		return cloned_manager;
    }
    
    /**
     * Nécessaire au fonctionnement du memory manager
     * @param other
     */
    public void copy(ObstacleManager other, long date)
    {
		supprimerObstaclesPerimes(date);
    	table.copy(other.table);
        if(other.hashObstacles != hashObstacles)
        {
        	other.listObstaclesMobiles.clear();
        	for(ObstacleProximity o: listObstaclesMobiles)
        		other.listObstaclesMobiles.add(o);
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
	public boolean obstacle_fixe_dans_segment_pathfinding(Vec2 A, Vec2 B)
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
			// il y a un obstacle: pas besoin de vérifier le reste du segment
			if(is_obstacle_fixe_present(new Vec2(x0, y0), dilatation_obstacle))
				return true;
			
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
		return false;
	}

	/**
	 * Y a-t-il un obstacle de table dans ce segment?
	 * @param A
	 * @param B
	 * @return
	 */
    public boolean obstacle_table_dans_segment(Vec2 A, Vec2 B)
    {
        for(GameElementNames g: GameElementNames.values())
        	// Si on a interprété que l'ennemi est passé sur un obstacle,
        	// on peut passer dessus par la suite.
            if(table.isDone(g) == Tribool.FALSE && table.obstacle_proximite_dans_segment(g, A, B, dilatation_obstacle))
            {
//            	log.debug(o.getName()+" est dans le chemin.", this);
                return true;
            }

        return false;    	
    }

    /**
     * Y a-t-il un obstacle de proximité dans ce segment?
     * @param sommet1
     * @param sommet2
     * @return
     */
    public boolean obstacle_proximite_dans_segment(Vec2 A, Vec2 B, int date)
    {
    	return obstacle_proximite_dans_segment(A,B,dilatation_obstacle,date);
    }

    /**
     * Y a-t-il un obstacle de proximité dans ce segment?
     * Va-t-il disparaître pendant le temps de parcours?
     * @param sommet1
     * @param sommet2
     * @return
     */
    public boolean obstacle_proximite_dans_segment(Vec2 A, Vec2 B, int distance, int date)
    {
        Iterator<ObstacleProximity> iterator = listObstaclesMobiles.iterator();
        while(iterator.hasNext())
        {
        	ObstacleProximity o = iterator.next();
            if(o.obstacle_proximite_dans_segment(A, B, distance, date))
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
    public boolean is_obstacle_fixe_present_capteurs(Vec2 position)
    {
    	return is_obstacle_fixe_present(position, distanceApproximation);
    }

    public boolean is_obstacle_fixe_present_pathfinding(Vec2 position)
    {
    	return is_obstacle_fixe_present(position, dilatation_obstacle);
    }
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * Cela permet de ne pas détecter en obstacle mobile des obstacles fixes.
     * De plus, ça allège le nombre d'obstacles.
     * Utilisé pour savoir s'il y a un ennemi devant nous.
     * @param position
     * @return
     */
    public boolean is_obstacle_mobile_present(Vec2 position, int distance) 
    {
        Iterator<ObstacleProximity> iterator2 = listObstaclesMobiles.iterator();
        while(iterator2.hasNext())
        {
            Obstacle o = iterator2.next();
            if(is_obstacle_present(position, o, distance))
                return true;
        }
        return false;
    }

    /**
     * Y a-t-il un obstacle fixe près de ce point?
     * @param position
     * @param distance
     * @return
     */
    private boolean is_obstacle_fixe_present(Vec2 position, int distance) {
        Iterator<Obstacle> iterator2 = listObstaclesFixes.iterator();
        while(iterator2.hasNext())
        {
            Obstacle o = iterator2.next();
            if(is_obstacle_present(position, o, distance))
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
		rayon_robot_adverse = Integer.parseInt(config.get(ConfigInfo.RAYON_ROBOT_ADVERSE));
		dureeAvantPeremption = Integer.parseInt(config.get(ConfigInfo.DUREE_PEREMPTION_OBSTACLES));		
		dilatation_obstacle = Integer.parseInt(config.get(ConfigInfo.MARGE))+Integer.parseInt(config.get(ConfigInfo.RAYON_ROBOT));
		distanceApproximation = Integer.parseInt(config.get(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET));
	}
	
	/**
	 * Utilisé pour l'affichage
	 * @return 
	 */
	public ArrayList<Obstacle> getListObstaclesFixes()
	{
		return listObstaclesFixes;
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
	 * Utilisé pour l'affichage
	 * @return 
	 */
	public GameElement[] getListGameElement()
	{
		return table.getObstacles();
	}

	/**
	 * Utilisé pour l'affichage
	 * @return 
	 */
	public int getDilatationObstacle()
	{
		return dilatation_obstacle;
	}

	public void setDone(GameElementNames element, Tribool done)
	{
		table.setDone(element, done);
	}

	public Tribool isDone(GameElementNames element)
	{
		return table.isDone(element);
	}

	public int getHashTable()
	{
		return table.getHash();
	}

}
