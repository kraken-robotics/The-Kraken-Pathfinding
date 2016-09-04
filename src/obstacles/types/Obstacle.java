package obstacles.types;

import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

/**
 * Superclasse abstraite des obstacles.
 * @author pf
 *
 */

public abstract class Obstacle
{
	protected final Vec2<ReadWrite> position;
	protected int distance_dilatation;
	protected static Log log;
	
    protected static int rayonRobot;
    protected static int marge;
    protected static int distanceApprox;
	
	public static void setLog(Log log)
	{
		Obstacle.log = log;
	}
	
	public static void useConfig(Config config)
	{
/*		largeurRobot = config.getInt(ConfigInfo.LARGEUR_ROBOT_AXE_GAUCHE_DROITE);
		longueurRobot = config.getInt(ConfigInfo.LONGUEUR_ROBOT_AXE_AVANT_ARRIERE);
		largeurRobotDeploye = config.getInt(ConfigInfo.LARGEUR_ROBOT_AXE_GAUCHE_DROITE_DEPLOYE);
		longueurRobotDeploye = config.getInt(ConfigInfo.LONGUEUR_ROBOT_AXE_AVANT_ARRIERE_DEPLOYE);*/
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
		distanceApprox = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);
		marge = config.getInt(ConfigInfo.MARGE);
	}
	
	public Obstacle (Vec2<ReadOnly> position)
	{
		this.position = position.clone();
	}
	
	@Override
	public String toString()
	{
		return "Obstacle en "+position;
	}

	public abstract double squaredDistance(Vec2<ReadOnly> position);
	
	public boolean isProcheCentre(Vec2<ReadOnly> position, int distance)
	{
		return this.position.squaredDistance(position) < distance * distance;
	}

	public boolean isProcheObstacle(Vec2<ReadOnly> position, int distance)
	{
		return squaredDistance(position) < distance * distance;
	}

	public abstract boolean isColliding(ObstacleRectangular obs);
	
	public boolean isColliding(ObstacleArcCourbe obs)
	{
		for(ObstacleRectangular o : obs.ombresRobot)
		{
			if(isColliding(o))
				return true;
		}
		return false;
	}
	
	public Vec2<ReadOnly> getPosition()
	{
		return position.getReadOnly();
	}
}
