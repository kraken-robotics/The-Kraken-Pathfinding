package threads;

import enums.SerialProtocol.InOrder;
import enums.SerialProtocol.OutOrder;
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
					if(serie.isEmpty())
						serie.wait();

					Paquet paquet = serie.poll();
					int[] data = paquet.message;
					
					/**
					 * Couleur du robot
					 */
					if(paquet.origine == OutOrder.ASK_COLOR)
					{
						if(!matchDemarre)
						{
							if(data[0] == InOrder.COULEUR_ROBOT_DROITE.codeInt || data[0] == InOrder.COULEUR_ROBOT_GAUCHE.codeInt)
							{
								paquet.ticket.set(Ticket.State.OK);
								config.set(ConfigInfo.COULEUR, RobotColor.getCouleur(data[0] == InOrder.COULEUR_ROBOT_GAUCHE.codeInt));
							}
							else
							{
								paquet.ticket.set(Ticket.State.KO);
								if(data[0] != InOrder.COULEUR_ROBOT_INCONNU.codeInt)
									log.critical("Code couleur inconnu : "+data[0]);
							}
						}
						else
							log.critical("Le bas niveau a signalé un changement de couleur en plein match : "+data[0]);
					}
					
					/**
					 * Capteurs
					 */
					else if(paquet.origine == OutOrder.START_CAPTEURS)
					{
						/**
						 * Récupération de la position et de l'orientation
						 */
						int xRobot = data[0] << 4;
						xRobot += data[1] >> 4;
						xRobot -= 1500;
						int yRobot = (data[1] & 0x0F) << 8;
						yRobot = yRobot + data[2];
						Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(xRobot, yRobot);
		
						double orientationRobot = ((data[3] << 8) + data[4]) / 1000.;
						double courbure = data[5] / 1000.;
						boolean enMarcheAvant = data[0] == 0;
						double vitesseLineaire = (data[6] << 8) + data[7];
						double vitesseRotation = (data[8] << 8) + data[9];
		
						/**
						 * Acquiert ce que voit les capteurs
					 	 */
						int[] mesures = new int[nbCapteurs];
						for(int i = 0; i < nbCapteurs / 2; i++)
						{
							mesures[2*i] = (data[10+3*i] << 4) + (data[10+3*i+1] >> 4);
							mesures[2*i+1] = ((data[10+3*i+1] & 0x0F) << 8) + data[10+3*i+2];
						}
						
						if(Config.debugSerie)
							log.debug("Le robot est en "+positionRobot+", orientation : "+orientationRobot);
		
						Cinematique c = new Cinematique(positionRobot.x, positionRobot.y, orientationRobot, enMarcheAvant, courbure, vitesseLineaire, vitesseRotation, Speed.STANDARD);
						robot.setCinematique(c);
						if(capteursOn)
							buffer.add(new IncomingData(mesures, c));
					}
		
					/**
					 * Démarrage du match
					 */
					else if(paquet.origine == OutOrder.MATCH_BEGIN)
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
					else if(paquet.origine == OutOrder.MATCH_END)
					{
						log.debug("Fin du Match !");
						config.set(ConfigInfo.FIN_MATCH, true);
						return;
					}
							
					/**
					 * Le robot est arrivé après un arrêt demandé par le haut niveau
					 */
					else if(paquet.origine == OutOrder.AVANCER ||
							paquet.origine == OutOrder.AVANCER_IDEM ||
							paquet.origine == OutOrder.AVANCER_NEG ||
							paquet.origine == OutOrder.AVANCER_REVERSE)
					{

						if(data[0] == InOrder.MOUVEMENT_ANNULE.codeInt ||
								data[0] == InOrder.ROBOT_ARRIVE.codeInt)
							paquet.ticket.set(Ticket.State.OK);
						else
						{
							paquet.ticket.set(Ticket.State.KO);
							if(data[0] != InOrder.PB_DEPLACEMENT.codeInt)
								log.critical("Code fin mouvement inconnu : "+data[0]);
						}
					}
					
					else if(data.length != 0)
						log.critical("On a ignoré un paquet d'origine "+paquet.origine+" (taille : "+data.length+")");

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
