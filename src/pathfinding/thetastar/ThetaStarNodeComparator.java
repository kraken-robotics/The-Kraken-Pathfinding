package pathfinding.thetastar;

import java.util.Comparator;

public class ThetaStarNodeComparator implements Comparator<ThetaStarNode>
{

	@Override
	public int compare(ThetaStarNode o, ThetaStarNode o2)
	{
		return (o.f_score < o2.f_score || (o.f_score == o2.f_score && o.g_score > o2.g_score)) ? -1 : 1;
	}

}
