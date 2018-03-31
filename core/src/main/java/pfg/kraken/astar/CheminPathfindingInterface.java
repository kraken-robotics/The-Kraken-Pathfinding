/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar;

import java.util.LinkedList;

import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.ItineraryPoint;

/**
 * Interface pour pouvoir interchanger le vrai chemin de pathfinding et le faux
 * FIXME UNUSED FOR THE MOMENT
 * @author pf
 *
 */

public interface CheminPathfindingInterface
{
	public void addToEnd(LinkedList<ItineraryPoint> points);

	public void setUptodate();

	public boolean aAssezDeMarge();

	public boolean needStop();

	public Cinematique getLastValidCinematique();
}
