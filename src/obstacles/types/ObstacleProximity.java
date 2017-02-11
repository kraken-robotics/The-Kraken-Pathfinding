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

package obstacles.types;

import graphic.printable.Couleur;
import pathfinding.dstarlite.gridspace.Masque;
import utils.Vec2RO;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf
 */
public class ObstacleProximity extends ObstacleMasque
{
	private long death_date;
	
	public ObstacleProximity(Vec2RO position, int rad, long death_date, Masque masque)
	{
		super(position, rad, Couleur.OBSTACLES_PROX, masque);
		this.death_date = death_date;
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
	
	/**
	 * Renvoi "vrai" si position est à moins de distance du centre de l'obstacle
	 * @param position
	 * @param distance
	 * @return
	 */
	public boolean isProcheCentre(Vec2RO position, int distance)
	{
		return this.position.squaredDistance(position) < distance * distance;
	}

	// TODO : utilisé où ?
	@Override
	public int hashCode()
	{
		return (int) (super.hashCode()+(death_date & 0xFFFF));
	}
	
}
