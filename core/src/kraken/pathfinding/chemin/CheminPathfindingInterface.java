/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.chemin;

import java.util.LinkedList;
import kraken.robot.Cinematique;
import kraken.robot.ItineraryPoint;

/**
 * Interface pour pouvoir interchanger le vrai chemin de pathfinding et le faux
 * 
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
