package obstacles.types;

import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Superclasse abstraite des obstacles.
 * @author pf
 *
 */

public abstract class Obstacle
{
	protected final Vec2RW position;
	protected int distance_dilatation;
	protected static Log log;
	
    protected static int rayonRobot;
    protected static int marge;
    protected static int distanceApprox;
	
	public static void setLog(Log log)
	{
		Obstacle.log = log;
	}
	
	public static void useConfigStatic(Config config)
	{
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
		distanceApprox = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);
		marge = config.getInt(ConfigInfo.MARGE);
	}
	
	/**
	 * Constructeur. La position est celle du centre de rotation de l'obstacle
	 * @param position
	 */
	public Obstacle(Vec2RO position)
	{
		this.position = position.clone();
	}
	
	@Override
	public String toString()
	{
		return "Obstacle en "+position;
	}

	/**
	 * Renvoie la distance au carré de l'obstacle avec cette position
	 * @param position
	 * @return
	 */
	public abstract double squaredDistance(Vec2RO position);
	
	/**
	 * Renvoi "vrai" si position est à moins de distance du centre de l'obstacle
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheCentre(Vec2RO position, int distance)
	{
		return this.position.squaredDistance(position) < distance * distance;
	}

	/**
	 * Renvoi "vrai" si position est à moins de distance d'un bord de l'obstacle ou à l'intérieur
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheObstacle(Vec2RO position, int distance)
	{
		return squaredDistance(position) < distance * distance;
	}

	/**
	 * Renvoi "vrai" si le centre de obs est à moins de distance d'un bord de l'obstacle ou à l'intérieur
	 * Ce n'est pas pareil que vérifier une collision !
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheObstacle(Obstacle obs, int distance)
	{
		return squaredDistance(obs.position.getReadOnly()) < distance * distance;
	}
	
	/**
	 * Renvoie vrai s'il y a collision avec obs
	 * @param obs
	 * @return
	 */
	public abstract boolean isColliding(ObstacleRectangular obs);

	/**
	 * Revoie vrai s'il y a une collision avec obs
	 * @param obs
	 * @return
	 */
	public boolean isColliding(ObstacleArcCourbe obs)
	{
		for(ObstacleRectangular o : obs.ombresRobot)
		{
			if(isColliding(o))
				return true;
		}
		return false;
	}
	
}
