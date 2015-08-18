package obstacles;
import permissions.ReadOnly;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;

/**
 * Superclasse abstraite des obstacles.
 * @author pf
 *
 */

public abstract class Obstacle
{
	public final Vec2<ReadOnly> position;
	protected int distance_dilatation;
	protected static Log log;
	
    protected static int largeurRobot; // le sens gauche-droite du robot
    protected static int longueurRobot; // le sens avant-arrière du robot
    protected static int rayonRobot;
    protected static int marge;
	protected static double anglePas; // utilisé pour les calculs de collision pendant les rotations
	
	public static void setLog(Log log)
	{
		Obstacle.log = log;
	}
	
	public static void useConfig(Config config)
	{
		largeurRobot = config.getInt(ConfigInfo.LARGEUR_ROBOT_AXE_GAUCHE_DROITE);
		longueurRobot = config.getInt(ConfigInfo.LONGUEUR_ROBOT_AXE_AVANT_ARRIERE);
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
		marge = config.getInt(ConfigInfo.MARGE);
		anglePas = Math.PI-2*Math.atan2(largeurRobot, longueurRobot);
	}
	
	public Obstacle (Vec2<ReadOnly> position)
	{
		this.position = position;
	}
	
	public abstract boolean isProcheObstacle(Vec2<ReadOnly> point, int distance);
	public abstract boolean isInObstacle(Vec2<ReadOnly> point);
	
/*
	public static final void setPosition(Obstacle o, Vec2<ReadOnly> v)
	{
		Vec2.copy(v, o.position);
	}
*/
	public String toString()
	{
		return "Obstacle en "+position;
	}
	
}
