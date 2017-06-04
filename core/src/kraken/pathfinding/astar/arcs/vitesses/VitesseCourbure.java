/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.astar.arcs.vitesses;

import kraken.pathfinding.DirectionStrategy;
import kraken.robot.Cinematique;

/**
 * Les différentes vitesses de courbure qu'on peut suivre
 * 
 * @author pf
 *
 */

public interface VitesseCourbure
{
	public boolean isAcceptable(Cinematique c, DirectionStrategy directionstrategyactuelle, double courbureMax);

	public int getNbArrets();
}
