package obstacles;
import utils.Config;
import utils.ConfigInfo;
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
	protected static Log log;
	protected static Config config;
	
    protected static int largeur_robot;
    protected static int longueur_robot;
    protected static int marge;
	
	public static void setLogConfig(Log log, Config config)
	{
		Obstacle.log = log;
		Obstacle.config = config;
		largeur_robot = config.getInt(ConfigInfo.LARGEUR_ROBOT);
		longueur_robot = config.getInt(ConfigInfo.LONGUEUR_ROBOT);
		marge = config.getInt(ConfigInfo.MARGE);
	}
	
	public Obstacle (Vec2 position)
	{
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
