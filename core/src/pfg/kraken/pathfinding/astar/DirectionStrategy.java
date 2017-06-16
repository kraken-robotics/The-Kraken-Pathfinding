/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.pathfinding.astar;

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
