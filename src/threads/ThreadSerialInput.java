package threads;

import enums.SerialProtocol;
import robot.Cinematique;
import robot.RobotReal;
import robot.Speed;
import serie.SerialLowLevel;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import container.Service;
import enums.RobotColor;
import obstacles.IncomingData;
import obstacles.IncomingDataBuffer;

/**
 * Thread qui écoute la série et appelle qui il faut.
 * @author pf
 *
 */

public class ThreadSerialInput extends Thread implements Service
{
	protected Log log;
	protected Config config;
	private SerialLowLevel serie;
	private IncomingDataBuffer buffer;
	private RobotReal robot;
	
	private boolean capteursOn = false;
	private volatile int nbCapteurs;
	private boolean matchDemarre = false;
	private int[] lecture = new int[100];
	private final static int COMMANDE = 0;
	private final static int PARAM = 1;
	
	public ThreadSerialInput(Log log, Config config, SerialLowLevel serie, IncomingDataBuffer buffer, RobotReal robot)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		this.robot = robot;
	}

	@Override
	public void run()
	{
		/**
		 * Initialisation des valeurs de la STM
		 */
		while(true)
		{
			lecture = serie.readData();
			
			if((lecture[COMMANDE] & SerialProtocol.MASK_LAST_BIT.codeInt) == SerialProtocol.IN_INFO_CAPTEURS.codeInt)
			{
				/**
				 * Récupération de la position et de l'orientation
				 */
				int xRobot = lecture[PARAM] << 4;
				xRobot += lecture[PARAM+1] >> 4;
				xRobot -= 1500;
				int yRobot = (lecture[PARAM+1] & 0x0F) << 8;
				yRobot = yRobot + lecture[PARAM+2];
				Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(xRobot, yRobot);

				double orientationRobot = ((lecture[PARAM+3] << 8) + lecture[PARAM+4]) / 1000.;
				double courbure = lecture[PARAM+5] / 1000.;
				boolean enMarcheAvant = lecture[COMMANDE] == SerialProtocol.IN_INFO_CAPTEURS.codeInt;
				double vitesseLineaire = (lecture[PARAM + 6] << 8) + lecture[PARAM + 7];
				double vitesseRotation = (lecture[PARAM + 8] << 8) + lecture[PARAM + 9];
				/**
				 * Acquiert ce que voit les capteurs
			 	 */
				int[] mesures = new int[nbCapteurs];
				for(int i = 0; i < nbCapteurs / 2; i++)
				{
					mesures[2*i] = (lecture[PARAM+10+3*i] << 4) + (lecture[PARAM+10+3*i+1] >> 4);
					mesures[2*i+1] = ((lecture[PARAM+10+3*i+1] & 0x0F) << 8) + lecture[PARAM+10+3*i+2];
				}
				
//						if(Config.debugSerie)
					log.debug("Le robot est en "+positionRobot+", orientation : "+orientationRobot);

				if(Config.debugCapteurs)
				{
					log.debug("droite : "+(mesures[0] == 0 ? "infini" : mesures[0])+", gauche : "+(mesures[1] == 0 ? "infini" : mesures[1]));
				}
				Cinematique c = new Cinematique(positionRobot.x, positionRobot.y, orientationRobot, enMarcheAvant, courbure, vitesseLineaire, vitesseRotation, Speed.STANDARD);
				robot.setCinematique(c);
				if(capteursOn)
					buffer.add(new IncomingData(mesures, c));
			}
			
			/**
			 * La couleur du robot
			 */
			else if((lecture[COMMANDE] & SerialProtocol.MASK_LAST_BIT.codeInt) == SerialProtocol.IN_COULEUR_ROBOT.codeInt)
			{
				if(!matchDemarre)
					config.set(ConfigInfo.COULEUR, RobotColor.getCouleur(lecture[COMMANDE] != SerialProtocol.IN_COULEUR_ROBOT.codeInt));
				else
					log.warning("Le bas niveau a signalé un changement de couleur en plein match");
			}


			/**
			 * Démarrage du match
			 */
			else if(lecture[COMMANDE] == SerialProtocol.IN_DEBUT_MATCH.codeInt)
			{
				capteursOn = true;
				synchronized(config)
				{
					config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
					config.set(ConfigInfo.MATCH_DEMARRE, true);
					matchDemarre = true;
				}
			}
			
			/**
			 * Fin du match, on coupe la série et on arrête ce thread
			 */
			else if(lecture[COMMANDE] == SerialProtocol.IN_MATCH_FINI.codeInt)
			{
				log.debug("Fin du Match !");
				config.set(ConfigInfo.FIN_MATCH, true);
				serie.close();
				return;
			}

			else if(lecture[COMMANDE] == SerialProtocol.IN_ROBOT_ARRIVE.codeInt)
			{
				log.debug("Le robot est arrivé !");
//				requete.set(RequeteType.TRAJET_FINI);
			}

			else
			{
				log.critical("Commande série inconnue: "+lecture[COMMANDE]);
			}
		}
//		log.debug("Fermeture de ThreadSerialInput");
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
	}

}
