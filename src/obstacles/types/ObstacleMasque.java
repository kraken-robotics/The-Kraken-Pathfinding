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

import graphic.printable.Couleur;
import pathfinding.dstarlite.gridspace.Masque;
import utils.Vec2RO;

/**
 * Obstacle avec un masque (c'est-à-dire utilisable par le D* Lite)
 * @author pf
 */
public class ObstacleMasque extends ObstacleCircular
{
	private Masque masque;
	
	public ObstacleMasque(Vec2RO position, int rad, Couleur couleur, Masque masque)
	{
		super(position,rad,couleur);
		this.masque = masque;
	}

	public Masque getMasque()
	{
		return masque;
	}
	
	@Override
	public int hashCode()
	{
		return (int) (masque.hashCode());
	}

	/**
	 * Utilisé pour les cylindres pour qui on n'a pas le masque à la construction
	 * @param masque
	 */
	public void setMasque(Masque masque)
	{
		this.masque = masque;
	}
}
