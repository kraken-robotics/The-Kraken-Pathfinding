/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.obstacles.types;

import java.awt.Graphics;
import graphic.Fenetre;
import graphic.printable.Layer;
import kraken.pathfinding.dstarlite.gridspace.Masque;
import kraken.pathfinding.dstarlite.gridspace.MasqueManager;
import kraken.utils.Vec2RO;

/**
 * Obstacle avec un masque (c'est-à-dire utilisable par le D* Lite)
 * 
 * @author pf
 */
public class ObstacleMasque implements ObstacleInterface
{
	private static final long serialVersionUID = -7303433427716127840L;
	private static MasqueManager mm;
	protected Obstacle o;
	private Masque masque;
	
	public ObstacleMasque(Obstacle o)
	{
		this.o = o;
		masque = mm.getMasque(o);
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
	public void print(Graphics g, Fenetre f)
	{
		o.print(g, f);
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
