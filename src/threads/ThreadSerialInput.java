package threads;

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
import table.Table;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.SerialConnexion;
import utils.Vec2;
import container.Service;
import enums.RobotColor;
import enums.Tribool;

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
	
	private RequeteSTM requete;
	private boolean capteursOn = false;
	private volatile int nbCapteurs;
	
	public ThreadSerialInput(Log log, Config config, SerialConnexion serie, IncomingDataBuffer buffer, IncomingHookBuffer hookbuffer, RequeteSTM requete, Table table, RobotReal robot)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		this.hookbuffer = hookbuffer;
		this.requete = requete;
		this.table = table;
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
							int courbure = Integer.parseInt(serie.read());
							int[] mesures = new int[nbCapteurs];
							for(int i = 0; i < nbCapteurs; i++)
								mesures[i] = Integer.parseInt(serie.read());
							robot.setPositionOrientationJava(positionRobot, orientationRobot);
							robot.setCourbure(courbure);
							buffer.add(new IncomingData(mesures, capteursOn));
							break;
							
							/**
							 * Récupère la couleur du robot.
							 */
						case "color":
							config.set(ConfigInfo.COULEUR, RobotColor.parse(serie.read()));
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
