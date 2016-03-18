package buffer;

import hook.Hook;

import java.util.ArrayList;
import java.util.LinkedList;

import pathfinding.astarCourbe.ArcCourbe;
import permissions.ReadOnly;
import robot.ActuatorOrder;
import robot.Speed;
import container.Service;
import enums.SerialProtocol;
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
		
	private int nbPaquet = 0; // numéro du prochain paquet
	private static final int NB_BUFFER_SAUVEGARDE = 50; // on a de la place de toute façon…
	private volatile byte[][] derniersEnvois = new byte[NB_BUFFER_SAUVEGARDE][];
	private volatile boolean[] derniersEnvoisPriority = new boolean[NB_BUFFER_SAUVEGARDE];
	
	// priorité 0 = priorité minimale
	private volatile LinkedList<byte[]> bufferBassePriorite = new LinkedList<byte[]>();
	private volatile LinkedList<byte[]> bufferTrajectoireCourbe = new LinkedList<byte[]>();
	private volatile boolean stop = false;
	
	private final static int ID_FORT = 0;
	private final static int ID_FAIBLE = 1;
	private final static int COMMANDE = 2;
	private final static int PARAM = 3;

	/**
	 * Le buffer est-il vide?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return bufferBassePriorite.isEmpty() && bufferTrajectoireCourbe.isEmpty() && !stop;
	}

	private synchronized void completePaquet(byte[] out)
	{
		out[ID_FORT] = (byte) ((nbPaquet>>8) & 0xFF);
		out[ID_FAIBLE] = (byte) (nbPaquet & 0xFF);
		nbPaquet++;
	}
	
	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized byte[] poll()
	{
		byte[] out;
		if(stop)
		{
			stop = false;
			bufferTrajectoireCourbe.clear(); // on annule tout mouvement
			out = new byte[2+1];
			out[COMMANDE] = SerialProtocol.OUT_STOP.code;
			completePaquet(out);
			return out;
		}
		else if(!bufferTrajectoireCourbe.isEmpty())
		{
			out = bufferTrajectoireCourbe.poll();
			derniersEnvois[nbPaquet % NB_BUFFER_SAUVEGARDE] = out;
			derniersEnvoisPriority[nbPaquet % NB_BUFFER_SAUVEGARDE] = true;
			completePaquet(out);
			return out;
		}
		else
		{
			out = bufferBassePriorite.poll();
			derniersEnvois[nbPaquet % NB_BUFFER_SAUVEGARDE] = out;
			derniersEnvoisPriority[nbPaquet % NB_BUFFER_SAUVEGARDE] = false;
			completePaquet(out);
			return out;
		}
	}
	
	/**
	 * Réenvoie un paquet à partir de son id
	 * Comme on ne conserve pas tous les précédents paquets, on n'est pas sûr de l'avoir encore…
	 * La priorité est rétablie et le message est envoyé aussi tôt que possible afin de bousculer le moins possible l'ordre
	 * @param id
	 */
	public synchronized void resend(int id)
	{
		if(id <= nbPaquet - NB_BUFFER_SAUVEGARDE)
			log.critical("Réenvoie de message impossible : message trop vieux");
		else if(id >= nbPaquet)
			log.critical("Réenvoie de message impossible : message demandé pas encore envoyé");
		else
		{
			if(derniersEnvoisPriority[id % NB_BUFFER_SAUVEGARDE]) // on redonne la bonne priorité
				bufferTrajectoireCourbe.addFirst(derniersEnvois[id % NB_BUFFER_SAUVEGARDE]);
			else
				bufferBassePriorite.addFirst(derniersEnvois[id % NB_BUFFER_SAUVEGARDE]);
			notify();
		}
	}
	
	public synchronized void askResend(int id)
	{
		byte[] out = new byte[2+3];
		out[COMMANDE] = SerialProtocol.OUT_RESEND_PACKET.code;
		out[PARAM] = (byte) (id >> 8);
		out[PARAM+1] = (byte) (id & 0xFF);
		bufferBassePriorite.addFirst(out);
		notify();
	}
	
	public synchronized void setSpeed(Speed speed)
	{
		byte[] out = new byte[2+3];
		out[COMMANDE] = SerialProtocol.OUT_SET_VITESSE.code;
		out[PARAM] = (byte) speed.PWMRotation;
		out[PARAM+1] = (byte) speed.PWMTranslation;
		bufferBassePriorite.add(out);
		notify();
	}
	
	public synchronized void getPositionOrientation()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_GET_XYO.code;
		bufferBassePriorite.add(out);
		notify();
	}
	
	public synchronized void initOdoSTM(Vec2<ReadOnly> pos, double angle)
	{
		byte[] out = new byte[2+6];
		out[COMMANDE] = SerialProtocol.OUT_INIT_ODO.code;
		out[PARAM] = (byte) ((pos.x+1500) >> 4);
		out[PARAM+1] = (byte) (((pos.x+1500) << 4) + (pos.y >> 8));
		out[PARAM+2] = (byte) (pos.y);
		out[PARAM+3] = (byte) (Math.round(angle*1000) >> 8);
		out[PARAM+4] = (byte) (Math.round(angle*1000) & 0xFF);
		bufferBassePriorite.add(out);
		notify();
	}

	public synchronized void vaAuPoint(Vec2<ReadOnly> pos)
	{
		byte[] out = new byte[2+4];
		out[COMMANDE] = SerialProtocol.OUT_VA_AU_POINT.code;
		out[PARAM] = (byte) ((pos.x+1500) >> 4);
		out[PARAM+1] = (byte) (((pos.x+1500) << 4) + (pos.y >> 8));
		out[PARAM+2] = (byte) (pos.y);
		bufferBassePriorite.add(out);
		notify();
	}
	
	public synchronized void setPIDconstVitesseGauche(double kp, double kd)
	{
		setPIDconst(SerialProtocol.OUT_PID_CONST_VIT_GAUCHE.code, kp, kd);
	}

	public synchronized void setPIDconstVitesseDroite(double kp, double kd)
	{
		setPIDconst(SerialProtocol.OUT_PID_CONST_VIT_DROITE.code, kp, kd);
	}
	
	public synchronized void setPIDconstTranslation(double kp, double kd)
	{
		setPIDconst(SerialProtocol.OUT_PID_CONST_TRANSLATION.code, kp, kd);
	}
	
	public synchronized void setPIDconstRotation(double kp, double kd)
	{
		setPIDconst(SerialProtocol.OUT_PID_CONST_ROTATION.code, kp, kd);
	}
	
	public synchronized void setPIDconstCourbure(double kp, double kd)
	{
		setPIDconst(SerialProtocol.OUT_PID_CONST_COURBURE.code, kp, kd);
	}
	
	public synchronized void setPIDconstVitesseLineaire(double kp, double kd)
	{
		setPIDconst(SerialProtocol.OUT_PID_CONST_VIT_LINEAIRE.code, kp, kd);
	}
	
	private synchronized void setPIDconst(byte code, double kp, double kd)
	{
		byte[] out = new byte[2+6];
		out[COMMANDE] = code;
		out[PARAM] = (byte) ((int)(kp*200) >> 8);
		out[PARAM+1] = (byte) ((int)(kp*200) & 0xFF);
		out[PARAM+2] = (byte) ((int)(kd*200) >> 8);
		out[PARAM+3] = (byte) ((int)(kd*200) & 0xFF);
		bufferBassePriorite.add(out);
		notify();
	}
	
	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * @param elem
	 */
	public synchronized void avancer(int distance, boolean mur)
	{
		byte[] out = new byte[2+3];
		if(mur)
			out[COMMANDE] = SerialProtocol.OUT_AVANCER_DANS_MUR.code;
		else
			out[COMMANDE] = SerialProtocol.OUT_AVANCER.code;
		out[PARAM] = (byte) (distance >> 8);
		out[PARAM+1] = (byte) (distance);
		bufferBassePriorite.add(out);
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre de tourner pour la série
	 * @param elem
	 */
	public synchronized void turn(double angle)
	{
		byte[] out = new byte[2+3];
		out[COMMANDE] = SerialProtocol.OUT_TOURNER.code;
		out[PARAM] = (byte) (Math.round(angle*1000) >> 8);
		out[PARAM+1] = (byte) (Math.round(angle*1000));
		bufferBassePriorite.add(out);
		notify();
	}

	public synchronized void envoieHooks(ArrayList<Hook> hooks)
	{
		if(hooks.isEmpty())
			return;

		for(Hook h : hooks)
		{
			ArrayList<Byte> list = h.toSerial();
			byte[] out = new byte[list.size()+3];
		    for (int i = 0; i < list.size(); i++)
		    {
		        out[i+2] = list.get(i).byteValue();
		    }
			bufferBassePriorite.add(out);
		}
		
		notify();
	}

	public synchronized void deleteHooks(ArrayList<Hook> hooks)
	{
		if(hooks.isEmpty())
			return;
		
		int size = hooks.size();
		byte[] out = new byte[2+2+size];
		out[COMMANDE] = SerialProtocol.OUT_REMOVE_SOME_HOOKS.code;
		out[PARAM] = (byte) (size);
		for(int i = 0; i < size; i++)
			out[4+i] = (byte) (hooks.get(i).getNum());
		bufferBassePriorite.add(out);
		notify();
	}
	
	public synchronized void deleteAllHooks()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_REMOVE_ALL_HOOKS.code;
		bufferBassePriorite.add(out);
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
		byte[] out = new byte[2+2];
		out[COMMANDE] = SerialProtocol.OUT_ACTIONNEUR.code;
		out[PARAM] = (byte) (0); // TODO
		bufferBassePriorite.add(out);
		notify();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	/**
	 * Envoi de tous les arcs élémentaires d'un arc courbe
	 * @param arc
	 */
	public synchronized void envoieArcCourbe(ArcCourbe arc)
	{
		for(int i = 0; i < arc.arcselems.length; i++)
		{
			byte[] out = new byte[2+1];
			if(arc.marcheAvant)
				out[COMMANDE] = SerialProtocol.OUT_SEND_ARC_MARCHE_AVANT.code;
			else
				out[COMMANDE] = SerialProtocol.OUT_SEND_ARC_MARCHE_ARRIERE.code;
			out[PARAM] = (byte) ((arc.arcselems[i].point.x+1500) >> 4);
			out[PARAM+1] = (byte) (((arc.arcselems[i].point.x+1500) << 4) + (arc.arcselems[i].point.y >> 8));
			out[PARAM+2] = (byte) (arc.arcselems[i].point.y);
			out[PARAM+3] = (byte) (Math.round(arc.arcselems[i].theta*1000) >> 8);
			out[PARAM+4] = (byte) (Math.round(arc.arcselems[i].theta*1000));
			out[PARAM+5] = (byte) (Math.round(arc.arcselems[i].courbure*1000) >> 8);
			out[PARAM+6] = (byte) (Math.round(arc.arcselems[i].courbure*1000));
			out[PARAM+7] = (byte) (Math.round(arc.vitesse));
			
			bufferBassePriorite.add(out);
		}
		notify();			
	}

	public synchronized void sendPong()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_PONG1.code;
		out[COMMANDE+1] = SerialProtocol.OUT_PONG2.code;
		bufferBassePriorite.add(out);
		notify();
	}

	public synchronized void addPing()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_PING.code;
		bufferBassePriorite.add(out);
	}

}
