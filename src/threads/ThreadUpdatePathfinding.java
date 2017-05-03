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

package threads;

import config.Config;
import config.ConfigInfo;
import container.dependances.HighPFClass;
import exceptions.PathfindingException;
import pathfinding.astar.AStarCourbe;
import pathfinding.chemin.CheminPathfinding;
import robot.Cinematique;
import robot.Speed;
import serie.BufferOutgoingOrder;
import utils.Log;

/**
 * Thread qui recalcule l'itinéraire à emprunter. Surveille CheminPathfinding.
 * @author pf
 *
 */

public class ThreadUpdatePathfinding extends ThreadService implements HighPFClass
{
	protected Log log;
	private AStarCourbe pathfinding;
	private CheminPathfinding chemin;
	private BufferOutgoingOrder out;
	private boolean debugCapteurs;

	public ThreadUpdatePathfinding(Log log, AStarCourbe pathfinding, CheminPathfinding chemin, Config config, BufferOutgoingOrder out)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.chemin = chemin;
		this.out = out;
		debugCapteurs = config.getBoolean(ConfigInfo.DEBUG_CAPTEURS);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			while(true)
			{
				synchronized(chemin)
				{
					if(chemin.isUptodate())
						chemin.wait();
				}
				// on a été prévenu que le chemin n'est plus à jour : ralentissement et replanification
				try {
					Cinematique lastValid = chemin.getLastValidCinematique();
					out.setMaxSpeed(Speed.REPLANIF.translationalSpeed);
					if(debugCapteurs)
						log.debug("Mise à jour du chemin");
					pathfinding.updatePath(true, lastValid);
					out.setMaxSpeed(Speed.STANDARD.translationalSpeed); // TODO et si ce n'était pas cette vitesse là ?…
				} catch (PathfindingException e) {
					log.critical(e);
					chemin.clear();
					out.immobilise();
				}
			}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		} catch (Exception e) {
			log.debug("Arrêt inattendu de "+Thread.currentThread().getName()+" : "+e);
		}
	}

}
