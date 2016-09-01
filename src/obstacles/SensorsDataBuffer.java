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
