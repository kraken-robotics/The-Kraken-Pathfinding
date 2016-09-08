package pathfinding.dstarlite.gridspace;

import java.util.ArrayList;
import java.util.BitSet;

import obstacles.ObstaclesFixes;
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.ObstacleArcCourbe;
import obstacles.types.ObstacleProximity;
import pathfinding.ChronoGameState;
import pathfinding.astarCourbe.AStarCourbeNode;
import table.GameElementNames;
import table.Table;
import table.Tribool;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import container.Service;

/**
 * La classe qui contient la grille utilisée par le pathfinding.
 * Utilisée uniquement pour le pathfinding DStarLite.
 * Notifie quand il y a un changement d'obstacles
 * @author pf
 *
 */

public class GridSpace implements Service
{
	protected Log log;
	private ObstaclesIteratorPresent iteratorDStarLite;
	private ObstaclesIteratorPresent iteratorRemoveNearby;
	private ObstaclesMemory obstaclesMemory;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;

	private int distanceMinimaleEntreProximite;
	private int rayonRobot;
	
	/**
	 * Comme on veut que le DStarLite recherche plus de noeuds qu'il n'y en aurait besoin, ce coeff ne vaut pas 1
	 */
//	private static final int COEFF_HEURISTIQUE = 2;
	
	// cette grille est constante, c'est-à-dire qu'elle ne contient que les obstacles fixes
	private BitSet grilleStatique = new BitSet(PointGridSpace.NB_POINTS);
	
	private static ArrayList<PointDirige> masque = new ArrayList<PointDirige>();
	private static int centreMasque;
	private long deathDateLastObstacle;
	
	public GridSpace(Log log, ObstaclesIteratorPresent iteratorDStarLite, ObstaclesIteratorPresent iteratorRemoveNearby, ObstaclesMemory obstaclesMemory, PointGridSpaceManager pointManager, PointDirigeManager pointDManager)
	{
		this.obstaclesMemory = obstaclesMemory;
		this.log = log;
		this.pointManager = pointManager;
		this.pointDManager = pointDManager;
		this.iteratorDStarLite = iteratorDStarLite;
		this.iteratorRemoveNearby = iteratorRemoveNearby;
		
		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			for(ObstaclesFixes o : ObstaclesFixes.values)
			{
				if(o.getObstacle().squaredDistance(PointGridSpace.computeVec2(new PointGridSpace(i))) < (int)(PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS/2 * PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS/2))
				{
					grilleStatique.set(i);
					break; // on ne vérifie pas les autres obstacles
				}
			}
		log.debug("Grille statique initialisée");
	}
	

