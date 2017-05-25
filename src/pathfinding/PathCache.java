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
import java.util.Arrays;
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
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.types.ObstacleRobot;
import pathfinding.astar.AStarCourbe;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.chemin.FakeCheminPathfinding;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.Speed;
import serie.BufferOutgoingOrder;
import serie.SerialProtocol.InOrder;
import serie.SerialProtocol.State;
import serie.Ticket;
import utils.Log;
import utils.Log.Verbose;
import utils.Vec2RO;

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
	private ObstaclesIteratorPresent iteratorObstacles;
	private RealGameState state;
	private int dureePeremption;
	private PFInstruction inst;
	private CapteursProcess capteurs;
	private int nbEssais;
	private boolean saveOnTheFly;
	private boolean enableScan;
	private boolean simuleSerie;
	private double longueurAvantRobot;

	/**
	 * Les chemins précalculés.
	 */
	public HashMap<String, LinkedList<CinematiqueObs>> paths;

	public PathCache(Log log, Config config, ObstaclesIteratorPresent iteratorObstacles, BufferOutgoingOrder out, CapteursProcess capteurs, RealGameState state, ChronoGameState chrono, AStarCourbe astar, CheminPathfinding realChemin, FakeCheminPathfinding fakeChemin, PFInstruction inst) throws MemoryManagerException, InterruptedException
	{
		this.iteratorObstacles = iteratorObstacles;
		this.capteurs = capteurs;
		this.out = out;
		this.state = state;
		this.inst = inst;
		nbEssais = config.getInt(ConfigInfo.NB_ESSAIS_PF);
		saveOnTheFly = config.getBoolean(ConfigInfo.SAVE_FOUND_PATH);
		simuleSerie = config.getBoolean(ConfigInfo.SIMULE_SERIE);
		dureePeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
		enableScan = config.getBoolean(ConfigInfo.ENABLE_SCAN);
		longueurAvantRobot = config.getDouble(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
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
		log.debug("Recherche de chemin pour " + k + " (" + paths.size() + " chemins mémorisés)");

		// On ne réutilise le chemin que s'il n'y a pas d'ennemis
		iteratorObstacles.reinit();
		LinkedList<CinematiqueObs> path = null;
		if(!iteratorObstacles.hasNext());
			path = paths.get(k.toString()+".dat");

		if(k.s != null)
		{
			Cinematique arrivee = k.s.s.getPointEntree();
			if(arrivee != null)
				astar.initializeNewSearch(arrivee, k.shoot, k.chrono);
			else
			{
				k.s.s.setUpCercleArrivee();
				astar.initializeNewSearchToCircle(k.shoot, k.chrono);
			}
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

	private void loadAll(ChronoGameState chrono, Cinematique start) throws MemoryManagerException, InterruptedException
	{
		log.debug("Début du chargement des trajectoires…");
		File f = new File("./paths/");
		List<String> names = new ArrayList<String>(Arrays.asList(f.list()));
		for(String s : names)
			loadPath(s);
	}

	@SuppressWarnings("unchecked")
	private void loadPath(String nom)
	{
		log.debug("Chargement d'une trajectoire : "+nom);
		ObjectInputStream ois = null;
		try
		{
			FileInputStream fichier = new FileInputStream(nom);
			ois = new ObjectInputStream(fichier);
			paths.put(nom, (LinkedList<CinematiqueObs>) ois.readObject());
		}
		catch(IOException | ClassNotFoundException e)
		{
			log.warning(e);
		}
		finally
		{
			if(ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void computeAndFollow(KeyPathCache c, Speed s) throws PathfindingException, InterruptedException, UnableToMoveException, MemoryManagerException
	{
		prepareNewPath(c);
		follow(c, s);
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
	public void follow(KeyPathCache k, Speed s) throws PathfindingException, InterruptedException, UnableToMoveException, MemoryManagerException
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
								Cinematique arrivee = k.s.s.getPointEntree();
								if(arrivee != null)
									astar.initializeNewSearch(arrivee, k.shoot, k.chrono);
								else
								{
									k.s.s.setUpCercleArrivee();
									astar.initializeNewSearchToCircle(k.shoot, k.chrono);
								}
							}
							else
								astar.initializeNewSearch(k.arrivee, k.shoot, k.chrono);
							inst.searchRequest();
						}
					}
					waitPathfinding();
					LinkedList<CinematiqueObs> path = fakeChemin.getPath();
					iteratorObstacles.reinit();	
					// on sauvegarde que si y'a aucun obstacle
					if(saveOnTheFly && !iteratorObstacles.hasNext())
						savePath(k, path);
					realChemin.addToEnd(path);
					log.debug("On va parcourir le chemin");
					if(!simuleSerie)
						state.robot.followTrajectory(s);
					else
						state.robot.setCinematique(realChemin.getLastCinematique());
					if(!astar.isArrivedAsser())
						throw new UnableToMoveException("On est arrivé bien trop loin de là où on devait !");
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
					
					if(enableScan && isScanNecessary())
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
//			log.debug("Compute and follow a terminé normalement", Verbose.CACHE.masque);
		}
		finally
		{
			ObstacleRobot.setMarge(true);
		}
	}
	
	/**
	 * Le scan est-il nécessaire ? Cette méthode vérifie s'il y a un obstacle de proximité devant le robot
	 * @return
	 */
	private boolean isScanNecessary()
	{
		Vec2RO posDevant = state.robot.getCinematique().getPosition().plusNewVector(new Vec2RO(longueurAvantRobot + 80, state.robot.getCinematique().orientationReelle, false));
		iteratorObstacles.reinit();
		while(iteratorObstacles.hasNext())
			if(iteratorObstacles.next().squaredDistance(posDevant) < 180 * 180)
				return true;
		return false;
	}
}
