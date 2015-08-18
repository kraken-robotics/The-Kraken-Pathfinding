package threads;

import buffer.IncomingData;
import buffer.IncomingDataBuffer;
import buffer.IncomingHookBuffer;
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

public class ThreadSerialInput extends Thread implements Service
{
	protected Log log;
	protected Config config;
	private SerialConnexion serie;
	private IncomingDataBuffer buffer;
	private IncomingHookBuffer hookbuffer;
	
	private RequeteSTM requete;
	private boolean capteursOn = false;
	private int nbCapteurs;
	
	public ThreadSerialInput(Log log, Config config, SerialConnexion serie, IncomingDataBuffer buffer, IncomingHookBuffer hookbuffer, RequeteSTM requete)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		this.hookbuffer = hookbuffer;
		this.requete = requete;
	}

	@Override
	public void run()
	{
		/**
		 * Initialisation des valeurs de la STM
		 */
		while(true)
		{
			try {
				/**
				 * La série est monopolisée pendant la lecture du message.
				 * Ainsi, on est assuré qu'un autre thread ne parlera pas pendant qu'on écoute...
				 */
				synchronized(serie)
				{
					while(!serie.canBeRead())
						serie.wait();

					String first = serie.read();

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

							buffer.add(new IncomingData(new Vec2<ReadOnly>(xRobot, yRobot), orientationRobot, portion, mesures, capteursOn));
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
							requete.set(RequeteType.MATCH_FINI);
							serie.close();
							return;

							/**
							 * Un actionneur est en difficulté
							 */
						case "acpb":
								requete.set(RequeteType.PROBLEME_ACTIONNEURS);
							break;

							/**
							 * Un actionneur a fini son mouvement
							 */
						case "acok":
							requete.set(RequeteType.ACTIONNEURS_FINI);
							break;

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
							requete.set(RequeteType.TRAJET_FINI);
							break;
		
							/**
							 * Il y a un blocage mécanique
							 */
						case "meca":
							requete.set(RequeteType.BLOCAGE_MECANIQUE);
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
//		log.debug("Fermeture de ThreadSerialInput");
	}
	
	@Override
	public void updateConfig(Config config)
	{
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
	}

	@Override
	public void useConfig(Config config)
	{}

}
