package buffer;

import hook.Hook;

import java.util.ArrayList;
import java.util.LinkedList;

import pathfinding.astarCourbe.ArcCourbe;
import permissions.ReadOnly;
import robot.ActuatorOrder;
import robot.Speed;
import container.Service;
import utils.Config;
import utils.Log;
import utils.Vec2;

/**
 * Classe qui contient les ordres à envoyer à la série
 * Il y a trois priorité
 * - la plus haute, l'arrêt
 * - ensuite, la trajectoire courbe
 * - enfin, tout le reste
 * @author pf
 *
 */

public class DataForSerialOutput implements Service
{
	protected Log log;
	private ArrayList<String> ordreStop;
	
	public DataForSerialOutput(Log log)
	{
		this.log = log;
		ordreStop = new ArrayList<String>();
		ordreStop.add(new String("stop"));
	}
		
	// priorité 0 = priorité minimale
	private volatile LinkedList<ArrayList<String>> bufferBassePriorite = new LinkedList<ArrayList<String>>();
	private volatile LinkedList<ArrayList<String>> bufferTrajectoireCourbe = new LinkedList<ArrayList<String>>();
	private volatile boolean stop = false;
	
	/**
	 * Le buffer est-il vide?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return bufferBassePriorite.isEmpty() && bufferTrajectoireCourbe.isEmpty() && !stop;
	}

	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized ArrayList<String> poll()
	{
		if(stop)
		{
			stop = false;
			bufferTrajectoireCourbe.clear(); // on annule tout mouvement
			return ordreStop;
		}
		else if(!bufferTrajectoireCourbe.isEmpty())
			return bufferTrajectoireCourbe.poll();
		return bufferBassePriorite.poll();
	}
	
	public synchronized void setSpeed(Speed speed)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("sspd"));
		elems.add(new String(Integer.toString(speed.PWMRotation)));
		elems.add(new String(Integer.toString(speed.PWMTranslation)));
		bufferBassePriorite.add(elems);
		notify();

	}
	
	public synchronized void getPositionOrientation()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("gxyo"));
		bufferBassePriorite.add(elems);
		notify();
	}

	
	public synchronized void setPositionOrientation(Vec2<ReadOnly> pos, double angle)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("sxyo"));
		elems.add(new String(Integer.toString(pos.x)));
		elems.add(new String(Integer.toString(pos.y)));
		elems.add(new String(Long.toString(Math.round(angle*1000))));
		bufferBassePriorite.add(elems);
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
		bufferBassePriorite.add(elems);
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
		bufferBassePriorite.add(elems);
		notify();
	}

	public synchronized void envoieHooks(ArrayList<Hook> hooks)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("hlst"));
		addHooks(elems, hooks);
		bufferBassePriorite.add(elems);
		notify();
	}

	
	/**
	 * Ajout d'une demande d'ordre de s'arrêter
	 */
	public synchronized void immobilise()
	{
		stop = true;
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
		bufferBassePriorite.add(elems);
		notify();
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
		bufferBassePriorite.add(elems);
		notify();
	}
	
	public synchronized void envoieArcCourbeLast(ArcCourbe arc)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("addl"));
		elems.add(String.valueOf(arc.vitesseCourbure));
		elems.add(String.valueOf(arc.destination.x));
		elems.add(String.valueOf(arc.destination.y));
		bufferTrajectoireCourbe.add(elems);
		notify();		
	}

	public synchronized void envoieArcCourbe(ArcCourbe arc)
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add(new String("add"));
		elems.add(String.valueOf(arc.vitesseCourbure));
		bufferTrajectoireCourbe.add(elems);
		notify();		
	}
}
