/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.path;

import java.util.LinkedList;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.ItineraryPoint;

/**
 * A path manager that can handle dynamic update
 * @author pf
 *
 */

public class DynamicPath implements PathManager
{
	private LinkedList<ItineraryPoint> path;
	
	public DynamicPath()
	{}

	@Override
	public synchronized void addToEnd(LinkedList<ItineraryPoint> points)
	{
		path = points;
		notifyAll();
	}

	@Override
	public void setUptodate()
	{}

	public LinkedList<ItineraryPoint> getPath()
	{
		return path;
	}

	@Override
	public boolean aAssezDeMarge()
	{
		return true;
	}

	@Override
	public boolean needStop()
	{
		return false;
	}

	@Override
	public Cinematique getLastValidCinematique()
	{
		return null;
	}

	@Override
	public synchronized void clear()
	{
		path = null;
		notifyAll();
	}
}
