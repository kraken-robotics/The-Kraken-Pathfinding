package table;

import java.util.ArrayList;
import java.util.Iterator;

import obstacles.Obstacle;
import obstacles.ObstacleProximity;
import obstacles.ObstacleRectangular;
import obstacles.gameElement.GameElement;
import obstacles.gameElement.GameElementNames;
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
    
    // Les obstacles fixes sont surtout utilisés pour savoir si un capteur détecte un ennemi ou un obstacle fixe
    // Commun à toutes les instances
    private static ArrayList<Obstacle> listObstaclesFixes = null;
  
    private int firstNotDead = 0;

    private int dilatation_obstacle = 300;
    private int rayon_robot_adverse = 200;
    private int distanceApproximation = 50;
    private int dureeAvantPeremption = 0;
    
    // L'initialisation a lieu une seule fois pour tous les objets.
    private void createListObstaclesFixes()
    {
        listObstaclesFixes = new ArrayList<Obstacle>();

        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(0,100),800,200)); // plaque rouge
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(0,2000-580/2),1066,580)); // escalier
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(-1500+400/2,1200),400,22)); // bandes de bois zone de départ
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(1500-400/2,1200),400,22));
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(-1500+400/2,800),400,22));
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(1500-400/2,800),400,22));
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(-1200+50/2,2000-50/2),50,50)); // distributeurs
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(-900+50/2,2000-50/2),50,50));
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(900-50/2,2000-50/2),50,50));
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(1200-50/2,2000-50/2),50,50));

        // bords
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(0,0),3000,1));
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(-1500,1000),1,2000));
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(1500,1000),1,2000));
        listObstaclesFixes.add(new ObstacleRectangular(log, config, new Vec2(0,2000),3000,1));
    }
    
    public ObstacleManager(Log log, Config config, Table table)
    {
        this.log = log;
        this.config = config;
        this.table = table;

        if(listObstaclesFixes == null)
        	createListObstaclesFixes();

        // On n'instancie hypotheticalEnnemy qu'une seule fois
        if(hypotheticalEnemy == null)
        	hypotheticalEnemy = new ObstacleProximity(log, config,  new Vec2(), rayon_robot_adverse, -1000);
        updateConfig();
    }

    public void createHypotheticalEnnemy(Vec2 position, int date_actuelle)
    {
    	isThereHypotheticalEnemy = true;
    	hypotheticalEnemy.setPosition(position);
    	hypotheticalEnemy.setDeathDate(date_actuelle+dureeAvantPeremption);
        check_game_element(position);
    }
    
    /**
     * Créer un obstacle de proximité
     * @param position
     */
    public void creer_obstacle(final Vec2 position, int date_actuelle)
    {
        Vec2 position_sauv = position.clone();
        ObstacleProximity obstacle = new ObstacleProximity(log, config, position_sauv, rayon_robot_adverse, date_actuelle+dureeAvantPeremption);
//        log.warning("Obstacle créé, rayon = "+rayon_robot_adverse+", centre = "+position+", meurt à "+(date_actuelle+dureeAvantPeremption), this);
        listObstaclesMobiles.add(obstacle);
        check_game_element(position);
    }
    
    /**
     * Supprime les éléments de jeux qui sont proches de cette position.
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
    public void clear_obstacles_mobiles()
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
     * Elle est *bien* plus lente que obstacle_proximite_dans_segment et doit être évitée autant que possible
     * Elle est utilisée dans le lissage.
     * @param A
     * @param B
     * @return
     */
	public boolean obstacle_fixe_dans_segment_pathfinding(Vec2 A, Vec2 B)
	{
		
	// ce code provient de http://tech-algorithm.com/articles/drawing-line-using-bresenham-algorithm/
	    int w = B.x - A.x ;
	    int h = B.y - A.y ;
	    int modulo = 0;
	    int x = A.x, y = A.y, dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
	    if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
	    if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
	    if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
	    int longest = Math.abs(w) ;
	    int shortest = Math.abs(h) ;
	    if (!(longest>shortest)) {
	        longest = Math.abs(h) ;
	        shortest = Math.abs(w) ;
	        if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
	        dx2 = 0 ;            
	    }
	    int numerator = longest >> 1 ;
	    for (int i=0;i<=longest;i++) {

	    	if((modulo++ & 7) == 0 && is_obstacle_fixe_present(new Vec2(x, y), dilatation_obstacle))
	    		return true;
	    	numerator += shortest ;
	        if (!(numerator<longest)) {
	            numerator -= longest ;
	            x += dx1 ;
	            y += dy1 ;
	        } else {
	            x += dx2 ;
	            y += dy2 ;
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
            if(table.isDone(g) == Tribool.FALSE && table.obstacle_proximite_dans_segment(g, A, B))
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
        if(isThereHypotheticalEnemy && hypotheticalEnemy.obstacle_proximite_dans_segment(A, B, distance, date))
        	return true;
        
        int size = listObstaclesMobiles.size();
        for(; firstNotDead < size; firstNotDead++)
        	if(listObstaclesMobiles.get(firstNotDead).obstacle_proximite_dans_segment(A, B, distance, date))
        		return true;

        return false;
/*        iterator = listObstaclesMobiles.listIterator(firstNotDead);
        while(iterator.hasNext())
            if(iterator.next().obstacle_proximite_dans_segment(A, B, distance, date))
                return true;
        return false;*/
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
        if(isThereHypotheticalEnemy && is_obstacle_present(position, hypotheticalEnemy, distance))
        	return true;
        int size = listObstaclesMobiles.size();
        for(; firstNotDead < size; firstNotDead++)
        {
        	if(is_obstacle_present(position, listObstaclesMobiles.get(firstNotDead), distance))
        		return true;
        }
        return false;
/*        iterator = listObstaclesMobiles.listIterator(firstNotDead);
        while(iterator.hasNext())
            if(is_obstacle_present(position, iterator.next(), distance))
                return true;
        return false;*/
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
        return firstNotDead;
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
		dilatation_obstacle = config.getInt(ConfigInfo.MARGE)
				+ config.getInt(ConfigInfo.RAYON_ROBOT);
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);
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
	
	public int getFirstNotDead()
	{
		return firstNotDead;
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

}
