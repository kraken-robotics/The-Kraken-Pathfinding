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
	
	public DataForSerialOutput(Log log)
	{
		this.log = log;
	}
		
	// priorité 0 = priorité minimale
	private volatile LinkedList<String> bufferBassePriorite = new LinkedList<String>();
	private volatile LinkedList<String> bufferTrajectoireCourbe = new LinkedList<String>();
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
	public synchronized String poll()
	{
		if(stop)
		{
			stop = false;
			bufferTrajectoireCourbe.clear(); // on annule tout mouvement
			return "stop";
		}
		else if(!bufferTrajectoireCourbe.isEmpty())
			return bufferTrajectoireCourbe.poll();
		return bufferBassePriorite.poll();
	}
	
	public synchronized void setSpeed(Speed speed)
	{
		bufferBassePriorite.add("sspd "+speed.PWMRotation+" "+speed.PWMTranslation);
		notify();

	}
	
	public synchronized void getPositionOrientation()
	{
		bufferBassePriorite.add("gxyo");
		notify();
	}
	
	public synchronized void initOdoSTM(Vec2<ReadOnly> pos, double angle)
	{
		bufferBassePriorite.add("initodo "+pos.x+" "+pos.y+" "+Math.round(angle*1000));
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * @param elem
	 */
	public synchronized void avancer(int distance, boolean mur)
	{
		String elems = "d "+distance;
		if(mur)
			elems += " T";
		else
			elems += " F";
		bufferBassePriorite.add(elems);
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre de tourner pour la série
	 * @param elem
	 */
	public synchronized void turn(double angle)
	{
		bufferBassePriorite.add("t "+Math.round(angle*1000));
		notify();
	}

	public synchronized void envoieHooks(ArrayList<Hook> hooks)
	{
		if(hooks.isEmpty())
			return;

		for(Hook h : hooks)
			bufferBassePriorite.add(h.toSerial());
		
		notify();
	}

	public synchronized void deleteHooks(ArrayList<Hook> hooks)
	{
		if(hooks.isEmpty())
			return;
		String out = "hkclr "+hooks.size();
		for(Hook h : hooks)
			out += " "+h.getNum();
		
		bufferBassePriorite.add(out);
		notify();
	}
	
	public synchronized void deleteAllHooks()
	{
		bufferBassePriorite.add("hkclrall");
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
		bufferBassePriorite.add("act "+elem.ordinal());
		notify();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	/**
	 * Informe la STM du protocole des actionneurs
	 */
/*	public synchronized void envoieActionneurs()
	{
		ArrayList<String> elems = new ArrayList<String>();
		elems.add("alst");
		elems.add(String.valueOf(ActuatorOrder.values().length));
		for(ActuatorOrder a : ActuatorOrder.values())
		{
			elems.add(a.getOrdreSSC32());
			elems.add(String.valueOf(a.hasSymmetry()));
		}
		bufferBassePriorite.add(elems);
		notify();
	}*/
	
	public synchronized void envoieArcCourbeLast(ArcCourbe arc)
	{
/*		ArrayList<String> elems = new ArrayList<String>();
		elems.add("addl");
		elems.add(String.valueOf(arc.pointDepart.x));
		elems.add(String.valueOf(arc.pointDepart.y));
		Vec2<ReadOnly> direction = new Vec2<ReadOnly>(arc.theta);
		elems.add(String.valueOf(direction.x));
		elems.add(String.valueOf(direction.y));
		elems.add(String.valueOf(arc.courbure));
		elems.add(String.valueOf(arc.theta));
		bufferTrajectoireCourbe.add(elems);*/
		notify();		
	}

	public synchronized void envoieArcCourbe(ArcCourbe arc)
	{
/*		ArrayList<String> elems = new ArrayList<String>();
		elems.add("add");
		elems.add(String.valueOf(arc.pointDepart.x));
		elems.add(String.valueOf(arc.pointDepart.y));
		Vec2<ReadOnly> direction = new Vec2<ReadOnly>(arc.theta);
		elems.add(String.valueOf(direction.x));
		elems.add(String.valueOf(direction.y));
		elems.add(String.valueOf(arc.courbure));
		elems.add(String.valueOf(arc.theta));
		bufferTrajectoireCourbe.add(elems);*/
		notify();			
	}
}
