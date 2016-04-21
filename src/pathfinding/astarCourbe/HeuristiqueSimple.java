package pathfinding.astarCourbe;

import container.Service;
import utils.Config;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

/**
 * Heuristique pour l'AStarCourbe pour la planification.
 * Pour le pathfinding dynamique, on utilise l'heuristique basée sur le DStarLite
 * @author pf
 *
 */

public class HeuristiqueSimple implements HeuristiqueCourbe, Service
{
	private Vec2<ReadWrite> arrivee = new Vec2<ReadWrite>();
	protected Log log;

	public HeuristiqueSimple(Log log)
	{
		this.log = log;
	}
	
	public void setPositionArrivee(Vec2<ReadOnly> arrivee)
	{
		Vec2.copy(arrivee, this.arrivee);
	}
	
	@Override
	public int heuristicCostCourbe(Vec2<ReadOnly> position)
	{
		return (int) arrivee.distance(position);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
