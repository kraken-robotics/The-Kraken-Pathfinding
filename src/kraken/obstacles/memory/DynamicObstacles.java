/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 */

/**
 * Interface for dynamic obstacles.
 * Due to the wide range of behaviour, the implementation is left to the user.
 */

package kraken.obstacles.memory;

import java.util.Iterator;

import kraken.obstacles.types.ObstacleMasque;

public interface DynamicObstacles
{
	public Iterator<ObstacleMasque> getFutureDynamicObstacles(long date);

	public Iterator<ObstacleMasque> getCurrentDynamicObstacles();
}
