package container;

import obstacles.ObstaclesRectangularMemory;
import obstacles.SensorsDataBuffer;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.Obstacle;
import pathfinding.dstarlite.gridspace.PointGridSpaceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

import exceptions.ContainerException;
import utils.*;
import serie.BufferIncomingBytes;
import serie.BufferIncomingOrder;
import serie.BufferOutgoingOrder;
import serie.SerieCouchePhysique;
import serie.SerieCoucheTrame;
import table.Table;
import threads.ThreadCapteurs;
import threads.ThreadConfig;
import threads.ThreadExit;
import threads.ThreadName;
import threads.ThreadPeremption;
import threads.serie.ThreadSerialInputCoucheTrame;
import threads.serie.ThreadSerialOutput;
import threads.serie.ThreadSerialOutputTimeout;

/**
 * 
 * Gestionnaire de la durée de vie des objets dans le code.
 * Permet à n'importe quelle classe implémentant l'interface "Service" d'appeller d'autres instances de services via son constructeur.
 * Une classe implémentant Service n'est instanciée que par la classe "Container"
 * 
 * @author pf
 */
public class Container implements Service
{
	// liste des services déjà instanciés. Contient au moins Config et Log. Les autres services appelables seront présents quand ils auront été appelés
	private HashMap<String, Service> instanciedServices = new HashMap<String, Service>();
	
	// permet de détecter les dépendances circulaires
	private volatile Stack<String> stack = new Stack<String>();
	
	private Log log;
	private Config config;
	
	private static int nbInstances = 0;
	
	private static final boolean showGraph = false;
	private FileWriter fw;

