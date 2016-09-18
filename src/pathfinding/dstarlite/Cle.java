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

package pathfinding.dstarlite;

/**
 * Clé utilisée dans le D* lite.
 * @author pf
 *
 */

class Cle
{
	// Quand un nœud est consistent, first = vraie distance + heuristique, second = vraie distance
	int first, second;

	public Cle()
	{}
	
	/**
	 * Teste la stricte infériorité
	 * @param other
	 * @return
	 */
	boolean isLesserThan(Cle other)
	{
		return first < other.first || (first == other.first && second < other.second);
	}

	void set(int first, int second)
	{
		this.first = first;
		this.second = second;
	}
	
	@Override
	public Cle clone()
	{
		Cle out = new Cle();
		copy(out);
		return out;
	}

	/**
	 * modified devient une copie de this
	 * @param modified
	 * @return
	 */
	void copy(Cle modified)
	{
		modified.set(first, second);
	}

	@Override
	public String toString()
	{
		return "first = "+first+", second = "+second;
	}
	
}
