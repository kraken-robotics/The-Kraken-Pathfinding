/*
Copyright (C) 2016 Pierre-François Gimenez

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

package obstacles;

import robot.Cinematique;

/**
 * Un groupe de mesures qui proviennent des capteurs
 * @author pf
 *
 */

public class SensorsData
{
	public Cinematique cinematique;
	/** Ce que voit chacun des capteurs */
	public int[] mesures;
	
	public SensorsData(int[] mesures, Cinematique cinematique)
	{
		this.mesures = mesures;
		this.cinematique = cinematique;
	}
	
}
