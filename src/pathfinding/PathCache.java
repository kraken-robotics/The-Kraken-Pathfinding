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

package pathfinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import capteurs.CapteursProcess;
import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.HighPFClass;
import exceptions.MemoryManagerException;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import obstacles.types.ObstacleRobot;
import pathfinding.astar.AStarCourbe;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.chemin.FakeCheminPathfinding;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.Speed;
import scripts.ScriptNames;
import serie.BufferOutgoingOrder;
import serie.SerialProtocol.InOrder;
import serie.SerialProtocol.State;
import serie.Ticket;
import utils.Log;
import utils.Log.Verbose;

/**
 * Service qui contient les chemins précalculés
 * 
 * @author pf
 *
 */

public class PathCache implements Service, HighPFClass
{
	public static volatile boolean precompute = false;
	private Log log;
	private AStarCourbe astar;
	private CheminPathfinding realChemin;
	private FakeCheminPathfinding fakeChemin;
	private BufferOutgoingOrder out;
	private RealGameState state;
	private int dureePeremption;
	private PFInstruction inst;
	private CapteursProcess capteurs;
	private int nbEssais;
	private boolean enableScan;
	private boolean simuleSerie;

	/**
	 * Les chemins précalculés.
	 */
	public HashMap<String, LinkedList<CinematiqueObs>> paths;

	public PathCache(Log log, Config config, BufferOutgoingOrder out, CapteursProcess capteurs, RealGameState state, ChronoGameState chrono, AStarCourbe astar, CheminPathfinding realChemin, FakeCheminPathfinding fakeChemin, PFInstruction inst) throws MemoryManagerException, InterruptedException
	{
		this.capteurs = capteurs;
		this.out = out;
		this.state = state;
		this.inst = inst;
		nbEssais = config.getInt(ConfigInfo.NB_ESSAIS_PF);
		simuleSerie = config.getBoolean(ConfigInfo.SIMULE_SERIE);
		dureePeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
		enableScan = config.getBoolean(ConfigInfo.ENABLE_SCAN);
		this.fakeChemin = fakeChemin;
		this.realChemin = realChemin;
		this.log = log;
		Cinematique start = new Cinematique(550, 1905, -Math.PI / 2, true, 0);
		chrono.robot.setCinematique(start);
		this.astar = astar;
		paths = new HashMap<String, LinkedList<CinematiqueObs>>();
		if(!new File("paths/").exists())
			new File("paths/").mkdir();
		if(config.getBoolean(ConfigInfo.ALLOW_PRECOMPUTED_PATH))
			loadAll(chrono, start);
	}

	private void savePath(KeyPathCache k, List<CinematiqueObs> path)
	{
		// log.debug("Sauvegarde d'une trajectoire : "+k.toString());
		try
		{
			FileOutputStream fichier;
			ObjectOutputStream oos;

			fichier = new FileOutputStream("paths/" + k.toString() + ".dat");
			oos = new ObjectOutputStream(fichier);
			oos.writeObject(path);
			oos.flush();
			oos.close();
			// log.debug("Sauvegarde terminée");
		}
		catch(IOException e)
		{
			log.critical("Erreur lors de la sauvegarde de la trajectoire ! " + e);
		}
	}

	/**
	 * Prépare un chemin
	 * 
	 * @param cinematiqueInitiale
	 * @param s
	 * @param shoot
	 * @throws PathfindingException
	 * @throws InterruptedException
	 */
	public void prepareNewPath(KeyPathCache k) throws PathfindingException, MemoryManagerException
	{
		log.debug("Recherche de chemin pour " + k + " (" + paths.size() + " chemins mémorisés)", Verbose.CACHE.masque);

		LinkedList<CinematiqueObs> path = paths.get(k.toString());

		if(k.s != null)
		{
			k.s.s.setUpCercleArrivee();
			astar.initializeNewSearchToCircle(k.shoot, k.chrono);
		}
		else
			astar.initializeNewSearch(k.arrivee, k.shoot, k.chrono);

		if(path == null)
			inst.searchRequest();
		else
		{
			log.debug("Utilisation d'un trajet précalculé !");
			inst.setDone();
			fakeChemin.addToEnd(path);
		}
	}

	/**
	 * On attend la fin de la recherche. On suppose qu'elle est démarrée !
	 * Lance les exceptions s'il y en a
	 * 
	 * @throws InterruptedException
	 * @throws PathfindingException
	 */
	public void waitPathfinding() throws InterruptedException, PathfindingException
	{
		synchronized(inst)
		{
			while(!inst.isDone())
				inst.wait();
			inst.throwException();
		}
	}

	private LinkedList<CinematiqueObs> loadOrCompute(KeyPathCache k) throws MemoryManagerException, PathfindingException, InterruptedException
	{
		LinkedList<CinematiqueObs> path;
		try
		{
			path = loadPath(k);
		}
		catch(ClassNotFoundException | IOException e1)
		{

			if(precompute)
			{
				log.warning("Calcul du chemin " + k);
				try
				{
					prepareNewPath(k);
					waitPathfinding();
					Thread.sleep(1000); // pour montrer le chemin
					path = fakeChemin.getPath();
					savePath(k, path);
				}
				catch(PathfindingException e)
				{
					log.warning("Précalcul du chemin échoué ! " + k + " : " + e);
					throw e;
				}
			}
			else
				throw new PathfindingException("Chargement du chemin " + k + " échoué : abandon.");
		}
		return path;
	}

