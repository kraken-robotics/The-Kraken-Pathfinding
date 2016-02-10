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
import utils.SerialConnexion;
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
	private SerialConnexion serie;
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
	
	public ThreadSerialInput(Log log, Config config, SerialConnexion serie, IncomingDataBuffer buffer, IncomingHookBuffer hookbuffer, RequeteSTM requete, Table table, RobotReal robot, HookFactory hookfactory, DataForSerialOutput output)
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
							continue;
						if(serie.read() != 0xAA)
							continue;
						
						lecture[index++] = serie.read(); // id partie 1
						lecture[index++] = serie.read(); // id partie 2
						int idPaquet = (lecture[0] << 8) + lecture[1];
						// Si on reçoit un vieux paquet, on ne réagit pas
						if(idPaquet >= idDernierPaquet)
						{
							idDernierPaquet++; // id paquet théoriquement reçu
		
							// tiens, on a raté des paquets…
							while(idPaquet > idDernierPaquet)
								output.askResend(idDernierPaquet++);
						}
					} catch(MissingCharacterException e)
					{
						log.critical("La série est trop longue à fournir la commande, annulation");
						continue;
					}
					lecture[index++] = serie.read(); // commande
					
					if(lecture[2] == SerialProtocol.IN_INFO_CAPTEURS.nb)
					{
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // o
						lecture[index++] = serie.read(); // o

						for(int i = 0; i < nbCapteurs / 2; i++)
						{
							lecture[index++] = serie.read(); // capteur
							lecture[index++] = serie.read(); // capteur
							lecture[index++] = serie.read(); // capteur
						}
						
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;

						/**
						 * Récupération de la position et de l'orientation
						 */
						int xRobot = lecture[3] << 4;
						xRobot += lecture[4] >> 4;
						int yRobot = (lecture[4] & 0x0F) << 8;
						yRobot = yRobot + lecture[5];
						Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(xRobot, yRobot);
						double orientationRobot = (lecture[6] << 8 + lecture[7]) / 1000.;
						double courbure = lecture[8] / 1000.;

						/**
						 * Acquiert ce que voit les capteurs
					 	 */
						int[] mesures = new int[nbCapteurs];
						for(int i = 0; i < nbCapteurs / 2; i++)
						{
							mesures[2*i] = (lecture[9+3*i] << 4) + (lecture[9+3*i+1] >> 4);
							if(2*i+1 != nbCapteurs-1)
								mesures[2*i+1] = ((lecture[9+3*i+1] & 0x0F) << 8) + lecture[9+3*i+2];
						}
						robot.setPositionOrientationJava(positionRobot, orientationRobot);
						robot.setCourbure(courbure);
						if(capteursOn)
							buffer.add(new IncomingData(mesures/*, capteursOn*/));
					}
							/**
							 * Récupère la couleur du robot.
							 */
					else if(lecture[2] == SerialProtocol.IN_COULEUR_ROBOT_SANS_SYMETRIE.nb)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;

						if(!matchDemarre)
							config.set(ConfigInfo.COULEUR, RobotColor.getCouleurSansSymetrie());
					}
					else if(lecture[2] == SerialProtocol.IN_COULEUR_ROBOT_AVEC_SYMETRIE.nb)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;

						if(!matchDemarre)
							config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
					}

					/**
					 * Erreur série : il faut renvoyer un message
					 */
					else if(lecture[2] == SerialProtocol.IN_RESEND_PACKET.nb)
					{
						lecture[index++] = serie.read(); // id à retransmettre
						lecture[index++] = serie.read(); // id à retransmettre
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;

						output.resend((lecture[3] << 8) + lecture[4]);
					}
					/**
					 * Récupère le code des coquillages
					 */
					else if(lecture[2] == SerialProtocol.IN_CODE_COQUILLAGES.nb)
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
							if(!verifieChecksum(lecture))
								continue;

							codeCoquillage = lecture[3];
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
					else if(lecture[2] == SerialProtocol.IN_ROBOT_EN_MARCHE_AVANT.nb)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;
						
						robot.setEnMarcheAvance(true);
					}
							/**
							 * Le robot est en marche arrière
							 */
					else if(lecture[2] == SerialProtocol.IN_ROBOT_EN_MARCHE_ARRIERE.nb)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;

						robot.setEnMarcheAvance(false);
					}
					/**
					 * Démarrage du match
					 */
					else if(lecture[2] == SerialProtocol.IN_DEBUT_MATCH.nb)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
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
					else if(lecture[2] == SerialProtocol.IN_MATCH_FINI.nb)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;

						requete.set(RequeteType.MATCH_FINI);
						serie.close();
						return;
					}
					/**
					 * Un élément a été shooté
					 */
					else if(lecture[2] == SerialProtocol.IN_ELT_SHOOT.nb)
					{
						lecture[index++] = serie.read(); // nb element
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;

						int nbElement = lecture[3];
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
					else if(lecture[2] == SerialProtocol.IN_ROBOT_ARRIVE.nb)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;

						requete.set(RequeteType.TRAJET_FINI);
					}

					/**
					 * Il y a un blocage mécanique
					 */
					else if(lecture[2] == SerialProtocol.IN_PB_DEPLACEMENT.nb)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture))
							continue;

						requete.set(RequeteType.BLOCAGE_MECANIQUE);
					}
					else
					{
						log.critical("Commande série inconnue: "+lecture[2]);
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
	 */
	private boolean verifieChecksum(byte[] lecture)
	{
		int c = 0;
		for(int i = 0; i < lecture.length-1; i++)
			c += lecture[i];
		if(lecture[lecture.length-1] != (byte)(~c))
		{
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
