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

import java.awt.Graphics;
import graphic.Fenetre;
import graphic.printable.Layer;
import pathfinding.dstarlite.gridspace.Masque;
import robot.RobotReal;
import utils.Vec2RO;

/**
 * Obstacle avec un masque (c'est-à-dire utilisable par le D* Lite)
 * 
 * @author pf
 */
public class ObstacleMasque implements ObstacleInterface
{
	private static final long serialVersionUID = -7303433427716127840L;
	protected Obstacle o;
	private Masque masque;

	public ObstacleMasque(Obstacle o, Masque masque)
	{
		this.o = o;
		this.masque = masque;
	}

	public Masque getMasque()
	{
		return masque;
	}

	@Override
	public int hashCode()
	{
		return masque.hashCode();
	}

	/**
	 * Utilisé pour les cylindres pour qui on n'a pas le masque à la
	 * construction
	 * 
	 * @param masque
	 */
	public void setMasque(Masque masque)
	{
		this.masque = masque;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		o.print(g, f, robot);
	}

	@Override
	public double squaredDistance(Vec2RO position)
	{
		return o.squaredDistance(position);
	}

	@Override
	public boolean isColliding(ObstacleRectangular obs)
	{
		return o.isColliding(obs);
	}

	@Override
	public Layer getLayer()
	{
		return o.getLayer();
	}

	@Override
	public boolean isProcheObstacle(Vec2RO position, int distance)
	{
		return o.isProcheObstacle(position, distance);
	}

	@Override
	public boolean isProcheObstacle(Obstacle obs, int distance)
	{
		return o.isProcheObstacle(obs, distance);
	}

	@Override
	public boolean isColliding(ObstacleArcCourbe obs)
	{
		return o.isColliding(obs);
	}

	@Override
	public Vec2RO getPosition()
	{
		return o.getPosition();
	}

	@Override
	public double getTopY()
	{
		return o.getTopY();
	}

	@Override
	public double getBottomY()
	{
		return o.getBottomY();
	}

	@Override
	public double getLeftmostX()
	{
		return o.getLeftmostX();
	}

	@Override
	public double getRightmostX()
	{
		return o.getRightmostX();
	}
}
