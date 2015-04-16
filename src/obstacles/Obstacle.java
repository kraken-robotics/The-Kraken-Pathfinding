package obstacles;
import permissions.ReadOnly;
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
	protected final Vec2<ReadOnly> position;
	protected int distance_dilatation;
	protected static Log log;
	protected static Config config;
	
    protected static int largeurRobot; // le sens gauche-droite du robot
    protected static int longueurRobot; // le sens avant-arrière du robot
    protected static int marge;
	protected static double anglePas; // utilisé pour les calculs de collision pendant les rotations
	
	public static void setLogConfig(Log log, Config config)
	{
		Obstacle.log = log;
		Obstacle.config = config;
		largeurRobot = config.getInt(ConfigInfo.LARGEUR_ROBOT);
		longueurRobot = config.getInt(ConfigInfo.LONGUEUR_ROBOT);
		marge = config.getInt(ConfigInfo.MARGE);
		anglePas = Math.PI-2*Math.atan2(largeurRobot, longueurRobot);
	}
	
	public Obstacle (Vec2<ReadOnly> position)
	{
		this.position = position;
	}
	
	public abstract boolean isProcheObstacle(Vec2<ReadOnly> point, int distance);
	public abstract boolean isInObstacle(Vec2<ReadOnly> point);
	
	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public Vec2<ReadOnly> getPosition()
	{
		return position.getReadOnly();
	}
	
	public String toString()
	{
		return "Obstacle en "+position;
	}
	
}
