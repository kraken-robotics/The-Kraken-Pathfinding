/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package kraken.obstacles.types;

import java.io.Serializable;
import graphic.printable.Printable;
import kraken.utils.XY;

/**
 * Superclasse abstraite des obstacles.
 * 
 * @author pf
 *
 */

public abstract interface ObstacleInterface extends Printable, Serializable
{
	/**
	 * Renvoie la distance au carré de l'obstacle avec cette position
	 * 
	 * @param position
	 * @return
	 */
	public double squaredDistance(XY position);

	/**
	 * Renvoi "vrai" si position est à moins de distance d'un bord de l'obstacle
	 * ou à l'intérieur
	 * 
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheObstacle(XY position, int distance);

	/**
	 * Renvoi "vrai" si le centre de obs est à moins de distance d'un bord de
	 * l'obstacle ou à l'intérieur
	 * Ce n'est pas pareil que vérifier une collision !
	 * 
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheObstacle(Obstacle obs, int distance);

	/**
	 * Renvoie vrai s'il y a collision avec obs
	 * 
	 * @param obs
	 * @return
	 */
	public abstract boolean isColliding(ObstacleRectangular obs);

	/**
	 * Revoie vrai s'il y a une collision avec obs
	 * 
	 * @param obs
	 * @return
	 */
	public boolean isColliding(ObstacleArcCourbe obs);

	public XY getPosition();

	public double getTopY();

	public double getBottomY();

	public double getLeftmostX();

	public double getRightmostX();

}
