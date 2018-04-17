/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
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
	public void update();
	public boolean isThereCollision(Iterable<RectangularObstacle> tentacle);
}
