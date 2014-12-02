package obstacles;
import smartMath.Vec2;

/**
 * Superclasse abstraite des obstacles.
 * @author pf, marsu
 *
 */
public abstract class Obstacle
{
	protected Vec2 position;
	
	public Obstacle (Vec2 position)
	{
		this.position = position;
	}
	
	public abstract Obstacle clone();

	public abstract boolean isProcheObstacle(Vec2 point, int distance);
	
	public Vec2 getPosition()
	{
		return this.position;
	}
	
	public boolean isDestructionNecessary(long date)
	{
		return false;
	}
	
	public String toString()
	{
		return "Obstacle en "+position;
	}
	
}
