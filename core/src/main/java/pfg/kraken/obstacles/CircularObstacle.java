/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.obstacles;

import java.awt.Color;
import java.awt.Graphics;

import pfg.graphic.GraphicPanel;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XY_RW;

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
		assert rad > 0;
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
		return o.squaredDistance(position) < squared_radius;
	}

	@Override
	public void print(Graphics g, GraphicPanel f)
	{
//		if(radius <= 0)
//			g.fillOval(f.XtoWindow(position.getX()) - 5, f.YtoWindow(position.getY()) - 5, 10, 10);
//		else
		g.drawOval(f.XtoWindow(position.getX() - radius), f.YtoWindow(position.getY() + radius), f.distanceXtoWindow((radius) * 2), f.distanceYtoWindow((radius) * 2));
		
		Color c = g.getColor();
		Color cTransparent = new Color(c.getRed(), c.getGreen(), c.getBlue(), 30);
		g.setColor(cTransparent);
		
		g.fillOval(f.XtoWindow(position.getX() - radius), f.YtoWindow(position.getY() + radius), f.distanceXtoWindow((radius) * 2), f.distanceYtoWindow((radius) * 2));		
	}
	
	@Override
	public XY[] getExpandedConvexHull(double expansion, double longestAllowedLength)
	{
		int nbPoints = (int) Math.ceil(2 * Math.PI * (radius + expansion) / longestAllowedLength);
		if(nbPoints < 3)
			nbPoints = 3;

		XY[] out = new XY[nbPoints];
		
		for(int i = 0; i < nbPoints; i++)
			out[i] = new XY_RW(expansion + radius, i * 2 * Math.PI / nbPoints, true).plus(position);
		return out;
	}

	@Override
	public boolean isInObstacle(XY pos)
	{
		return pos.squaredDistance(position) <= squared_radius;
	}

	@Override
	public boolean isColliding(XY pointA, XY pointB)
	{
    	/**
    	 * Ce code a été honteusement pompé sur http://openclassrooms.com/courses/theorie-des-collisions/formes-plus-complexes
    	 */
    	
    	if (!isCollidingLine(pointA, pointB))
	        return false;  // si on ne touche pas la droite, on ne touchera jamais le segment
    	
    	double pscal1 = pointB.minusNewVector(pointA).dot(position.minusNewVector(pointA));
    	double pscal2 = pointA.minusNewVector(pointB).dot(position.minusNewVector(pointB));
	    if (pscal1>=0 && pscal2>=0)
	       return true;
	    
	    // dernière possibilité, A ou B dans le cercle
	    return isInObstacle(pointA) || isInObstacle(pointB);
	}
	
	private boolean isCollidingLine(XY pointA, XY pointB)
	{
		XY C = position;
		XY_RW AB = pointB.minusNewVector(pointA);
		XY_RW AC = C.minusNewVector(pointA);
	    double numerateur = Math.abs(AB.getX()*AC.getY() - AB.getY()*AC.getX());
	    double denominateur = AB.squaredNorm();
	    double CI = numerateur*numerateur / denominateur;
	    return CI < squared_radius;
	}

}
