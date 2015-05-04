package astar;

/**
 * Clé utilisé par le D* lite.
 * @author pf
 *
 */

public class Cle
{
	public double first, second;

	public Cle(double first, double second)
	{
		this.first = first;
		this.second = second;
	}
	
	public boolean isLesserThan(Cle other)
	{
		return first < other.first || (first == other.first && second < other.second);
	}
	
}
