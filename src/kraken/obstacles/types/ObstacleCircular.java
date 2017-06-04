/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package kraken.obstacles.types;

import java.awt.Graphics;
import graphic.Fenetre;
import kraken.graphic.Couleur;
import kraken.utils.Vec2RO;

/**
 * Obstacle circulaire
 * 
 * @author pf
 *
 */
public class ObstacleCircular extends Obstacle
{
	// le Vec2 "position" indique le centre de l'obstacle
	private static final long serialVersionUID = 5090691605874028970L;

	// rayon de cet obstacle
	public final int radius;
	public final int squared_radius;

	public ObstacleCircular(Vec2RO position, int rad)
	{
		super(position);
		this.radius = rad;
		squared_radius = rad * rad;
	}

	public ObstacleCircular(Vec2RO position, int rad, Couleur c)
	{
		super(position, c);
		this.radius = rad;
		squared_radius = rad * rad;
	}

	@Override
	public String toString()
	{
		return super.toString() + ", rayon: " + radius;
	}

	@Override
	public double squaredDistance(Vec2RO position)
	{
		double out = Math.max(0, position.distance(this.position) - radius);
		return out * out;
	}

	@Override
	public boolean isColliding(ObstacleRectangular o)
	{
		// Calcul simple permettant de vérifier les cas absurdes où les
		// obstacles sont loin l'un de l'autre
		if(position.squaredDistance(o.centreGeometrique) >= (radius + o.getDemieDiagonale()) * (radius + o.getDemieDiagonale()))
			return false;
		return o.squaredDistance(position) < radius * radius;
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		if(c != null)
			g.setColor(c);
		if(radius <= 0)
			g.fillOval(f.XtoWindow(position.getX()) - 5, f.YtoWindow(position.getY()) - 5, 10, 10);
		else
			g.fillOval(f.XtoWindow(position.getX() - radius), f.YtoWindow(position.getY() + radius), f.distanceXtoWindow((radius) * 2), f.distanceYtoWindow((radius) * 2));
	}

	@Override
	public double getTopY()
	{
		return position.getY() + radius;
	}

	@Override
	public double getBottomY()
	{
		return position.getY() - radius;
	}

	@Override
	public double getLeftmostX()
	{
		return position.getX() - radius;
	}

	@Override
	public double getRightmostX()
	{
		return position.getX() + radius;
	}

}
