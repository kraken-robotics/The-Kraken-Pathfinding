/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package pathfinding.dstarlite;

/**
 * Clé utilisée dans le D* lite.
 * 
 * @author pf
 *
 */

public class Cle
{
	// Quand un nœud est consistent, first = vraie distance + heuristique,
	// second = vraie distance
	int first, second;

	public void set(int first, int second)
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
	 * 
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
		return "first = " + first + ", second = " + second;
	}

	public final boolean lesserThan(Cle autre)
	{
		int tmp = first - autre.first;
		return tmp < 0 || (tmp == 0 && second - autre.second < 0);
	}

	public final boolean greaterThan(Cle autre)
	{
		int tmp = first - autre.first;
		return tmp > 0 || (tmp == 0 && second - autre.second > 0);
	}

	/**
	 * Pour calculer a > b, on vérifie a.compare(b) > 0
	 * 
	 * @param autre
	 * @return
	 */
	/*
	 * public final int compare(Cle autre)
	 * {
	 * // Ordre lexico : on compare d'abord first, puis second
	 * int tmp = first - autre.first;
	 * if(tmp != 0)
	 * return tmp;
	 * return second - autre.second;
	 * }
	 */

}
