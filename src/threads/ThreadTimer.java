package threads;

import exceptions.serial.SerialConnexionException;
import robot.cardsWrappers.LocomotionCardWrapper;
import robot.cardsWrappers.SensorsCardWrapper;
import smartMath.Vec2;
import table.Table;
import utils.Sleep;

/**
 * Thread qui s'occupe de la gestion du temps: début du match, péremption des obstacles
 * C'est lui qui active les capteurs en début de match.
 * @author pf
 *
 */

public class ThreadTimer extends AbstractThread
{

	// Dépendance
	private Table table;
	private SensorsCardWrapper capteur;
	private LocomotionCardWrapper deplacements;
	
	public static boolean match_demarre = false;
	public static boolean fin_match = false;
	public static long date_debut;
	public static long duree_match = 90000;
	public static int obstacleRefreshInterval = 500; // temps en ms entre deux appels par le thread timer du rafraichissement des obstacles de la table
		
	ThreadTimer(Table table, SensorsCardWrapper capteur, LocomotionCardWrapper deplacements)
	{
		this.table = table;
		this.capteur = capteur;
		this.deplacements = deplacements;
		
		updateConfig();
		Thread.currentThread().setPriority(1);
	}

	@Override
	public void run()
	{
		log.debug("Lancement du thread timer", this);

		// allume les capteurs
		config.set("capteurs_on", false);
		capteur.updateConfig();	
		
		// Attente du démarrage du match
		while(!capteur.demarrage_match() && !match_demarre)
		{
			if(stopThreads)
			{
				log.debug("Arrêt du thread timer avant le début du match", this);
				return;
			}
			Sleep.sleep(50);
		}
		date_debut = System.currentTimeMillis();
		match_demarre = true;

		config.set("capteurs_on", true);
		capteur.updateConfig();

		log.debug("LE MATCH COMMENCE !", this);


		// Le match à démarré. On retire périodiquement les obstacles périmés
		while(System.currentTimeMillis() - date_debut < duree_match)
		{
			if(stopThreads)
			{
				log.debug("Arrêt du thread timer demandé durant le match", this);
				return;
			}
			table.gestionobstacles.supprimerObstaclesPerimes(System.currentTimeMillis());
			
			try
			{
				Thread.sleep(obstacleRefreshInterval);
			}
			catch(Exception e)
			{
				log.warning(e.toString(), this);
			}
		}

		onMatchEnded();
		
		log.debug("Fin du thread timer", this);
		
	}
	
	private void onMatchEnded()
	{

		log.debug("Fin du Match !", this);

		// Le match est fini, désasservissement
		fin_match = true;

		try {
			deplacements.immobilise();
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		}

		try {
			// on s'oriente pour tirer le fillet
			double[] infos = deplacements.getCurrentPositionAndOrientation();
			Vec2 position = new Vec2((int) infos[0], (int) infos[1]);
			Vec2 positionMammouth1 = new Vec2(-750, 2000);
			Vec2 positionMammouth2 = new Vec2(750, 2000);
			double angle;
			if (position.SquaredDistance(positionMammouth1) < position
					.SquaredDistance(positionMammouth2))
				angle = Math.atan2(positionMammouth1.y - position.y,
						positionMammouth1.x - position.x);
			else
				angle = Math.atan2(positionMammouth2.y - position.y,
						positionMammouth2.x - position.x);
			deplacements.immobilise();
			deplacements.turn(angle - Math.PI / 2); // le filet est sur le coté
													// gauche

			// fin du match : désasser final
			try {
				deplacements.disableRotationnalFeedbackLoop();
				deplacements.disableTranslationnalFeedbackLoop();
			} catch (SerialConnexionException e) {
				e.printStackTrace();
			}
			deplacements.closeLocomotion();

		} catch (SerialConnexionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	public long temps_restant()
	{
		return date_debut + duree_match - System.currentTimeMillis();
	}
	
	public void updateConfig()
	{
		// facteur 1000 car temps_match est en secondes et duree_match en ms
		try {
			duree_match = 1000*Long.parseLong(config.get("temps_match"));
		}
		catch(Exception e)
		{
			log.warning(e, this);
		}
	}
	
}
