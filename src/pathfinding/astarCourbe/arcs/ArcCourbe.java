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

import obstacles.types.ObstacleArcCourbe;
import robot.Cinematique;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * @author pf
 *
 */

public abstract class ArcCourbe {

	public boolean rebrousse; // cet arc commence par un rebroussement, c'est-à-dire que la marche avant change
	public boolean stop; // cet arc commence par un arrêt du robot
	public ObstacleArcCourbe obstacle = new ObstacleArcCourbe();
	
	public ArcCourbe(boolean rebrousse, boolean stop)
	{
		this.rebrousse = rebrousse;
		this.stop = stop;
	}
	
	public abstract int getNbPoints();
	public abstract Cinematique getPoint(int indice);
	public abstract Cinematique getLast();
	public abstract double getDuree();
	public abstract double getVitesseTr();

}
