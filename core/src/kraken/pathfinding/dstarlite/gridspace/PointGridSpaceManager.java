/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.dstarlite.gridspace;

import kraken.utils.Log;
import kraken.utils.XY;

/**
 * S'occupe de gérer les PointGridSpace
 * 
 * @author pf
 *
 */

public class PointGridSpaceManager
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
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public PointGridSpace get(int x, int y)
	{
		if(x < 0 || x > X_MAX || y < 0 || y > Y_MAX)
			return null;

		return get(index(x, y));
	}

	public boolean isValid(int x, int y)
	{
		return !(x < 0 || x > X_MAX || y < 0 || y > Y_MAX);
	}

	/**
	 * Renvoie l'indice du gridpoint le plus proche de cette position
	 * 
	 * @param p
	 * @return
	 */
	public PointGridSpace get(XY p)
	{
		int y = (int) Math.round(p.getY() / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		int x = (int) Math.round((p.getX() + 1500) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);

		if(x < 0 || x > X_MAX || y < 0 || y > Y_MAX)
			return null;

		return allPoints[index(x, y)];
	}

	private int index(int x, int y)
	{
		return (y << PointGridSpace.PRECISION) + x;
	}

	/**
	 * Récupère un gridpointspace à partir de son hashcode
	 * 
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
	 * 
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
		return get(x, y);
	}

	/**
	 * Donne le point voisin au point dirigé
	 * 
	 * @param point
	 * @return
	 */
	public PointGridSpace getGridPointVoisin(PointDirige point)
	{
		return getGridPointVoisin(point.point, point.dir);
	}
}
