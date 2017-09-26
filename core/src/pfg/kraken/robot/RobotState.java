/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.robot;

import pfg.kraken.astar.tentacles.Tentacle;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée
 * des actions
 * 
 * @author pf
 */

public class RobotState
{
	protected Cinematique cinematique = new Cinematique();

	// Date en millisecondes depuis le début du match.
	protected long date = 0;

	public void initDate()
	{
		date = 0;
	}
	
	public void suitArcCourbe(Tentacle came_from_arc, double duration)
	{
		date += duration;
		came_from_arc.getLast().copy(cinematique);
	}

	public Cinematique getCinematique()
	{
		return cinematique;
	}

	/**
	 * Copy this dans rc. this reste inchangé.
	 * 
	 * @param rc
	 */
	public final void copy(RobotState rc)
	{
		cinematique.copy(rc.cinematique);
		rc.date = date;
	}

	public void setCinematique(Cinematique cinematique)
	{
		cinematique.copy(this.cinematique);
	}

}
