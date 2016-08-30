package container;

import obstacles.Capteurs;
import obstacles.IncomingDataBuffer;
import obstacles.MoteurPhysique;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.Obstacle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import pathfinding.CheminPathfinding;
import pathfinding.RealGameState;
import pathfinding.astarCourbe.AStarCourbe;
import pathfinding.astarCourbe.MemoryManager;
import pathfinding.astarCourbe.arcs.ArcManager;
import pathfinding.astarCourbe.arcs.ClothoidesComputer;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import exceptions.ContainerException;
import utils.*;
import serie.BufferIncomingOrder;
import serie.BufferOutgoingOrder;
import serie.SerieCouchePhysique;
import serie.SerialInterface;
import serie.BufferIncomingBytes;
import serie.SerieCoucheTrame;
import serie.SerialSimulation;
import table.Table;
import threads.ThreadCapteurs;
import threads.ThreadConfig;
import threads.ThreadEvitement;
import threads.ThreadExit;
import threads.ThreadPathfinding;
import threads.ThreadPeremption;
import threads.serie.ThreadSerialInputCoucheOrdre;
import threads.serie.ThreadSerialInputCoucheTrame;
import threads.serie.ThreadSerialOutput;
import threads.serie.ThreadSerialOutputTimeout;
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

	// liste des services déjà instanciés. Contient au moins Config et Log. Les autres services appelables seront présents quand ils auront été appelés
	private Service[] instanciedServices = new Service[ServiceNames.values().length];
	
	//gestion des log
	private Log log;
	private Config config;
	
	private static int nbInstances = 0;
	private boolean threadsStarted = false;
	
	private static final boolean showGraph = false;
	private FileWriter fw;

	/**
	 * Fonction à appeler à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le log.
	 * @throws InterruptedException 
	 * @throws ContainerException 
	 */
	public void destructor() throws ContainerException, InterruptedException
	{
		// arrêt des threads
		if(threadsStarted)
		{
			for(ServiceNames s: ServiceNames.values())
				if(s.isThread())
					((Thread)getService(s)).interrupt();

			threadsStarted = false;
		}

		Thread.sleep(100); // on attend la fermeture des autres threads
			
		log.debug("Fermeture de la série");
		// fermeture de la connexion série
		
		SerialInterface serialOutput = (SerialInterface)instanciedServices[ServiceNames.SERIE_COUCHE_PHYSIQUE.ordinal()];
		if(serialOutput != null)
			serialOutput.close();

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
		System.out.println("Singularité évaporée.");
		System.out.println();
		printMessage("outro.txt");
	}
	
	/**
	 * instancie le gestionnaire de dépendances et quelques services critiques
	 * Services instanciés:
	 * 		Config
	 * 		Log
	 * @throws ContainerException si un autre container est déjà instancié
	 * @throws InterruptedException 
	 */
	public Container() throws ContainerException, InterruptedException
	{
		if(nbInstances != 0)
		{
			System.out.println("Un autre container existe déjà! Annulation du constructeur.");
			throw new ContainerException();
		}
		nbInstances++;
		
		/**
		 * Open and read a file, and return the lines in the file as a list
		 * of Strings.
		 * (Demonstrates Java FileReader, BufferedReader, and Java5.)
		 */
		printMessage("intro.txt");
		
		/**
		 * Affiche la version du programme
		 */
		try {
			Process p = Runtime.getRuntime().exec("git log -1 --oneline");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = in.readLine();
			int index = s.indexOf(" ");
			System.out.println("Version : "+s.substring(0, index)+" - ["+s.substring(index+1)+"]");
			in.close();
		} catch (IOException e1) {
			System.out.println(e1);
		}
		
		/**
		 * Infos diverses
		 */
		System.out.println("System : "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch"));
		System.out.println("Java : "+System.getProperty("java.vendor")+" "+System.getProperty("java.version")+", max memory : "+Math.round(100.*Runtime.getRuntime().maxMemory()/(1024.*1024.*1024.))/100.+"G, available processors : "+Runtime.getRuntime().availableProcessors());
		System.out.println();

		System.out.println("    Remember, with great power comes great current squared times resistance !");
		System.out.println();
		
		// affiche la configuration avant toute autre chose
		log = (Log)getServiceRecursif(ServiceNames.LOG);
		config = (Config)getServiceRecursif(ServiceNames.CONFIG);
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
				log.warning(e);
			}
		}
		
		startAllThreads();
	}
	
	public Service getService(ServiceNames serviceTo) throws ContainerException, InterruptedException
	{
		return getServiceDisplay(null, serviceTo);
	}
	
	@SuppressWarnings("unused")
	private Service getServiceDisplay(ServiceNames serviceFrom, ServiceNames serviceTo) throws ContainerException, InterruptedException
	{
		if(showGraph && !serviceTo.equals(ServiceNames.LOG))
		{
			ArrayList<ServiceNames> ok = new ArrayList<ServiceNames>();
/*			ok.add(ServiceNames.CAPTEURS);
			ok.add(ServiceNames.CONFIG);
			ok.add(ServiceNames.SERIE_COUCHE_PHYSIQUE);
			ok.add(ServiceNames.TABLE);
			ok.add(ServiceNames.GRID_SPACE);
			ok.add(ServiceNames.INCOMING_DATA_BUFFER);
			ok.add(ServiceNames.OUTGOING_ORDER_BUFFER);
			ok.add(ServiceNames.A_STAR_COURBE_MEMORY_MANAGER_DYN);
			ok.add(ServiceNames.A_STAR_COURBE_MEMORY_MANAGER_PLANIF);
			ok.add(ServiceNames.D_STAR_LITE);
			ok.add(ServiceNames.CLOTHOIDES_COMPUTER);
			ok.add(ServiceNames.OBSTACLES_MEMORY);
			ok.add(ServiceNames.THREAD_SERIAL_INPUT_COUCHE_ORDRE);
			ok.add(ServiceNames.THREAD_SERIAL_OUTPUT);
			ok.add(ServiceNames.THREAD_SERIAL_OUTPUT_TIMEOUT);
			ok.add(ServiceNames.THREAD_PEREMPTION);
			ok.add(ServiceNames.THREAD_CAPTEURS);
			ok.add(ServiceNames.THREAD_CONFIG);
*/
			try {
				if(ok.contains(serviceTo))
					fw.write(serviceTo+" [color=grey80, style=filled];\n");
				else
					fw.write(serviceTo+";\n");

				if(serviceFrom != null)
					fw.write(serviceFrom+" -> "+serviceTo+";\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return getServiceRecursif(serviceTo);
	}


	@SuppressWarnings("unused")
	/**
	 * Fournit un service. Deux possibilités: soit il n'est pas encore instancié et on l'instancie.
	 * Soit il est déjà instancié et on le renvoie.
	 * @param serviceRequested
	 * @return l'instance du service demandé
	 * @throws ContainerException
	 * @throws FinMatchException
	 * @throws PointSortieException
	 */
	private Service getServiceRecursif(ServiceNames serviceRequested) throws ContainerException, InterruptedException
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
			instanciedServices[serviceRequested.ordinal()] = (Service)new Capteurs((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																					(GridSpace)getServiceDisplay(serviceRequested, ServiceNames.GRID_SPACE),
																					(MoteurPhysique)getServiceDisplay(serviceRequested, ServiceNames.MOTEUR_PHYSIQUE));
		else if(serviceRequested == ServiceNames.TABLE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new Table((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.D_STAR_LITE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new DStarLite((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																				(GridSpace)getServiceDisplay(serviceRequested, ServiceNames.GRID_SPACE));
		else if(serviceRequested == ServiceNames.CHEMIN_PATHFINDING)
			instanciedServices[serviceRequested.ordinal()] = (Service)new CheminPathfinding((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
		
		else if(serviceRequested == ServiceNames.OBSTACLES_MEMORY)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ObstaclesMemory((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.GRID_SPACE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new GridSpace((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																					(ObstaclesMemory)getServiceDisplay(serviceRequested, ServiceNames.OBSTACLES_MEMORY),
																					(Table)getServiceDisplay(serviceRequested, ServiceNames.TABLE));
		else if(serviceRequested == ServiceNames.INCOMING_DATA_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new IncomingDataBuffer((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.INCOMING_ORDER_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new BufferIncomingOrder((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.OUTGOING_ORDER_BUFFER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new BufferOutgoingOrder((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.SERIE_COUCHE_PHYSIQUE && !Config.simuleSerie)
			instanciedServices[serviceRequested.ordinal()] = (Service)new SerieCouchePhysique((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
		 													 (BufferIncomingBytes)getServiceDisplay(serviceRequested, ServiceNames.BUFFER_INCOMING_BYTES));
		else if(serviceRequested == ServiceNames.SERIE_COUCHE_PHYSIQUE && Config.simuleSerie)
			instanciedServices[serviceRequested.ordinal()] = (Service)new SerialSimulation((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.BUFFER_INCOMING_BYTES)
			instanciedServices[serviceRequested.ordinal()] = (Service)new BufferIncomingBytes((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
		else if(serviceRequested == ServiceNames.ROBOT_REAL)
			instanciedServices[serviceRequested.ordinal()] = (Service)new RobotReal((BufferOutgoingOrder)getServiceDisplay(serviceRequested, ServiceNames.OUTGOING_ORDER_BUFFER),
															 (Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
        else if(serviceRequested == ServiceNames.REAL_GAME_STATE)
            instanciedServices[serviceRequested.ordinal()] = (Service) new RealGameState((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
                                                             (RobotReal)getServiceDisplay(serviceRequested, ServiceNames.ROBOT_REAL),
															 (ObstaclesMemory)getServiceDisplay(serviceRequested, ServiceNames.OBSTACLES_MEMORY),
															 (Table)getServiceDisplay(serviceRequested, ServiceNames.TABLE));
		else if(serviceRequested == ServiceNames.THREAD_PEREMPTION)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadPeremption((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(GridSpace)getServiceDisplay(serviceRequested, ServiceNames.GRID_SPACE));
		else if(serviceRequested == ServiceNames.THREAD_EVITEMENT)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadEvitement((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(BufferOutgoingOrder)getServiceDisplay(serviceRequested, ServiceNames.OUTGOING_ORDER_BUFFER),
																		(CheminPathfinding)getServiceDisplay(serviceRequested, ServiceNames.CHEMIN_PATHFINDING));
		else if(serviceRequested == ServiceNames.THREAD_CAPTEURS)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadCapteurs((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(IncomingDataBuffer)getServiceDisplay(serviceRequested, ServiceNames.INCOMING_DATA_BUFFER),
																		(Capteurs)getServiceDisplay(serviceRequested, ServiceNames.CAPTEURS));
		else if(serviceRequested == ServiceNames.THREAD_SERIAL_INPUT_COUCHE_ORDRE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSerialInputCoucheOrdre((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(Config)getServiceDisplay(serviceRequested, ServiceNames.CONFIG),
																		(BufferIncomingOrder)getServiceDisplay(serviceRequested, ServiceNames.INCOMING_ORDER_BUFFER),
																		(IncomingDataBuffer)getServiceDisplay(serviceRequested, ServiceNames.INCOMING_DATA_BUFFER),
			                                                            (RobotReal)getServiceDisplay(serviceRequested, ServiceNames.ROBOT_REAL),
																        (CheminPathfinding)getServiceDisplay(serviceRequested, ServiceNames.CHEMIN_PATHFINDING));
		else if(serviceRequested == ServiceNames.THREAD_SERIAL_INPUT_COUCHE_TRAME)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSerialInputCoucheTrame((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(SerieCoucheTrame)getServiceDisplay(serviceRequested, ServiceNames.SERIE_COUCHE_TRAME),
																		(BufferIncomingOrder)getServiceDisplay(serviceRequested, ServiceNames.INCOMING_ORDER_BUFFER));
		else if(serviceRequested == ServiceNames.THREAD_SERIAL_OUTPUT)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSerialOutput((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(SerieCoucheTrame)getServiceDisplay(serviceRequested, ServiceNames.SERIE_COUCHE_TRAME),
																		(BufferOutgoingOrder)getServiceDisplay(serviceRequested, ServiceNames.OUTGOING_ORDER_BUFFER));
		else if(serviceRequested == ServiceNames.THREAD_SERIAL_OUTPUT_TIMEOUT)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadSerialOutputTimeout((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(SerieCoucheTrame)getServiceDisplay(serviceRequested, ServiceNames.SERIE_COUCHE_TRAME));
		else if(serviceRequested == ServiceNames.SERIE_COUCHE_TRAME)
			instanciedServices[serviceRequested.ordinal()] = (Service)new SerieCoucheTrame((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(SerieCouchePhysique)getServiceDisplay(serviceRequested, ServiceNames.SERIE_COUCHE_PHYSIQUE),
																		(BufferIncomingBytes)getServiceDisplay(serviceRequested, ServiceNames.BUFFER_INCOMING_BYTES));
		else if(serviceRequested == ServiceNames.THREAD_CONFIG)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadConfig((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(Config)getServiceDisplay(serviceRequested, ServiceNames.CONFIG),
																		this);
		else if(serviceRequested == ServiceNames.THREAD_PATHFINDING)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ThreadPathfinding((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																		(AStarCourbe)getServiceDisplay(serviceRequested, ServiceNames.A_STAR_COURBE),
																		 (GridSpace)getServiceDisplay(serviceRequested, ServiceNames.GRID_SPACE));
		else if(serviceRequested == ServiceNames.MOTEUR_PHYSIQUE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new MoteurPhysique((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG)); 		
		else if(serviceRequested == ServiceNames.A_STAR_COURBE)
			instanciedServices[serviceRequested.ordinal()] = (Service)new AStarCourbe((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																					(DStarLite)getServiceDisplay(serviceRequested, ServiceNames.D_STAR_LITE),
																					(ArcManager)getServiceDisplay(serviceRequested, ServiceNames.ARC_MANAGER),
																					(RealGameState)getServiceDisplay(serviceRequested, ServiceNames.REAL_GAME_STATE),
																					(CheminPathfinding)getServiceDisplay(serviceRequested, ServiceNames.CHEMIN_PATHFINDING),
																					(MemoryManager)getServiceDisplay(serviceRequested, ServiceNames.MEMORY_MANAGER));
		else if(serviceRequested == ServiceNames.MEMORY_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new MemoryManager((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																								   (RealGameState)getServiceDisplay(serviceRequested, ServiceNames.REAL_GAME_STATE));
		else if(serviceRequested == ServiceNames.ARC_MANAGER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ArcManager((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG),
																								(MoteurPhysique)getServiceDisplay(serviceRequested, ServiceNames.MOTEUR_PHYSIQUE),
																								(DStarLite)getServiceDisplay(serviceRequested, ServiceNames.D_STAR_LITE),
																								(ClothoidesComputer)getServiceDisplay(serviceRequested, ServiceNames.CLOTHOIDES_COMPUTER));
		else if(serviceRequested == ServiceNames.CLOTHOIDES_COMPUTER)
			instanciedServices[serviceRequested.ordinal()] = (Service)new ClothoidesComputer((Log)getServiceDisplay(serviceRequested, ServiceNames.LOG));
		// si le service demandé n'est pas connu, alors on log une erreur.
		else
		{
			log.critical("Erreur d'instanciation pour le service : "+serviceRequested+" (service inconnu)");
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
	private void startAllThreads() throws InterruptedException
	{
		if(threadsStarted)
		{
			log.warning("Threads déjà démarrés !");
			return;
		}
		
		for(ServiceNames s: ServiceNames.values())
		{
			if(s.isThread())
			{
				try {
					((Thread)getService(s)).start();
				} catch (ContainerException e) {
					e.printStackTrace();
				}
			}
		}
		Runtime.getRuntime().addShutdownHook(new ThreadExit(this));
		threadsStarted = true;
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
	 * Mise à jour de la config pour tous les services démarrés
	 * @param s
	 * @return
	 */
	public void updateConfigForAll()
	{
		for(int i = 0; i < instanciedServices.length; i++)
			if(instanciedServices[i] != null)
				instanciedServices[i].updateConfig(config);
	}
	
	/**
	 * Affichage un fichier
	 * @param filename
	 */
	private void printMessage(String filename)
	{
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(filename));
				String line;
				    
				while((line = reader.readLine()) != null)
					System.out.println(line);
				    
				reader.close();
			} catch (IOException e) {
				log.warning(e);
			}

	}
	
}
