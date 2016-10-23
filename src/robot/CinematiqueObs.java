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

package robot;

import memory.Memorizable;
import obstacles.types.ObstacleRobot;

/**
 * Une cinématique + un obstacle associé
 * @author pf
 *
 */

public class CinematiqueObs extends Cinematique implements Memorizable
{
	public volatile ObstacleRobot obstacle;
	private int indiceMemory;	

	public CinematiqueObs(RobotReal r)
	{
		super();
		obstacle = new ObstacleRobot(r);
	}
	
/*	public CinematiqueObs(double x, double y, double orientation, boolean enMarcheAvant, double courbure, Speed vitesseMax, RobotChrono r)
	{
		super(x,y,orientation,enMarcheAvant, courbure,vitesseMax);
		obstacle = new ObstacleRectangular();
		this.obstacle.update(position, orientationReelle, r);
	}
*/
	@Override
	public void setIndiceMemoryManager(int indice)
	{
		indiceMemory = indice;
	}

	@Override
	public int getIndiceMemoryManager()
	{
		return indiceMemory;
	}

	/**
	 * Met à jour les données
	 * @param x
	 * @param y
	 * @param orientation
	 * @param enMarcheAvant
	 * @param courbure
	 * @param vitesseMax
	 * @param obstacle
	 * @param r
	 */
	public void update(double x, double y, double orientation, boolean enMarcheAvant, double courbure, Speed vitesseMax)
	{
		super.update(x,y,orientation,enMarcheAvant, courbure,vitesseMax);
		obstacle.update(position, orientationReelle);
	}

}
