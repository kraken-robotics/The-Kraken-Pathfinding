package threads;

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
	protected Config config;
	protected Log log;
	
	public IncomingDataBuffer(Log log, Config config)
	{
		this.log = log;
		this.config = config;
	}
	
	private Queue<IncomingData> buffer = new LinkedList<IncomingData>();
	
	/**
	 * Ajout d'un élément dans le buffer et provoque un "notifyAll"
	 * @param elem
	 */
	public synchronized void add(IncomingData elem)
	{
		buffer.add(elem);
		log.debug("Taille buffer: "+buffer.size());
		synchronized(this)
		{
			notifyAll();
		}
	}
	
	public void notifyIfNecessary()
	{
		if(buffer.size() > 0)
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
	public void updateConfig()
	{}
	
}
