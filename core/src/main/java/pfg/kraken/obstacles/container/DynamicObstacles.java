/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


/**
 * Interface for dynamic obstacles.
 * Due to the wide range of behaviour, the implementation is left to the user.
 */

package pfg.kraken.obstacles.container;

import java.util.Iterator;

import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.struct.EmbodiedKinematic;

/**
 * The interface of a dynamic obstacles container
 * @author pf
 *
 */

public interface DynamicObstacles
{
	public int isThereCollision(EmbodiedKinematic[] l, int from, int to);
	public Iterator<Obstacle> getCurrentDynamicObstacles();
	public boolean needCollisionCheck();
}
