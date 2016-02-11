package threads;

import java.io.IOException;

import buffer.DataForSerialOutput;
import buffer.IncomingData;
import buffer.IncomingDataBuffer;
import buffer.IncomingHookBuffer;
import enums.SerialProtocol;
import permissions.ReadOnly;
import requete.RequeteSTM;
import requete.RequeteType;
import robot.RobotReal;
import table.GameElementNames;
import table.GameElementType;
import table.Table;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.SerialSTM;
import utils.Vec2;
import container.Service;
import enums.RobotColor;
import enums.Tribool;
import exceptions.MissingCharacterException;
import hook.HookFactory;
import obstacles.types.ObstacleCircular;

/**
 * Thread qui écoute la série et appelle qui il faut.
 * @author pf
 *
 */

public class ThreadSerialInput extends Thread implements Service
{
	protected Log log;
	protected Config config;
	private SerialSTM serie;
	private IncomingDataBuffer buffer;
	private IncomingHookBuffer hookbuffer;
	private Table table;
	private RobotReal robot;
	private HookFactory hookfactory;
	private DataForSerialOutput output;
	private int codeCoquillage;
	
	private RequeteSTM requete;
	private boolean capteursOn = false;
	private volatile int nbCapteurs;
	private boolean matchDemarre = false;
	private byte[] lecture = new byte[100];
	private int idDernierPaquet = -1;
	private final static int ID_FORT = 0;
	private final static int ID_FAIBLE = 1;
	private final static int COMMANDE = 2;
	private final static int PARAM = 3;

	
	public ThreadSerialInput(Log log, Config config, SerialSTM serie, IncomingDataBuffer buffer, IncomingHookBuffer hookbuffer, RequeteSTM requete, Table table, RobotReal robot, HookFactory hookfactory, DataForSerialOutput output)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		this.hookbuffer = hookbuffer;
		this.requete = requete;
		this.table = table;
		this.robot = robot;
		this.hookfactory = hookfactory;
		this.output = output;
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
					while(!serie.available())
						serie.wait();
					int index = 0;

					try {
						// On s'assure de bien commencer au début d'un message
						if(serie.read() != 0x55)
						{
							log.warning("Mauvais entête (0x55)");
							continue;
						}
						if(serie.read() != 0xAA)
						{
							log.warning("Mauvais entête (0xAA)");
							continue;
						}
						
						lecture[index++] = serie.read(); // id partie 1
						lecture[index++] = serie.read(); // id partie 2
						int idPaquet = (lecture[ID_FORT] << 8) + lecture[ID_FAIBLE];
						
						// Si on reçoit un vieux paquet, on ne réagit pas
						if(idPaquet > idDernierPaquet)
						{
							idDernierPaquet++; // id paquet théoriquement reçu
		
							// tiens, on a raté des paquets…
							while(idPaquet > idDernierPaquet)
								output.askResend(idDernierPaquet++);
							
							// on a idDernierPaquet = idPaquet
						}
					} catch(MissingCharacterException e)
					{
						/**
						 * Si on a même pas eu l'id du paquet, on ne peut rien faire…
						 */
						log.critical("La série est trop longue à fournir la commande, annulation");
						continue;
					}
					lecture[index++] = serie.read(); // commande

