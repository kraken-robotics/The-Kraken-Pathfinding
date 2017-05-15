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

import robot.RobotChrono;
import table.Table;

/**
 * Le game state rassemble toutes les informations disponibles à un instant
 * - infos sur le robot (position, objet, ...) dans Robot
 * - infos sur les obstacles mobiles dans ObstaclesIteratorFutur
 * - infos sur les éléments de jeux dans Table
 * Utilisé dans l'arbre des possibles
 * 
 * @author pf
 */

public class ChronoGameState extends GameState<RobotChrono>
{
	// public final ObstaclesIteratorFutur iterator;

	public ChronoGameState(RobotChrono robot, /*
												 * ObstaclesIteratorFutur
												 * iterator,
												 */ Table table)
	{
		this.robot = robot;
		// this.iterator = iterator;
		this.table = table;
	}

	/**
	 * Copie this dans other. this reste inchangé.
	 * Cette copie met à jour les obstacles et les attributs de temps.
	 * 
	 * @param other
	 * @throws FinMatchException
	 */
	@Override
	public final void copyAStarCourbe(ChronoGameState modified)
	{
		table.copy(modified.table);
		robot.copy(modified.robot);
		// iterator.copy(modified.iterator, robot.getTempsDepuisDebutMatch());
	}
}
