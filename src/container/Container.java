package container;

import obstacles.Capteurs;
import obstacles.ObstaclesMobilesMemory;
import obstacles.types.Obstacle;
import buffer.DataForSerialOutput;
import buffer.IncomingDataBuffer;
import buffer.IncomingHookBuffer;
import pathfinding.DStarLite;
import pathfinding.GridSpace;
import pathfinding.MoteurPhysique;
import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.MemoryManager;
import hook.HookFactory;
import exceptions.ContainerException;
import exceptions.PointSortieException;
import utils.*;
import scripts.ScriptManager;
import serial.SerialConnexion;
import strategie.Execution;
import strategie.GameState;
import strategie.Strategie;
import strategie.StrategieNotifieur;
import table.StrategieInfo;
import table.Table;
import threads.ThreadCapteurs;
import threads.ThreadConfig;
import threads.ThreadFinMatch;
import threads.ThreadGameElementDoneByEnemy;
import threads.ThreadGridSpace;
import threads.ThreadSerialInput;
import threads.ThreadSerialOutput;
import requete.RequeteSTM;
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
	@SuppressWarnings("deprecation")
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
//						((ThreadAvecStop)getService(s)).setFinThread();
						// C'est déprécié. Mais ce n'est utilisé qu'en fin de match ;
						// en fait, ce n'est utile que pour les tests
						((Thread)getService(s)).stop();
					}
				}
				threadsStarted = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		log.debug("Fermeture de la série");
		// fermeture de la connexion série
		
		SerialConnexion stm = (SerialConnexion)instanciedServices[ServiceNames.SERIE_STM.ordinal()];
		if(stm != null)
			stm.close();

		// fermeture du log
		log.debug("Fermeture du log");
		log.close();
		nbInstances--;
		System.out.println("Container détruit");
	}
	
	
	/**
	 * instancie le gestionnaire de dépendances et quelques services critiques
	 * Services instanciés:
	 * 		Config
	 * 		Log
	 * @throws ContainerException si un autre container est déjà instancié
	 */
	public Container() throws ContainerException
	{
		if(nbInstances != 0)
		{
			System.out.println("Un autre container existe déjà! Annulation du constructeur.");
			throw new ContainerException();
		}
		nbInstances++;
			// affiche la configuration avant toute autre chose
			System.out.println("== Container bootstrap ==");
			System.out.println("Loading config from current directory : " +  System.getProperty("user.dir"));

			try {
				log = (Log)getService(ServiceNames.LOG);
				config = (Config)getService(ServiceNames.CONFIG);
			} catch (PointSortieException e) {
				// Impossible
				e.printStackTrace();
			}
			log.updateConfig(config);
			log.useConfig(config);
			config.init(log);
			
			Obstacle.setLog(log);
			Obstacle.useConfig(config);
		startAllThreads();
	}

	@SuppressWarnings("unchecked")
	/**
	 * Fournit un service. Deux possibilités: soit il n'est pas encore instancié et on l'instancie.
	 * Soit il est déjà instancié et on le renvoie.
	 * @param serviceRequested
	 * @return l'instance du service demandé
	 * @throws ContainerException
	 * @throws FinMatchException
	 * @throws PointSortieException
	 */
	public Service getService(ServiceNames serviceRequested) throws ContainerException, PointSortieException
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
			instanciedServices[serviceRequested.ordinal()] = (Service)new Config();
		else if(serviceRequested == ServiceNames.CAPTEURS)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Capteurs((Log)getService(ServiceNames.LOG),
																					(Config) getService(ServiceNames.CONFIG),
																					(ObstaclesMobilesMemory)getService(ServiceNames.OBSTACLES_MOBILES_MEMORY));
		else if(serviceRequested == ServiceNames.TABLE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Table((Log)getService(ServiceNames.LOG),
																				(StrategieNotifieur)getService(ServiceNames.STRATEGIE_NOTIFIEUR));
		else if(serviceRequested == ServiceNames.PATHFINDING)
			instanciedServices[serviceRequested.ordinal()] = (Service)new DStarLite((Log)getService(ServiceNames.LOG),
																				(GridSpace)getService(ServiceNames.GRID_SPACE));

		else if(serviceRequested == ServiceNames.OBSTACLES_MOBILES_MEMORY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ObstaclesMobilesMemory((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.GRID_SPACE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new GridSpace((Log)getService(ServiceNames.LOG),
																					(ObstaclesMobilesMemory)getService(ServiceNames.OBSTACLES_MOBILES_MEMORY),
																					(Table)getService(ServiceNames.TABLE));

		else if(serviceRequested == ServiceNames.STRATEGIE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Strategie(/*(Log)getService(ServiceNames.LOG),
																				(ScriptManager)getService(ServiceNames.SCRIPT_MANAGER),
																				(GameState<RobotReal,ReadOnly>)getService(ServiceNames.REAL_GAME_STATE),
																				(HookFactory)getService(ServiceNames.HOOK_FACTORY),
																				(Pathfinding)getService(ServiceNames.PATHFINDING),
																				(MemoryManager)getService(ServiceNames.MEMORY_MANAGER)*/);		
		else if(serviceRequested == ServiceNames.INCOMING_DATA_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new IncomingDataBuffer((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.INCOMING_HOOK_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new IncomingHookBuffer((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.SERIAL_OUTPUT_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new DataForSerialOutput((Log)getService(ServiceNames.LOG));
				
		else if(serviceRequested == ServiceNames.SERIE_STM)
			instanciedServices[serviceRequested.ordinal()] = (Service)new SerialConnexion((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.EXECUTION)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Execution((Log)getService(ServiceNames.LOG),
			                                                 (Strategie)getService(ServiceNames.STRATEGIE),
  															 (ScriptManager)getService(ServiceNames.SCRIPT_MANAGER),
        													 (GameState<RobotReal,ReadWrite>)getService(ServiceNames.REAL_GAME_STATE),
        													 (RequeteSTM)getService(ServiceNames.REQUETE_STM));
		else if(serviceRequested == ServiceNames.MEMORY_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new MemoryManager((Log)getService(ServiceNames.LOG),
        													 (GameState<RobotReal,ReadOnly>)getService(ServiceNames.REAL_GAME_STATE));
		else if(serviceRequested == ServiceNames.HOOK_FACTORY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new HookFactory((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.ROBOT_REAL)
			instanciedServices[serviceRequested.ordinal()] = (Service)new RobotReal((DataForSerialOutput)getService(ServiceNames.SERIAL_OUTPUT_BUFFER),
															 (Log)getService(ServiceNames.LOG),
															 (RequeteSTM)getService(ServiceNames.REQUETE_STM));
        else if(serviceRequested == ServiceNames.REAL_GAME_STATE)
        	// ici la construction est un petit peu différente car on interdit l'instanciation publique d'un GameSTate<RobotChrono>
            instanciedServices[serviceRequested.ordinal()] = (Service) GameState.constructRealGameState((Log)getService(ServiceNames.LOG),
                                                             (Table)getService(ServiceNames.TABLE),                                                             
                                                             (RobotReal)getService(ServiceNames.ROBOT_REAL),
        													 (HookFactory)getService(ServiceNames.HOOK_FACTORY),
		 													 (DataForSerialOutput)getService(ServiceNames.SERIAL_OUTPUT_BUFFER),
															 (ObstaclesMobilesMemory)getService(ServiceNames.OBSTACLES_MOBILES_MEMORY));
		else if(serviceRequested == ServiceNames.SCRIPT_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ScriptManager(	(HookFactory)getService(ServiceNames.HOOK_FACTORY),
																					(Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.THREAD_FIN_MATCH)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadFinMatch((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG));
		else if(serviceRequested == ServiceNames.THREAD_CAPTEURS)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadCapteurs((Log)getService(ServiceNames.LOG),
																		(IncomingDataBuffer)getService(ServiceNames.INCOMING_DATA_BUFFER),
																		(Capteurs)getService(ServiceNames.CAPTEURS),
																		(RobotReal)getService(ServiceNames.ROBOT_REAL));
		else if(serviceRequested == ServiceNames.THREAD_SERIAL_INPUT)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSerialInput((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG),
																		(SerialConnexion)getService(ServiceNames.SERIE_STM),
																		(IncomingDataBuffer)getService(ServiceNames.INCOMING_DATA_BUFFER),
																		(IncomingHookBuffer)getService(ServiceNames.INCOMING_HOOK_BUFFER),
																		(RequeteSTM)getService(ServiceNames.REQUETE_STM),
																		(Table)getService(ServiceNames.TABLE),
																		(RobotReal)getService(ServiceNames.ROBOT_REAL));
		else if(serviceRequested == ServiceNames.REQUETE_STM)
			instanciedServices[serviceRequested.ordinal()] = (Service)new RequeteSTM((Log)getService(ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.THREAD_SERIAL_OUTPUT)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSerialOutput((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG),
																		(SerialConnexion)getService(ServiceNames.SERIE_STM),
																		(DataForSerialOutput)getService(ServiceNames.SERIAL_OUTPUT_BUFFER));
		else if(serviceRequested == ServiceNames.THREAD_GRID_SPACE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadGridSpace((Log)getService(ServiceNames.LOG),
																		(ObstaclesMobilesMemory)getService(ServiceNames.OBSTACLES_MOBILES_MEMORY),
																		(GridSpace)getService(ServiceNames.GRID_SPACE));
		else if(serviceRequested == ServiceNames.THREAD_GAME_ELEMENT_DONE_BY_ENEMY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadGameElementDoneByEnemy((Log)getService(ServiceNames.LOG),
																		(ObstaclesMobilesMemory)getService(ServiceNames.OBSTACLES_MOBILES_MEMORY),
																		(Table)getService(ServiceNames.TABLE),
																		(MoteurPhysique)getService(ServiceNames.MOTEUR_PHYSIQUE));
		else if(serviceRequested == ServiceNames.THREAD_CONFIG)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadConfig((Log)getService(ServiceNames.LOG),
																		(Config)getService(ServiceNames.CONFIG),
																		this);
		else if(serviceRequested == ServiceNames.STRATEGIE_INFO)
			instanciedServices[serviceRequested.ordinal()] = (Service)new StrategieInfo((Log)getService(ServiceNames.LOG), 
																		(StrategieNotifieur)getService(ServiceNames.STRATEGIE_NOTIFIEUR));
		else if(serviceRequested == ServiceNames.MOTEUR_PHYSIQUE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new MoteurPhysique((Log)getService(ServiceNames.LOG)); 		
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

	//TODO voir doc
	public void printLock()
	{
		for(ServiceNames s: ServiceNames.values())
		{
			if(instanciedServices[s.ordinal()] != null && Thread.holdsLock(instanciedServices[s.ordinal()]))
				log.debug("Lock sur "+s);
		}
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
		log.debug("Démarrage des threads fini");
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
				log.critical("Non instancié : "+s);
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
