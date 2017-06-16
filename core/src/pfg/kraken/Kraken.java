/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.List;
import config.Config;
import graphic.Fenetre;
import graphic.AbstractPrintBuffer;
import injector.Injector;
import injector.InjectorException;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.EmptyDynamicObstacles;
import pfg.kraken.obstacles.container.ObstaclesFixes;
import pfg.kraken.obstacles.types.Obstacle;
import pfg.kraken.pathfinding.astar.AStarCourbe;
import pfg.kraken.pathfinding.astar.arcs.ArcCourbe;
import pfg.kraken.utils.*;

/**
 * 
 * Gestionnaire de la durée de vie des objets dans le code.
 * Permet à n'importe quelle classe implémentant l'interface "Service"
 * d'appeller d'autres instances de services via son constructeur.
 * Une classe implémentant Service n'est instanciée que par la classe
 * "Container"
 * 
 * @author pf
 */
public class Kraken
{
	private Log log;
	private Config config;
	private Injector injector;

	private static int nbInstances = 0;

	/**
	 * Fonction appelé automatiquement à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le
	 * log.
	 */
	public synchronized void destructor()
	{	
		/*
		 * Il ne faut pas appeler deux fois le destructeur
		 */
		if(nbInstances == 0)
			return;

		AbstractPrintBuffer buffer = injector.getExistingService(AbstractPrintBuffer.class);
		// On appelle le destructeur du PrintBuffer
		if(buffer != null)
			buffer.destructor();

		// fermeture du log
		log.debug("Fermeture du log");
		log.close();
		nbInstances--;
	}
	
	public Kraken(List<Obstacle> fixedObstacles)
	{
		this(fixedObstacles, new EmptyDynamicObstacles());
	}
	
	/**
	 * Instancie le gestionnaire de dépendances et quelques services critiques
	 * (log et config qui sont interdépendants)
	 */
	public Kraken(List<Obstacle> fixedObstacles, DynamicObstacles dynObs)
	{
		/**
		 * On vérifie qu'il y ait un seul container à la fois
		 */
		if(nbInstances != 0)
		{
			log.critical("Un autre container existe déjà! Annulation du constructeur.");
			int z = 0;
			z = 1/z;
			return;
		}

		nbInstances++;

		injector = new Injector();

		try {
			log = injector.getService(Log.class);
			config = new Config(ConfigInfoKraken.values(), "kraken.conf", false);
			injector.addService(Config.class, config);
			injector.addService(DynamicObstacles.class, dynObs);
	
			log.useConfig(config);
	
			injector.addService(Kraken.class, this);
	
			if(config.getBoolean(ConfigInfoKraken.GRAPHIC_ENABLE))
			{
				Fenetre f = injector.getService(Fenetre.class);
//				if(config.getBoolean(ConfigInfoKraken.GRAPHIC_EXTERNAL))
//					injector.addService(PrintBufferInterface.class, f.getBu);
//				else
					injector.addService(AbstractPrintBuffer.class, f.getPrintBuffer());
			}
			else
			{
				ConfigInfoKraken.unsetGraphic();
				config.reload();
				injector.addService(AbstractPrintBuffer.class, null);
			}
	
			Obstacle.set(log, injector.getService(AbstractPrintBuffer.class));
			Obstacle.useConfig(config);
			ArcCourbe.useConfig(config);
			if(fixedObstacles != null)
				injector.getService(ObstaclesFixes.class).addAll(fixedObstacles);
		}
		catch(InjectorException e)
		{
			System.err.println("Fatal error : "+e);
		}
	}

	public AStarCourbe getAStar()
	{
		try
		{
			return injector.getService(AStarCourbe.class);
		}
		catch(InjectorException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	protected Injector getInjector()
	{
		return injector;
	}
}
