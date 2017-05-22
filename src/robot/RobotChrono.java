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

package robot;

import exceptions.ActionneurException;
import exceptions.MemoryManagerException;
import exceptions.UnableToMoveException;
import pathfinding.astar.arcs.ArcCourbe;
import pathfinding.chemin.CheminPathfinding;
import serie.Ticket;
import utils.Log;
import utils.Vec2RO;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée
 * des actions
 * 
 * @author pf
 */

public class RobotChrono extends Robot
{
	// Date en millisecondes depuis le début du match.
	protected long date = 0;
	private CheminPathfinding chemin;

	/**
	 * Constructeur clone
	 * 
	 * @param log
	 * @param robot
	 */
	public RobotChrono(Log log, RobotReal robot, CheminPathfinding chemin)
	{
		super(log);
		robot.copy(this);
		this.chemin = chemin;
	}

	@Override
	public long getTempsDepuisDebutMatch()
	{
		return date;
	}

	public void suitArcCourbe(ArcCourbe came_from_arc, double translationalSpeed)
	{
		date += came_from_arc.getDuree(translationalSpeed);
		came_from_arc.getLast().copy(cinematique);
	}

	public Cinematique getCinematique()
	{
		return cinematique;
	}

	@Override
	protected void bloque(String nom, Object... param) throws InterruptedException
	{}

	@Override
	public void avance(double distance, Speed speed)
	{
		cinematique.getPositionEcriture().plus(new Vec2RO(distance, cinematique.orientationReelle, true));
	}
	
	// TODO
	public void avanceToCircle(Speed speed) throws InterruptedException, UnableToMoveException, MemoryManagerException
	{}


	@Override
	public void followTrajectory(Speed vitesse) throws InterruptedException, UnableToMoveException
	{
		// ne mets pas à jour la date, c'est normal
		chemin.getLastCinematique().copy(cinematique);
	}

	@Override
	public boolean isArrivedAsser()
	{
		return true;
	}
	
	public Ticket traverseBascule() throws InterruptedException, ActionneurException
	{
		return null;
	}


}
