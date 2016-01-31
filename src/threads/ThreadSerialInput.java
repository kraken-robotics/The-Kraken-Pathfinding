package threads;

import buffer.DataForSerialOutput;
import buffer.IncomingData;
import buffer.IncomingDataBuffer;
import buffer.IncomingHook;
import buffer.IncomingHookBuffer;
import permissions.ReadOnly;
import requete.RequeteSTM;
import requete.RequeteType;
import robot.RobotReal;
import scripts.ScriptHookNames;
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
							Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(xRobot, yRobot);
							double orientationRobot = Integer.parseInt(serie.read()) / 1000.;
							double courbure = Integer.parseInt(serie.read()) / 1000.;
							int[] mesures = new int[nbCapteurs];
							for(int i = 0; i < nbCapteurs; i++)
								mesures[i] = Integer.parseInt(serie.read());
							robot.setPositionOrientationJava(positionRobot, orientationRobot);
							robot.setCourbure(courbure);
							if(capteursOn)
								buffer.add(new IncomingData(mesures/*, capteursOn*/));
							break;
							
							/**
							 * Récupère la couleur du robot.
							 */
						case "color":
							if(!matchDemarre)
								config.set(ConfigInfo.COULEUR, RobotColor.parse(serie.read()));
							break;

							/**
							 * Récupère le code des coquillages
							 */
						case "cdcoq":
							if(!matchDemarre)
							{
								/**
								 * Avant le démarrage du match, la seule chose que le code va changer se situe dans les hooks.
								 * Du coup, on les renvoie quand le code change
								 */
								int tmp = codeCoquillage;
								codeCoquillage = Integer.parseInt(serie.read());
								if(tmp != codeCoquillage)
								{
									codeCoquillage = tmp;
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

											GameElementNames.COQUILLAGE_ROCHER_DROITE_SOMMET.set(GameElementType.COQUILLAGE_NEUTRE, null);
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
									output.envoieHooks(hookfactory.getHooksPermanents());
								}
							}
							break;
							
							/**
							 * Récupère la marche avant
							 */
						case "avt":
							robot.setEnMarcheAvance(Boolean.parseBoolean(serie.read()));
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
								matchDemarre = true;
							}
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
							 * Un élément a été shooté
							 */
						case "tbl":
							int nbElement = Integer.parseInt(serie.read());
							int done = Integer.parseInt(serie.read());
							table.setDone(GameElementNames.values()[nbElement], Tribool.parse(done));
							break;

							/**
							 * Demande de hook
							 */
						case "dhk":
							int nbScript = Integer.parseInt(serie.read());
							ScriptHookNames s = ScriptHookNames.values()[nbScript];
							int param = Integer.parseInt(serie.read());
							hookbuffer.add(new IncomingHook(s, param));
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
	{}

	@Override
	public void useConfig(Config config)
	{
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
	}

}
