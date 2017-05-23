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

package robot;

import java.io.Serializable;
import memory.Memorizable;
import obstacles.types.ObstacleRobot;

/**
 * Une cinématique + un obstacle associé
 * 
 * @author pf
 *
 */

public class CinematiqueObs extends Cinematique implements Memorizable, Serializable
{
	private static final long serialVersionUID = 1L;
	public volatile ObstacleRobot obstacle;
	private int indiceMemory;

	public CinematiqueObs(int demieLargeurNonDeploye, int demieLongueurArriere, int demieLongueurAvant, int marge)
	{
		super();
		obstacle = new ObstacleRobot(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant, marge);
	}

	public void copy(CinematiqueObs autre)
	{
		super.copy(autre);
		obstacle.copy(autre.obstacle);
	}

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
	 * 
	 * @param x
	 * @param y
	 * @param orientation
	 * @param enMarcheAvant
	 * @param courbure
	 * @param vitesseMax
	 * @param obstacle
	 * @param r
	 */
	@Override
	public void update(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbure)
	{
		super.update(x, y, orientationGeometrique, enMarcheAvant, courbure);
		obstacle.update(position, orientationReelle);
	}
	
	@Override
	public void updateReel(double x, double y, double orientationReelle, boolean enMarcheAvant, double courbure)
	{
		super.updateReel(x, y, orientationReelle, enMarcheAvant, courbure);
		obstacle.update(position, orientationReelle);
	}

}
