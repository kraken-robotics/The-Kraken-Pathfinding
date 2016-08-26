package threads;

import enums.SerialProtocol;
import robot.Cinematique;
import robot.RobotReal;
import robot.Speed;
import serie.BufferIncomingOrder;
import serie.Ticket;
import serie.trame.Paquet;
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

public class ThreadSerialInputCoucheOrdre extends Thread implements Service
{
	protected Log log;
	protected Config config;
	private BufferIncomingOrder serie;
	private IncomingDataBuffer buffer;
	private RobotReal robot;
	
	private boolean capteursOn = false;
	private volatile int nbCapteurs;
	private boolean matchDemarre = false;
	private Paquet paquet;
	private final static int COMMANDE = 0;
	private final static int PARAM = 1;
	
	public ThreadSerialInputCoucheOrdre(Log log, Config config, BufferIncomingOrder serie, IncomingDataBuffer buffer, RobotReal robot)
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
		while(true)
		{
			try {
				synchronized(serie)
				{
					if(!serie.isEmpty())
						paquet = serie.poll();
					else
					{
						serie.wait();
						paquet = serie.poll();
					}
					int[] lecture = paquet.message;
					
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
						
						if(Config.debugSerie)
							log.debug("Le robot est en "+positionRobot+", orientation : "+orientationRobot);
		
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
						{
							if(lecture[COMMANDE] == SerialProtocol.IN_COULEUR_ROBOT_INCONNU.codeInt)
								paquet.ticket.set(Ticket.State.KO);
							else
							{
								paquet.ticket.set(Ticket.State.OK);
								config.set(ConfigInfo.COULEUR, RobotColor.getCouleur(lecture[COMMANDE] != SerialProtocol.IN_COULEUR_ROBOT.codeInt));
							}
						}
						else
							log.warning("Le bas niveau a signalé un changement de couleur en plein match : "+lecture[COMMANDE]);
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
							paquet.ticket.set(Ticket.State.OK);
						}
					}
					
					/**
					 * Fin du match, on coupe la série et on arrête ce thread
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_MATCH_FINI.codeInt)
					{
						log.debug("Fin du Match !");
						config.set(ConfigInfo.FIN_MATCH, true);
						return;
					}
		
					/**
					 * Le robot est arrivé après un arrêt demandé par le haut niveau
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_ROBOT_ARRIVE_APRES_ARRET.codeInt)
						paquet.ticket.set(Ticket.State.OK);

					/**
					 * Le robot est arrivé
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_ROBOT_ARRIVE.codeInt)
						paquet.ticket.set(Ticket.State.OK);

					/**
					 * Le robot a rencontré un problème
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_PB_DEPLACEMENT.codeInt)
					{
						log.warning("Le robot a recontré un problème !");
						paquet.ticket.set(Ticket.State.KO);
					}
					
					else
					{
						log.critical("Commande série inconnue: "+lecture[COMMANDE]);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
