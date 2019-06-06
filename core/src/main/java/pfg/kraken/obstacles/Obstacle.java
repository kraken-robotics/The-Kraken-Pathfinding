/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.obstacles;

import java.io.Serializable;
import pfg.kraken.display.Printable;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XY_RW;

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
	
	/**
	 * Constructeur. La position est celle du centre de rotation de l'obstacle
	 * 
	 * @param position
	 */
	public Obstacle(XY position)
	{
		this.position = position.clone();
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
/*	public boolean isProcheObstacle(Obstacle obs, int distance)
	{
		return squaredDistance(obs.position) < distance * distance;
	}*/
	
	public abstract boolean isInObstacle(XY pos);

	public int hashCode()
	{
		return position.hashCode();
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
	 * Renvoie
	 * @param obs
	 * @return
	 */
	public abstract double squaredDistanceTo(RectangularObstacle obs);
	
	/**
	 * Is there a collision between this obstacle and this line ?
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public abstract boolean isColliding(XY pointA, XY pointB);
	
}
