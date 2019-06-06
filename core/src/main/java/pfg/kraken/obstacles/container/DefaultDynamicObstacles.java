/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import pfg.kraken.obstacles.Obstacle;

/**
 * A default dynamical obstacles manager
 * @author pf
 *
 */

public final class DefaultDynamicObstacles extends SmartDynamicObstacles
{
	private List<Obstacle> obsList = new ArrayList<Obstacle>();

	protected void addObstacle(Obstacle obs)
	{
		obsList.add(obs);
	}
	
	@Override
	public Iterator<Obstacle> getCurrentDynamicObstacles()
	{
		return obsList.iterator();
	}
	
	@Override
	public synchronized void clear()
	{
		super.clear();
		obsList.clear();
	}

}