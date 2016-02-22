package pathfinding.astarCourbe;

import permissions.ReadOnly;
import utils.Vec2;

/**
 * Interface pour l'heuristique du pathfinding courbe
 * @author pf
 *
 */

public interface HeuristiqueCourbe {

	public int heuristicCostCourbe(Vec2<ReadOnly> position);
	
}
