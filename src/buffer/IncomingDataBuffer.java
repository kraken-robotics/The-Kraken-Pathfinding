package buffer;

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

public class IncomingDataBuffer implements Service
{
	protected Log log;
	
	public IncomingDataBuffer(Log log)
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
	
	private volatile Queue<IncomingData> buffer = new LinkedList<IncomingData>();
	
	/**
	 * Ajout d'un élément dans le buffer et provoque un "notifyAll"
	 * @param elem
	 */
	public synchronized void add(IncomingData elem)
	{
		buffer.add(elem);
//		log.debug("Taille buffer: "+buffer.size());
		synchronized(this)
		{
			notifyAll();
		}
	}
	
	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized IncomingData poll()
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
