package pathfinding;

/**
 * Clé utilisé par le D* lite.
 * @author pf
 *
 */

public class Cle
{
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
		out.set(first, second);
		return out;
	}
	
}
