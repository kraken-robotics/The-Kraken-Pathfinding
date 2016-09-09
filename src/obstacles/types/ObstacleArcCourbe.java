/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package obstacles.types;

import java.awt.Graphics;
import java.util.ArrayList;

import debug.Fenetre;
import utils.Vec2RO;

/**
 * Obstacle d'un arc de trajectoire courbe
 * Construit à partir de plein d'obstacles rectangulaires
 * @author pf
 *
 */

public class ObstacleArcCourbe extends Obstacle
{
	public ObstacleArcCourbe()
	{
		super(new Vec2RO());
	}
	
	public ArrayList<ObstacleRectangular> ombresRobot = new ArrayList<ObstacleRectangular>();

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
	public boolean isColliding(ObstacleRectangular obs) {
		for(ObstacleRectangular o : ombresRobot)
			if(obs.isColliding(o))
				return true;
		return false;
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		for(ObstacleRectangular o : ombresRobot)
			o.print(g,f);
	}

}
