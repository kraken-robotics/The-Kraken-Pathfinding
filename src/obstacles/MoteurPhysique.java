package obstacles;

import obstacles.types.ObstacleArcCourbe;
import obstacles.types.ObstacleProximity;
import pathfinding.ChronoGameState;
import pathfinding.astarCourbe.AStarCourbeNode;
import table.GameElementNames;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import container.Service;

/**
 * Service qui permet de calculer, pour un certain GameState, des collisions
 * @author pf
 *
 */

public class MoteurPhysique implements Service {
	
	protected Log log;
	
	private int distanceApproximation;
	private int distanceMinimaleEntreProximite;
	private int rayonRobot;
	
	public MoteurPhysique(Log log)
	{
		this.log = log;
	}

    /**
     * Utilisé pour savoir si ce qu'on voit est un obstacle fixe.
     * @param position
     * @return
     */
    public boolean isObstacleFixePresentCapteurs(Vec2<ReadOnly> position)
    {
    	for(ObstaclesFixes o: ObstaclesFixes.obstaclesFixesVisibles)
    		if(o.getObstacle().squaredDistance(position) < distanceApproximation * distanceApproximation)
                return true;
        return false;
    }

    /**
     * Supprime les obstacles mobiles proches
     * Ça allège le nombre d'obstacles.
     * @param position
     * @return
     */
    public void removeNearbyObstacles(ChronoGameState state, Vec2<ReadOnly> position)
    {
    	state.iterator.reinit();
    	while(state.iterator.hasNext())
        	if(state.iterator.next().isProcheObstacle(position, distanceMinimaleEntreProximite))
        		state.iterator.remove();
    }
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * Utilisé pour savoir s'il y a un ennemi devant nous.
     * @param position
     * @return
     */
    public boolean isThereObstacle(ChronoGameState state, Vec2<ReadOnly> position)
    {
    	state.iterator.reinit();
    	while(state.iterator.hasNext())
        	if(state.iterator.next().isProcheObstacle(position, rayonRobot + 20))
        		return true;
       return false;
    }
    
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);		
		distanceMinimaleEntreProximite = config.getInt(ConfigInfo.DISTANCE_BETWEEN_PROXIMITY_OBSTACLES);
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
	}
	
	/**
	 * Y a-t-il collision sur le chemin d'une trajectoire courbe ?
	 * @param node
	 * @return
	 */
	public boolean isTraversableCourbe(AStarCourbeNode node)
	{
		ObstacleArcCourbe obs = new ObstacleArcCourbe(node.came_from_arc);

		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values)
    		if(o.getObstacle().isColliding(obs))
    			return false;

    	// Collision avec un obstacle de proximité ?
    	node.state.iterator.reinit();
    	while(node.state.iterator.hasNext())
           	if(node.state.iterator.next().isColliding(obs))
        		return false;

    	return true;
	}
	
	/**
	 * g est-il proche de position? (utilisé pour vérifier si on shoot dans un élément de jeu)
	 * @param g
	 * @param positionCentreRotation
	 * @param rayon_robot_adverse
	 * @return
	 */
	public boolean didTheEnemyTakeIt(GameElementNames g, ObstacleProximity o)
	{
		return g.getObstacle().isProcheObstacle(o.position, o.radius);
	}

	public boolean didWeShootIt(GameElementNames g, Vec2<ReadOnly> position)
	{
		return g.getObstacle().isProcheObstacle(position, rayonRobot);
	}

}
