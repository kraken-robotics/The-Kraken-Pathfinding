/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.engine;

import pfg.kraken.obstacles.RectangularObstacle;

/**
 * A physics engine
 * @author pf
 *
 */

public interface PhysicsEngine
{
	/**
	 * This function is called at the initialization and each time an obstacle changes
	 */
	public void update();
	
	/**
	 * Is there a collision with a current obstacle ?
	 * @param tentacle
	 * @return
	 */
	public boolean isThereCollision(Iterable<RectangularObstacle> tentacle);
}
