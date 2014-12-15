package pathfinding;

import java.util.ArrayList;

import obstacles.ObstacleManager;
import container.Service;
import smartMath.Vec2;
import table.Table;
import utils.Config;
import utils.Log;
import enums.ConfigInfo;
import enums.NodesConnection;
import enums.PathfindingNodes;
import exceptions.GridSpaceException;

/**
 * Contient les informations sur le graphe utilisé par le pathfinding.
 * Intègre un mécanisme de cache afin d'accélérer les calculs.
 * @author pf
 *
 */

public class GridSpace implements Service {
	
	private Log log;
	private Config config;
	
	// afin d'éviter les obstacles fixes et mobiles
	private ObstacleManager obstaclemanager;
		
	// Rempli de ALWAYS_IMPOSSIBLE et null. Ne change pas.
	private static NodesConnection[][] isConnectedModel = null;

	// Doit-on éviter les éléments de jeux? Ou peut-on foncer dedans?
	private boolean avoidGameElement = true;
	
	public GridSpace(Log log, Config config, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
				
		// Il est très important de ne faire ce long calcul qu'une seule fois,
		// à la première initialisation
		if(isConnectedModel == null)
		{
			initStatic();
			check_pathfinding_nodes();
		}
	}
	
    public void check_pathfinding_nodes()
    {
    	for(PathfindingNodes i: PathfindingNodes.values())
    		if(obstaclemanager.is_obstacle_fixe_present_pathfinding(i.getCoordonnees()))
    			log.warning("Node "+i+" dans obstacle fixe!", this);
    }
    

	private void initStatic()
	{
		log.debug("Calcul de isConnectedModel", this);
		isConnectedModel = new NodesConnection[PathfindingNodes.values().length][PathfindingNodes.values().length];

		for(PathfindingNodes i : PathfindingNodes.values())			
			for(PathfindingNodes j : PathfindingNodes.values())
				if(obstaclemanager.obstacle_fixe_dans_segment_pathfinding(i.getCoordonnees(), j.getCoordonnees()))
					isConnectedModel[i.ordinal()][j.ordinal()] = NodesConnection.ALWAYS_IMPOSSIBLE;
				else
					isConnectedModel[i.ordinal()][j.ordinal()] = null;
	}
	
	/**
	 * Réinitialise l'état des liaisons.
	 * A faire quand les obstacles mobiles ont changé.
	 */
	public void reinitConnections(long date)
	{
		obstaclemanager.supprimerObstaclesPerimes(date);
	}

	/**
	 * Surcouche de isConnected qui gère le cache
	 * @return
	 */
	public boolean isTraversable(PathfindingNodes i, PathfindingNodes j)
	{
		if(isConnectedModel[i.ordinal()][j.ordinal()] == NodesConnection.ALWAYS_IMPOSSIBLE)
		{
//			log.debug("Trajet entre "+i+" et "+j+" impossible à cause d'un obstacle fixe!", this);			
			return false;
		}
/*		else if(isConnected[i.ordinal()][j.ordinal()] != null)
		{
//			log.debug("Is traversable: use cache", this);			
			return isConnected[i.ordinal()][j.ordinal()].isTraversable();			
		}*/
		else if(obstaclemanager.obstacle_proximite_dans_segment(i.getCoordonnees(), j.getCoordonnees()))
		{
//			log.debug("Trajet entre "+i+" et "+j+" impossible à cause d'un obstacle de proximité", this);
			return NodesConnection.TMP_IMPOSSIBLE.isTraversable();
		}
		else if(avoidGameElement && obstaclemanager.obstacle_table_dans_segment(i.getCoordonnees(), j.getCoordonnees()))
		{
//			log.debug("Trajet entre "+i+" et "+j+" impossible à cause d'un élément de jeu", this);
			return NodesConnection.TMP_IMPOSSIBLE.isTraversable();
		}
		else
		{
//			log.debug("Pas de problème entre "+i+" et "+j, this);
			return NodesConnection.POSSIBLE.isTraversable();
		}
	}
	
