package buffer;

import hook.Hook;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import permissions.ReadOnly;
import robot.ActuatorOrder;
import robot.Speed;
import container.Service;
import utils.Config;
import utils.Log;
import utils.Vec2;

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
	
	private volatile Queue<ArrayList<String>> buffer = new LinkedList<ArrayList<String>>();
	
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
	
	/*
	public synchronized void desactiveAsservissement()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("nas"));
		buffer.add(elems);
		notify();
	}

	public synchronized void activeAsservissement()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("oas"));
		buffer.add(elems);
		notify();
	}*/

	public synchronized void setSpeed(Speed speed)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("sspd"));
		elems.add(new String(Integer.toString(speed.PWMRotation)));
		elems.add(new String(Integer.toString(speed.PWMTranslation)));
		buffer.add(elems);
//		log.debug("Taille buffer: "+buffer.size());
		notify();

	}
	
	public synchronized void getPositionOrientation()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("gxyo"));
		buffer.add(elems);
//		log.debug("Taille buffer: "+buffer.size());
		notify();
	}

	
	public synchronized void setPositionOrientation(Vec2<ReadOnly> pos, double angle)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("sxyo"));
		elems.add(new String(Integer.toString(pos.x)));
		elems.add(new String(Integer.toString(pos.y)));
		elems.add(new String(Long.toString(Math.round(angle*1000))));
		buffer.add(elems);
//		log.debug("Taille buffer: "+buffer.size());
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * TODO à compléter
	 * @param elem
	 */
	public synchronized void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("d"));
		elems.add(new String(Integer.toString(distance)));
		elems.add(new String(Boolean.toString(mur)));
		addHook(elems, hooks);
		buffer.add(elems);
//		log.debug("Taille buffer: "+buffer.size());
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre de tourner pour la série
	 * TODO à compléter
	 * @param elem
	 */
	public synchronized void turn(double angle, ArrayList<Hook> hooks)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("t"));
		elems.add(new String(Long.toString(Math.round(angle*1000))));
		addHook(elems, hooks);
		buffer.add(elems);
//		log.debug("Taille buffer: "+buffer.size());
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre de s'arrêter
	 * TODO à compléter
	 * @param elem
	 */
	public synchronized void immobilise()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("stop"));
		// TODO : faire passer en urgence ?
		buffer.add(elems);
//		log.debug("Taille buffer: "+buffer.size());
		notify();
	}

	
	/**
	 * Ajout d'une demande d'ordre d'actionneurs pour la série
	 * @param elem
	 */
	public synchronized void utiliseActionneurs(ActuatorOrder elem)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(elem.getSerialOrder());
		buffer.add(elems);
//		log.debug("Taille buffer: "+buffer.size());
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
	public synchronized ArrayList<String> poll()
	{
//		log.debug("poll");
		return buffer.poll();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	private void addHook(ArrayList<String> elems, ArrayList<Hook> hooks)
	{
		elems.add(new String(Integer.toString(hooks.size())));
		for(Hook h : hooks)
			elems.addAll(h.toSerial());
	}
}
