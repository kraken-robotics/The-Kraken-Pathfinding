/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite;

/**
 * Clé utilisée dans le D* lite.
 * 
 * @author pf
 *
 */

public final class Key
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
	public Key clone()
	{
		Key out = new Key();
		copy(out);
		return out;
	}

	/**
	 * modified devient une copie de this
	 * 
	 * @param modified
	 * @return
	 */
	void copy(Key modified)
	{
		modified.set(first, second);
	}

	@Override
	public String toString()
	{
		return "first = " + first + ", second = " + second;
	}

	public final boolean isEqualsTo(Key autre)
	{
		return first == autre.first && second == autre.second;
	}
	
	public final boolean lesserThan(Key autre)
	{
		int tmp = first - autre.first;
		return tmp < 0 || (tmp == 0 && second - autre.second < 0);
	}

	public final boolean greaterThan(Key autre)
	{
		int tmp = first - autre.first;
		return tmp > 0 || (tmp == 0 && second - autre.second > 0);
	}

}
