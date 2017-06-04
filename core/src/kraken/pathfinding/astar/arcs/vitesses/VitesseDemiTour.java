/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.astar.arcs.vitesses;

import kraken.pathfinding.astar.DirectionStrategy;
import kraken.robot.Cinematique;

/**
 * Arc de clothoïde qui fait un demi-tour
 * 
 * @author pf
 *
 */

public enum VitesseDemiTour implements VitesseCourbure
{
	DEMI_TOUR_DROITE(VitesseClotho.DROITE_2), // TODO version avec d'autres
												// vitesses ?
	DEMI_TOUR_GAUCHE(VitesseClotho.GAUCHE_2);

	public VitesseClotho v;

	private VitesseDemiTour(VitesseClotho v)
	{
		this.v = v;
	}

	@Override
	public boolean isAcceptable(Cinematique c, DirectionStrategy directionstrategyactuelle, double courbureMax)
	{
		// on évite les demi-tours absurdes
		if(((v.positif && c.courbureGeometrique < -1) || (!v.positif && c.courbureGeometrique > 1)))
			return false;

		return true;
	}

	@Override
	public int getNbArrets()
	{
		return 1;
	}
}
