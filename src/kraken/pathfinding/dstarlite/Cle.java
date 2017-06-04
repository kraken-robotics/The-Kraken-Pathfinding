/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.dstarlite;

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
