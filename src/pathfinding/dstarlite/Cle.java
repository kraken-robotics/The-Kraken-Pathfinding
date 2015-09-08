package pathfinding.dstarlite;

/**
 * Clé utilisée dans le D* lite.
 * @author pf
 *
 */

public class Cle
{
	// En gros, first c'est f_score et second c'est g_score
	public int first, second;

	public Cle()
	{}
	
	/**
	 * Teste la stricte infériorité
	 * @param other
	 * @return
	 */
	public boolean isLesserThan(Cle other)
	{
		return first < other.first || (first == other.first && second < other.second);
	}

	public void set(int first, int second)
	{
		this.first = first;
		this.second = second;
	}
	
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
	public void copy(Cle modified)
	{
		modified.set(first, second);
	}

	@Override
	public String toString()
	{
		return "first = "+first+", second = "+second;
	}
	
}
