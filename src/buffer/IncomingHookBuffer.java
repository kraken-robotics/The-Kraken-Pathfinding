package buffer;

import java.util.LinkedList;
import java.util.Queue;

import container.Service;
import utils.Config;
import utils.Log;

/**
 * Un buffer qui contient les hooks appelés bas niveau qu'il faut maintenant appeler haut niveau.
 * @author pf
 *
 */

public class IncomingHookBuffer implements Service
{
	protected Log log;
	
	public IncomingHookBuffer(Log log)
	{
		this.log = log;
	}
	
	private volatile Queue<IncomingHook> buffer = new LinkedList<IncomingHook>();
	
	/**
	 * Ajout d'un élément dans le buffer et provoque un "notifyAll"
	 * @param elem
	 */
	public synchronized void add(IncomingHook elem)
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
	public synchronized IncomingHook poll()
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
