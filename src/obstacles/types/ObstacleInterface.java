/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package obstacles.types;

import java.io.Serializable;
import graphic.printable.Printable;
import utils.Vec2RO;

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
	public double squaredDistance(Vec2RO position);

	/**
	 * Renvoi "vrai" si position est à moins de distance d'un bord de l'obstacle
	 * ou à l'intérieur
	 * 
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheObstacle(Vec2RO position, int distance);

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

	public Vec2RO getPosition();

	public double getTopY();

	public double getBottomY();

	public double getLeftmostX();

	public double getRightmostX();

}
