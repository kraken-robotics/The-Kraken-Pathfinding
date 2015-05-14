package threads;

import container.Service;
import exceptions.FinMatchException;
import exceptions.SerialConnexionException;
import robot.serial.SerialManager;
import robot.stm.STMcard;
import table.ObstacleManager;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;

/**
 * Thread qui s'occupe de la gestion du temps: début du match, péremption des obstacles
 * C'est lui qui active les stms en début de match.
 * @author pf
 *
 */

public class ThreadTimer extends AbstractThread implements Service
{

	// Dépendance
	private Log log;
	private Config config;
	private ObstacleManager obstaclemanager;
	private SerialManager serialmanager;
	private STMcard stm;
	
	private long dureeMatch = 90000;
	private long dateFin;
	public static int obstacleRefreshInterval = 500; // temps en ms entre deux appels par le thread timer du rafraichissement des obstacles de la table
		
	public ThreadTimer(Log log, Config config, ObstacleManager obstaclemanager, STMcard stm, SerialManager serialmanager)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		this.stm = stm;
		this.serialmanager = serialmanager;
		
		updateConfig();
		Thread.currentThread().setPriority(1);
	}

	@Override
	public void run()
	{
		log.debug("Lancement du thread timer");

		// les stms sont initialement éteints
		stm.setCapteursOn(false);
		
		// Attente du démarrage du match
		while(!Config.matchDemarre)
		{
			// Permet de signaler que le match a démarré
			try {
				Config.matchDemarre |= stm.demarrageMatch();
			} catch (SerialConnexionException e) {
				e.printStackTrace();
			} catch (FinMatchException e) {
				// Normalement impossible...
				stopThreads = true;
				e.printStackTrace();
			}
			if(stopThreads)
			{
				log.debug("Arrêt du thread timer avant le début du match");
				return;
			}
			Sleep.sleep(50);
		}

		config.setDateDebutMatch();

		// On démarre les stms
		stm.setCapteursOn(true);

		log.debug("LE MATCH COMMENCE !");

		dateFin = dureeMatch + Config.getDateDebutMatch();

		// Le match à démarré. On retire périodiquement les obstacles périmés
		while(System.currentTimeMillis() < dateFin)
		{
			if(stopThreads)
			{
				log.debug("Arrêt du thread timer");
				return;
			}
			obstaclemanager.supprimerObstaclesPerimes();
			Sleep.sleep(obstacleRefreshInterval);
		}

		onMatchEnded();
		
		log.debug("Fin du thread timer");
		
	}
	
	private void onMatchEnded()
	{
		log.debug("Fin du Match !");
		
		finMatch = true;

		// DEPENDS_ON_RULES
		// potentielle attente avant de tout désactiver afin de laisser la funny action

		// fin du match : désasser final
		try {
				stm.disableRotationalFeedbackLoop();
			stm.disableTranslationalFeedbackLoop();
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		} catch (FinMatchException e) {
			e.printStackTrace();
		}
		
		serialmanager.closeAll();
	}
	
	public void updateConfig()
	{
		// facteur 1000 car temps_match est en secondes et duree_match en ms
		dureeMatch = 1000*config.getInt(ConfigInfo.DUREE_MATCH_EN_S);
		log.updateConfig();
		obstaclemanager.updateConfig();
		stm.updateConfig();
		serialmanager.updateConfig();
	}
	
}
