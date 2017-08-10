/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.obstacles;

import java.awt.Color;
import java.awt.Graphics;
import pfg.graphic.Fenetre;
import pfg.graphic.printable.Layer;
import pfg.kraken.utils.XY;

/**
 * Obstacle circulaire
 * 
 * @author pf
 *
 */
public class CircularObstacle extends Obstacle
{
	// le Vec2 "position" indique le centre de l'obstacle
	private static final long serialVersionUID = 5090691605874028970L;

	// rayon de cet obstacle
	public final int radius;
	public final int squared_radius;

	public CircularObstacle(XY position, int rad)
	{
		super(position);
		this.radius = rad;
		squared_radius = rad * rad;
	}

	public CircularObstacle(XY position, int rad, Color c, Layer l)
	{
		super(position, c, l);
		this.radius = rad;
		squared_radius = rad * rad;
	}

	@Override
	public String toString()
	{
		return super.toString() + ", rayon: " + radius;
	}

	@Override
	public double squaredDistance(XY position)
	{
		double out = Math.max(0, position.distance(this.position) - radius);
		return out * out;
	}

	@Override
	public boolean isColliding(RectangularObstacle o)
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
	public XY[] getExpandedConvexHull(double expansion, double longestAllowedLength)
	{
		return null; // TODO
	}

}
