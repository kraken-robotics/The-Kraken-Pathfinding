/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.CinematiqueObs;
import pfg.kraken.robot.ItineraryPoint;

/**
 * A path manager that can handle dynamic update
 * @author pf
 *
 */

public class DynamicPath
{
	private LinkedList<CinematiqueObs> path;
	
	public DynamicPath()
	{}

	public synchronized void addToEnd(LinkedList<CinematiqueObs> points)
	{
		path = points;
		notifyAll();
	}

	public void setUptodate()
	{}

	public LinkedList<CinematiqueObs> getPath()
	{
		return path;
	}

	public boolean aAssezDeMarge()
	{
		return true;
	}

	public boolean needStop()
	{
		return false;
	}

	public Cinematique getLastValidCinematique()
	{
		return null;
	}

	public synchronized void clear()
	{
		path = null;
		notifyAll();
	}

	public List<ItineraryPoint> getPathItineraryPoint()
	{
		List<ItineraryPoint> pathIP = new ArrayList<ItineraryPoint>();
		for(CinematiqueObs o : path)
			pathIP.add(new ItineraryPoint(o));

		return pathIP;
	}
}
