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

import config.Config;
import config.ConfigInfo;
import robot.CinematiqueObs;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * @author pf
 *
 */

public abstract class ArcCourbe
{

	public boolean rebrousse; // cet arc commence par un rebroussement, c'est-à-dire que la marche avant change
//	public ObstacleArcCourbe obstacle = new ObstacleArcCourbe();
	protected static int tempsRebroussement;
	
	public ArcCourbe(boolean rebrousse)
	{
		this.rebrousse = rebrousse;
	}
	
	public abstract int getNbPoints();
	public abstract CinematiqueObs getPoint(int indice);
	public abstract CinematiqueObs getLast();
	public abstract double getVitesseTr();
	protected abstract double getLongueur();

	public final double getDuree()
	{
		if(rebrousse)
			return getLongueur() / getVitesseTr() + tempsRebroussement;
		return getLongueur() / getVitesseTr();
	}
	
	@Override
	public String toString()
	{
		String out = getClass().getSimpleName()+" :\n";
		for(int i = 0; i < getNbPoints()-1; i++)
			out += getPoint(i)+"\n";
		out += getLast();
		return out;
	}
	
	public static void useConfig(Config config)
	{
		tempsRebroussement = config.getInt(ConfigInfo.TEMPS_REBROUSSEMENT);
	}

}
