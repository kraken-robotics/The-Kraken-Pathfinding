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
		int out = (arg0.cle.first - arg1.cle.first) << 2;
		int out2 = arg0.cle.second - arg1.cle.second;
		if(out2 == 0)
			return out;
		else if(out2 > 100000)
			return out + 3;
		else if(out2 > 10000)
			return out + 2;
		else if(out2 > 0)
			return out + 1;
		else if(out2 < -100000)
			return out - 3;
		else if(out2 < -10000)
			return out - 2;
		else// if(out2 < 0)
			return out - 1;
	}
	
}
