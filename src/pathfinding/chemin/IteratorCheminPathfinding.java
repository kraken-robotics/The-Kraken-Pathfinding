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

package pathfinding.chemin;

import java.util.Iterator;

import pathfinding.astarCourbe.arcs.ArcCourbe;

/**
 * Un iterateur pour manipuler facilement le chemin pathfinding
 * @author pf
 *
 */

public class IteratorCheminPathfinding implements Iterator<ArcCourbe>
{
	private int index;
	private CheminPathfinding chemin;
	
	public IteratorCheminPathfinding(CheminPathfinding chemin)
	{
		this.chemin = chemin;
		index = -1;
	}
	
	public void reinit()
	{
		index = chemin.indexFirst;
	}

	@Override
	public boolean hasNext()
	{
		return index != chemin.indexLast;

	}

	@Override
	public ArcCourbe next()
	{
		index++;
		index &= 0xFF;
		return chemin.get(index);
	}

	@Override
	public void remove()
	{
		chemin.indexFirst++;
		chemin.indexFirst &= 0xFF;
	}

	public int getIndex()
	{
		return index;
	}

}
