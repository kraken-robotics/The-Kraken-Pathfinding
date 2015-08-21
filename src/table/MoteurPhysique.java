package table;

import obstacles.Obstacle;
import obstacles.ObstacleRectangular;
import obstacles.ObstacleTrajectoireCourbe;
import obstacles.ObstaclesFixes;
import permissions.ReadOnly;
import planification.astar.arc.PathfindingNodes;
import robot.Speed;
import utils.Config;
import utils.Log;
import utils.Vec2;
import container.Service;
import enums.Tribool;

public class MoteurPhysique implements Service {
	
	private Log log;
	private ObstacleManager obstaclemanager;
	private Table table;
	
	public MoteurPhysique(Log log, ObstacleManager obstaclemanager, Table table)
	{
		this.log = log;
		this.obstaclemanager = obstaclemanager;
		this.table = table;
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
    public boolean obstacleTableDansSegment(Vec2<ReadOnly> A, Vec2<ReadOnly> B)
    {
    	ObstacleRectangular chemin = new ObstacleRectangular(A, B);
        for(GameElementNames g: GameElementNames.values())
        	// Si on a interprété que l'ennemi est passé sur un obstacle,
        	// on peut passer dessus par la suite.
            if(table.isDone(g) == Tribool.FALSE && table.obstacle_proximite_dans_segment(g, chemin))
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
    public boolean obstacleProximiteDansSegment(Vec2<ReadOnly> A, Vec2<ReadOnly> B, int date)
    {
  //      if(isThereHypotheticalEnemy && hypotheticalEnemy.obstacle_proximite_dans_segment(A, B, date))
  //      	return true;
        
        int size = listObstaclesMobiles.size();
        for(int tmpFirstNotDead = firstNotDead; tmpFirstNotDead < size; tmpFirstNotDead++)
        	if(listObstaclesMobiles.get(tmpFirstNotDead).obstacle_proximite_dans_segment(A, B, date))
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
    public boolean isObstacleMobilePresent(Vec2<ReadOnly> position, int distance) 
    {
    //    if(isThereHypotheticalEnemy && isObstaclePresent(position, hypotheticalEnemy.getReadOnly(), distance))
    //    	return true;
        int size = listObstaclesMobiles.size();
        for(int tmpFirstNotDead = firstNotDead; tmpFirstNotDead < size; tmpFirstNotDead++)
        {
        	if(isObstaclePresent(position, listObstaclesMobiles.get(tmpFirstNotDead), distance))
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
    private boolean isObstaclePresent(Vec2<ReadOnly> position, Obstacle o, int distance)
    {
    	return o.isProcheObstacle(position, distance);
    }
    
	
	@Override
	public void updateConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void useConfig(Config config) {
		// TODO Auto-generated method stub
		
	}
	

	public boolean isTraversableCourbe(PathfindingNodes objectifFinal, PathfindingNodes intersection, Vec2<ReadOnly> directionAvant, int tempsDepuisDebutMatch)
	{
		// TODO: calculer la vitesse qui permet exactement de passer?
		Speed vitesse = Speed.BETWEEN_SCRIPTS;
		
		obstacleTrajectoireCourbe = new ObstacleTrajectoireCourbe(objectifFinal, intersection, directionAvant, vitesse);

		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values)
    		if(obstacleTrajectoireCourbe.isColliding(o.getObstacle()))
    			return false;

    	// Collision avec un ennemi hypothétique?
  //      if(isThereHypotheticalEnemy && obstacleTrajectoireCourbe.isColliding(hypotheticalEnemy.getReadOnly()))
  //      	return false;

        // Collision avec un obtacle mobile?
        int size = listObstaclesMobiles.size();
        for(int tmpFirstNotDead = firstNotDead; tmpFirstNotDead < size; tmpFirstNotDead++)
        	if(obstacleTrajectoireCourbe.isColliding(listObstaclesMobiles.get(tmpFirstNotDead)))
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
	public boolean isProcheObstacle(GameElementNames g, Vec2<ReadOnly> position, int rayon_robot_adverse)
	{
		return g.getObstacle().isProcheObstacle(position, rayon_robot_adverse);
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
