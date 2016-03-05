package pathfinding.astarCourbe;

import permissions.ReadOnly;
import permissions.ReadWrite;
import utils.Vec2;

/**
 * Heuristique pour l'AStarCourbe pour la planification.
 * Pour le pathfinding dynamique, on utilise l'heuristique basée sur le DStarLite
 * @author pf
 *
 */

public class HeuristiqueSimple implements HeuristiqueCourbe
{
	private Vec2<ReadWrite> arrivee = new Vec2<ReadWrite>();

	public void setPositionArrivee(Vec2<ReadOnly> arrivee)
	{
		Vec2.copy(arrivee, this.arrivee);
	}
	
	@Override
	public int heuristicCostCourbe(Vec2<ReadOnly> position)
	{
		return (int) arrivee.distance(position);
	}

}
