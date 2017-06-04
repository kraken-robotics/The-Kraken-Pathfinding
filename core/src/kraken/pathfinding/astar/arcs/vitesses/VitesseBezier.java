/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.astar.arcs.vitesses;

import kraken.pathfinding.astar.DirectionStrategy;
import kraken.robot.Cinematique;

/**
 * Interpolation par courbe de Bézier
 * 
 * @author pf
 *
 */

public enum VitesseBezier implements VitesseCourbure
{
	BEZIER_QUAD(0),
	CIRCULAIRE_VERS_CERCLE(1);

	private final int nbArrets;

	private VitesseBezier(int nbArrets)
	{
		this.nbArrets = nbArrets;
	}

	@Override
	public boolean isAcceptable(Cinematique c, DirectionStrategy directionstrategyactuelle, double courbureMax)
	{
		return true;
	}

	@Override
	public int getNbArrets()
	{
		return nbArrets;
	}
}
