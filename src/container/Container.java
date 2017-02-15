/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import config.DynamicConfigurable;
import container.Service;
import utils.*;
import exceptions.ContainerException;
import graphic.PrintBuffer;
import memory.CinemObsMM;
import obstacles.types.Obstacle;
import pathfinding.astar.AStarCourbe;
import pathfinding.astar.arcs.ArcCourbe;
import pathfinding.astar.arcs.ArcManager;
import pathfinding.astar.arcs.BezierComputer;
import pathfinding.astar.arcs.CercleArrivee;
import pathfinding.astar.arcs.ClothoidesComputer;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.dstarlite.DStarLite;
import serie.BufferIncomingBytes;
import serie.BufferOutgoingOrder;
import serie.SerieCouchePhysique;
import serie.SerieCoucheTrame;
import threads.ThreadName;
import threads.ThreadPathfinding;
import threads.ThreadService;
import threads.ThreadShutdown;
import threads.serie.ThreadSerialOutput;

/**
 * 
 * Gestionnaire de la durée de vie des objets dans le code.
 * Permet à n'importe quelle classe implémentant l'interface "Service" d'appeller d'autres instances de services via son constructeur.
 * Une classe implémentant Service n'est instanciée que par la classe "Container"
 * 
 * @author pf
 */
public class Container implements Service, Configurable
{
	// liste des services déjà instanciés. Contient au moins Config et Log. Les autres services appelables seront présents quand ils auront été appelés
	private HashMap<String, Service> instanciedServices = new HashMap<String, Service>();
	
	private Log log;
	private Config config;
	
	private static int nbInstances = 0;
	
	private boolean showGraph;

