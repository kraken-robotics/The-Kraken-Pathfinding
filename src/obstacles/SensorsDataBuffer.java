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

package obstacles;

import java.util.LinkedList;
import java.util.Queue;

import utils.Config;
import utils.Log;
import container.Service;

/**
 * Buffer qui contient les infos provenant des capteurs de la STM
 * @author pf
 *
 */

public class SensorsDataBuffer implements Service
{
	protected Log log;
	
	public SensorsDataBuffer(Log log)
	{
		this.log = log;
	}
	
	/**
	 * Le buffer est-il vide?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return buffer.isEmpty();
	}
	
	private volatile Queue<SensorsData> buffer = new LinkedList<SensorsData>();
	
	/**
	 * Ajout d'un élément dans le buffer et provoque un "notifyAll"
	 * @param elem
	 */
	public synchronized void add(SensorsData elem)
	{
		buffer.add(elem);
		if(buffer.size() > 5)
		{
			buffer.poll(); // on évacue une ancienne valeur
//			log.debug("Capteurs traités trop lentement");
		}
//		log.debug("Taille buffer: "+buffer.size());
		notify();
	}
	
	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized SensorsData poll()
	{
		return buffer.poll();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
