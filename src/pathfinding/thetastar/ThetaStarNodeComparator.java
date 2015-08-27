package pathfinding.thetastar;

import java.util.Comparator;

/**
 * Comparateur utilisé pour la PriorityQueue de ThetaStar
 * @author pf
 *
 */

public class ThetaStarNodeComparator implements Comparator<ThetaStarNode>
{

	@Override
	public int compare(ThetaStarNode arg0, ThetaStarNode arg1)
	{
		int out = (arg0.f_score - arg1.f_score) << 2;
		int out2 = arg1.g_score - arg0.g_score;
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
