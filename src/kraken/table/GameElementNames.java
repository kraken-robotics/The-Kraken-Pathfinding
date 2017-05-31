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

package kraken.table;

import kraken.obstacles.types.ObstacleInterface;
import kraken.obstacles.types.ObstacleMasque;

/**
 * Enumérations contenant tous les éléments de jeux
 * 
 * @author pf
 *
 */

public enum GameElementNames
{
;

	public final ObstacleInterface obstacle; // il se trouve qu'ils sont tous
												// circulaires…
	public final boolean aUnMasque;
	public final boolean cylindre;
	public final double orientationArriveeDStarLite;
	public final Double[] anglesAttaque;

	private GameElementNames(ObstacleInterface obs, double orientationArriveeDStarLite, Double[] anglesAttaque)
	{
		this.anglesAttaque = anglesAttaque;
		cylindre = toString().startsWith("CYLINDRE");
		aUnMasque = obs instanceof ObstacleMasque;
		obstacle = obs;
		this.orientationArriveeDStarLite = orientationArriveeDStarLite;
	}

	private GameElementNames(ObstacleInterface obs)
	{
		anglesAttaque = null;
		cylindre = toString().startsWith("CYLINDRE");
		aUnMasque = obs instanceof ObstacleMasque;
		obstacle = obs;
		orientationArriveeDStarLite = 0;
	}

}
