package pathfinding.dstarlite.gridspace;

import container.Service;
import utils.Config;
import utils.Log;

/**
 * Gestionnaire des points dirigés
 * @author pf
 *
 */
public class PointDirigeManager implements Service
{
	private static PointDirige[] mem = new PointDirige[PointGridSpace.NB_POINTS * 8];
	private PointGridSpaceManager pm;
	protected Log log;
	
	public PointDirigeManager(PointGridSpaceManager pm, Log log)
	{
		this.pm = pm;
		this.log = log;
		for(int x = 0; x < PointGridSpace.NB_POINTS_POUR_TROIS_METRES; x++)
			for(int y = 0; y < PointGridSpace.NB_POINTS_POUR_DEUX_METRES; y++)
				for(Direction d : Direction.values())
				{
					PointDirige p = new PointDirige(pm.get(x,y),d);
					mem[p.hashCode()] = p;
				}
	}
	
	public PointDirige get(int x, int y, Direction d)
	{
		PointGridSpace p = pm.get(x,y);
		
		if(p == null) // hors table
			return null;
		
		return mem[(p.hashcode << 3) + d.ordinal()];
	}

	public PointDirige get(PointGridSpace p, Direction d)
	{
		if(p == null)
			return null;
		
		return mem[(p.hashcode << 3) + d.ordinal()];
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