					if(lecture[COMMANDE] == SerialProtocol.IN_PING.code)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						output.sendPong();
					}

					else if(lecture[COMMANDE] == SerialProtocol.IN_PONG1.code) // si le pong est corrompu, pas besoin de le redemander…
					{
						lecture[index++] = serie.read(); // pong2
						lecture[index++] = serie.read(); // checksum
						if(lecture[COMMANDE+1] != SerialProtocol.IN_PONG2.code)
							log.warning("Pong reçu non conforme");
					}
					else if(lecture[COMMANDE] == SerialProtocol.IN_XYO.code)
					{
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // o
						lecture[index++] = serie.read(); // o
						lecture[index++] = serie.read(); // courbure

						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						/**
						 * Récupération de la position et de l'orientation
						 */
						int xRobot = lecture[PARAM] << 4;
						xRobot += lecture[PARAM+1] >> 4;
						xRobot -= 1500;
						int yRobot = (lecture[PARAM+1] & 0x0F) << 8;
						yRobot = yRobot + lecture[PARAM+2];
						Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(xRobot, yRobot);
						double orientationRobot = (lecture[PARAM+3] << 8 + lecture[PARAM+4]) / 1000.;
						double courbure = lecture[PARAM+5] / 1000.;
						robot.setPositionOrientationJava(positionRobot, orientationRobot);
						robot.setCourbure(courbure);

					}
					else if(lecture[COMMANDE] == SerialProtocol.IN_INFO_CAPTEURS.code)
					{
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // o
						lecture[index++] = serie.read(); // o
						lecture[index++] = serie.read(); // courbure

						for(int i = 0; i < nbCapteurs / 2; i++)
						{
							lecture[index++] = serie.read(); // capteur
							lecture[index++] = serie.read(); // capteur
							lecture[index++] = serie.read(); // capteur
						}
						
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						/**
						 * Récupération de la position et de l'orientation
						 */
						int xRobot = lecture[PARAM] << 4;
						xRobot += lecture[PARAM+1] >> 4;
						xRobot -= 1500;
						int yRobot = (lecture[PARAM+1] & 0x0F) << 8;
						yRobot = yRobot + lecture[PARAM+2];
						Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(xRobot, yRobot);
						double orientationRobot = (lecture[PARAM+3] << 8 + lecture[PARAM+4]) / 1000.;
						double courbure = lecture[PARAM+5] / 1000.;

						/**
						 * Acquiert ce que voit les capteurs
					 	 */
						int[] mesures = new int[nbCapteurs];
						for(int i = 0; i < nbCapteurs / 2; i++)
						{
							mesures[2*i] = (lecture[PARAM+6+3*i] << 4) + (lecture[PARAM+6+3*i+1] >> 4);
							if(2*i+1 != nbCapteurs-1)
								mesures[2*i+1] = ((lecture[PARAM+6+3*i+1] & 0x0F) << 8) + lecture[PARAM+6+3*i+2];
						}
						robot.setPositionOrientationJava(positionRobot, orientationRobot);
						robot.setCourbure(courbure);
						if(capteursOn)
							buffer.add(new IncomingData(mesures/*, capteursOn*/));
					}
					
					/**
					 * Le robot commence à droite
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_COULEUR_ROBOT_SANS_SYMETRIE.code)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						if(!matchDemarre)
							config.set(ConfigInfo.COULEUR, RobotColor.getCouleurSansSymetrie());
					}
					
					/**
					 * Le robot commence à gauche
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_COULEUR_ROBOT_AVEC_SYMETRIE.code)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						if(!matchDemarre)
							config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
					}

					/**
					 * Erreur série : il faut renvoyer un message
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_RESEND_PACKET.code)
					{
						lecture[index++] = serie.read(); // id à retransmettre
						lecture[index++] = serie.read(); // id à retransmettre
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						output.resend((lecture[PARAM] << 8) + lecture[PARAM+1]);
					}
					
					/**
					 * Récupère le code des coquillages
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_CODE_COQUILLAGES.code)
					{
						if(!matchDemarre)
						{
							/**
							 * Avant le démarrage du match, la seule chose que le code va changer se situe dans les hooks.
							 * Du coup, on les renvoie quand le code change
							 */
							int tmp = codeCoquillage;
							lecture[index++] = serie.read(); // code
							
							// Mauvais checksum. Annulation.
							if(!verifieChecksum(lecture, index))
								continue;

							codeCoquillage = lecture[PARAM];
							if(tmp != codeCoquillage)
							{
								switch(codeCoquillage)
								{
									case 0:
										GameElementNames.COQUILLAGE_1.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(1300,450), 38));
										GameElementNames.COQUILLAGE_2.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(1300,750), 38));
										GameElementNames.COQUILLAGE_3.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(600,550), 38));
										GameElementNames.COQUILLAGE_4.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(300,350), 38));
										GameElementNames.COQUILLAGE_5.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(0,150), 38));
										GameElementNames.COQUILLAGE_6.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(0,450), 38));
										GameElementNames.COQUILLAGE_7.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(-300,350), 38));
										GameElementNames.COQUILLAGE_8.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-600,550), 38));
										GameElementNames.COQUILLAGE_9.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(-1300,750), 38));
										GameElementNames.COQUILLAGE_10.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(-1300,450), 38));

										GameElementNames.COQUILLAGE_ROCHER_DROITE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										break;
									case 1:
										GameElementNames.COQUILLAGE_1.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(1300,450), 38));
										GameElementNames.COQUILLAGE_2.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(1300,750), 38));
										GameElementNames.COQUILLAGE_3.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(600,550), 38));
										GameElementNames.COQUILLAGE_4.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(300,350), 38));
										GameElementNames.COQUILLAGE_5.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(0,150), 38));
										GameElementNames.COQUILLAGE_6.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(0,450), 38));
										GameElementNames.COQUILLAGE_7.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-300,350), 38));
										GameElementNames.COQUILLAGE_8.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-600,550), 38));
										GameElementNames.COQUILLAGE_9.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-1300,750), 38));
										GameElementNames.COQUILLAGE_10.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(-1300,450), 38));

										GameElementNames.COQUILLAGE_ROCHER_DROITE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										break;
									case 2:
										GameElementNames.COQUILLAGE_1.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(1300,450), 38));
										GameElementNames.COQUILLAGE_2.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(1300,750), 38));
										GameElementNames.COQUILLAGE_3.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(900,450), 38));
										GameElementNames.COQUILLAGE_4.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(900,750), 38));
										GameElementNames.COQUILLAGE_5.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(300,350), 38));
										GameElementNames.COQUILLAGE_6.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-300,350), 38));
										GameElementNames.COQUILLAGE_7.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-900,750), 38));
										GameElementNames.COQUILLAGE_8.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(-900,450), 38));
										GameElementNames.COQUILLAGE_9.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-1300,750), 38));
										GameElementNames.COQUILLAGE_10.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(-1300,450), 38));

										GameElementNames.COQUILLAGE_ROCHER_DROITE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										break;
									case 3:
										GameElementNames.COQUILLAGE_1.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(1300,450), 38));
										GameElementNames.COQUILLAGE_2.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(1300,750), 38));
										GameElementNames.COQUILLAGE_3.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(900,450), 38));
										GameElementNames.COQUILLAGE_4.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(900,750), 38));
										GameElementNames.COQUILLAGE_5.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(300,350), 38));
										GameElementNames.COQUILLAGE_6.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(-300,350), 38));
										GameElementNames.COQUILLAGE_7.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-900,750), 38));
										GameElementNames.COQUILLAGE_8.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(-900,450), 38));
										GameElementNames.COQUILLAGE_9.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-1300,750), 38));
										GameElementNames.COQUILLAGE_10.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-1300,450), 38));

										GameElementNames.COQUILLAGE_ROCHER_DROITE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										break;
									case 4:
										GameElementNames.COQUILLAGE_1.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(1300,450), 38));
										GameElementNames.COQUILLAGE_2.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(1300,750), 38));
										GameElementNames.COQUILLAGE_3.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(900,450), 38));
										GameElementNames.COQUILLAGE_4.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(900,750), 38));
										GameElementNames.COQUILLAGE_5.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(900,150), 38));
										GameElementNames.COQUILLAGE_6.set(GameElementType.COQUILLAGE_NEUTRE, new ObstacleCircular(new Vec2<ReadOnly>(-900,150), 38));
										GameElementNames.COQUILLAGE_7.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-900,750), 38));
										GameElementNames.COQUILLAGE_8.set(GameElementType.COQUILLAGE_AMI, new ObstacleCircular(new Vec2<ReadOnly>(-900,450), 38));
										GameElementNames.COQUILLAGE_9.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-1300,750), 38));
										GameElementNames.COQUILLAGE_10.set(GameElementType.COQUILLAGE_ENNEMI, new ObstacleCircular(new Vec2<ReadOnly>(-1300,450), 38));

										GameElementNames.COQUILLAGE_ROCHER_DROITE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_DROITE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_AMI, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_SOMMET.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_INTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_NEUTRE, null);
										GameElementNames.COQUILLAGE_ROCHER_GAUCHE_EXTERIEUR.set(GameElementType.COQUILLAGE_EN_HAUTEUR_ENNEMI, null);
										break;
									default:
										log.critical("Code coquillage inconnu ! "+codeCoquillage);
										break;
								}
								
								// Evitons un nullpointer exception…
								if(codeCoquillage >= 0 && codeCoquillage <= 4)
								{
									output.deleteAllHooks();
									output.envoieHooks(hookfactory.getHooksPermanentsAEnvoyer());
								}
							}
						}
					}
							/**
							 * Le robot est en marche avant
							 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_ROBOT_EN_MARCHE_AVANT.code)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;
						
						robot.setEnMarcheAvance(true);
					}
							/**
							 * Le robot est en marche arrière
							 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_ROBOT_EN_MARCHE_ARRIERE.code)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						robot.setEnMarcheAvance(false);
					}
					/**
					 * Démarrage du match
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_DEBUT_MATCH.code)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

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
					else if(lecture[COMMANDE] == SerialProtocol.IN_MATCH_FINI.code)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						requete.set(RequeteType.MATCH_FINI);
						serie.close();
						return;
					}
					/**
					 * Un élément a été shooté
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_ELT_SHOOT.code)
					{
						lecture[index++] = serie.read(); // nb element
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						int nbElement = lecture[PARAM];
						table.setDone(GameElementNames.values()[nbElement], Tribool.TRUE);
					}
							/**
							 * Demande de hook
							 */
