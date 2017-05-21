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
import pathfinding.PFInstruction;
import pathfinding.astar.AStarCourbe;
import pathfinding.chemin.FakeCheminPathfinding;
import utils.Log;
import utils.Log.Verbose;

/**
 * Thread qui calcule le prochain pathfinding pendant que le robot fait un
 * script
 * 
 * @author pf
 *
 */

public class ThreadPreparePathfinding extends ThreadService implements HighPFClass
{
	protected Log log;
	private AStarCourbe astar;
	private PFInstruction inst;
	private FakeCheminPathfinding fakeChemin;

	public ThreadPreparePathfinding(Log log, AStarCourbe astar, PFInstruction inst, FakeCheminPathfinding fakeChemin)
	{
		this.fakeChemin = fakeChemin;
		this.log = log;
		this.astar = astar;
		this.inst = inst;
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
				synchronized(inst)
				{
					while(!inst.hasRequest())
						inst.wait();
					log.debug("Préparation du prochain trajet en cours…", Verbose.PF.masque);
					inst.beginSearch();
				}
				try
				{
					fakeChemin.clear();
					astar.process(fakeChemin, false);
					log.debug("Prochain trajet préparé.", Verbose.PF.masque);
					inst.setDone();
				}
				catch(PathfindingException e)
				{
					log.warning("Exception dans la préparation du pathfinding ! " + e, Verbose.PF.masque);
					inst.setException(e);
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
			log.debug("Arrêt inattendu de " + Thread.currentThread().getName() + " : " + e);
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
			Thread.currentThread().interrupt();
		}
	}

}
