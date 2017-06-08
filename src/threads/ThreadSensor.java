package threads;

import robot.RobotReal;
import robot.cardsWrappers.SensorsCardWrapper;
import table.Table;
import utils.Sleep;

/**
 * Thread qui ajoute en continu les obstacles détectés par les capteurs
 * @author pf, Krissprolls
 *
 */

class ThreadSensor extends AbstractThread
{

	private SensorsCardWrapper capteur;
	
	// Valeurs par défaut s'il y a un problème de config
	private int capteurs_frequence = 5;
	
	ThreadSensor(RobotReal robotvrai, Table table, SensorsCardWrapper capteur)
	{
		super(config, log);
		this.capteur = capteur;
		Thread.currentThread().setPriority(2);
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de capteurs", this);
//		boolean marche_arriere = false;
		updateConfig();
		
		while(!ThreadTimer.match_demarre)
		{
			if(stopThreads)
			{
				log.debug("Stoppage du thread capteurs", this);
				return;
			}
			Sleep.sleep(50);
		}
		
		log.debug("Activation des capteurs", this);
		while(!ThreadTimer.fin_match)
		{
			if(stopThreads)
			{
				log.debug("Stoppage du thread capteurs", this);
				return;
			}

			// affiche la distance mesurée par l'ultrason
			int distance = capteur.mesurer();
			log.debug("Distance selon ultrason: "+distance, this);
			if (distance > 0 && distance < 70)
				log.debug("Câlin !", this);
			
			
			
			Sleep.sleep((long)(1000./capteurs_frequence));
			
		}
        log.debug("Fin du thread de capteurs", this);
		
	}
	
	public void updateConfig()
	{
			capteurs_frequence = Integer.parseInt(config.get("capteurs_frequence"));
	}
	
}
