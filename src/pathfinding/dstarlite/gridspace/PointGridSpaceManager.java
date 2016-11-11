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

package pathfinding.dstarlite.gridspace;

import container.Service;
import utils.Log;
import utils.Vec2RO;

/**
 * S'occupe de gérer les PointGridSpace
 * @author pf
 *
 */

public class PointGridSpaceManager implements Service
{
	private static final int X_MAX = PointGridSpace.NB_POINTS_POUR_TROIS_METRES - 1;
	private static final int Y_MAX = PointGridSpace.NB_POINTS_POUR_DEUX_METRES - 1;
	
	protected Log log;
	private PointGridSpace[] allPoints = new PointGridSpace[PointGridSpace.NB_POINTS];
	
	public PointGridSpaceManager(Log log)
	{
		this.log = log;
		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			allPoints[i] = new PointGridSpace(i);			
	}
	
	/**
	 * Récupère un PointGridSpace à partir de ses coordonnées
	 * @param x
	 * @param y
	 * @return
	 */
	public PointGridSpace get(int x, int y)
	{
		if(x < 0 || x > X_MAX || y < 0 || y > Y_MAX)
			return null;

		return get(index(x,y));
	}
	
	/**
	 * Renvoie l'indice du gridpoint le plus proche de cette position
	 * @param p
	 * @return
	 */
	public PointGridSpace get(Vec2RO p)
	{
		int y = (int) Math.round(p.getY() / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		int x = (int) Math.round((p.getX() + 1500) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		
		if(x < 0 || x > X_MAX || y < 0 || y > Y_MAX)
			return null;

		return allPoints[index(x,y)];
	}
	
	private int index(int x, int y)
	{
		return (y << PointGridSpace.PRECISION) + x;
	}

	/**
	 * Récupère un gridpointspace à partir de son hashcode
	 * @param i
	 * @return
	 */
	public PointGridSpace get(int i)
	{
		return allPoints[i];
	}
	
	/**
	 * Récupère le voisin de "point" dans la direction indiquée.
	 * Renvoie null si un tel voisin est hors table
	 * @param point
	 * @param direction
	 * @return
	 */
	public PointGridSpace getGridPointVoisin(PointGridSpace point, Direction direction)
	{
		int x = point.x + direction.deltaX;
		int y = point.y + direction.deltaY;

		if(x < 0 || x > X_MAX || y < 0 || y > Y_MAX)
			return null;
		return get(x,y);
	}

	/**
	 * Donne le point voisin au point dirigé
	 * @param point
	 * @return
	 */
	public PointGridSpace getGridPointVoisin(PointDirige point)
	{
		return getGridPointVoisin(point.point, point.dir);
	}
}
