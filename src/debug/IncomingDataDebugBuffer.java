package debug;

import java.util.LinkedList;
import java.util.Queue;

import container.Service;
import utils.Config;
import utils.Log;

/**
 * Buffer des données de debug de l'asser
 * @author pf
 *
 */

public class IncomingDataDebugBuffer implements Service
{

	protected Log log;
	
	public IncomingDataDebugBuffer(Log log)
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
	
	private volatile Queue<IncomingDataDebug> buffer = new LinkedList<IncomingDataDebug>();
	
	/**
	 * Ajout d'un élément dans le buffer et provoque un "notifyAll"
	 * @param elem
	 */
	public synchronized void add(IncomingDataDebug elem)
	{
		buffer.add(elem);
		notify();
	}
	
	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized IncomingDataDebug poll()
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
