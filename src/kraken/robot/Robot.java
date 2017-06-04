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

package kraken.robot;

import kraken.exceptions.MemoryManagerException;
import kraken.utils.Log;

/**
 * Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * 
 * @author pf
 */

public abstract class Robot
{
	/*
	 * DÉPLACEMENT HAUT NIVEAU
	 */

	public abstract long getTempsDepuisDebutMatch();

	protected Cinematique cinematique;
	protected Log log;

	public abstract void avance(double distance, Speed speed) throws InterruptedException, MemoryManagerException;

	public Robot(Log log)
	{
		this.log = log;
		cinematique = new Cinematique();
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
	public final void copy(RobotChrono rc)
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
	
	public Cinematique getCinematique()
	{
		return cinematique;
	}

}
