package threads;

import java.util.ArrayList;

import buffer.IncomingData;
import buffer.IncomingDataBuffer;
import buffer.IncomingHookBuffer;
import hook.Hook;
import hook.HookFactory;
import permissions.ReadOnly;
import requete.RequeteSTM;
import requete.RequeteType;
import serial.SerialConnexion;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import container.Service;
import enums.RobotColor;

/**
 * Thread qui écoute la série et appelle qui il faut.
 * @author pf
 *
 */

public class ThreadSerial extends ThreadAvecStop implements Service
{
	protected Log log;
	protected Config config;
	private SerialConnexion serie;
	private IncomingDataBuffer buffer;
	private IncomingHookBuffer hookbuffer;
	private HookFactory hookfactory;
	
	private RequeteSTM requete;
	private boolean capteursOn = false;
	private int nbCapteurs;
	
	public ThreadSerial(Log log, Config config, SerialConnexion serie, IncomingDataBuffer buffer, IncomingHookBuffer hookbuffer, HookFactory hookfactory)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		this.hookbuffer = hookbuffer;
		this.hookfactory = hookfactory;
		requete = RequeteSTM.getInstance();
	}

	@Override
	public void run()
	{
		/**
		 * Initialisation des valeurs de la STM
		 */
		// TODO: envoyer hook, etc.
/*		ArrayList<Hook> hooks = hookfactory.getHooksTable();
		for(Hook hook: hooks)
		{
			serie.communiquer("hk");
			serie.communiquer(hook.toSerial());
		}
		serie.communiquer("hkFin");
	*/	
		while(!finThread)
		{
			try {
				/**
				 * La série est monopolisée pendant la lecture du message.
				 * Ainsi, on est assuré qu'un autre thread ne parlera pas pendant qu'on écoute...
				 */
				synchronized(serie)
				{
					while(!serie.canBeRead())
					{
						serie.wait();
					}
					String first = serie.read();
					log.debug(first);
					switch(first)
					{
						case "cpt":
							/**
							 * Acquiert ce que voit les capteurs
						 	 */
							int xRobot = Integer.parseInt(serie.read());
							int yRobot = Integer.parseInt(serie.read());
							double orientationRobot = Double.parseDouble(serie.read());
							int portion = Integer.parseInt(serie.read());
							int[] mesures = new int[nbCapteurs];
							for(int i = 0; i < nbCapteurs; i++)
								mesures[i] = Integer.parseInt(serie.read());

							if(capteursOn)
								buffer.add(new IncomingData(new Vec2<ReadOnly>(xRobot, yRobot), orientationRobot, portion, mesures));
							break;
							
							/**
							 * Récupère la couleur du robot.
							 */
						case "color":
							config.set(ConfigInfo.COULEUR, RobotColor.parse(serie.read()));
							break;
							
						case "go":
							/**
							 * Démarrage du match
							 */
							capteursOn = true;
							synchronized(config)
							{
								config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
								config.set(ConfigInfo.MATCH_DEMARRE, true);
							}
							config.updateConfigServices();
							break;
							
							/**
							 * Fin du match, on coupe la série et on arrête ce thread
							 */
						case "end":
							synchronized(requete)
							{
								requete.type = RequeteType.MATCH_FINI;
								requete.notifyAll();
							}
							serie.close();
							return;
		
							/**
							 * Un hook a été appelé
							 */
						case "hk":
							// TODO: l'exécuter aussi (mise à jour table, robot, etc).
							hookbuffer.add(null);
							break;
							
							/**
							 * On est arrivé à destination.
							 */
						case "arv":
							synchronized(requete)
							{
								requete.type = RequeteType.TRAJET_FINI;
								requete.notifyAll();
							}
							break;
		
						default:
							log.critical("Commande série inconnue: "+first);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{
	}

	@Override
	public void useConfig(Config config)
	{
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
	}

}
