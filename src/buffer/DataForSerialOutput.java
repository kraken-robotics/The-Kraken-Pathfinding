package buffer;

import java.util.LinkedList;
import java.util.Queue;

import robot.ActuatorOrder;
import container.Service;
import utils.Config;
import utils.Log;

/**
 * Classe qui contient les ordres à envoyer à la série
 * @author pf
 *
 */

public class DataForSerialOutput implements Service
{
	protected Log log;
	
	public DataForSerialOutput(Log log)
	{
		this.log = log;
	}
	
	private volatile Queue<String[]> buffer = new LinkedList<String[]>();
	
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
/*	public synchronized void add(String[] elem)
	{
		buffer.add(elem);
		log.debug("Taille buffer: "+buffer.size());
		synchronized(this)
		{
			notifyAll();
		}
	}*/
	
	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * TODO à compléter
	 * @param elem
	 */
	public synchronized void addAvance(int distance)
	{
		String[] elems = new String[1];
		// ...
		buffer.add(elems);
		log.debug("Taille buffer: "+buffer.size());
		notify();
	}
	
	/**
	 * Ajout d'une demande d'ordre d'actionneurs pour la série
	 * @param elem
	 */
	public synchronized void add(ActuatorOrder elem)
	{
		String[] elems = new String[1];
		elems[0] = elem.getSerialOrder();
		buffer.add(elems);
		log.debug("Taille buffer: "+buffer.size());
		notify();
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
	public synchronized String[] poll()
	{
		log.debug("poll");
		return buffer.poll();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
}
