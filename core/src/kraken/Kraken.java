/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken;

import java.util.List;
import config.Config;
import graphic.ExternalPrintBuffer;
import graphic.PrintBuffer;
import graphic.PrintBufferInterface;
import injector.Injector;
import injector.InjectorException;
import kraken.obstacles.container.ObstaclesFixes;
import kraken.obstacles.types.Obstacle;
import kraken.pathfinding.astar.AStarCourbe;
import kraken.pathfinding.astar.arcs.ArcCourbe;
import kraken.utils.*;

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
	private boolean showGraph;

	/**
	 * Fonction appelé automatiquement à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le
	 * log.
	 * 
	 * @throws InterruptedException
	 * @throws ContainerException
	 * @throws InjectorException 
	 */
	public synchronized void destructor()
	{	
		/*
		 * Il ne faut pas appeler deux fois le destructeur
		 */
		if(nbInstances == 0)
			return;

		PrintBufferInterface buffer = injector.getExistingService(PrintBufferInterface.class);
		// On appelle le destructeur du PrintBuffer
		if(buffer != null)
			buffer.destructor();

		// fermeture du log
		log.debug("Fermeture du log");
		log.close();
		nbInstances--;
	}

	/**
	 * Instancie le gestionnaire de dépendances et quelques services critiques
	 * (log et config qui sont interdépendants)
	 * 
	 * @throws ContainerException si un autre container est déjà instancié
	 * @throws InterruptedException
	 * @throws InjectorException 
	 */
	public Kraken(List<Obstacle> fixedObstacles)
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
			config = new Config(ConfigInfoKraken.values(), "kraken.conf", true);
			injector.addService(Config.class, config);
	
			log.useConfig(config);
	
			injector.addService(Kraken.class, this);
			
			showGraph = config.getBoolean(ConfigInfoKraken.GENERATE_DEPENDENCY_GRAPH);
	
			if(showGraph)
				log.warning("Le graphe de dépendances va être généré !");
	
			if(config.getBoolean(ConfigInfoKraken.GRAPHIC_EXTERNAL))
				injector.addService(PrintBufferInterface.class, injector.getService(ExternalPrintBuffer.class));
			else
				injector.addService(PrintBufferInterface.class, injector.getService(PrintBuffer.class));
	
			Obstacle.set(log, injector.getService(PrintBufferInterface.class));
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
