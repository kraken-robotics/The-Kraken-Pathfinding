package pathfinding.chemin;

import java.util.Iterator;

import pathfinding.astarCourbe.arcs.ArcCourbe;

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
