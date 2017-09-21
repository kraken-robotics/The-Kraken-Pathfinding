/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.obstacles;

import java.io.Serializable;
import pfg.graphic.PrintBuffer;
import pfg.graphic.printable.Printable;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XY_RW;
import pfg.log.Log;

/**
 * Superclasse abstraite des obstacles.
 * 
 * @author pf
 *
 */

public abstract class Obstacle implements Printable, Serializable
{
	private static final long serialVersionUID = -2508727703931042322L;
	protected XY_RW position;
	protected transient int distance_dilatation;
	protected static Log log;
	protected static PrintBuffer buffer;

	// Pour l'affichage du robot
	protected static boolean printAllObstacles = false;

	public static void set(Log log, PrintBuffer buffer)
	{
		Obstacle.log = log;
		Obstacle.buffer = buffer;
	}

	
	/**
	 * Constructeur. La position est celle du centre de rotation de l'obstacle
	 * 
	 * @param position
	 */
	public Obstacle(XY position)
	{
		if(position != null)
			this.position = position.clone();
		else
			this.position = null;
	}

	@Override
	public String toString()
	{
		return "Obstacle en " + position;
	}

	/**
	 * Renvoi "vrai" si position est à moins de distance d'un bord de l'obstacle
	 * ou à l'intérieur
	 * 
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheObstacle(XY position, int distance)
	{
		return squaredDistance(position) < distance * distance;
	}

	/**
	 * Renvoi "vrai" si le centre de obs est à moins de distance d'un bord de
	 * l'obstacle ou à l'intérieur
	 * Ce n'est pas pareil que vérifier une collision !
	 * 
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheObstacle(Obstacle obs, int distance)
	{
		return squaredDistance(obs.position) < distance * distance;
	}

	/**
	 * Revoie vrai s'il y a une collision avec obs
	 * 
	 * @param obs
	 * @return
	 */
	public boolean isColliding(TentacleObstacle obs)
	{
		for(RectangularObstacle o : obs.ombresRobot)
		{
			if(isColliding(o))
				return true;
		}
		return false;
	}
	
	public abstract boolean isInObstacle(XY pos);

	public XY getPosition()
	{
		return position;
	}

	public abstract double squaredDistance(XY position);
	
	/**
	 * The expanded convex hull of the obstacle.
	 * @param expansion
	 * @param longestAllowedLength
	 * @return
	 */
	public abstract XY[] getExpandedConvexHull(double expansion, double longestAllowedLength);
	
	/**
	 * Renvoie vrai s'il y a collision avec obs
	 * 
	 * @param obs
	 * @return
	 */
	public abstract boolean isColliding(RectangularObstacle obs);

	/**
	 * Is there a collision between this obstacle and this line ?
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public abstract boolean isColliding(XY pointA, XY pointB);

}
