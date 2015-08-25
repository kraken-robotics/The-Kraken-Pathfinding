package pathfinding.thetastar;

import java.util.Comparator;

public class ThetaStarNodeComparator implements Comparator<ThetaStarNode>
{

	@Override
	public int compare(ThetaStarNode o, ThetaStarNode o2)
	{
		return o.toInt() - o2.toInt();
	}

}
