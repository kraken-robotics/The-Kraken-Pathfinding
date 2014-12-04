package threads;

import container.Service;
import obstacles.ObstacleManager;
import exceptions.FinMatchException;
import exceptions.serial.SerialConnexionException;
import robot.cardsWrappers.LocomotionCardWrapper;
import robot.cardsWrappers.SensorsCardWrapper;
import robot.serial.SerialManager;
import utils.Config;
import utils.Log;
import utils.Sleep;

/**
 * Thread qui s'occupe de la gestion du temps: début du match, péremption des obstacles
 * C'est lui qui active les capteurs en début de match.
 * @author pf
 *
 */

public class ThreadTimer extends AbstractThread implements Service
{

	// Dépendance
	private Log log;
	private Config config;
	private ObstacleManager obstaclemanager;
	private SensorsCardWrapper capteur;
	private LocomotionCardWrapper deplacements;
	private SerialManager serialmanager;
	
	private long dureeMatch = 90000;
	private long dateFin;
	public static int obstacleRefreshInterval = 500; // temps en ms entre deux appels par le thread timer du rafraichissement des obstacles de la table
		
	public ThreadTimer(Log log, Config config, ObstacleManager obstaclemanager, SensorsCardWrapper capteur, LocomotionCardWrapper deplacements, SerialManager serialmanager)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		this.capteur = capteur;
		this.deplacements = deplacements;
		this.serialmanager = serialmanager;
		
		updateConfig();
		Thread.currentThread().setPriority(1);
	}

	@Override
	public void run()
	{
		log.debug("Lancement du thread timer", this);

		// les capteurs sont initialement éteints
		capteur.setCapteursOn(false);
		
		// Attente du démarrage du match
		while(!Config.matchDemarre)
		{
			// Permet de signaler que le match a démarré
			try {
				Config.matchDemarre = capteur.demarrage_match();
			} catch (SerialConnexionException e) {
				e.printStackTrace();
			} catch (FinMatchException e) {
				// Normalement impossible...
				stopThreads = true;
				e.printStackTrace();
			}
			if(stopThreads)
			{
				log.debug("Arrêt du thread timer avant le début du match", this);
				return;
			}
			Sleep.sleep(50);
		}

		Config.dateDebutMatch = System.currentTimeMillis();

		// On démarre les capteurs
		capteur.setCapteursOn(true);

		log.debug("LE MATCH COMMENCE !", this);

		dateFin = dureeMatch + Config.dateDebutMatch;

		// Le match à démarré. On retire périodiquement les obstacles périmés
		while(System.currentTimeMillis() < dateFin)
		{
			if(stopThreads)
			{
				log.debug("Arrêt du thread timer", this);
				return;
			}
			obstaclemanager.supprimerObstaclesPerimes();
			Sleep.sleep(obstacleRefreshInterval);
		}

		onMatchEnded();
		
		log.debug("Fin du thread timer", this);
		
	}
	
	private void onMatchEnded()
	{
		log.debug("Fin du Match !", this);
		
		finMatch = true;

		// DEPENDS_ON_RULES
		// potentielle attente avant de tout désactiver afin de laisser la funny action

		// fin du match : désasser final
		try {
				deplacements.disableRotationnalFeedbackLoop();
			deplacements.disableTranslationnalFeedbackLoop();
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
		try {
			dureeMatch = 1000*Long.parseLong(config.get("temps_match"));
		}
		catch(Exception e)
		{
			log.warning(e, this);
		}
	}
	
}
