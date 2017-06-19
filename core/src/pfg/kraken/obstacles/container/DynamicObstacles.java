/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


/**
 * Interface for dynamic obstacles.
 * Due to the wide range of behaviour, the implementation is left to the user.
 */

package pfg.kraken.obstacles.container;

import java.util.Iterator;
import pfg.kraken.obstacles.Obstacle;

public interface DynamicObstacles
{
	public Iterator<Obstacle> getFutureDynamicObstacles(long date);

	public Iterator<Obstacle> getCurrentDynamicObstacles();
}
