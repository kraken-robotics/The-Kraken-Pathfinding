package buffer;

import hook.Hook;

import java.util.ArrayList;
import java.util.PriorityQueue;

import pathfinding.astar_courbe.ArcCourbe;
import pathfinding.thetastar.VitesseCourbure;
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
//	private GridSpace gridspace;
	
	public DataForSerialOutput(Log log)
	{
		this.log = log;
//		this.gridspace = gridspace;
	}
	
	// TODO : passer en priorityqueue
	private volatile PriorityQueue<SerialOutput> buffer = new PriorityQueue<SerialOutput>();
	
	/**
	 * Le buffer est-il vide?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return buffer.isEmpty();
	}
	
	public synchronized void setSpeed(Speed speed)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("sspd"));
		elems.add(new String(Integer.toString(speed.PWMRotation)));
		elems.add(new String(Integer.toString(speed.PWMTranslation)));
		buffer.add(new SerialOutput(elems,0));
//		log.debug("Taille buffer: "+buffer.size());
		notify();

	}
	
	public synchronized void getPositionOrientation()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("gxyo"));
		buffer.add(new SerialOutput(elems,0));
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
		buffer.add(new SerialOutput(elems,0));
//		log.debug("Taille buffer: "+buffer.size());
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * @param elem
	 */
	public synchronized void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("d"));
		elems.add(new String(Integer.toString(distance)));
		if(mur)
			elems.add("T");
		else
			elems.add("F");
		addHooks(elems, hooks);
		buffer.add(new SerialOutput(elems,0));
//		log.debug("Taille buffer: "+buffer.size());
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre de tourner pour la série
	 * @param elem
	 */
	public synchronized void turn(double angle)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("t"));
		elems.add(new String(Long.toString(Math.round(angle*1000))));
//		addHook(elems, hooks);
		buffer.add(new SerialOutput(elems,0));
//		log.debug("Taille buffer: "+buffer.size());
		notify();
	}

	public synchronized void envoieHooks(ArrayList<Hook> hooks)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("hlst"));
		addHooks(elems, hooks);
		buffer.add(new SerialOutput(elems,0));
		notify();
	}

	
	/**
	 * Ajout d'une demande d'ordre de s'arrêter
	 */
	public synchronized void immobilise()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("stop"));
		buffer.add(new SerialOutput(elems,2));
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
		elems.add("act");
		elems.add(String.valueOf(elem.ordinal()));
		buffer.add(new SerialOutput(elems,0));
		notify();
	}
/*	
	public void notifyIfNecessary()
	{
		if(buffer.size() > 0)
			synchronized(this)
			{
				notifyAll();
			}
	}*/

	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized ArrayList<String> poll()
	{
//		log.debug("poll");
		return buffer.poll().output;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	private void addHooks(ArrayList<String> elems, ArrayList<Hook> hooks)
	{
		elems.add(new String(Integer.toString(hooks.size())));
		for(Hook h : hooks)
			elems.addAll(h.toSerial());
	}

	/**
	 * Informe la STM du protocole des actionneurs
	 */
	public synchronized void envoieActionneurs()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("alst"));
		elems.add(String.valueOf(ActuatorOrder.values().length));
		for(ActuatorOrder a : ActuatorOrder.values())
		{
			elems.add(a.getOrdreSSC32());
			elems.add(String.valueOf(a.hasSymmetry()));
		}
		buffer.add(new SerialOutput(elems,0));
		notify();
	}

	/**
	 * Informe la STM des rayons de courbure
	 */
	public synchronized void envoieRayonsCourbure()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("src"));
		elems.add(String.valueOf(VitesseCourbure.values().length));

		for(VitesseCourbure r : VitesseCourbure.values())
		{
			elems.add(String.valueOf(r.rayon));
			elems.add(String.valueOf(r.PWMTranslation));
		}
			
		buffer.add(new SerialOutput(elems,0));
		notify();		
	}
	
	public synchronized void envoieArcCourbeLast(ArcCourbe arc)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("addl"));
		elems.add(String.valueOf(arc.vitesseCourbure));
		elems.add(String.valueOf(arc.destination.x));
		elems.add(String.valueOf(arc.destination.y));
		buffer.add(new SerialOutput(elems,1));
		notify();		
	}

	public synchronized void envoieArcCourbe(ArcCourbe arc)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("add"));
		elems.add(String.valueOf(arc.vitesseCourbure));
		buffer.add(new SerialOutput(elems,1));
		notify();		
	}
}