/*						case "dhk":
							int nbScript = Integer.parseInt(messages[1]);
							ScriptHookNames s = ScriptHookNames.values()[nbScript];
							int param = Integer.parseInt(messages[2]);
							hookbuffer.add(new IncomingHook(s, param));
							break;
*/
					/**
					 * On est arrivé à destination.
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_ROBOT_ARRIVE.code)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						requete.set(RequeteType.TRAJET_FINI);
					}

					/**
					 * Il y a un blocage mécanique
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_PB_DEPLACEMENT.code)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						requete.set(RequeteType.BLOCAGE_MECANIQUE);
					}
					else
					{
						log.critical("Commande série inconnue: "+lecture[COMMANDE]);
						output.askResend(idDernierPaquet); // on redemande, il y a certainement eu un problème
					}
					
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			} catch (MissingCharacterException e) {
				log.critical("Série trop longue. Redemande");
				output.askResend(idDernierPaquet); // on redemande, il y a certainement eu un problème
			}
		}
//		log.debug("Fermeture de ThreadSerialInput");
	}
	
	/**
	 * Vérifie si le checksum est bon. En cas de problème, il relance la STM
	 * @param lecture
	 * @return
	 * @throws MissingCharacterException 
	 * @throws IOException 
	 */
	private boolean verifieChecksum(byte[] lecture, int longueur) throws IOException, MissingCharacterException
	{
		lecture[longueur++] = serie.read(); // checksum

		int c = 0;
		for(int i = 0; i < longueur; i++)
			c += lecture[i];
		if(lecture[longueur] != (byte)(~c))
		{
			log.warning("Erreur de checksum. Paquet redemandé");
			output.askResend(idDernierPaquet);
			return false;
		}
		return true;
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
