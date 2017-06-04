/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package kraken.obstacles.types;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import graphic.Fenetre;
import kraken.utils.Vec2RO;

/**
 * Obstacle d'un arc de trajectoire courbe
 * Construit à partir de plein d'obstacles rectangulaires
 * 
 * @author pf
 *
 */

public class ObstacleArcCourbe extends Obstacle
{
	private static final long serialVersionUID = -2425339148551754268L;

	public ObstacleArcCourbe()
	{
		super(null);
	}

	public List<ObstacleRectangular> ombresRobot = new ArrayList<ObstacleRectangular>();

	@Override
	public double squaredDistance(Vec2RO position)
	{
		double min = Double.MAX_VALUE;
		for(ObstacleRectangular o : ombresRobot)
		{
			min = Math.min(min, o.squaredDistance(position));
			if(min == 0)
				return 0;
		}
		return min;
	}

	@Override
	public boolean isColliding(ObstacleRectangular obs)
	{
		for(ObstacleRectangular o : ombresRobot)
			if(obs.isColliding(o))
				return true;
		return false;
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		for(ObstacleRectangular o : ombresRobot)
			o.print(g, f);
	}

	@Override
	public double getTopY()
	{
		double out = ombresRobot.get(0).getTopY();
		for(ObstacleRectangular o : ombresRobot)
			out = Math.max(out, o.getTopY());
		return out;
	}

	@Override
	public double getBottomY()
	{
		double out = ombresRobot.get(0).getBottomY();
		for(ObstacleRectangular o : ombresRobot)
			out = Math.min(out, o.getBottomY());
		return out;
	}

	@Override
	public double getLeftmostX()
	{
		double out = ombresRobot.get(0).getLeftmostX();
		for(ObstacleRectangular o : ombresRobot)
			out = Math.min(out, o.getLeftmostX());
		return out;
	}

	@Override
	public double getRightmostX()
	{
		double out = ombresRobot.get(0).getRightmostX();
		for(ObstacleRectangular o : ombresRobot)
			out = Math.max(out, o.getRightmostX());
		return out;
	}

}
