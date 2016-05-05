package threads;

import java.io.IOException;

import enums.SerialProtocol;
import robot.Cinematique;
import robot.RobotReal;
import robot.Speed;
import robot.requete.RequeteSTM;
import robot.requete.RequeteType;
import serie.DataForSerialOutput;
import serie.SerialInterface;
import table.GameElementNames;
import table.GameElementType;
import table.Table;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import container.Service;
import debug.IncomingDataDebug;
import debug.IncomingDataDebugBuffer;
import enums.RobotColor;
import enums.Tribool;
import exceptions.MissingCharacterException;
import hook.HookFactory;
import obstacles.IncomingData;
import obstacles.IncomingDataBuffer;
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
	private SerialInterface serie;
	private IncomingDataBuffer buffer;
	private HookFactory hookfactory;
	private DataForSerialOutput output;
	private RobotReal robot;
	private Table table;
	private IncomingDataDebugBuffer bufferdebug;
	
	private int codeCoquillage;
	
	private RequeteSTM requete;
	private boolean capteursOn = false;
	private volatile int nbCapteurs;
	private boolean matchDemarre = false;
	private int[] lecture = new int[100];
	private int idDernierPaquet = -1;
	private final static int ID_FORT = 0;
	private final static int ID_FAIBLE = 1;
	private final static int COMMANDE = 2;
	private final static int PARAM = 3;

	
	public ThreadSerialInput(Log log, Config config, SerialInterface serie, IncomingDataBuffer buffer, IncomingDataDebugBuffer bufferdebug, RequeteSTM requete, RobotReal robot, Table table, HookFactory hookfactory, DataForSerialOutput output)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		this.requete = requete;
		this.hookfactory = hookfactory;
		this.output = output;
		this.robot = robot;
		this.table = table;
		this.bufferdebug = bufferdebug;
		idDernierPaquet = serie.getFirstID();
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
						int tmp = serie.read();
						if(tmp != 0x55)
						{
							log.warning("Mauvais entête (0x55) : "+tmp);
							continue;
						}
						tmp = serie.read();
						if(tmp != 0xAA)
						{
							log.warning("Mauvais entête (0xAA) : "+tmp);
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
							{
								if(Config.debugSerie)
									log.warning("On a raté un message");
								output.askResend(idDernierPaquet++);
							}
							
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

					if(lecture[COMMANDE] == SerialProtocol.IN_PING.codeInt)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						output.sendPong();
					}

					else if(lecture[COMMANDE] == SerialProtocol.IN_PONG1.codeInt) // si le pong est corrompu, pas besoin de le redemander…
					{
						lecture[index++] = serie.read(); // pong2
						lecture[index++] = serie.read(); // checksum
						if(lecture[COMMANDE+1] != SerialProtocol.IN_PONG2.codeInt)
							log.warning("Pong reçu non conforme");
						else if(Config.debugSerieTrame)
							log.debug("Reçu pong");
					}
					else if(lecture[COMMANDE] == SerialProtocol.IN_DEBUG_ASSER.codeInt)
					{
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();
						lecture[index++] = serie.read();

						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						bufferdebug.add(new IncomingDataDebug((lecture[PARAM] << 8) + lecture[PARAM+1], 
								(lecture[PARAM+2] << 8) + lecture[PARAM+3],
								(lecture[PARAM+4] << 8) + lecture[PARAM+5],
								(lecture[PARAM+6] << 8) + lecture[PARAM+7],
								(lecture[PARAM+8] << 8) + lecture[PARAM+9],
								(lecture[PARAM+10] << 8) + lecture[PARAM+11],
								(lecture[PARAM+12] << 8) + lecture[PARAM+13],
								(lecture[PARAM+14] << 8) + lecture[PARAM+15]));
					}
					else if((lecture[COMMANDE] & SerialProtocol.MASK_LAST_BIT.codeInt) == SerialProtocol.IN_INFO_CAPTEURS.codeInt)
					{
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // xy
						lecture[index++] = serie.read(); // o
						lecture[index++] = serie.read(); // o
						lecture[index++] = serie.read(); // courbure
						lecture[index++] = serie.read(); // vitesse linéaire
						lecture[index++] = serie.read(); // vitesse linéaire
						lecture[index++] = serie.read(); // vitesse rotation
						lecture[index++] = serie.read(); // vitesse rotation

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
							mesures[2*i] = convertIR((lecture[PARAM+10+3*i] << 4) + (lecture[PARAM+10+3*i+1] >> 4));
//							log.debug("distance : "+mesures[2*i]);
//							log.debug("Capteur "+(2*i)+" voit "+mesures[2*i]+"mm (brut "+((lecture[PARAM+6+3*i] << 4) + (lecture[PARAM+6+3*i+1] >> 4))+")");
//							if(2*i+1 != nbCapteurs-1)
//							{
								mesures[2*i+1] = convertIR(((lecture[PARAM+10+3*i+1] & 0x0F) << 8) + lecture[PARAM+10+3*i+2]);
//								log.debug("distance : "+mesures[2*i+1]);
//								log.debug("Capteur "+(2*i+1)+" voit "+mesures[2*i+1]+"mm (brut "+(((lecture[PARAM+6+3*i+1] & 0x0F) << 8) + lecture[PARAM+6+3*i+2])+")");
//							}
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
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						if(!matchDemarre)
							config.set(ConfigInfo.COULEUR, RobotColor.getCouleur(lecture[COMMANDE] != SerialProtocol.IN_COULEUR_ROBOT.codeInt));
						else
							log.warning("Le bas niveau a signalé un changement de couleur en plein match");
					}

					/**
					 * La balise est-elle présente ?
					 */
					else if((lecture[COMMANDE] & SerialProtocol.MASK_LAST_BIT.codeInt) == SerialProtocol.IN_PRESENCE_BALISE.codeInt)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						if(!matchDemarre)
							config.set(ConfigInfo.BALISE_PRESENTE, lecture[COMMANDE] == SerialProtocol.IN_PRESENCE_BALISE.codeInt);
						else
							log.warning("Le bas niveau a signalé un changement de présence de balise en plein match");
					}

					/**
					 * Erreur série : il faut renvoyer un message
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_RESEND_PACKET.codeInt)
					{
						lecture[index++] = serie.read(); // id à retransmettre
						lecture[index++] = serie.read(); // id à retransmettre

						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						String s = Integer.toHexString(((lecture[PARAM] << 8) + lecture[PARAM+1])).toUpperCase();
						if(s.length() == 1)
							s = "0"+s;
						else
							s = s.substring(s.length()-2, s.length());

						log.warning("Demande de renvoi du paquet "+s);
						
						output.resend((lecture[PARAM] << 8) + lecture[PARAM+1]);
					}
					
					/**
					 * Récupère le code des coquillages
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_CODE_COQUILLAGES.codeInt)
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
							
							log.debug("Code coquillage reçu : "+codeCoquillage);
							
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
					 * Démarrage du match
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_DEBUT_MATCH.codeInt)
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
					else if(lecture[COMMANDE] == SerialProtocol.IN_MATCH_FINI.codeInt)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;

						log.debug("Fin du Match !");
						config.set(ConfigInfo.FIN_MATCH, true);
						serie.close();
						return;
					}
					/**
					 * Un élément a été shooté
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_ELT_SHOOT.codeInt)
					{
						lecture[index++] = serie.read(); // nb element
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;
						int nbElement = lecture[PARAM];
						log.debug(nbElement);
						table.setDone(GameElementNames.values()[nbElement], Tribool.TRUE);
					}

					/**
					 * Lancement de l'odo
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_RAB.codeInt)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;
						if(!matchDemarre)
						{
							output.initOdoSTM(robot.getCinematique().getPosition(), robot.getCinematique().orientation);
							output.initOdoSTM(robot.getCinematique().getPosition(), robot.getCinematique().orientation);
							output.initOdoSTM(robot.getCinematique().getPosition(), robot.getCinematique().orientation);
						}
					}

					else if(lecture[COMMANDE] == SerialProtocol.IN_ROBOT_ARRIVE.codeInt)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;
						log.debug("Le robot est arrivé !");
						requete.set(RequeteType.TRAJET_FINI);
					}

					/**
					 * Il y a un blocage mécanique
					 */
					else if(lecture[COMMANDE] == SerialProtocol.IN_PB_DEPLACEMENT_VITESSE.codeInt)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;
						log.critical("Le robot a rencontré un problème mécanique : vitesse trop basse");
						requete.set(RequeteType.BLOCAGE_MECANIQUE_VITESSE);
					}
					else if(lecture[COMMANDE] == SerialProtocol.IN_PB_DEPLACEMENT_ACC.codeInt)
					{
						// Mauvais checksum. Annulation.
						if(!verifieChecksum(lecture, index))
							continue;
						log.critical("Le robot a rencontré un problème mécanique : accélération trop forte");
						requete.set(RequeteType.BLOCAGE_MECANIQUE_ACCELERATION);
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
	 * Passe de la mesure analogique à la distance
	 * @param capteur
	 * @return
	 */
	private int convertIR(int capteur)
	{
//		log.debug("Brut : "+capteur);
		double V = (capteur - 24) / 1341.; // formule trouvée expérimentalement

		// Ces formules ont été calculée à partir de la datasheet des capteurs IR
	    if(V < 0.3) // tension trop basse, obstacle à perpèt'
	    	return 0;
	    else if(V < 2.75) // au-dessus de 8cm
	        return (int) (207.7 / (V - 0.15));
	    else if(V < 3)
	        return (int) (140 / (V - 1));
	    else
	        return (int) (63 / (V - 2.1));
	}

	/**
	 * Vérifie si le checksum est bon. En cas de problème, il relance la STM
	 * @param lecture
	 * @return
	 * @throws MissingCharacterExcept
	 * @throws IOException 
	 */
	private boolean verifieChecksum(int[] lecture, int longueur) throws IOException, MissingCharacterException
	{
//		lecture[longueur++] = serie.read(); // checksum
		lecture[longueur] = serie.read(); // checksum

		int c = 0;
		for(int i = 0; i < longueur; i++)
			c += lecture[i];
		if(lecture[longueur] != ((~c) & 0xFF))
		{
			log.warning("Erreur de checksum (attendu : "+((~c) & 0xFF)+", obtenu : "+lecture[longueur]+") pour "+lecture[COMMANDE]+". Paquet redemandé");
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
