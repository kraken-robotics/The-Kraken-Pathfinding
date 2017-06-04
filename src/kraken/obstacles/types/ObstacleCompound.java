/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package kraken.obstacles.types;

import java.awt.Graphics;

import kraken.graphic.Fenetre;
import kraken.utils.Vec2RO;

/**
 * A compound obstacle
 * 
 * @author pf
 *
 */

public class ObstacleCompound extends Obstacle
{
	// Position est le centre de rotation

	private static final long serialVersionUID = 7643797598957137648L;
	private Obstacle[] obs;

	public ObstacleCompound(Obstacle... obs)
	{
		super(new Vec2RO(0,0)); // TODO
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
	public boolean isColliding(ObstacleRectangular r)
	{
		for(Obstacle o : obs)
			if(o.isColliding(r))
				return true;
		return false;
	}

	/**
	 * Teste la séparation à partir des projections.
	 * Vérifie simplement si a et b sont bien séparés de a2, b2, c2 et d2,
	 * c'est-à-dire s'il existe x tel que a < x, b < x et
	 * a2 > x, b2 > x, c2 > x, d2 > x
	 * 
	 * @param a
	 * @param b
	 * @param a2
	 * @param b2
	 * @param c2
	 * @param d2
	 * @return
	 */
	protected final boolean testeSeparation(double a, double b, double a2, double b2, double c2, double d2)
	{
		double min1 = Math.min(a, b);
		double max1 = Math.max(a, b);

		double min2 = Math.min(Math.min(a2, b2), Math.min(c2, d2));
		double max2 = Math.max(Math.max(a2, b2), Math.max(c2, d2));

		return min1 > max2 || min2 > max1; // vrai s'il y a une séparation
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
	public double squaredDistance(Vec2RO v)
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
	public void print(Graphics g, Fenetre f)
	{
		// TODO
	}

	// TODO
	
	@Override
	public double getTopY()
	{
		double max = Double.MIN_VALUE;
		for(Obstacle o : obs)
			max = Math.max(max, o.getTopY());
		return max;
	}

	@Override
	public double getBottomY()
	{
		double min = Double.MAX_VALUE;
		for(Obstacle o : obs)
			min = Math.min(min, o.getBottomY());
		return min;
	}

	@Override
	public double getLeftmostX()
	{
		double min = Double.MAX_VALUE;
		for(Obstacle o : obs)
			min = Math.min(min, o.getLeftmostX());
		return min;
	}

	@Override
	public double getRightmostX()
	{
		double max = Double.MIN_VALUE;
		for(Obstacle o : obs)
			max = Math.max(max, o.getRightmostX());
		return max;
	}
}
