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

package pathfinding.astar.arcs;

import robot.CinematiqueObs;

/**
 * Arc courbe de longueur fixe
 * @author pf
 *
 */

public class ArcCourbeStatique extends ArcCourbe
{
	public CinematiqueObs[] arcselems = new CinematiqueObs[ClothoidesComputer.NB_POINTS];
	
	public ArcCourbeStatique(int demieLargeurNonDeploye, int demieLongueurArriere, int demieLongueurAvant)
	{
		for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			arcselems[i] = new CinematiqueObs(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);
	}
	
	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcCourbe
	 */
	public void copy(ArcCourbeStatique arcCourbe)
	{
		for(int i = 0; i < arcselems.length; i++)
			arcselems[i].copy(arcCourbe.arcselems[i]);
	}
	
	@Override
	public int getNbPoints()
	{
		return ClothoidesComputer.NB_POINTS;
	}
	
	@Override
	public CinematiqueObs getPoint(int indice)
	{
		return arcselems[indice];
	}
	
	@Override
	public CinematiqueObs getLast()
	{
		return arcselems[ClothoidesComputer.NB_POINTS - 1];
	}
	
	@Override
	public double getVitesseTr()
	{
		double v = 0;
		for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			v += arcselems[i].vitesseMax;
		return v / (ClothoidesComputer.NB_POINTS);
	}

	@Override
	protected double getLongueur()
	{
		return ClothoidesComputer.DISTANCE_ARC_COURBE;
	}

}
