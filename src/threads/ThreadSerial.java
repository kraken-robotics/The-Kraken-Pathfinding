package threads;

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
	
	private RequeteSTM requete;
	private volatile boolean capteursOn;
	
	public ThreadSerial(Log log, Config config, SerialConnexion serie, IncomingDataBuffer buffer)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		requete = RequeteSTM.getInstance();
	}

	@Override
	public void run()
	{
		/**
		 * Initialisation des valeurs de la STM
		 */
		// TODO: envoyer hook, etc.
		
		while(!finThread)
		{
			try {
				/**
				 * La série est monopolisée pendant la lecture du message.
				 * Ainsi, on est assuré qu'un autre thread ne parlera pas pendant qu'on écoute...
				 */
				synchronized(serie)
				{
					serie.wait();
					String first = serie.read();
					switch(first)
					{
						case "obs":
							/**
							 * Un obstacle est vu. Il y a deux positions:
							 * - la position brute, ce que voit vraiment le capteur
							 * - la position supposée de l'obstacle
						 	 */
							int xBrut = Integer.parseInt(serie.read());
							int yBrut = Integer.parseInt(serie.read());
							int xEnnemi = Integer.parseInt(serie.read());
							int yEnnemi = Integer.parseInt(serie.read());
							int portion = Integer.parseInt(serie.read());

							if(capteursOn)
								buffer.add(new IncomingData(new Vec2<ReadOnly>(xBrut, yBrut), new Vec2<ReadOnly>(xEnnemi, yEnnemi), portion));
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
							synchronized(config)
							{
								config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
								config.set(ConfigInfo.CAPTEURS_ON, true);
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
		capteursOn = config.getBoolean(ConfigInfo.CAPTEURS_ON);
	}

	@Override
	public void useConfig(Config config)
	{}

}
