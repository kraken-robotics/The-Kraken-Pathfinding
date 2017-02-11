/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package pathfinding.astar.arcs.vitesses;

import pathfinding.DirectionStrategy;
import robot.Cinematique;

/**
 * Arc de clothoïde qui fait un demi-tour
 * @author pf
 *
 */

public enum VitesseDemiTour implements VitesseCourbure
{
	DEMI_TOUR_DROITE(VitesseClotho.DROITE_3), // TODO version avec d'autres vitesses ?
	DEMI_TOUR_GAUCHE(VitesseClotho.GAUCHE_3);

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
}
