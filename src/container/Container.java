package container;

import obstacles.ObstacleManager;
import pathfinding.*;
import hook.types.HookFactory;
import enums.ServiceNames;
import enums.ServiceNames.TypeService;
import exceptions.ContainerException;
import exceptions.FinMatchException;
import exceptions.ThreadException;
import exceptions.serial.SerialManagerException;
import utils.*;
import scripts.ScriptManager;
import strategie.Execution;
import strategie.GameState;
import strategie.MemoryManager;
import table.Table;
import threads.AbstractThread;
import threads.ThreadSensor;
import threads.ThreadStrategy;
import threads.ThreadTimer;
import robot.Locomotion;
import robot.RobotReal;
import robot.cardsWrappers.ActuatorCardWrapper;
import robot.cardsWrappers.LocomotionCardWrapper;
import robot.cardsWrappers.SensorsCardWrapper;
import robot.serial.SerialManager;
import robot.serial.SerialConnexion;


/**
 * 
 * Gestionnaire de la durée de vie des objets dans le code.
 * Permet à n'importe quelle classe implémentant l'interface "Service" d'appeller d'autres instances de services via son constructeur.
 * Une classe implémentant service n'est instanciée que par la classe "Container"
 * 
 * @author pf
 */
public class Container
{

	// liste des services déjà instanciés. Contient au moins Config et Log. Les autres services appelables seront présents s'ils ont déjà étés appellés au moins une fois
	private Service[] instanciedServices = new Service[ServiceNames.values().length];
	
	//gestion des log
	private Log log;
	
	//gestion de la configuration du robot
	private Config config;

