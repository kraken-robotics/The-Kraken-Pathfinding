/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.path;

import java.util.LinkedList;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.ItineraryPoint;

/**
 * A very simple path manager
 * @author pf
 *
 */

public class StaticPath implements PathManager
{
	private LinkedList<ItineraryPoint> path;
	
	public StaticPath()
	{}

	@Override
	public void addToEnd(LinkedList<ItineraryPoint> points)
	{
		path = points;
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
	public void clear()
	{
		path = null;
	}
}
