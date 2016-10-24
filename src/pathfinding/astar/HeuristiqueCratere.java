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

package pathfinding.astar;

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import obstacles.types.ObstacleCircular;
import robot.CinematiqueObs;
import table.GameElementNames;
import utils.Vec2RW;

/**
 * Heuristique pour l'arrivée sur un cratère
 * @author pf
 *
 */

public class HeuristiqueCratere implements Service, Configurable
{
	private ObstacleCircular cratere;
	private int distance;
	private Vec2RW nearestPos = new Vec2RW();

	public HeuristiqueCratere(GameElementNames cratere)
	{
		this.cratere = (ObstacleCircular) cratere.obstacle;
	}
	
	/**
	 * Heuristique de la cinématique donnée
	 * @param c
	 * @return
	 */
	public double heuristicCost(CinematiqueObs c)
	{
		cratere.getNearestPosition(c.getPosition(), nearestPos, distance);
		double erreurDistance = nearestPos.distance(c.getPosition());
		// marche arrière imposée
		double erreurOrientation = Math.abs((c.orientationReelle - cratere.getNearestOrientation(c.getPosition())) % (2 * Math.PI)); // erreur en radian
		return erreurDistance + 5*erreurOrientation;
	}
	
	/**
	 * Prévient si l'heuristique peut être utilisée
	 * @param c
	 * @return
	 */
	public boolean useIt(CinematiqueObs c)
	{
		return cratere.isProcheObstacle(c.getPosition(), 150);
	}
	
	public boolean estArrive(CinematiqueObs c)
	{
		return cratere.isProcheObstacle(c.getPosition(), distance);
	}

	@Override
	public void useConfig(Config config)
	{
		distance = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE) + config.getInt(ConfigInfo.DISTANCE_AU_CRATERE);
	}
	
}
