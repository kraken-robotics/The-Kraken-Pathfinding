/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.robot;

import pfg.kraken.pathfinding.astar.tentacles.Tentacle;
import pfg.kraken.utils.XY;

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

	public long getTempsDepuisDebutMatch()
	{
		return date;
	}

	public void suitArcCourbe(Tentacle came_from_arc, double translationalSpeed)
	{
		date += came_from_arc.getDuree(translationalSpeed);
		came_from_arc.getLast().copy(cinematique);
	}

	public Cinematique getCinematique()
	{
		return cinematique;
	}

	public void avance(double distance, Speed speed)
	{
		// TODO update date
		cinematique.getPositionEcriture().plus(new XY(distance, cinematique.orientationReelle, true));
	}


	public int codeForPFCache()
	{
		return cinematique.codeForPFCache();
	}

	/**
	 * Copy this dans rc. this reste inchangé.
	 * 
	 * @param rc
	 */
	public final void copy(RobotState rc)
	{
		cinematique.copy(rc.cinematique);
		// pas besoin de copier symétrie car elle ne change pas en cours de
		// match
		rc.date = getTempsDepuisDebutMatch();
	}

	@Override
	public String toString()
	{
		return cinematique.toString();
	}

	public void setCinematique(Cinematique cinematique)
	{
		cinematique.copy(this.cinematique);
	}

}
