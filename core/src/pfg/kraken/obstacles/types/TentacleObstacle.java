/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.obstacles.types;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import graphic.Fenetre;
import pfg.kraken.utils.XY;

/**
 * Obstacle d'un arc de trajectoire courbe
 * Construit à partir de plein d'obstacles rectangulaires
 * 
 * @author pf
 *
 */

public class TentacleObstacle extends Obstacle
{
	private static final long serialVersionUID = -2425339148551754268L;

	public TentacleObstacle()
	{
		super(null);
	}

	public List<RectangularObstacle> ombresRobot = new ArrayList<RectangularObstacle>();

	@Override
	public double squaredDistance(XY position)
	{
		double min = Double.MAX_VALUE;
		for(RectangularObstacle o : ombresRobot)
		{
			min = Math.min(min, o.squaredDistance(position));
			if(min == 0)
				return 0;
		}
		return min;
	}

	@Override
	public boolean isColliding(RectangularObstacle obs)
	{
		for(RectangularObstacle o : ombresRobot)
			if(obs.isColliding(o))
				return true;
		return false;
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		for(RectangularObstacle o : ombresRobot)
			o.print(g, f);
	}

	@Override
	public double getTopY()
	{
		double out = ombresRobot.get(0).getTopY();
		for(RectangularObstacle o : ombresRobot)
			out = Math.max(out, o.getTopY());
		return out;
	}

	@Override
	public double getBottomY()
	{
		double out = ombresRobot.get(0).getBottomY();
		for(RectangularObstacle o : ombresRobot)
			out = Math.min(out, o.getBottomY());
		return out;
	}

	@Override
	public double getLeftmostX()
	{
		double out = ombresRobot.get(0).getLeftmostX();
		for(RectangularObstacle o : ombresRobot)
			out = Math.min(out, o.getLeftmostX());
		return out;
	}

	@Override
	public double getRightmostX()
	{
		double out = ombresRobot.get(0).getRightmostX();
		for(RectangularObstacle o : ombresRobot)
			out = Math.max(out, o.getRightmostX());
		return out;
	}

}
