package container;

import obstacles.Capteurs;
import obstacles.ClothoidesComputer;
import obstacles.MoteurPhysique;
import obstacles.ObstaclesMemory;
import obstacles.types.Obstacle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import buffer.DataForSerialOutput;
import buffer.IncomingDataBuffer;
import buffer.IncomingHookBuffer;
import pathfinding.CheminPathfinding;
import pathfinding.GameState;
import pathfinding.astarCourbe.AStarCourbe;
import pathfinding.astarCourbe.AStarCourbeArcManager;
import pathfinding.astarCourbe.AStarCourbeMemoryManager;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import hook.HookFactory;
import exceptions.ContainerException;
import exceptions.PointSortieException;
import utils.*;
import scripts.ScriptManager;
import strategie.Execution;
import strategie.StrategieInfo;
import strategie.StrategieNotifieur;
import strategie.astar.AStar;
import strategie.astar.AStarArcManager;
import strategie.astar.AStarMemoryManager;
import strategie.astar.GridSpaceStrategie;
import strategie.lpastar.LPAStar;
import table.Table;
import threads.ThreadCapteurs;
import threads.ThreadConfig;
import threads.ThreadEvitement;
import threads.ThreadFinMatch;
import threads.ThreadGameElementDoneByEnemy;
import threads.ThreadPathfinding;
import threads.ThreadPeremption;
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
	
	private static final boolean showGraph = true;
	private FileWriter fw;

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
//						((ThreadAvecStop)getServicePrivate(s)).setFinThread();
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

		if(showGraph)
		{
			try {
				fw.write("}\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
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
			log = (Log)getServiceRecursif(ServiceNames.LOG);
			config = (Config)getServiceRecursif(ServiceNames.CONFIG);
		} catch (PointSortieException e) {
			// Impossible
			e.printStackTrace();
		}
		log.updateConfig(config);
		log.useConfig(config);
		config.init(log);
		
		Obstacle.setLog(log);
		Obstacle.useConfig(config);
		
		if(showGraph)
		{
			try {
				fw = new FileWriter(new File("dependances.dot"));
				fw.write("digraph dependancesJava {\n");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Service getService(ServiceNames serviceTo) throws ContainerException, PointSortieException
	{
		return getServicePrivate(null, serviceTo);
	}
	
	@SuppressWarnings("unused")
	public Service getServicePrivate(ServiceNames serviceFrom, ServiceNames serviceTo) throws ContainerException, PointSortieException
	{
		if(showGraph && !serviceTo.equals(ServiceNames.LOG))
		{
			ArrayList<ServiceNames> ok = new ArrayList<ServiceNames>();
			ok.add(ServiceNames.CONFIG);
			ok.add(ServiceNames.SERIE_STM);
			ok.add(ServiceNames.SERIE_XBEE);
			ok.add(ServiceNames.INCOMING_HOOK_BUFFER);
			ok.add(ServiceNames.INCOMING_DATA_BUFFER);
			ok.add(ServiceNames.SERIAL_OUTPUT_BUFFER);
			ok.add(ServiceNames.REQUETE_STM);
			ok.add(ServiceNames.OBSTACLES_MEMORY);
			ok.add(ServiceNames.THREAD_SERIAL_INPUT);
			ok.add(ServiceNames.THREAD_SERIAL_OUTPUT);
			ok.add(ServiceNames.THREAD_FIN_MATCH);
			ok.add(ServiceNames.THREAD_PEREMPTION);
			ok.add(ServiceNames.THREAD_CAPTEURS);
			ok.add(ServiceNames.THREAD_CONFIG);

			try {
				if(ok.contains(serviceTo))
					fw.write(serviceTo+" [color=grey80, style=filled];\n");
				else
					fw.write(serviceTo+";\n");
				if(serviceFrom != null)
				{
					fw.write(serviceFrom+" -> "+serviceTo+";\n");					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return getServiceRecursif(serviceTo);
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
	public Service getServiceRecursif(ServiceNames serviceRequested) throws ContainerException, PointSortieException
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
			instanciedServices[serviceRequested.ordinal()] = (Service)new Capteurs((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																					(Config) getServicePrivate(serviceRequested, ServiceNames.CONFIG),
																					(ObstaclesMemory)getServicePrivate(serviceRequested, ServiceNames.OBSTACLES_MEMORY),
																					(RobotReal)getServicePrivate(serviceRequested, ServiceNames.ROBOT_REAL));
		else if(serviceRequested == ServiceNames.TABLE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Table((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																				(StrategieNotifieur)getServicePrivate(serviceRequested, ServiceNames.STRATEGIE_NOTIFIEUR));
		else if(serviceRequested == ServiceNames.D_STAR_LITE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new DStarLite((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																				(GridSpace)getServicePrivate(serviceRequested, ServiceNames.GRID_SPACE));
		else if(serviceRequested == ServiceNames.CHEMIN_PATHFINDING)
			instanciedServices[serviceRequested.ordinal()] = (Service)new CheminPathfinding((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		
		
		else if(serviceRequested == ServiceNames.OBSTACLES_MEMORY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ObstaclesMemory((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.GRID_SPACE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new GridSpace((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																					(ObstaclesMemory)getServicePrivate(serviceRequested, ServiceNames.OBSTACLES_MEMORY),
																					(Table)getServicePrivate(serviceRequested, ServiceNames.TABLE));
		else if(serviceRequested == ServiceNames.LPA_STAR)
			instanciedServices[serviceRequested.ordinal()] = (Service)new LPAStar((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																					(GameState<RobotReal,ReadOnly>)getServicePrivate(serviceRequested, ServiceNames.REAL_GAME_STATE),
																					(AStar)getServicePrivate(serviceRequested, ServiceNames.A_STAR));
		else if(serviceRequested == ServiceNames.A_STAR)
			instanciedServices[serviceRequested.ordinal()] = (Service)new AStar((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																					(AStarArcManager)getServicePrivate(serviceRequested, ServiceNames.A_STAR_ARC_MANAGER),
																					(AStarMemoryManager)getServicePrivate(serviceRequested, ServiceNames.A_STAR_MEMORY_MANAGER),
																					(GridSpaceStrategie)getServicePrivate(serviceRequested, ServiceNames.GRID_SPACE_STRATEGIE));
		else if(serviceRequested == ServiceNames.GRID_SPACE_STRATEGIE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new GridSpaceStrategie((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.A_STAR_ARC_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new AStarArcManager((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.A_STAR_MEMORY_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new AStarMemoryManager((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																							(GameState<RobotReal,ReadOnly>)getServicePrivate(serviceRequested, ServiceNames.REAL_GAME_STATE));
		else if(serviceRequested == ServiceNames.INCOMING_DATA_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new IncomingDataBuffer((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.INCOMING_HOOK_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new IncomingHookBuffer((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.SERIAL_OUTPUT_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new DataForSerialOutput((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.SERIE_STM)
			instanciedServices[serviceRequested.ordinal()] = (Service)new SerialConnexion((Log)getServicePrivate(serviceRequested, ServiceNames.LOG), "?", "T3", config.getInt(ConfigInfo.BAUDRATE_STM));
		else if(serviceRequested == ServiceNames.SERIE_XBEE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new SerialConnexion((Log)getServicePrivate(serviceRequested, ServiceNames.LOG), "", "", config.getInt(ConfigInfo.BAUDRATE_XBEE));
		else if(serviceRequested == ServiceNames.EXECUTION)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Execution((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
			                                                 (LPAStar)getServicePrivate(serviceRequested, ServiceNames.LPA_STAR),
  															 (ScriptManager)getServicePrivate(serviceRequested, ServiceNames.SCRIPT_MANAGER),
        													 (GameState<RobotReal,ReadWrite>)getServicePrivate(serviceRequested, ServiceNames.REAL_GAME_STATE),
        													 (RequeteSTM)getServicePrivate(serviceRequested, ServiceNames.REQUETE_STM));
		else if(serviceRequested == ServiceNames.HOOK_FACTORY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new HookFactory((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.ROBOT_REAL)
			instanciedServices[serviceRequested.ordinal()] = (Service)new RobotReal((DataForSerialOutput)getServicePrivate(serviceRequested, ServiceNames.SERIAL_OUTPUT_BUFFER),
															 (Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
															 (RequeteSTM)getServicePrivate(serviceRequested, ServiceNames.REQUETE_STM),
															 (GridSpace)getServicePrivate(serviceRequested, ServiceNames.GRID_SPACE));
        else if(serviceRequested == ServiceNames.REAL_GAME_STATE)
        	// ici la construction est un petit peu différente car on interdit l'instanciation publique d'un GameSTate<RobotChrono>
            instanciedServices[serviceRequested.ordinal()] = (Service) GameState.constructRealGameState((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
                                                             (Table)getServicePrivate(serviceRequested, ServiceNames.TABLE),
                                                             (RobotReal)getServicePrivate(serviceRequested, ServiceNames.ROBOT_REAL),
        													 (HookFactory)getServicePrivate(serviceRequested, ServiceNames.HOOK_FACTORY),
		 													 (DataForSerialOutput)getServicePrivate(serviceRequested, ServiceNames.SERIAL_OUTPUT_BUFFER),
															 (ObstaclesMemory)getServicePrivate(serviceRequested, ServiceNames.OBSTACLES_MEMORY));
		else if(serviceRequested == ServiceNames.SCRIPT_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ScriptManager((HookFactory)getServicePrivate(serviceRequested, ServiceNames.HOOK_FACTORY),
																					(Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																					 (GridSpace)getServicePrivate(serviceRequested, ServiceNames.GRID_SPACE));
		else if(serviceRequested == ServiceNames.THREAD_FIN_MATCH)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadFinMatch((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																		(Config)getServicePrivate(serviceRequested, ServiceNames.CONFIG));
		else if(serviceRequested == ServiceNames.THREAD_PEREMPTION)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadPeremption((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																		(ObstaclesMemory)getServicePrivate(serviceRequested, ServiceNames.OBSTACLES_MEMORY));
		else if(serviceRequested == ServiceNames.THREAD_EVITEMENT)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadEvitement((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																		(ThreadPathfinding)getServicePrivate(serviceRequested, ServiceNames.THREAD_PATHFINDING),
																		(DataForSerialOutput)getServicePrivate(serviceRequested, ServiceNames.SERIAL_OUTPUT_BUFFER),
																		(AStarCourbe)getServicePrivate(serviceRequested, ServiceNames.A_STAR_COURBE),
																		(CheminPathfinding)getServicePrivate(serviceRequested, ServiceNames.CHEMIN_PATHFINDING));
		else if(serviceRequested == ServiceNames.THREAD_CAPTEURS)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadCapteurs((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																		(IncomingDataBuffer)getServicePrivate(serviceRequested, ServiceNames.INCOMING_DATA_BUFFER),
																		(Capteurs)getServicePrivate(serviceRequested, ServiceNames.CAPTEURS));
		else if(serviceRequested == ServiceNames.THREAD_SERIAL_INPUT)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSerialInput((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																		(Config)getServicePrivate(serviceRequested, ServiceNames.CONFIG),
																		(SerialConnexion)getServicePrivate(serviceRequested, ServiceNames.SERIE_STM),
																		(IncomingDataBuffer)getServicePrivate(serviceRequested, ServiceNames.INCOMING_DATA_BUFFER),
																		(IncomingHookBuffer)getServicePrivate(serviceRequested, ServiceNames.INCOMING_HOOK_BUFFER),
																		(RequeteSTM)getServicePrivate(serviceRequested, ServiceNames.REQUETE_STM),
																		(Table)getServicePrivate(serviceRequested, ServiceNames.TABLE),
																		(RobotReal)getServicePrivate(serviceRequested, ServiceNames.ROBOT_REAL),
																		(HookFactory)getServicePrivate(serviceRequested, ServiceNames.HOOK_FACTORY),
					 													(DataForSerialOutput)getServicePrivate(serviceRequested, ServiceNames.SERIAL_OUTPUT_BUFFER));
		else if(serviceRequested == ServiceNames.REQUETE_STM)
			instanciedServices[serviceRequested.ordinal()] = (Service)new RequeteSTM((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.THREAD_SERIAL_OUTPUT)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSerialOutput((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																		(Config)getServicePrivate(serviceRequested, ServiceNames.CONFIG),
																		(SerialConnexion)getServicePrivate(serviceRequested, ServiceNames.SERIE_STM),
																		(DataForSerialOutput)getServicePrivate(serviceRequested, ServiceNames.SERIAL_OUTPUT_BUFFER));
		else if(serviceRequested == ServiceNames.THREAD_GAME_ELEMENT_DONE_BY_ENEMY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadGameElementDoneByEnemy((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																		(ObstaclesMemory)getServicePrivate(serviceRequested, ServiceNames.OBSTACLES_MEMORY),
																		(Table)getServicePrivate(serviceRequested, ServiceNames.TABLE),
																		(MoteurPhysique)getServicePrivate(serviceRequested, ServiceNames.MOTEUR_PHYSIQUE));
		else if(serviceRequested == ServiceNames.THREAD_CONFIG)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadConfig((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																		(Config)getServicePrivate(serviceRequested, ServiceNames.CONFIG),
																		this);
		else if(serviceRequested == ServiceNames.THREAD_PATHFINDING)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadPathfinding((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																		(AStarCourbe)getServicePrivate(serviceRequested, ServiceNames.A_STAR_COURBE),
																		(ObstaclesMemory)getServicePrivate(serviceRequested, ServiceNames.OBSTACLES_MEMORY));
		else if(serviceRequested == ServiceNames.STRATEGIE_INFO)
			instanciedServices[serviceRequested.ordinal()] = (Service)new StrategieInfo((Log)getServicePrivate(serviceRequested, ServiceNames.LOG), 
																		(StrategieNotifieur)getServicePrivate(serviceRequested, ServiceNames.STRATEGIE_NOTIFIEUR));
		else if(serviceRequested == ServiceNames.MOTEUR_PHYSIQUE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new MoteurPhysique((Log)getServicePrivate(serviceRequested, ServiceNames.LOG)); 		
		else if(serviceRequested == ServiceNames.STRATEGIE_NOTIFIEUR)
			instanciedServices[serviceRequested.ordinal()] = (Service)new StrategieNotifieur();		
		else if(serviceRequested == ServiceNames.A_STAR_COURBE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new AStarCourbe((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																					(DStarLite)getServicePrivate(serviceRequested, ServiceNames.D_STAR_LITE),
																					(AStarCourbeArcManager)getServicePrivate(serviceRequested, ServiceNames.A_STAR_COURBE_ARC_MANAGER),
																					(GameState<RobotReal,ReadOnly>)getServicePrivate(serviceRequested, ServiceNames.REAL_GAME_STATE),
																					(CheminPathfinding)getServicePrivate(serviceRequested, ServiceNames.CHEMIN_PATHFINDING),
																					(AStarCourbeMemoryManager)getServicePrivate(serviceRequested, ServiceNames.A_STAR_COURBE_MEMORY_MANAGER));
		else if(serviceRequested == ServiceNames.A_STAR_COURBE_MEMORY_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new AStarCourbeMemoryManager((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																								   (GameState<RobotReal,ReadOnly>)getServicePrivate(serviceRequested, ServiceNames.REAL_GAME_STATE));
		else if(serviceRequested == ServiceNames.A_STAR_COURBE_ARC_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new AStarCourbeArcManager((Log)getServicePrivate(serviceRequested, ServiceNames.LOG),
																								(MoteurPhysique)getServicePrivate(serviceRequested, ServiceNames.MOTEUR_PHYSIQUE),
																								(DStarLite)getServicePrivate(serviceRequested, ServiceNames.D_STAR_LITE));
		else if(serviceRequested == ServiceNames.CLOTHOIDES_COMPUTER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ClothoidesComputer((Log)getServicePrivate(serviceRequested, ServiceNames.LOG));
		// si le service demandé n'est pas connu, alors on log une erreur.
		else
		{
			log.critical("Erreur de getServicePrivate pour le service (service inconnu): "+serviceRequested);
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
	public void startAllThreads()
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
