/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar;

import java.util.LinkedList;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.ItineraryPoint;
import pfg.log.Log;

/**
 * Faux chemin, sert à la prévision d'itinéraire
 * FIXME UNUSED FOR THE MOMENT
 * @author pf
 *
 */

public class DefaultCheminPathfinding implements CheminPathfindingInterface
{
	private LinkedList<ItineraryPoint> path;
	protected Log log;
	
	public DefaultCheminPathfinding(Log log)
	{
		this.log = log;
	}

	@Override
	public synchronized void addToEnd(LinkedList<ItineraryPoint> points)
	{
		path = points;
	}

	@Override
	public void setUptodate()
	{}

	public LinkedList<ItineraryPoint> getPath()
	{
		LinkedList<ItineraryPoint> out = path;
		path = null;
		return out;
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

	public void clear()
	{
		path = null;
	}
}
