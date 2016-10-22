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

package pathfinding.dstarlite.gridspace;

import container.Service;
import utils.Log;

/**
 * Gestionnaire des points dirigés
 * @author pf
 *
 */
public class PointDirigeManager implements Service
{
	private PointDirige[] mem = new PointDirige[PointGridSpace.NB_POINTS * 8];
	private PointGridSpaceManager pm;
	protected Log log;
	
	public PointDirigeManager(PointGridSpaceManager pm, Log log)
	{
		this.pm = pm;
		this.log = log;
		for(int x = 0; x < PointGridSpace.NB_POINTS_POUR_TROIS_METRES; x++)
			for(int y = 0; y < PointGridSpace.NB_POINTS_POUR_DEUX_METRES; y++)
				for(Direction d : Direction.values)
				{
					PointDirige p = new PointDirige(pm.get(x,y),d);
					mem[p.hashCode()] = p;
				}
	}
	
	public PointDirige get(int x, int y, Direction d)
	{
		PointGridSpace p = pm.get(x,y);
		
		if(p == null) // hors table
			return null;
		
		return mem[(p.hashcode << 3) + d.ordinal()];
	}

	public PointDirige get(PointGridSpace p, Direction d)
	{
		if(p == null)
			return null;
		
		return mem[(p.hashcode << 3) + d.ordinal()];
	}

	/**
	 * Récupère un PointDirige à partir de son hash
	 * @param indice
	 * @return
	 */
	public PointDirige get(int indice)
	{
		return mem[indice];
	}

}