	private List<DynamicConfigurable> dynaConf = new ArrayList<DynamicConfigurable>();
	private HashMap<Class<? extends Service>, Set<String>> grapheDep = new HashMap<Class<? extends Service>, Set<String>>();
	private List<String> classesSerie = new ArrayList<String>();
	private List<String> classesHighPF = new ArrayList<String>();
	private List<String> classesLowPF = new ArrayList<String>();
	private List<String> classesBothPF = new ArrayList<String>();
	private List<String> classesCore = new ArrayList<String>();
	private List<String> classesAutres = new ArrayList<String>();
	/**
	 * Fonction appelé automatiquement à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le log.
	 * @throws InterruptedException 
	 * @throws ContainerException 
	 */
	public void destructor(boolean unitTest) throws ContainerException, InterruptedException
	{
		ThreadName threadError = null;
		// arrêt des threads
		for(ThreadName n : ThreadName.values())
		{
			if(!getService(n.c).isAlive())
				threadError = n;
			getService(n.c).interrupt();
		}

		for(ThreadName n : ThreadName.values())
		{
			if(n == ThreadName.FENETRE && config.getBoolean(ConfigInfo.GRAPHIC_PRODUCE_GIF))
				getService(n.c).join(120000); // spécialement pour lui qui enregistre un gif…
			else
				getService(n.c).join(10000); // on attend un peu que le thread s'arrête
			if(getService(n.c).isAlive())
				log.critical(n.c.getSimpleName()+" encore vivant !");
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
			log.warning("Sauvegarde du graphe de dépendances");

			for(Class<? extends Service> classe : grapheDep.keySet())
			{
				if(HighPFClass.class.isAssignableFrom(classe))
				{
					if(LowPFClass.class.isAssignableFrom(classe))
						classesBothPF.add(classe.getSimpleName());
					else
						classesHighPF.add(classe.getSimpleName());
				}
				else if(SerialClass.class.isAssignableFrom(classe))
					classesSerie.add(classe.getSimpleName());
				else if(LowPFClass.class.isAssignableFrom(classe))
					classesLowPF.add(classe.getSimpleName());
				else if(CoreClass.class.isAssignableFrom(classe))
					classesCore.add(classe.getSimpleName());				
				else
					classesAutres.add(classe.getSimpleName());
			}
			
			try {
				FileWriter fw = new FileWriter(new File("dependances.dot"));
				fw.write("digraph dependancesJava {\n\n");

				
				fw.write("subgraph clusterPF {\n");
				fw.write("label = \"Pathfinding\";\n");
				for(String s : classesBothPF)					
					fw.write(s+";\n");
				
				fw.write("subgraph clusterPFCourbe {\n");
				fw.write("label = \"PF courbe\";\n");
				for(String s : classesHighPF)					
					fw.write(s+";\n");
				fw.write("}\n\n");
				
				fw.write("subgraph clusterPFlow {\n");
				fw.write("label = \"PF bas niveau\";\n");
				for(String s : classesLowPF)					
					fw.write(s+";\n");
				fw.write("}\n\n");
				fw.write("}\n\n");
				
				fw.write("subgraph clusterSerie {\n");
				fw.write("label = \"Série\";\n");
				for(String s : classesSerie)					
					fw.write(s+";\n");
				fw.write("}\n\n");
				
				fw.write("subgraph clusterCore {\n");
				fw.write("label = \"Core\";\n");
				for(String s : classesCore)					
					fw.write(s+";\n");
				fw.write("}\n\n");
				
				for(String s : classesAutres)					
					fw.write(s+";\n");
				
				fw.write("\n");
				
				for(Class<? extends Service> classe : grapheDep.keySet())
				{
					Set<String> enf = grapheDep.get(classe);
					if(!enf.isEmpty())
					{
						fw.write(classe.getSimpleName()+" -> {");
						for(String e : enf)
							fw.write(e+" ");
						fw.write("};\n");
					}
				}
				fw.write("\n}\n");
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
		
		if(threadError != null)
			throw new ContainerException("Un thread a planté : "+threadError);
		
		/**
		 * Arrête tout, même si destructor est appelé depuis un thread
		 */
		if(!unitTest)
			System.exit(0);
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
		
		Thread.currentThread().setName("ThreadPrincipal");
		
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
		System.out.println("Date : "+new SimpleDateFormat("E dd/MM à kk:mm").format(new Date()));
		System.out.println();

		System.out.println("    Remember, with great power comes great current squared times resistance !");
		System.out.println();
		
		log = new Log();
		config = new Config();
		log.useConfig(config);
		// Interdépendance entre log et config…
		config.init(log);

		instanciedServices.put(Log.class.getSimpleName(), log);
		instanciedServices.put(Config.class.getSimpleName(), config);

		// Le container est aussi un service
		instanciedServices.put(getClass().getSimpleName(), this);
		useConfig(config);
	
		if(showGraph)
			log.warning("Le graphe de dépendances va être généré !");
		
		Obstacle.set(log, getService(PrintBuffer.class));
		Obstacle.useConfig(config);
		ArcCourbe.useConfig(config);

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
	public synchronized <S extends Service> S getService(Class<S> serviceTo) throws ContainerException
	{
		return getServiceRecursif(serviceTo, new Stack<String>());
	}
	
	/**
	 * Aucune différence avec getService ; c'est juste que c'est fait pour les non-services aussi
	 * @param serviceTo
	 * @return
	 * @throws ContainerException
	 */
	public synchronized <S> S make(Class<S> serviceTo, Object... extraParam) throws ContainerException
	{
		if(Service.class.isAssignableFrom(serviceTo))
			throw new ContainerException("make doit être utilisé avec des non-services");
		return getServiceRecursif(serviceTo, new Stack<String>(), extraParam);
	}

	/**
	 * Méthode récursive qui fait tout le boulot
	 * @param classe
	 * @return
	 * @throws ContainerException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	private synchronized <S> S getServiceRecursif(Class<S> classe, Stack<String> stack, Object... extraParam) throws ContainerException
	{
		try {
			/**
			 * Si l'objet existe déjà et que c'est un Service, on le renvoie
			 */	
			if(Service.class.isAssignableFrom(classe) && instanciedServices.containsKey(classe.getSimpleName()))
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
			Constructor<S> constructeur;
			
			if(classe.getConstructors().length > 1)
			{
				try
				{
					// Plus d'un constructeur ? On prend celui par défaut
					constructeur = classe.getConstructor();
				} catch(Exception e)
				{
					throw new ContainerException(classe.getSimpleName()+" a plusieurs constructeurs et aucun constructeur par défaut !");					
				}
			}
			else if(classe.getConstructors().length == 0)
				throw new ContainerException(classe.getSimpleName()+" n'a aucun constructeur !");
			else
				constructeur = (Constructor<S>) classe.getConstructors()[0];
			
			Class<?>[] param = constructeur.getParameterTypes();
			
			/*
			 * Récupération du graphe de dépendances
			 */
			if(showGraph && Service.class.isAssignableFrom(classe) && !classe.equals(Log.class) && !classe.equals(PrintBuffer.class))
			{
				Set<String> enf = grapheDep.get(classe.getSimpleName());
				if(enf == null)
				{
					enf = new HashSet<String>();
					grapheDep.put((Class<Service>) classe, enf);
				}
				for(int i = 0; i < param.length - extraParam.length; i++)
				{
					String fils = param[i].getSimpleName();
					if(!param[i].equals(Log.class) && !param[i].equals(PrintBuffer.class) && !param[i].equals(Container.class) && Service.class.isAssignableFrom(param[i]))
						enf.add(fils);
				}				
			}
			
			/**
			 * On demande récursivement chacun de ses paramètres
			 * On complète automatiquement avec ceux déjà donnés
			 */
			Object[] paramObject = new Object[param.length];
			for(int i = 0; i < param.length - extraParam.length; i++)
				paramObject[i] = getServiceRecursif(param[i], stack);
			for(int i = 0; i < extraParam.length; i++)
				paramObject[i + param.length - extraParam.length] = extraParam[i];

			/**
			 * Instanciation et sauvegarde
			 */
			S s = constructeur.newInstance(paramObject);

			if(Service.class.isAssignableFrom(classe))
				instanciedServices.put(classe.getSimpleName(), (Service)s);
			
			/**
			 * Mise à jour de la config
			 */
			if(config != null && Configurable.class.isAssignableFrom(classe))
				((Configurable) s).useConfig(config);
			if(DynamicConfigurable.class.isAssignableFrom(classe))
			{
				synchronized(dynaConf)
				{
					dynaConf.add((DynamicConfigurable) s);
				}
				if(config != null)
					((DynamicConfigurable) s).updateConfig(config);
			}
			
			// Mise à jour de la pile
			stack.pop();
			
			return s;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SecurityException | InstantiationException e) {
			e.printStackTrace();
			throw new ContainerException(e.toString()+"\nClasse demandée : "+classe.getSimpleName());
		}
	}

	public void restartThread(ThreadName n) throws InterruptedException
	{
		try {
			ThreadService t = getService(n.c);
			if(t.isAlive()) // s'il est encore en vie, on le tue
			{
				t.interrupt();
				t.join(1000);
			}
			instanciedServices.remove(n.c.getSimpleName());
			getService(n.c).start(); // et on le redémarre
		} catch (ContainerException e) {
			log.critical(e);
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
		try {
			Runtime.getRuntime().addShutdownHook(getService(ThreadShutdown.class));
		} catch (ContainerException e) {
			log.critical(e);
		}
	}

	/**
	 * Mise à jour de la config pour tous les services démarrés
	 * @param s
	 * @return
	 */
	public void updateConfigForAll()
	{
		synchronized(dynaConf)
		{
			for(DynamicConfigurable s : dynaConf)
				s.updateConfig(config);
		}
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
			System.err.println(e); // peut-être que log n'est pas encore démarré…
		}
	}

	@Override
	public void useConfig(Config config)
	{
		showGraph = config.getBoolean(ConfigInfo.GENERATE_DEPENDENCY_GRAPH);
	}
	
}
