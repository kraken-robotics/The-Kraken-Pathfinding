/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package pathfinding;

/**
 * Énumération des différentes stratégies de déplacement pour les trajectoires
 * courbes.
 * 
 * @author pf
 *
 */

public enum DirectionStrategy
{
	FASTEST(true, true), // faire au plus vite
	FORCE_BACK_MOTION(false, true), // forcer la marche arrière
	FORCE_FORWARD_MOTION(true, false); // forcer la marche avant

	public final boolean marcheAvantPossible, marcheArrierePossible;

	public static final DirectionStrategy defaultStrategy = FASTEST;

	/**
	 * Cette direction est-elle possible pour cette stratégie ?
	 * 
	 * @param marcheAvant
	 * @return
	 */
	public boolean isPossible(boolean marcheAvant)
	{
		if(marcheAvant)
			return marcheAvantPossible;

		return marcheArrierePossible;
	}

	private DirectionStrategy(boolean marcheAvantPossible, boolean marcheArrierePossible)
	{
		this.marcheAvantPossible = marcheAvantPossible;
		this.marcheArrierePossible = marcheArrierePossible;
	}

}
