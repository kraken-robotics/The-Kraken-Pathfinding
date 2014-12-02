package pathfinding;

import smartMath.Vec2;

/**
 * Utilisé par le A*
 * Juste un Vec2 avec une distance en plus
 * @author pf
 *
 */

class PFVec2 extends Vec2 {

	public int distance;

	public PFVec2(Vec2 point, int distance)
	{
		super(point);
		this.distance = distance;
	}
	
}
