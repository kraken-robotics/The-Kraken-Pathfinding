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

package obstacles.types;

import java.util.ArrayList;

import pathfinding.dstarlite.gridspace.PointDirige;
import utils.Vec2RO;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf
 */
public class ObstacleProximity extends ObstacleCircular
{
	private long death_date;
	private ArrayList<PointDirige> masque;
	
	public ObstacleProximity(Vec2RO position, int rad, long death_date, ArrayList<PointDirige> masque)
	{
		super(position,rad);
		this.death_date = death_date;
		this.masque = masque;
	}

	public ArrayList<PointDirige> getMasque()
	{
		return masque;
	}
	
	@Override
	public String toString()
	{
		return super.toString()+", meurt à "+death_date+" ms";
	}
	
	public boolean isDestructionNecessary(long date)
	{
		return death_date < date;
	}
		
	public long getDeathDate()
	{
		return death_date;
	}
	
}