	@Override
	public void useConfig(Config config)
	{
		distanceMinimaleEntreProximite = config.getInt(ConfigInfo.DISTANCE_BETWEEN_PROXIMITY_OBSTACLES);
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
		int rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		int rayonPoint = (int) Math.round((rayonEnnemi + rayonRobot) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		int tailleMasque = 2*(rayonPoint+1)+1;
		int squaredRayonPoint = rayonPoint * rayonPoint;
		centreMasque = tailleMasque / 2;
		for(int i = 0; i < tailleMasque; i++)
			for(int j = 0; j < tailleMasque; j++)
				if((i-centreMasque) * (i-centreMasque) + (j-centreMasque) * (j-centreMasque) > squaredRayonPoint)
					for(Direction d : Direction.values())
					{
						int i2 = i + d.deltaX, j2 = j + d.deltaY;
						if((i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) <= squaredRayonPoint)
							masque.add(pointDManager.get(j,i,d));
					}
	}

	@Override
	public void updateConfig(Config config)
	{}

	/**
	 * Renvoie la distance en fonction de la direction.
	 * Attention ! Ne prend pas en compte les obstacles dynamiques
	 * @param i
	 * @return
	 */
	public int distanceStatique(PointDirige point)
	{
		if(!isTraversableStatique(point.point))
			return Integer.MAX_VALUE;
		if(point.dir.isDiagonal())
			return 1414;
		return 1000;
	}

	/**
	 * Ajoute le contour d'un obstacle de proximité dans la grille dynamique
	 * @param o
	 */
	private ArrayList<PointDirige> getMasqueObstacle(Vec2<ReadOnly> position)
	{
		PointGridSpace p = pointManager.get(position);
		ArrayList<PointDirige> out = new ArrayList<PointDirige>();
		
		for(PointDirige c : masque)
		{
			PointDirige point = pointDManager.get(pointManager.get(c.point.x + p.x - centreMasque, c.point.y + p.y - centreMasque), c.dir);
			if(point != null)
				out.add(point);
		}
		return out;
	}

	public synchronized void deleteOldObstacles()
	{
		// S'il y a effectivement suppression, on régénère la grille
		if(obstaclesMemory.deleteOldObstacles())
			notify(); // changement de la grille dynamique !
	}

	public long getNextDeathDate()
	{
		return obstaclesMemory.getNextDeathDate();
	}

	private boolean isTraversableStatique(PointGridSpace point)
	{
		return !grilleStatique.get(point.hashCode());
	}
	
	/**
	 * Un nouveau DStarLite commence. Il faut lui fournir les obstacles actuels
	 * @return
	 */
	public ArrayList<PointDirige> startNewPathfinding() // TODO renommer
	{
		iteratorDStarLite.reinit();
		ArrayList<PointDirige> out = new ArrayList<PointDirige>();
		ObstacleProximity o = null;
		while(iteratorDStarLite.hasNext())
		{
			o = iteratorDStarLite.next();
//			log.debug("Ajout d'un obstacle au début du dstarlite");
			out.addAll(o.getMasque());
		}
		if(o != null)
			deathDateLastObstacle = o.getDeathDate();
		else
			deathDateLastObstacle = 0;
		
		return out;
	}
	
	/**
	 * Retourne les obstacles à supprimer (indice 0) et ceux à ajouter (indice 1) dans le DStarLite
	 */
	public ArrayList<ObstacleProximity>[] getOldAndNewObstacles()
	{
		synchronized(obstaclesMemory)
		{
			@SuppressWarnings("unchecked")
			ArrayList<ObstacleProximity>[] out = new ArrayList[2];
			out[0] = new ArrayList<ObstacleProximity>();
			out[1] = new ArrayList<ObstacleProximity>();
	
			while(iteratorDStarLite.hasNextDead())
				out[0].add(iteratorDStarLite.next());
			ObstacleProximity p;
			while((p = obstaclesMemory.pollMortTot()) != null)
				out[0].add(p);
	
			long tmp = deathDateLastObstacle;
			while(iteratorDStarLite.hasNext())
			{
				ObstacleProximity o = iteratorDStarLite.next();
				long deathDate = o.getDeathDate();
				if(deathDate > deathDateLastObstacle)
				{
					tmp = deathDate;
					out[1].add(o);
				}
			}
			deathDateLastObstacle = tmp;
			iteratorDStarLite.reinit(); // l'itérateur reprendra juste avant les futurs obstacles périmés
			return out;
		}
	}

    /**
	 * Appelé par le thread des capteurs par l'intermédiaire de la classe capteurs
	 * Ajoute l'obstacle à la mémoire et dans le gridspace
     * Supprime les obstacles mobiles proches
     * Ça allège le nombre d'obstacles.
     * Utilisé par les capteurs
     * @param position
     * @return 
     * @return
     */
    public ObstacleProximity addObstacleAndRemoveNearbyObstacles(Vec2<ReadOnly> position)
    {
    	iteratorRemoveNearby.reinit();
    	while(iteratorRemoveNearby.hasNext())
        	if(iteratorRemoveNearby.next().isProcheCentre(position, distanceMinimaleEntreProximite))
        	{
        		log.debug("Suppression d'un obstacle");
        		iteratorRemoveNearby.remove();
        	}

    	ArrayList<PointDirige> masque = getMasqueObstacle(position);
		ObstacleProximity o = obstaclesMemory.add(position, masque);
		// pour un ajout, pas besoin de tout régénérer
		return o;
    }
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * Utilisé pour savoir s'il y a un ennemi devant nous.
     * @param position
     * @return
     */
/*    public boolean isThereObstacle(ChronoGameState state, Vec2<ReadOnly> position)
    {
    	state.iterator.reinit();
    	while(state.iterator.hasNext())
        	if(state.iterator.next().isProcheObstacle(position, rayonRobot + 20))
        		return true;
       return false;
    }*/

}
