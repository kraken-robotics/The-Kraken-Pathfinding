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

package threads;

import container.dependances.HighPFClass;
import exceptions.PathfindingException;
import pathfinding.astar.AStarCourbe;
import pathfinding.chemin.CheminPathfinding;
import robot.Cinematique;
import utils.Log;
import utils.Log.Verbose;

/**
 * Thread qui recalcule l'itinéraire à emprunter. Surveille CheminPathfinding.
 * 
 * @author pf
 *
 */

public class ThreadUpdatePathfinding extends ThreadService implements HighPFClass
{
	protected Log log;
	private AStarCourbe pathfinding;
	private CheminPathfinding chemin;

	public ThreadUpdatePathfinding(Log log, AStarCourbe pathfinding, CheminPathfinding chemin)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.chemin = chemin;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de " + Thread.currentThread().getName());
		try
		{
			while(true)
			{
				synchronized(chemin)
				{
					if(chemin.isUptodate())
						chemin.wait();
				}

				// on a été prévenu que le chemin n'est plus à jour
				// : ralentissement et replanification
				try
				{
					if(chemin.needStop())
						throw new PathfindingException("Trajectoire vide");
					Cinematique lastValid = chemin.getLastValidCinematique();
					log.debug("Mise à jour du chemin", Verbose.REPLANIF.masque);
					pathfinding.updatePath(lastValid);
				}
				catch(PathfindingException e)
				{
					log.critical(e);
//					if(!chemin.isEmpty())
//					else
//						log.debug("Robot déjà à l'arrêt", Verbose.REPLANIF.masque);
					chemin.clear();
				}
			}
		}
		catch(InterruptedException e)
		{
			log.debug("Arrêt de " + Thread.currentThread().getName());
			Thread.currentThread().interrupt();
		}
		catch(Exception e)
		{
			log.critical("Exception inattendue dans " + Thread.currentThread().getName() + " : " + e);
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
			try
			{
				while(true)
				{
					synchronized(chemin)
					{
						if(chemin.isUptodate())
							chemin.wait();
					}
					chemin.clear();
				}
			}
			catch(InterruptedException e1)
			{
				log.debug("Arrêt de " + Thread.currentThread().getName() + " après une exception inattendue récupérée");
				Thread.currentThread().interrupt();
			}
		}
	}

}
