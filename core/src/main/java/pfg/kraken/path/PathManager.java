/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.path;

import java.util.LinkedList;

import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.ItineraryPoint;

/**
 * Interface for the path managers
 * @author pf
 *
 */

public interface PathManager
{
	public void addToEnd(LinkedList<ItineraryPoint> points);

	public void setUptodate();

	public boolean aAssezDeMarge();

	public boolean needStop();

	public Cinematique getLastValidCinematique();
	
	public void clear();
}
