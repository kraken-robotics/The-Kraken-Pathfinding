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

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import robot.CinematiqueObs;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * @author pf
 *
 */

public abstract class ArcCourbe implements Configurable
{

	public boolean rebrousse; // cet arc commence par un rebroussement, c'est-à-dire que la marche avant change
	public boolean stop; // cet arc commence par un arrêt du robot
//	public ObstacleArcCourbe obstacle = new ObstacleArcCourbe();
	protected int tempsRebroussement;
	
	public ArcCourbe(boolean rebrousse, boolean stop)
	{
		this.rebrousse = rebrousse;
		this.stop = stop;
	}
	
	public abstract int getNbPoints();
	public abstract CinematiqueObs getPoint(int indice);
	public abstract CinematiqueObs getLast();
	public abstract double getVitesseTr();
	protected abstract double getLongueur();

	public double getDuree()
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
	
	@Override
	public void useConfig(Config config)
	{
		tempsRebroussement = config.getInt(ConfigInfo.TEMPS_REBROUSSEMENT);
	}

}
