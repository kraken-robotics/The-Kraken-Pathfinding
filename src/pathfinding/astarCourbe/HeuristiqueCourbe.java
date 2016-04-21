package pathfinding.astarCourbe;

import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Interface pour l'heuristique du pathfinding courbe
 * @author pf
 *
 */

public interface HeuristiqueCourbe
{
	public double heuristicCostCourbe(Vec2<ReadOnly> position);
}
