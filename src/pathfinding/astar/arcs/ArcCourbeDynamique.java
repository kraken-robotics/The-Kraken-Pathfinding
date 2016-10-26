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

package pathfinding.astar.arcs;

import java.util.List;

import robot.CinematiqueObs;

/**
 * Arc courbe de longueur inconnue à l'avance
 * @author pf
 *
 */

public class ArcCourbeDynamique extends ArcCourbe
{
	public List<CinematiqueObs> arcs;
	public double longueur;
	
	public ArcCourbeDynamique(List<CinematiqueObs> arcs, double longueur, boolean rebrousse)
	{
		super(rebrousse);
		this.arcs = arcs;
		this.longueur = longueur;
	}
	
	@Override
	public int getNbPoints()
	{
		return arcs.size();
	}
	
	@Override
	public CinematiqueObs getPoint(int indice)
	{
		return arcs.get(indice);
	}
	
	@Override
	public CinematiqueObs getLast()
	{
		return arcs.get(arcs.size()-1);
	}

	@Override
	public double getLongueur()
	{
		return longueur;
	}
	
	@Override
	public double getVitesseTr()
	{
		double v = 0;
		for(CinematiqueObs c : arcs)
			v += c.vitesseMax;
		return v / arcs.size();
	}

}