	/**
	 * Retourne le point de passage le plus proche et accessible en ligne droite
	 * Attention, peut renvoyer "null" si aucun point de passage n'est atteignable en ligne droite.
	 * Cette méthode ne prend en compte que les obstacles de proximité, et pas les obstacles fixes.
	 * Donc elle ne peut pas planter parce qu'on est trop près d'un mur, par exemple.
	 * @param point
	 * @return
	 * @throws GridSpaceException 
	 */
	public PathfindingNodes nearestReachableNode(Vec2 point) throws GridSpaceException
	{
		PathfindingNodes indice_point_depart = null;
		float distance_min = Float.MAX_VALUE;
		for(PathfindingNodes i : PathfindingNodes.values())
		{
			float tmp = point.squaredDistance(i.getCoordonnees());
			if(tmp < distance_min && !obstaclemanager.obstacle_proximite_dans_segment(point, i.getCoordonnees()))
			{
				distance_min = tmp;
				indice_point_depart = i;
			}
		}
		if(indice_point_depart == null)
			throw new GridSpaceException();

		return indice_point_depart;
	}
	
	@Override
	public void updateConfig() {
		avoidGameElement = Boolean.parseBoolean(config.get(ConfigInfo.EVITE_ELEMENT_JEU));
	}

	public void copy(GridSpace other, long date)
	{
		obstaclemanager.copy(other.obstaclemanager, date);
		// On détruit le cache car le robot aura bougé
		other.reinitConnections(date);
	}
	
	public GridSpace clone(long date, Table table)
	{
		// On réutilise la table donnée.
		// Sinon, la table dans le GameState n'est pas la table de l'ObstacleManager.
		GridSpace cloned_gridspace = new GridSpace(log, config, obstaclemanager.clone(date, table));
		copy(cloned_gridspace, date);
		return cloned_gridspace;
	}
    
	/**
	 * Utilisé uniquement pour les tests
	 * @return
	 */
    public int nbObstaclesMobiles()
    {
    	return obstaclemanager.nbObstaclesMobiles();
    }

    /** 
     * A utiliser entre deux points
     * @param pointA
     * @param pointB
     * @return
     */
    public boolean isTraversable(Vec2 pointA, Vec2 pointB)
    {
    	// Evaluation paresseuse importante, car obstacle_proximite_dans_segment est bien plus rapide que obstacle_fixe_dans_segment
    	return !obstaclemanager.obstacle_proximite_dans_segment(pointA, pointB) && !obstaclemanager.obstacle_fixe_dans_segment_pathfinding(pointA, pointB);
    }
    
    
    /**
     * Créer un obstacle à une certaine date
     * Utilisé dans l'arbre des possibles.
     * @param position
     * @param date
     */
    public void creer_obstacle(Vec2 position, long date)
    {
    	obstaclemanager.creer_obstacle(position);
    	reinitConnections(date);
    }
    
    /**
     * Créer un obstacle maintenant.
     * Utilisé par le thread de capteurs.
     * @param position
     */
    public void creer_obstacle(Vec2 position)
    {
    	creer_obstacle(position, System.currentTimeMillis() - Config.getDateDebutMatch());
    }

    public void setAvoidGameElement(boolean avoidGameElement)
    {
    	this.avoidGameElement = avoidGameElement;
    }

    /**
     * Retourne la distance cumulée d'un chemin entre deux PathfindingNodes.
     * @param chemin
     * @return
     */
    public double distanceCumulee(ArrayList<PathfindingNodes> chemin)
    {
    	PathfindingNodes last = chemin.get(0);
    	double distance = 0;
    	for(PathfindingNodes n: chemin)
    	{
    		if(!isTraversable(last, n))
    			return Double.MAX_VALUE;
    		distance += last.distanceTo(n);
    		last = n;
    	}
    	return distance;
    }

}
