/*
Copyright (C) 2016 Pierre-François Gimenez

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

import graphic.PrintBuffer;
import obstacles.memory.ObstaclesMemory;
import pathfinding.dstarlite.DStarLite;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;

/**
 * Thread qui gère la péremption des obstacles en dormant
 * le temps exact entre deux péremptions.
 * @author pf
 *
 */

public class ThreadPeremption extends ThreadService
{
	private ObstaclesMemory memory;
	protected Log log;
	private PrintBuffer buffer;
	private DStarLite dstarlite;
	
	private int dureePeremption;
	private boolean printProxObs;
	
	public ThreadPeremption(Log log, ObstaclesMemory memory, PrintBuffer buffer, DStarLite dstarlite)
	{
		this.log = log;
		this.memory = memory;
		this.buffer = buffer;
		this.dstarlite = dstarlite;
	}
	
	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			while(true)
			{
				if(memory.deleteOldObstacles())
					dstarlite.updateObstacles();
	
				// mise à jour des obstacles : on réaffiche
				if(printProxObs)
					synchronized(buffer)
					{
						buffer.notify();
					}
				
				long prochain = memory.getNextDeathDate();
				
				/**
				 * S'il n'y a pas d'obstacles, on dort de dureePeremption, qui est la durée minimale avant la prochaine péremption.
				 */
				if(prochain == Long.MAX_VALUE)
					Thread.sleep(dureePeremption);
				else
					// Il faut toujours s'assurer qu'on dorme un temps positif. Il y a aussi une petite marge
					Thread.sleep(Math.min(dureePeremption, Math.max(prochain - System.currentTimeMillis() + 2, 5)));
				}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		printProxObs = config.getBoolean(ConfigInfo.GRAPHIC_PROXIMITY_OBSTACLES);
		dureePeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
	}

}
