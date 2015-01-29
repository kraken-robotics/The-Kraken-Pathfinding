package obstacles;

/**
 * Interface pour tous les obstacles qui modélisent le robot et qui
 * servent aux calculs de collisions.
 * @author pf
 *
 */

public interface ObstacleCollision
{
	public boolean isColliding(Obstacle o);
	public boolean isCollidingObstacleFixe();
}
