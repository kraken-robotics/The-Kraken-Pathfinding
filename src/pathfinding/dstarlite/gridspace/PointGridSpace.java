/*
Copyright (C) 2013-2017 Pierre-François Gimenez

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

package pathfinding.dstarlite.gridspace;

import utils.Vec2RO;

import java.awt.Graphics;

import container.Service;
import graphic.Fenetre;
import graphic.printable.Layer;
import graphic.printable.Printable;
import robot.RobotReal;

/**
 * Un point du gridspace
 * @author pf
 *
 */

public class PointGridSpace implements Service, Printable
{
	private static final long serialVersionUID = -5975399070001506596L;
	public static final int PRECISION = 7;
	public static final int NB_POINTS_POUR_TROIS_METRES = (1 << PRECISION);
	public static final int NB_POINTS_POUR_DEUX_METRES = (int) ((1 << PRECISION)*2./3.)+1;
	public static final double DISTANCE_ENTRE_DEUX_POINTS = 3000./(NB_POINTS_POUR_TROIS_METRES-1);
	public static final int DISTANCE_ENTRE_DEUX_POINTS_1024 = (int)(1024*3000./(NB_POINTS_POUR_TROIS_METRES-1));
	public static final int NB_POINTS = NB_POINTS_POUR_DEUX_METRES * NB_POINTS_POUR_TROIS_METRES;

	/**
	 * Attention ! Le repère de ce x,y est celui pour lequel x et y sont toujours positifs
	 */
	public final int x, y, hashcode;
	
	/**
	 * Construit à partir du hashCode
	 * @param i
	 */
	PointGridSpace(int i)
	{
		y = i >> PRECISION;
		x = i & (NB_POINTS_POUR_TROIS_METRES - 1);
		hashcode = i;
	}

	@Override
	public int hashCode()
	{
		return hashcode;
	}
		
	@Override
	public boolean equals(Object o)
	{
		return o instanceof PointGridSpace && hashcode == o.hashCode();
	}
	
	/**
	 * On utilise la distance octile pour l'heuristique (surtout parce que c'est rapide)
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public final int distanceOctile(PointGridSpace point)
	{
		int dx = Math.abs(x - point.x);
		int dy = Math.abs(y - point.y);
		return 1000 * Math.max(dx, dy) + 414 * Math.min(dx, dy);
	}

	public final Vec2RO computeVec2()
	{
		return new Vec2RO(((x * DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10) - 1500, (y * DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10);
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		Vec2RO p = computeVec2();
		g.fillOval(f.XtoWindow(p.getX())-f.distanceXtoWindow((int) DISTANCE_ENTRE_DEUX_POINTS)/2,
				f.YtoWindow(p.getY())-f.distanceYtoWindow((int) DISTANCE_ENTRE_DEUX_POINTS)/2,
				f.distanceXtoWindow((int) (DISTANCE_ENTRE_DEUX_POINTS*0.7)),
				f.distanceYtoWindow((int) (DISTANCE_ENTRE_DEUX_POINTS*0.7)));
	}

	@Override
	public String toString()
	{
		return computeVec2().toString();
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}
	
}