	private void loadAll(ChronoGameState chrono, Cinematique start) throws MemoryManagerException, InterruptedException
	{
		log.debug("Début du chargement des trajectoires…");
		List<String> errors = new ArrayList<String>();
		List<String> ok = new ArrayList<String>();

		for(int i = 0; i < 2; i++)
		{
			KeyPathCache k = new KeyPathCache(chrono);
			for(ScriptNames s : ScriptNames.values())
			{
				k.chrono.robot.setCinematique(start);
				k.s = s;
				k.shoot = i == 0;

				if(k.s == ScriptNames.SCRIPT_DEPOSE_MINERAI) // c'est
																// particulier
					continue;

				// log.debug("Script : "+k.s);

				// log.debug(k);
				LinkedList<CinematiqueObs> path;
				try
				{
					path = loadOrCompute(k);
				}
				catch(PathfindingException e1)
				{
					// log.warning(e1);
					errors.add(k.toString());
					continue;
				}

				ok.add(k.toString());
				paths.put(k.toString(), path);

				// calcul du chemin retour
				k.chrono.robot.setCinematique(path.getLast());
				k.s = ScriptNames.SCRIPT_DEPOSE_MINERAI;
				for(int j = 0; j < 2; j++)
				{
					k.shoot = j == 0;
					LinkedList<CinematiqueObs> pathRetour;
					try
					{
						pathRetour = loadOrCompute(k);
					}
					catch(PathfindingException e1)
					{
						errors.add(k.toString());
						continue;
					}
					ok.add(k.toString());
					paths.put(k.toString(), pathRetour);
				}
			}
		}
		String out;

		/*
		 * if(!ok.isEmpty())
		 * {
		 * out = "Chargement/génération réussie pour : ";
		 * for(int i = 0; i < ok.size(); i++)
		 * {
		 * out += ok.get(i);
		 * if(i < ok.size() - 1)
		 * out += ", ";
		 * }
		 * log.debug(out);
		 * }
		 */

		if(!errors.isEmpty())
		{
			out = "Chargement/génération échouée pour : ";
			for(int i = 0; i < errors.size(); i++)
			{
				out += errors.get(i);
				if(i < errors.size() - 1)
					out += ", ";
			}
			log.critical(out);
		}
	}

	@SuppressWarnings("unchecked")
	private LinkedList<CinematiqueObs> loadPath(KeyPathCache k) throws ClassNotFoundException, IOException
	{
		// log.debug("Chargement d'une trajectoire : "+k.toString());
		FileInputStream fichier = new FileInputStream("paths/" + k.toString() + ".dat");
		ObjectInputStream ois = new ObjectInputStream(fichier);
		LinkedList<CinematiqueObs> path;
		try
		{
			path = (LinkedList<CinematiqueObs>) ois.readObject();
		}
		finally
		{
			ois.close();
		}
		if(path == null)
			throw new IOException();
		return path;
	}

	public void computeAndFollow(KeyPathCache c) throws PathfindingException, InterruptedException, UnableToMoveException, MemoryManagerException
	{
		prepareNewPath(c);
		follow(c);
	}

	/**
	 * Calcule un chemin et le suit jusqu'à un point
	 * 
	 * @param arrivee
	 * @param shoot
	 * @throws PathfindingException
	 * @throws InterruptedException
	 * @throws UnableToMoveException
	 * @throws MemoryManagerException 
	 */
	public void follow(KeyPathCache k) throws PathfindingException, InterruptedException, UnableToMoveException, MemoryManagerException
	{
		try
		{
			int essai = nbEssais;
			boolean restart = false;
			do
			{
				restart = false;
				try
				{
					// il est parfaitement possible que la recherche soit déjà
					// faite
					synchronized(inst)
					{
						if(!inst.isSearching() && !inst.isDone() && !inst.hasRequest()) // pas
																						// commencé,
																						// pas
																						// fini
						{
							if(k.s != null)
							{
								k.s.s.setUpCercleArrivee();
								astar.initializeNewSearchToCircle(k.shoot, k.chrono);
							}
							else
								astar.initializeNewSearch(k.arrivee, k.shoot, k.chrono);
							inst.searchRequest();
						}
					}
					waitPathfinding();
					realChemin.addToEnd(fakeChemin.getPath());
					log.debug("On va parcourir le chemin", Verbose.CACHE.masque);
					if(!simuleSerie)
						state.robot.followTrajectory(Speed.STANDARD);
					
					if(!astar.isArrived())
						throw new UnableToMoveException("Le robot est arrivé au mauvais endroit !");
				}
				catch(PathfindingException | UnableToMoveException e)
				{
					log.warning("Il y a eu un problème de pathfinding : " + e);
					essai--;
					if(essai == 0)
					{
						log.critical("Abandon de l'objectif.");
						throw e;
					}
					log.debug("On retente !");
					ObstacleRobot.setMarge(false);
					
					if(enableScan)
					{
						log.debug("Début du scan", Verbose.CAPTEURS.masque);
						capteurs.startScan();
						Ticket t = out.doScan();
						InOrder o = t.attendStatus();
						if(o.etat == State.KO)
							log.critical("Erreur lors du scan : "+o);
						capteurs.endScan();
						log.debug("Scan fini", Verbose.CAPTEURS.masque);
					}
					else
						Thread.sleep(dureePeremption);
					restart = true;
				}
			} while(restart);
			log.debug("Compute and follow a terminé normalement", Verbose.CACHE.masque);
		}
		finally
		{
			ObstacleRobot.setMarge(true);
		}
	}
}