	/**
	 * Fonction appelé automatiquement à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le log.
	 * @throws InterruptedException 
	 * @throws ContainerException 
	 */
	public void destructor() throws ContainerException, InterruptedException
	{
		// arrêt des threads
		for(ThreadName n : ThreadName.values())
		{
			getService(n.c).interrupt();
			getService(n.c).join(100); // on attend au plus 50ms que le thread s'arrête
		}

		/**
		 * Affiche la liste des threads qui ne sont pas fermés
		 */
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for(Thread t : threadSet)
		{
			if(!t.getName().equals(Thread.currentThread().getName()) && t.getName().startsWith("ThreadRobot"))
				log.critical("Thread "+t.getName()+" pas arrêté !");
		}
		
		log.debug("Fermeture de la série");
		/**
		 * Mieux vaut écrire SerieCouchePhysique.class.getSimpleName()) que "SerieCouchePhysique",
		 * car en cas de refactor, le premier est automatiquement ajusté
		 */
		if(instanciedServices.containsKey(SerieCouchePhysique.class.getSimpleName()))
			((SerieCouchePhysique)instanciedServices.get(SerieCouchePhysique.class.getSimpleName())).close();

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
	 * Instancie le gestionnaire de dépendances et quelques services critiques (log et config qui sont interdépendants)
	 * @throws ContainerException si un autre container est déjà instancié
	 * @throws InterruptedException 
	 */
	public Container() throws ContainerException, InterruptedException
	{
		/**
		 * On vérifie qu'il y ait un seul container à la fois
		 */
		if(nbInstances != 0)
			throw new ContainerException("Un autre container existe déjà! Annulation du constructeur.");

		nbInstances++;
		
		/**
		 * Affichage d'un petit message de bienvenue
		 */
		printMessage("intro.txt");
		
		/**
		 * Affiche la version du programme (dernier commit et sa branche)
		 */
		try {
			Process p = Runtime.getRuntime().exec("git log -1 --oneline");
			Process p2 = Runtime.getRuntime().exec("git branch");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader in2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
			String s = in.readLine();
			int index = s.indexOf(" ");
			in.close();
			String s2 = in2.readLine();

			while(!s2.contains("*"))
				s2 = in2.readLine();

			int index2 = s2.indexOf(" ");
			System.out.println("Version : "+s.substring(0, index)+" on "+s2.substring(index2+1)+" - ["+s.substring(index+1)+"]");
			in2.close();
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
		
		log = getService(Log.class);
		config = getService(Config.class);
		log.updateConfig(config);
		log.useConfig(config);
		// Interdépendance entre log et config…
		config.init(log);
		
		// Le container est aussi un service
		instanciedServices.put(getClass().getSimpleName(), this);

		Obstacle.setLog(log);
		Obstacle.useConfig(config);
				
		if(showGraph)
		{
			log.warning("Le graphe de dépendances va être généré !");
			try {
				fw = new FileWriter(new File("dependances.dot"));
				fw.write("digraph dependancesJava {\n");
			} catch (IOException e) {
				log.warning(e);
			}
		}
		
		startAllThreads();
	}
	
	/**
	 * Créé un object de la classe demandée, ou le récupère s'il a déjà été créé
	 * S'occupe automatiquement des dépendances
	 * Toutes les classes demandées doivent implémenter Service ; c'est juste une sécurité.
	 * @param classe
	 * @return un objet de cette classe
	 * @throws ContainerException
	 * @throws InterruptedException 
	 */
	public synchronized <S extends Service> S getService(Class<S> serviceTo) throws ContainerException, InterruptedException
	{
		stack.clear(); // pile d'appel vidée
		return getServiceDisplay(null, serviceTo);
	}
	
	@SuppressWarnings("unused")
	private synchronized <S extends Service> S getServiceDisplay(Class<? extends Service> serviceFrom, Class<S> serviceTo) throws ContainerException, InterruptedException
	{
		/**
		 * On ne crée pas forcément le graphe de dépendances pour éviter une lourdeur inutile
		 */
		if(showGraph && !serviceTo.equals(Log.class))
		{
			ArrayList<String> ok = new ArrayList<String>();
			ok.add(Config.class.getSimpleName());
			ok.add(ThreadSerialOutput.class.getSimpleName());
			ok.add(BufferIncomingBytes.class.getSimpleName());
			ok.add(SerieCouchePhysique.class.getSimpleName());
			ok.add(SerieCoucheTrame.class.getSimpleName());
			ok.add(ThreadSerialOutputTimeout.class.getSimpleName());
			ok.add(BufferIncomingOrder.class.getSimpleName());
			ok.add(Container.class.getSimpleName());
			ok.add(ThreadConfig.class.getSimpleName());
			ok.add(BufferOutgoingOrder.class.getSimpleName());
			ok.add(Table.class.getSimpleName());
			ok.add(SensorsDataBuffer.class.getSimpleName());
			ok.add(ThreadPeremption.class.getSimpleName());
			ok.add(ObstaclesRectangularMemory.class.getSimpleName());
			ok.add(ThreadSerialInputCoucheTrame.class.getSimpleName());
			ok.add(ObstaclesMemory.class.getSimpleName());
			ok.add(ThreadCapteurs.class.getSimpleName());
			ok.add(PointGridSpaceManager.class.getSimpleName());
			
			try {
				if(ok.contains(serviceTo.getSimpleName()))
					fw.write(serviceTo.getSimpleName()+" [color=green3, style=filled];\n");
				else
					fw.write(serviceTo.getSimpleName()+";\n");

				if(serviceFrom != null)
					fw.write(serviceFrom.getSimpleName()+" -> "+serviceTo.getSimpleName()+";\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return getServiceRecursif(serviceTo);
	}

	/**
	 * Méthode récursive qui fait tout le boulot
	 * @param classe
	 * @return
	 * @throws ContainerException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	private synchronized <S extends Service> S getServiceRecursif(Class<S> classe) throws ContainerException, InterruptedException
	{
		try {
			/**
			 * Si l'objet existe déjà, on le renvoie
			 */			
			if(instanciedServices.containsKey(classe.getSimpleName()))
				return (S) instanciedServices.get(classe.getSimpleName());
			
			/**
			 * Détection de dépendances circulaires
			 */
			if(stack.contains(classe.getSimpleName()))
			{
				// Dépendance circulaire détectée !
				String out = "Dépendance circulaire détectée : ";
				for(String s : stack)
					out += s + " -> ";
				out += classe.getSimpleName();
				throw new ContainerException(out);
			}
			
			// Pas de dépendance circulaire
			
			// On met à jour la pile
			stack.push(classe.getSimpleName());

			/**
			 * Récupération du constructeur et de ses paramètres
			 * On suppose qu'il n'y a chaque fois qu'un seul constructeur pour cette classe
			 */
			if(classe.getConstructors().length > 1)
				throw new ContainerException(classe.getSimpleName()+" a plusieurs constructeurs !");

			Constructor<S> constructeur = (Constructor<S>) classe.getConstructors()[0];
			Class<Service>[] param = (Class<Service>[]) constructeur.getParameterTypes();
			
			/**
			 * On demande récursivement chacun de ses paramètres
			 */
			Object[] paramObject = new Object[param.length];
			for(int i = 0; i < param.length; i++)
				paramObject[i] = getServiceDisplay(classe, param[i]);

			/**
			 * Instanciation et sauvegarde
			 */
			System.out.println(classe.getSimpleName());
			S s = constructeur.newInstance(paramObject);
			instanciedServices.put(classe.getSimpleName(), s);
			
			/**
			 * Mise à jour de la config
			 */
			if(config != null)
				for(Method m : Service.class.getMethods())
					classe.getMethod(m.getName(), Config.class).invoke(s, config);
			
			// Mise à jour de la pile
			stack.pop();
			
			return s;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException | InstantiationException e) {
			e.printStackTrace();
			throw new ContainerException(e.getMessage());
		}
	}

	/**
	 * Démarrage de tous les threads
	 */
	private void startAllThreads() throws InterruptedException
	{
		for(ThreadName n : ThreadName.values())
		{
			try {
				getService(n.c).start();
			} catch (ContainerException e) {
				log.critical(e);
			}
		}
		
		/**
		 * Planification du hook de fermeture
		 */
		ThreadExit.makeInstance(this);
		Runtime.getRuntime().addShutdownHook(ThreadExit.getInstance());
		
		log.debug("Démarrage des threads fini");
	}

	/**
	 * Mise à jour de la config pour tous les services démarrés
	 * @param s
	 * @return
	 */
	public void updateConfigForAll()
	{
		for(Service s : instanciedServices.values())
			s.updateConfig(config);
	}
	
	/**
	 * Affichage d'un fichier
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

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}
