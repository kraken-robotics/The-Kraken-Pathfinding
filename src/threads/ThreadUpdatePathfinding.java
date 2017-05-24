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
import robot.Speed;
import serie.BufferOutgoingOrder;
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
	private BufferOutgoingOrder out;

	public ThreadUpdatePathfinding(Log log, AStarCourbe pathfinding, CheminPathfinding chemin, BufferOutgoingOrder out)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.chemin = chemin;
		this.out = out;
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
					out.setMaxSpeed(Speed.REPLANIF, chemin.getCurrentMarcheAvant());
					log.debug("Mise à jour du chemin", Verbose.REPLANIF.masque);
					pathfinding.updatePath(lastValid);
					out.setMaxSpeed(Speed.STANDARD, chemin.getCurrentMarcheAvant());
				}
				catch(PathfindingException e)
				{
					log.critical(e);
//					if(!chemin.isEmpty())
					out.immobilise();
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
					out.immobilise();
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