	/**
	 * Fonction à appeler à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le log.
	 */
	public void destructor()
	{
		// stoppe les différents threads
		stopAllThreads();
		Sleep.sleep(700); // attends qu'ils soient bien tous arrètés
		
		// coupe les connexions séries
		SerialManager serialmanager;
		try {
			serialmanager = (SerialManager)getService(ServiceNames.SERIAL_MANAGER);
			serialmanager.closeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ferme le log
		log.close();
	}
	
	
	/**
	 * instancie le gestionnaire de dépendances et quelques services critiques
	 * Services instanciés:
	 * 		Config
	 * 		Log
	 * @throws ContainerException en cas de problème avec le fichier de configuration ou le système de log
	 */
	public Container() throws ContainerException
	{
		try
		{
			// affiche la configuration avant toute autre chose
			System.out.println("== Container bootstrap ==");
			System.out.println("Loading config from current directory : " +  System.getProperty("user.dir"));
			
			//parse le ficher de configuration.
			instanciedServices[ServiceNames.CONFIG.ordinal()] = (Service)new Config("./config/");
			config = (Config)instanciedServices[ServiceNames.CONFIG.ordinal()];
			
			// démarre le système de log
			instanciedServices[ServiceNames.LOG.ordinal()] = (Service)new Log(config);
			log = (Log)instanciedServices[ServiceNames.LOG.ordinal()];
		}
		catch(Exception e)
		{
			throw new ContainerException();
		}
	}

	@SuppressWarnings("unchecked")
	public Service getService(ServiceNames serviceRequested) throws ContainerException, ThreadException, SerialManagerException, FinMatchException
	{
    	// instancie le service demandé lors de son premier appel 
    	
    	// si le service est déja instancié, on ne le réinstancie pas
		if(instanciedServices[serviceRequested.ordinal()] != null)
			;
		
		// Si le service n'est pas encore instancié, on l'instancie avant de le retourner à l'utilisateur
		else if(serviceRequested == ServiceNames.TABLE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Table((Log)getService(ServiceNames.LOG),
																				(Config)getService(ServiceNames.CONFIG));
		else if(serviceRequested == ServiceNames.OBSTACLE_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ObstacleManager((Log)getService(ServiceNames.LOG),
																				(Config)getService(ServiceNames.CONFIG),
																				(Table)getService(ServiceNames.TABLE));
		else if(serviceRequested == ServiceNames.A_STAR)
			instanciedServices[serviceRequested.ordinal()] = (Service)new AStar((Log)getService(ServiceNames.LOG),
																				(Config)getService(ServiceNames.CONFIG),
																				(PathfindingArcManager)getService(ServiceNames.PATHFINDING_ARC_MANAGER),
																				(StrategyArcManager)getService(ServiceNames.STRATEGY_ARC_MANAGER),
																				(MemoryManager)getService(ServiceNames.MEMORY_MANAGER));
		else if(serviceRequested == ServiceNames.GRID_SPACE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new GridSpace((Log)getService(ServiceNames.LOG),
																				(Config)getService(ServiceNames.CONFIG),
																				(ObstacleManager)getService(ServiceNames.OBSTACLE_MANAGER));
		else if(serviceRequested == ServiceNames.SERIAL_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new SerialManager((Log)getService(ServiceNames.LOG),
																				(Config)getService(ServiceNames.CONFIG));
		
		else if(serviceRequested.getType() == TypeService.SERIE) // les séries
		{
			try {
				SerialManager serialmanager = (SerialManager)getService(ServiceNames.SERIAL_MANAGER);
				instanciedServices[serviceRequested.ordinal()] = (Service)serialmanager.getSerial(serviceRequested);
			}
			catch(Exception e)
			{
				log.critical("Série introuvable!", this);
				e.printStackTrace();
			}
		}
		else if(serviceRequested == ServiceNames.LOCOMOTION_CARD_WRAPPER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new LocomotionCardWrapper((Log)getService(ServiceNames.LOG),
															 (SerialConnexion)getService(ServiceNames.SERIE_ASSERVISSEMENT));
		else if(serviceRequested == ServiceNames.SENSORS_CARD_WRAPPER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new SensorsCardWrapper((Config)getService(ServiceNames.CONFIG),
			                                                 (Log)getService(ServiceNames.LOG),
			                                                 (SerialConnexion)getService(ServiceNames.SERIE_CAPTEURS_ACTIONNEURS));
		else if(serviceRequested == ServiceNames.ACTUATOR_CARD_WRAPPER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ActuatorCardWrapper((Config)getService(ServiceNames.CONFIG),
															 (Log)getService(ServiceNames.LOG),
															 (SerialConnexion)getService(ServiceNames.SERIE_CAPTEURS_ACTIONNEURS));
		else if(serviceRequested == ServiceNames.MEMORY_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new MemoryManager((Log)getService(ServiceNames.LOG),
															 (Config)getService(ServiceNames.CONFIG),
        													 (GameState<RobotReal>)getService(ServiceNames.REAL_GAME_STATE));
		else if(serviceRequested == ServiceNames.HOOK_FACTORY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new HookFactory((Config)getService(ServiceNames.CONFIG),
															 (Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.ROBOT_REAL)
			instanciedServices[serviceRequested.ordinal()] = (Service)new RobotReal((ActuatorCardWrapper)getService(ServiceNames.ACTUATOR_CARD_WRAPPER),
															 (Locomotion)getService(ServiceNames.LOCOMOTION),
															 (Table)getService(ServiceNames.TABLE),
															 (Config)getService(ServiceNames.CONFIG),
															 (Log)getService(ServiceNames.LOG));
        else if(serviceRequested == ServiceNames.LOCOMOTION)
            instanciedServices[serviceRequested.ordinal()] = (Service)new Locomotion((Log)getService(ServiceNames.LOG),
                                                             (Config)getService(ServiceNames.CONFIG),
                                                             (LocomotionCardWrapper)getService(ServiceNames.LOCOMOTION_CARD_WRAPPER),
															 (ObstacleManager)getService(ServiceNames.OBSTACLE_MANAGER));
        else if(serviceRequested == ServiceNames.REAL_GAME_STATE)
        	// ici la construction est un petit peu différente car on interdit l'instanciation publique d'un GameSTate<RobotChrono>
            instanciedServices[serviceRequested.ordinal()] = (Service) GameState.constructRealGameState(  (Config)getService(ServiceNames.CONFIG),
                                                             (Log)getService(ServiceNames.LOG),
                                                             (Table)getService(ServiceNames.TABLE),
                                                             (GridSpace)getService(ServiceNames.GRID_SPACE),                                                             
                                                             (RobotReal)getService(ServiceNames.ROBOT_REAL),
        													 (HookFactory)getService(ServiceNames.HOOK_FACTORY));
		else if(serviceRequested == ServiceNames.EXECUTION)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Execution((Log)getService(ServiceNames.LOG),
			                                                 (Config)getService(ServiceNames.CONFIG),
			                                                 (AStar)getService(ServiceNames.A_STAR),
			                                                 (GameState<RobotReal>)getService(ServiceNames.REAL_GAME_STATE),
			                                                 (ScriptManager)getService(ServiceNames.SCRIPT_MANAGER),
			                                                 (HookFactory)getService(ServiceNames.HOOK_FACTORY),
        													 (ThreadStrategy)getService(ServiceNames.THREAD_STRATEGY));
		else if(serviceRequested == ServiceNames.SCRIPT_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ScriptManager(	(HookFactory)getService(ServiceNames.HOOK_FACTORY),
																					(Config)getService(ServiceNames.CONFIG),
																					(Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.THREAD_TIMER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadTimer((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG),
																		(ObstacleManager)getService(ServiceNames.OBSTACLE_MANAGER),
																		(SensorsCardWrapper)getService(ServiceNames.SENSORS_CARD_WRAPPER),
																		(LocomotionCardWrapper)getService(ServiceNames.LOCOMOTION_CARD_WRAPPER),
																		(SerialManager)getService(ServiceNames.SERIAL_MANAGER));
		else if(serviceRequested == ServiceNames.THREAD_SENSOR)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSensor((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG),
																		(RobotReal)getService(ServiceNames.ROBOT_REAL),
																		(GridSpace)getService(ServiceNames.GRID_SPACE),
																		(ObstacleManager)getService(ServiceNames.OBSTACLE_MANAGER),
																		(SensorsCardWrapper)getService(ServiceNames.SENSORS_CARD_WRAPPER));
		else if(serviceRequested == ServiceNames.THREAD_STRATEGY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadStrategy((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG),
																		(ScriptManager)getService(ServiceNames.SCRIPT_MANAGER),
																		(AStar)getService(ServiceNames.A_STAR),
																		(GameState<RobotReal>)getService(ServiceNames.REAL_GAME_STATE),
																		(HookFactory)getService(ServiceNames.HOOK_FACTORY));
		else if(serviceRequested == ServiceNames.PATHFINDING_ARC_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new PathfindingArcManager((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG));
		else if(serviceRequested == ServiceNames.STRATEGY_ARC_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new StrategyArcManager((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG),
																		(ScriptManager)getService(ServiceNames.SCRIPT_MANAGER),
																		(GameState<RobotReal>)getService(ServiceNames.REAL_GAME_STATE),
																		(HookFactory)getService(ServiceNames.HOOK_FACTORY));
		
		// si le service demandé n'est pas connu, alors on log une erreur.
		else
		{
			log.critical("Erreur de getService pour le service (service inconnu): "+serviceRequested, this);
			throw new ContainerException();
		}
		
		// retourne le service en mémoire à l'utilisateur
		return instanciedServices[serviceRequested.ordinal()];
	}	

	/**
	 * Demande au thread manager de démarrer tous les threads
	 */
	public void startAllThreads()
	{
		try {
			getService(ServiceNames.THREAD_SENSOR);
			((Thread)instanciedServices[ServiceNames.THREAD_SENSOR.ordinal()]).start();

			getService(ServiceNames.THREAD_TIMER);
			((Thread)instanciedServices[ServiceNames.THREAD_TIMER.ordinal()]).start();

			getService(ServiceNames.THREAD_STRATEGY);
			((Thread)instanciedServices[ServiceNames.THREAD_STRATEGY.ordinal()]).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Méthode qui affiche le nom de tous les services non-instanciés.
	 * Renvoie true si cette liste est vide
	 */
	public boolean afficheNonInstancies()
	{
		boolean out = true;
		
		for(ServiceNames s : ServiceNames.values())
			if(instanciedServices[s.ordinal()] == null)
			{
				out = false;
				log.critical(s, this);
			}
		return out;
	}

	/**
	 * Arrête tous les threads
	 * Le thread principal (appellant cette méthode) continue son exécution
	 */
	public void stopAllThreads()
	{
		AbstractThread.stopAllThread();
	}
	
}
