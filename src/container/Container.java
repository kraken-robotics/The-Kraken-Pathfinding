package container;

import obstacles.Obstacle;
import buffer.IncomingDataBuffer;
import buffer.IncomingHookBuffer;
import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.MemoryManager;
import planification.Chemin;
import planification.Pathfinding;
import hook.HookFactory;
import exceptions.ContainerException;
import exceptions.FinMatchException;
import exceptions.PointSortieException;
import exceptions.SerialConnexionException;
import exceptions.ThreadException;
import utils.*;
import scripts.ScriptManager;
import serial.SerialConnexion;
import strategie.Execution;
import strategie.GameState;
import strategie.Strategie;
import strategie.StrategieNotifieur;
import table.GridSpace;
import table.ObstacleManager;
import table.StrategieInfo;
import table.Table;
import threads.ThreadAvecStop;
import threads.ThreadFinMatch;
import threads.ThreadGridSpace;
import threads.ThreadGridSpace2;
import threads.ThreadObstacleManager;
import threads.ThreadPathfinding;
import threads.ThreadSerial;
import threads.ThreadStrategie;
import threads.ThreadStrategieInfo;
import threads.ThreadTable;
import threads.ThreadPeremption;
import robot.RobotReal;


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
	private Config config;
	
	private static int nbInstances = 0;
	private boolean threadsStarted = false;
	
	/**
	 * Fonction à appeler à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le log.
	 */
	public void destructor()
	{
		// arrêt des threads
		if(threadsStarted)
			try {
				for(ServiceNames s: ServiceNames.values())
				{
					if(s.isThread())
					{
						log.debug("Arrêt de "+s);
						((ThreadAvecStop)getService(s)).setFinThread();
					}
				}
				threadsStarted = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		
		// fermeture de la connexion série
		SerialConnexion stm = (SerialConnexion)getInstanciedService(ServiceNames.SERIE_STM);
		if(stm != null)
			stm.close();

		// fermeture du log
		log.close();
		nbInstances--;
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
		if(nbInstances != 0)
		{
			System.out.println("Un autre container existe déjà! Annulation du constructeur.");
			throw new ContainerException();
		}
		nbInstances++;
		try
		{
			// affiche la configuration avant toute autre chose
			System.out.println("== Container bootstrap ==");
			System.out.println("Loading config from current directory : " +  System.getProperty("user.dir"));

			log = (Log)getService(ServiceNames.LOG);
			config = (Config)getService(ServiceNames.CONFIG);
			log.updateConfig(config);
			log.useConfig(config);
			config.init();
			
			Obstacle.setLog(log);
			Obstacle.useConfig(config);
		}
		catch(Exception e)
		{
			throw new ContainerException();
		}
		startAllThreads();
	}

	@SuppressWarnings("unchecked")
	/**
	 * Fournit un service. Deux possibilités: soit il n'est pas encore instancié et on l'instancie.
	 * Soit il est déjà instancié et on le renvoie.
	 * @param serviceRequested
	 * @return
	 * @throws ContainerException
	 * @throws ThreadException
	 * @throws SerialManagerException
	 * @throws FinMatchException
	 * @throws PointSortieException
	 */
	public Service getService(ServiceNames serviceRequested) throws ContainerException, ThreadException, SerialConnexionException, FinMatchException, PointSortieException
	{
    	// instancie le service demandé lors de son premier appel 
    	boolean updateConfig = true;
		
    	// si le service est déja instancié, on ne le réinstancie pas
		if(instanciedServices[serviceRequested.ordinal()] != null)
			updateConfig = false;

		// Si le service n'est pas encore instancié, on l'instancie avant de le retourner à l'utilisateur
		else if(serviceRequested == ServiceNames.LOG)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Log();
		else if(serviceRequested == ServiceNames.CONFIG)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Config((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.TABLE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Table((Log)getService(ServiceNames.LOG),
																				(StrategieNotifieur)getService(ServiceNames.STRATEGIE_NOTIFIEUR));
		else if(serviceRequested == ServiceNames.OBSTACLE_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ObstacleManager((Log)getService(ServiceNames.LOG),
																				(Table)getService(ServiceNames.TABLE));
		else if(serviceRequested == ServiceNames.PATHFINDING)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Pathfinding((Log)getService(ServiceNames.LOG),
																				(MemoryManager)getService(ServiceNames.MEMORY_MANAGER),
																				(GameState<RobotReal,ReadOnly>)getService(ServiceNames.REAL_GAME_STATE));
		else if(serviceRequested == ServiceNames.CHEMIN_ACTUEL)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Chemin((Log)getService(ServiceNames.LOG),
																				(Pathfinding)getService(ServiceNames.PATHFINDING));
		else if(serviceRequested == ServiceNames.GRID_SPACE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new GridSpace((Log)getService(ServiceNames.LOG),
																				(ObstacleManager)getService(ServiceNames.OBSTACLE_MANAGER),
																				(StrategieNotifieur)getService(ServiceNames.STRATEGIE_NOTIFIEUR));

		else if(serviceRequested == ServiceNames.STRATEGIE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Strategie((Log)getService(ServiceNames.LOG),
																				(ScriptManager)getService(ServiceNames.SCRIPT_MANAGER),
																				(GameState<RobotReal,ReadOnly>)getService(ServiceNames.REAL_GAME_STATE),
																				(HookFactory)getService(ServiceNames.HOOK_FACTORY),
																				(Pathfinding)getService(ServiceNames.PATHFINDING),
																				(MemoryManager)getService(ServiceNames.MEMORY_MANAGER));		
		else if(serviceRequested == ServiceNames.INCOMING_DATA_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new IncomingDataBuffer((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.INCOMING_HOOK_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new IncomingHookBuffer((Log)getService(ServiceNames.LOG));
				
		else if(serviceRequested == ServiceNames.SERIE_STM)
			instanciedServices[serviceRequested.ordinal()] = (Service)new SerialConnexion((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.EXECUTION)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Execution((Log)getService(ServiceNames.LOG),
			                                                 (Strategie)getService(ServiceNames.STRATEGIE),
  															 (ScriptManager)getService(ServiceNames.SCRIPT_MANAGER),
        													 (GameState<RobotReal,ReadWrite>)getService(ServiceNames.REAL_GAME_STATE));
		else if(serviceRequested == ServiceNames.MEMORY_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new MemoryManager((Log)getService(ServiceNames.LOG),
        													 (GameState<RobotReal,ReadOnly>)getService(ServiceNames.REAL_GAME_STATE));
		else if(serviceRequested == ServiceNames.HOOK_FACTORY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new HookFactory((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.ROBOT_REAL)
			instanciedServices[serviceRequested.ordinal()] = (Service)new RobotReal((SerialConnexion)getService(ServiceNames.SERIE_STM),
															 (Log)getService(ServiceNames.LOG));
        else if(serviceRequested == ServiceNames.REAL_GAME_STATE)
        	// ici la construction est un petit peu différente car on interdit l'instanciation publique d'un GameSTate<RobotChrono>
            instanciedServices[serviceRequested.ordinal()] = (Service) GameState.constructRealGameState((Log)getService(ServiceNames.LOG),
                                                             (GridSpace)getService(ServiceNames.GRID_SPACE),                                                             
                                                             (RobotReal)getService(ServiceNames.ROBOT_REAL),
        													 (HookFactory)getService(ServiceNames.HOOK_FACTORY));
		else if(serviceRequested == ServiceNames.SCRIPT_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ScriptManager(	(HookFactory)getService(ServiceNames.HOOK_FACTORY),
																					(Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.THREAD_PEREMPTION)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadPeremption((Log)getService(ServiceNames.LOG),
																		(ObstacleManager)getService(ServiceNames.OBSTACLE_MANAGER),
																		(IncomingDataBuffer)getService(ServiceNames.INCOMING_DATA_BUFFER));
		else if(serviceRequested == ServiceNames.THREAD_FIN_MATCH)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadFinMatch((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG));
		else if(serviceRequested == ServiceNames.THREAD_STRATEGIE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadStrategie((Log)getService(ServiceNames.LOG),
																		(StrategieNotifieur)getService(ServiceNames.STRATEGIE_NOTIFIEUR),
																		(Strategie)getService(ServiceNames.STRATEGIE));
		else if(serviceRequested == ServiceNames.THREAD_PATHFINDING)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadPathfinding((Log)getService(ServiceNames.LOG),
																		(GridSpace)getService(ServiceNames.GRID_SPACE),
																		(Chemin)getService(ServiceNames.CHEMIN_ACTUEL));
		else if(serviceRequested == ServiceNames.THREAD_GRID_SPACE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadGridSpace((Log)getService(ServiceNames.LOG),
																		(ObstacleManager)getService(ServiceNames.OBSTACLE_MANAGER),
																		(GridSpace)getService(ServiceNames.GRID_SPACE));
		else if(serviceRequested == ServiceNames.THREAD_GRID_SPACE2)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadGridSpace2((Log)getService(ServiceNames.LOG),
																		(Table)getService(ServiceNames.TABLE),
																		(GridSpace)getService(ServiceNames.GRID_SPACE));
		else if(serviceRequested == ServiceNames.THREAD_OBSTACLE_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadObstacleManager((Log)getService(ServiceNames.LOG),
																		(IncomingDataBuffer)getService(ServiceNames.INCOMING_DATA_BUFFER),
																		(ObstacleManager)getService(ServiceNames.OBSTACLE_MANAGER));
		else if(serviceRequested == ServiceNames.THREAD_TABLE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadTable((Log)getService(ServiceNames.LOG),
																		(IncomingHookBuffer)getService(ServiceNames.INCOMING_HOOK_BUFFER),
																		(Table)getService(ServiceNames.TABLE));
		else if(serviceRequested == ServiceNames.THREAD_STRATEGIE_INFO)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadStrategieInfo((Log)getService(ServiceNames.LOG),
																		(StrategieInfo)getService(ServiceNames.STRATEGIE_INFO),
																		(ObstacleManager)getService(ServiceNames.OBSTACLE_MANAGER));
		else if(serviceRequested == ServiceNames.THREAD_SERIE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSerial((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG),
																		(SerialConnexion)getService(ServiceNames.SERIE_STM),
																		(IncomingDataBuffer)getService(ServiceNames.INCOMING_DATA_BUFFER),
																		(IncomingHookBuffer)getService(ServiceNames.INCOMING_HOOK_BUFFER),
																		(HookFactory)getService(ServiceNames.HOOK_FACTORY));
		else if(serviceRequested == ServiceNames.STRATEGIE_INFO)
			instanciedServices[serviceRequested.ordinal()] = (Service)new StrategieInfo((Log)getService(ServiceNames.LOG), 
																		(StrategieNotifieur)getService(ServiceNames.STRATEGIE_NOTIFIEUR));
		
		else if(serviceRequested == ServiceNames.STRATEGIE_NOTIFIEUR)
			instanciedServices[serviceRequested.ordinal()] = (Service)new StrategieNotifieur();		
		// si le service demandé n'est pas connu, alors on log une erreur.
		else
		{
			log.critical("Erreur de getService pour le service (service inconnu): "+serviceRequested);
			throw new ContainerException();
		}
		
		if(updateConfig && config != null)
		{
			instanciedServices[serviceRequested.ordinal()].useConfig(config);
			instanciedServices[serviceRequested.ordinal()].updateConfig(config);
		}
		
		// retourne le service en mémoire à l'utilisateur
		return instanciedServices[serviceRequested.ordinal()];
	}	

	/**
	 * Démarrage de tous les threads
	 */
	private void startAllThreads()
	{
		if(threadsStarted)
			return;
		try {
			for(ServiceNames s: ServiceNames.values())
			{
				if(s.isThread())
				{
					log.debug("Démarrage de "+s);
					((Thread)getService(s)).start();
				}
			}
			threadsStarted = true;
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
				log.critical(s);
			}
		return out;
	}


	/**
	 * Renvoie le service demandé s'il est déjà instancié, null sinon.
	 * Utilisé pour la mise à jour de la config.
	 * @param s
	 * @return
	 */
	public Service getInstanciedService(ServiceNames serviceRequested) {
		return instanciedServices[serviceRequested.ordinal()];
	}
	
}
