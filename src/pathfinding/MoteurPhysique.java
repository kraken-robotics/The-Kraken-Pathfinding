package pathfinding;

import obstacles.types.Obstacle;
import obstacles.types.ObstacleProximity;
import obstacles.types.ObstacleRectangular;
import obstacles.types.ObstacleTrajectoireCourbe;
import obstacles.types.ObstaclesFixes;
import permissions.ReadOnly;
import planification.astar.arc.PathfindingNodes;
import robot.RobotReal;
import robot.Speed;
import strategie.GameState;
import table.GameElementNames;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import container.Service;
import enums.Tribool;

/**
 * Service qui permet de calculer, pour un certain GameState, des collisions
 * @author pf
 *
 */

public class MoteurPhysique implements Service {
	
	protected Log log;
	
	private int distanceApproximation;
	
	public MoteurPhysique(Log log)
	{
		this.log = log;
	}

    /**
     * Cette méthode vérifie les obstacles fixes uniquement.
     * Elle est utilisée dans le lissage.
     * @param A
     * @param B
     * @return
     */
    public boolean obstacleFixeDansSegmentPathfinding(Vec2<ReadOnly> A, Vec2<ReadOnly> B)
    {
    	ObstacleRectangular chemin = new ObstacleRectangular(A, B);
    	for(ObstaclesFixes o: ObstaclesFixes.values)
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
    public boolean obstacleTableDansSegment(GameState<RobotReal,ReadOnly> state, Vec2<ReadOnly> A, Vec2<ReadOnly> B)
    {
    	ObstacleRectangular chemin = new ObstacleRectangular(A, B);
        for(GameElementNames g: GameElementNames.values())
        	// Si on a interprété que l'ennemi est passé sur un obstacle,
        	// on peut passer dessus par la suite.
            if(state.table.isDone(g) == Tribool.FALSE && obstacle_proximite_dans_segment(g, chemin))
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
    public boolean obstacleProximiteDansSegment(GameState<RobotReal,ReadOnly> state, Vec2<ReadOnly> A, Vec2<ReadOnly> B, int date)
    {
  //      if(isThereHypotheticalEnemy && hypotheticalEnemy.obstacle_proximite_dans_segment(A, B, date))
  //      	return true;
        
    	state.iterator.reinit();
    	while(state.iterator.hasNext())
        	if(state.iterator.next().obstacle_proximite_dans_segment(A, B, date))
        		return true;

        return false;
    }

    /**
     * Utilisé pour savoir si ce qu'on voit est un obstacle fixe.
     * @param position
     * @return
     */
    public boolean isObstacleFixePresentCapteurs(Vec2<ReadOnly> position)
    {
    	for(ObstaclesFixes o: ObstaclesFixes.values)
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
    public boolean isObstacleMobilePresent(GameState<RobotReal,ReadOnly> state, Vec2<ReadOnly> position, int distance) 
    {
    //    if(isThereHypotheticalEnemy && isObstaclePresent(position, hypotheticalEnemy.getReadOnly(), distance))
    //    	return true;
    	state.iterator.reinit();
    	while(state.iterator.hasNext())
        	if(isObstaclePresent(position, state.iterator.next(), distance))
        		return true;
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
    private boolean isObstaclePresent(Vec2<ReadOnly> position, Obstacle o, int distance)
    {
    	return o.isProcheObstacle(position, distance);
    }
    
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);		
	}
	

	public boolean isTraversableCourbe(GameState<RobotReal,ReadOnly> state, PathfindingNodes objectifFinal, PathfindingNodes intersection, Vec2<ReadOnly> directionAvant, int tempsDepuisDebutMatch)
	{
		// TODO: calculer la vitesse qui permet exactement de passer?
		Speed vitesse = Speed.BETWEEN_SCRIPTS;
		
		ObstacleTrajectoireCourbe obstacleTrajectoireCourbe = new ObstacleTrajectoireCourbe(objectifFinal, intersection, directionAvant, vitesse);

		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values)
    		if(obstacleTrajectoireCourbe.isColliding(o.getObstacle()))
    			return false;

    	// Collision avec un ennemi hypothétique?
  //      if(isThereHypotheticalEnemy && obstacleTrajectoireCourbe.isColliding(hypotheticalEnemy.getReadOnly()))
  //      	return false;

    	state.iterator.reinit();
    	while(state.iterator.hasNext())
           	if(obstacleTrajectoireCourbe.isColliding(state.iterator.next()))
        		return false;

        return true;
	}

	/**
	 * g est-il proche de position? (utilisé pour vérifier si on shoot dans un élément de jeu)
	 * @param g
	 * @param position
	 * @param rayon_robot_adverse
	 * @return
	 */
	public boolean didTheEnemyTakeIt(GameElementNames g, ObstacleProximity o)
	{
		return g.getObstacle().isProcheObstacle(o.position, o.radius);
	}

	/**
	 * g est-il dans le segment[a, b]?
	 * @param g
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean obstacle_proximite_dans_segment(GameElementNames g, ObstacleRectangular o)
	{
		return o.isColliding(g.getObstacle());
	}

}
