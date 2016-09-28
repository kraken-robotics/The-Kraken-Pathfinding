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

package pathfinding.astarCourbe.arcs;

import java.util.ArrayList;

import obstacles.types.ObstacleArcCourbe;
import robot.Cinematique;

/**
 * Arc de trajectoire courbe issu d'une interpolation cubique
 * @author pf
 *
 */

public class ArcCourbeCubique extends ArcCourbe
{
	public ArrayList<Cinematique> arcs;
	public double longueur;
	
	public ArcCourbeCubique(ObstacleArcCourbe obstacle, ArrayList<Cinematique> arcs, double longueur, boolean rebrousse, boolean stop)
	{
		super(rebrousse, stop);
		this.arcs = arcs;
		this.longueur = longueur;
		this.obstacle = obstacle;
	}
	
	@Override
	public int getNbPoints()
	{
		return arcs.size();
	}
	
	@Override
	public Cinematique getPoint(int indice)
	{
		return arcs.get(indice);
	}
	
	@Override
	public Cinematique getLast()
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
		for(Cinematique c : arcs)
			v += c.vitesseMax.translationalSpeed;
		return v / arcs.size();
	}

}
