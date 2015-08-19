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
	 * Le buffer est-il vide?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return buffer.isEmpty();
	}
	
	/**
	 * Ajout d'un élément dans le buffer et provoque un "notifyAll"
	 * @param elem
	 */
	public synchronized void add(IncomingHook elem)
	{
		buffer.add(elem);
		log.debug("Taille buffer: "+buffer.size());
		notify();
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
