package pathfinding.dstarlite;

import java.util.Comparator;

/**
 * Comparateur utilisé pour la PriorityQueue de DStarLite
 * @author pf
 *
 */

public class DStarLiteNodeComparator implements Comparator<DStarLiteNode>
{

	@Override
	public int compare(DStarLiteNode arg0, DStarLiteNode arg1)
	{
		return arg0.cle.toInt() - arg1.cle.toInt();
	}
	
}
