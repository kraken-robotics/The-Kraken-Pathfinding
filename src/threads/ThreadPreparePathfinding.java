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

import container.dependances.HighPFClass;
import exceptions.PathfindingException;
import pathfinding.PFInstruction;
import pathfinding.PathCache;
import utils.Log;

/**
 * Thread qui calcule le prochain pathfinding pendant que le robot fait un script
 * @author pf
 *
 */

public class ThreadPreparePathfinding extends ThreadService implements HighPFClass
{
	protected Log log;
	private PathCache pathfinding;
	private PFInstruction inst;

	public ThreadPreparePathfinding(Log log, PathCache pathfinding, PFInstruction inst)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.inst = inst;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			while(true)
			{
				synchronized(inst)
				{
					if(inst.isEmpty())
						inst.wait();					
				}
				try {
					pathfinding.prepareNewPathToScript(inst.getKey());
				} catch (PathfindingException e) {
					// TODO
				}
			}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}

}
