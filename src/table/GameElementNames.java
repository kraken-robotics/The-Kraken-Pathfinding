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

package table;

import obstacles.types.Obstacle;
import obstacles.types.ObstacleCircular;
import utils.Vec2RO;

/**
 * Enumérations contenant tous les éléments de jeux
 * @author pf
 *
 */

// DEPENDS_ON_RULES

public enum GameElementNames {
	TRUC(new ObstacleCircular(new Vec2RO(1410, 150), -1)),
	MACHIN(new ObstacleCircular(new Vec2RO(1410, 150), -1));

	public final Obstacle obstacle;

	private GameElementNames(Obstacle obs)
	{
		obstacle = obs;
	}
	
}
