/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.obstacles;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import pfg.graphic.Chart;
import pfg.graphic.GraphicPanel;
import pfg.kraken.utils.XY;

/**
 * A compound obstacle
 * 
 * @author pf
 *
 */

public class CompoundObstacle extends Obstacle
{
	// Position est le centre de rotation

	private static final long serialVersionUID = 7643797598957137648L;
	private Obstacle[] obs;

	public CompoundObstacle(Obstacle... obs)
	{
		super(new XY(0,0)); // TODO
		this.obs = obs;
	}


	/**
	 * Calcul s'il y a collision avec un ObstacleRectangularAligned.
	 * Attention! Ne pas utiliser un ObstacleRectangular au lieu de
	 * l'ObstacleRectangularAligned!
	 * Utilise le calcul d'axe de séparation
	 * 
	 * @param r
	 * @return
	 */
	@Override
	public boolean isColliding(RectangularObstacle r)
	{
		for(Obstacle o : obs)
			if(o.isColliding(r))
				return true;
		return false;
	}
	
	@Override
	public String toString()
	{
		return "ObstacleCompound " + obs;
	}

	/**
	 * Fourni la plus petite distance au carré entre le point fourni et
	 * l'obstacle
	 * 
	 * @param in
	 * @return la plus petite distance au carré entre le point fourni et
	 * l'obstacle
	 */
	@Override
	public double squaredDistance(XY v)
	{
		double min = Double.MAX_VALUE;
		for(Obstacle o : obs)
		{
			min = Math.min(min, o.squaredDistance(v));
			if(min == 0)
				break;
		}
		return min;
	}

	@Override
	public void print(Graphics g, GraphicPanel f, Chart a)
	{
		for(Obstacle o : obs)
			o.print(g, f, a);
	}
	
	@Override
	public XY[] getExpandedConvexHull(double expansion, double longestAllowedLength)
	{
		List<XY[]> points = new ArrayList<XY[]>();
		int nbPoints = 0;
		for(Obstacle o : obs)
		{
			XY[] tmp = o.getExpandedConvexHull(expansion, longestAllowedLength);
			points.add(tmp);
			nbPoints += tmp.length;
		}
		XY[] out = new XY[nbPoints];
		
		int i = 0;
		for(XY[] array : points)
			for(int j = 0; j < array.length; j++)
				out[i++] = array[j];
		assert i == nbPoints;
		return out;
	}


	@Override
	public boolean isInObstacle(XY pos)
	{
		for(Obstacle o : obs)
			if(o.isInObstacle(pos))
				return true;
		return false;
	}
	
	@Override
	public boolean isColliding(XY pointA, XY pointB)
	{
		for(Obstacle o : obs)
			if(o.isColliding(pointA, pointB))
				return true;
		return false;
	}

}
