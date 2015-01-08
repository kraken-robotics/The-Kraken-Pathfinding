package obstacles;
import utils.Config;
import utils.Log;
import utils.Vec2;

/**
 * Superclasse abstraite des obstacles.
 * @author pf, marsu
 *
 */

public abstract class Obstacle
{
	protected Vec2 position;
	protected int distance_dilatation;
	protected Log log;
	protected Config config;
	
	public Obstacle (Log log, Config config, Vec2 position)
	{
		this.log = log;
		this.config = config;
		this.position = position;
	}
	
	public abstract boolean isProcheObstacle(Vec2 point, int distance);
	public abstract boolean isInObstacle(Vec2 point);
	
	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public Vec2 getPosition()
	{
		return this.position;
	}
	
	public String toString()
	{
		return "Obstacle en "+position;
	}
	
}
