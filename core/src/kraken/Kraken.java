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
	private Thread mainThread;
	private ErrorCode errorCode = ErrorCode.NO_ERROR;
	private boolean shutdown = false;
	private boolean showGraph;

	public boolean isShutdownInProgress()
	{
		return shutdown;
	}
	
	public enum ErrorCode
	{
		NO_ERROR(0),
		END_OF_MATCH(0),
		EMERGENCY_STOP(2),
		TERMINATION_SIGNAL(3),
		DOUBLE_DESTRUCTOR(4);
		
		public final int code;
		
		private ErrorCode(int code)
		{
			this.code = code;
		}
	}
	
	/**
	 * Fonction appelé automatiquement à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le
	 * log.
	 * 
	 * @throws InterruptedException
	 * @throws ContainerException
	 * @throws InjectorException 
	 */
	public synchronized ErrorCode destructor()
	{
		if(Thread.currentThread().getId() != mainThread.getId())
		{
			log.critical("Le destructor de container doit être appelé depuis le thread principal !");
			return ErrorCode.DOUBLE_DESTRUCTOR;
		}
	
		/*
		 * Il ne faut pas appeler deux fois le destructeur
		 */
		if(nbInstances == 0)
		{
			log.critical("Double appel au destructor !");
			return ErrorCode.DOUBLE_DESTRUCTOR;
		}

		shutdown = true;

		PrintBufferInterface buffer = injector.getExistingService(PrintBufferInterface.class);
		// On appelle le destructeur du PrintBuffer
		if(buffer != null)
			buffer.destructor();

		nf(showGraph)
			injector.saveGraph("dependances.dot");

		// fermeture du log
		log.debug("Code d'erreur : " + errorCode);
		log.debug("Fermeture du log");
		log.close();
		nbInstances--;

		return errorCode;
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
		mainThread = Thread.currentThread();
		Thread.currentThread().setName("ThreadPrincipal");

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
	
			Obstacle.set(log, getService(PrintBufferInterface.class));
			Obstacle.useConfig(config);
			ArcCourbe.useConfig(config);
			if(fixedObstacles != null)
				getService(ObstaclesFixes.class).addAll(fixedObstacles);
		}
		catch(InjectorException e)
		{
			System.err.println("Fatal error : "+e);
		}
	}

	/**
	 * Créé un object de la classe demandée, ou le récupère s'il a déjà été créé
	 * S'occupe automatiquement des dépendances
	 * Toutes les classes demandées doivent implémenter Service ; c'est juste
	 * une sécurité.
	 * 
	 * @param classe
	 * @return un objet de cette classe
	 * @throws ContainerException
	 * @throws InjectorException 
	 * @throws InterruptedException
	 */
	public synchronized <S> S getService(Class<S> serviceTo)
	{
		try {
			return injector.getService(serviceTo);
		}
		catch(InjectorException e)
		{
			System.err.println("Fatal error : "+e);
			return null;
		}
	}

	public synchronized <S> S getExistingService(Class<S> classe)
	{
		return injector.getExistingService(classe);
	}

	public void interruptWithCodeError(ErrorCode code)
	{
		log.warning("Demande d'interruption avec le code : "+code);
		errorCode = code;
		mainThread.interrupt();
	}

}
