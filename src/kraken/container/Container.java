/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package kraken.container;

import java.util.List;
import config.Config;
import graphic.ExternalPrintBuffer;
import graphic.PrintBuffer;
import graphic.PrintBufferInterface;
import injector.Injector;
import injector.InjectorException;
import kraken.config.ConfigInfoKraken;
import kraken.obstacles.types.Obstacle;
import kraken.obstacles.types.ObstaclesFixes;
import kraken.pathfinding.astar.arcs.ArcCourbe;
import kraken.robot.Speed;
import kraken.threads.ThreadName;
import kraken.threads.ThreadShutdown;
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
public class Container
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
	public synchronized ErrorCode destructor() throws InterruptedException, InjectorException
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

		// arrêt des threads
		for(ThreadName n : ThreadName.values())
			if(getService(n.c).isAlive())
				getService(n.c).interrupt();

		for(ThreadName n : ThreadName.values())
		{
			try {
				if(n == ThreadName.FENETRE && config.getBoolean(ConfigInfoKraken.GRAPHIC_PRODUCE_GIF))
				{
					log.debug("Attente de "+n);
					getService(n.c).join(120000); // spécialement pour lui qui
													// enregistre un gif…
				}
				else
				{
					log.debug("Attente de "+n);
					getService(n.c).join(1000); // on attend un peu que le thread
													// s'arrête
				}
			}
			catch(InterruptedException e)
			{
				e.printStackTrace(log.getPrintWriter());
			}
		}

		Thread.sleep(100);
		for(ThreadName n : ThreadName.values())
			if(injector.getService(n.c).isAlive())
				log.critical(n.c.getSimpleName() + " encore vivant !");

		injector.getService(ThreadShutdown.class).interrupt();

		if(showGraph)
			injector.saveGraph("dependances.dot");

		// fermeture du log
		log.debug("Code d'erreur : " + errorCode);
		log.debug("Fermeture du log");
		log.close();
		nbInstances--;

		Thread.sleep(300);
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
	public Container(List<Obstacle> fixedObstacles) throws InterruptedException, InjectorException
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

		log = injector.getService(Log.class);
		config = new Config(ConfigInfoKraken.values(), "kraken.conf", true);
		injector.addService(Config.class, config);

		log.useConfig(config);

		Speed.TEST.translationalSpeed = config.getDouble(ConfigInfoKraken.VITESSE_ROBOT_TEST) / 1000.;
		Speed.REPLANIF.translationalSpeed = config.getDouble(ConfigInfoKraken.VITESSE_ROBOT_REPLANIF) / 1000.;
		Speed.STANDARD.translationalSpeed = config.getDouble(ConfigInfoKraken.VITESSE_ROBOT_STANDARD) / 1000.;
		Speed.BASCULE.translationalSpeed = config.getDouble(ConfigInfoKraken.VITESSE_ROBOT_BASCULE) / 1000.;

		injector.addService(Container.class, this);
		
		/**
		 * Planification du hook de fermeture
		 */
		try
		{
			log.debug("Mise en place du hook d'arrêt");
			Runtime.getRuntime().addShutdownHook(getService(ThreadShutdown.class));
		}
		catch(InjectorException e)
		{
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
		}
		
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
		startAllThreads();
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
	public synchronized <S> S getService(Class<S> serviceTo) throws InjectorException
	{
		return injector.getService(serviceTo);
	}

	public synchronized <S> S getExistingService(Class<S> classe)
	{
		return injector.getExistingService(classe);
	}

	public void restartThread(ThreadName n) throws InterruptedException
	{
		try
		{
			Thread t = injector.getService(n.c);
			if(t.isAlive()) // s'il est encore en vie, on le tue
			{
				t.interrupt();
				t.join(1000);
			}
			injector.removeService(n.c);
			injector.getService(n.c).start(); // et on le redémarre
		}
		catch(InjectorException e)
		{
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
		}
	}

	/**
	 * Démarrage de tous les threads
	 */
	private void startAllThreads() throws InterruptedException
	{
		for(ThreadName n : ThreadName.values())
		{
			try
			{
				injector.getService(n.c).start();
			}
			catch(InjectorException | IllegalThreadStateException e)
			{
				log.critical("Erreur lors de la création de thread " + n + " : " + e);
				e.printStackTrace();
				e.printStackTrace(log.getPrintWriter());
			}
		}
	}

	public void interruptWithCodeError(ErrorCode code)
	{
		log.warning("Demande d'interruption avec le code : "+code);
		errorCode = code;
		mainThread.interrupt();
	}

}
