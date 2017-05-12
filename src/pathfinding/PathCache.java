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

import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.HighPFClass;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import obstacles.types.ObstacleRobot;
import pathfinding.astar.AStarCourbe;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.chemin.CheminPathfindingInterface;
import pathfinding.chemin.FakeCheminPathfinding;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.Speed;
import scripts.ScriptNames;
import utils.Log;
import utils.Log.Verbose;

/**
 * Service qui contient les chemins précalculés
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
	private RealGameState state;	
	private int dureePeremption;
	private PFInstruction inst;
	private int nbEssais;
	
	private boolean simuleSerie;
	
	/**
	 * Les chemins précalculés.
	 */
	public HashMap<String, LinkedList<CinematiqueObs>> paths;
	
	public PathCache(Log log, Config config, RealGameState state, ChronoGameState chrono, AStarCourbe astar, CheminPathfinding realChemin, FakeCheminPathfinding fakeChemin, PFInstruction inst) throws InterruptedException
	{
		this.state = state;
		this.inst = inst;
		nbEssais = config.getInt(ConfigInfo.NB_ESSAIS_PF);
		simuleSerie = config.getBoolean(ConfigInfo.SIMULE_SERIE);
		dureePeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
		this.fakeChemin = fakeChemin;
		this.realChemin = realChemin;
		this.log = log;
		Cinematique start = new Cinematique(550, 1905, -Math.PI/2, true, 0);
		chrono.robot.setCinematique(start);
		this.astar = astar;
		paths = new HashMap<String, LinkedList<CinematiqueObs>>();
		if(!new File("paths/").exists())
			new File("paths/").mkdir();
		loadAll(chrono, start);
	}
	
	private void savePath(KeyPathCache k, List<CinematiqueObs> path)
	{
//    	log.debug("Sauvegarde d'une trajectoire : "+k.toString());
        try {
            FileOutputStream fichier;
            ObjectOutputStream oos;

            fichier = new FileOutputStream("paths/"+k.toString()+".dat");
            oos = new ObjectOutputStream(fichier);
            oos.writeObject(path);
            oos.flush();
            oos.close();
//        	log.debug("Sauvegarde terminée");
        }
        catch(IOException e)
        {
            log.critical("Erreur lors de la sauvegarde de la trajectoire ! "+e);
        }
	}

	/**
	 * Prépare un chemin
	 * @param cinematiqueInitiale
	 * @param s
	 * @param shoot
	 * @throws PathfindingException
	 * @throws InterruptedException 
	 */
	public void prepareNewPathToScript(KeyPathCache k)
	{
		log.debug("Recherche de chemin pour "+k+" ("+paths.size()+" chemins mémorisés)", Verbose.CACHE.masque);
		
		LinkedList<CinematiqueObs> path = paths.get(k.toString());
		
		k.s.s.setUpCercleArrivee();
		astar.initializeNewSearchToCircle(k.shoot, k.chrono);

		if(path == null)
		{
			k.s.s.setUpCercleArrivee();
			inst.set(k);
		}
		else
		{
			log.debug("Utilisation d'un trajet précalculé !");
			fakeChemin.addToEnd(path);
		}
	}
	
	private void waitPathfinding() throws InterruptedException, PathfindingException
	{
		synchronized(inst)
		{
			while(!inst.isDone())
				inst.wait();
		}
		inst.throwException();
	}
	
	/**
	 * Envoie le chemin précédemment préparé
	 * @throws InterruptedException 
	 */
	public void sendPreparedPath() throws PathfindingException
	{
		realChemin.addToEnd(fakeChemin.getPath());
	}
	
	private LinkedList<CinematiqueObs> loadOrCompute(KeyPathCache k) throws InterruptedException, PathfindingException
	{
		LinkedList<CinematiqueObs> path;
		try {
			path = loadPath(k);
		} catch (ClassNotFoundException | IOException e1) {
			
			if(precompute)
			{
				log.warning("Calcul du chemin "+k);
				try {
					prepareNewPathToScript(k);
					waitPathfinding();
					Thread.sleep(1000); // pour montrer le chemin
					path = fakeChemin.getPath();
					savePath(k, path);
				}
				catch(PathfindingException e)
				{
					log.warning("Précalcul du chemin échoué ! "+k+" : "+e);
					throw e;
				}
			}
			else
				throw new PathfindingException("Chargement du chemin "+k+" échoué : abandon.");
		}
		return path;
	}
	
	private void loadAll(ChronoGameState chrono, Cinematique start) throws InterruptedException
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

				if(k.s == ScriptNames.SCRIPT_DEPOSE_MINERAI) // c'est particulier
					continue;
				
//				log.debug("Script : "+k.s);
				
//				log.debug(k);				
				LinkedList<CinematiqueObs> path;
				try {
					path = loadOrCompute(k);
				} catch (PathfindingException e1) {
//					log.warning(e1);
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
					try {
						pathRetour = loadOrCompute(k);
					} catch (PathfindingException e1) {
						errors.add(k.toString());
						continue;
					}
					ok.add(k.toString());
					paths.put(k.toString(), pathRetour);
				}
			}
		}
		String out;
		
/*		if(!ok.isEmpty())
		{
			out = "Chargement/génération réussie pour : ";
			for(int i = 0; i < ok.size(); i++)
			{
				out += ok.get(i);
				if(i < ok.size() - 1)
				out += ", ";
			}
			log.debug(out);
		}*/
		
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
//    	log.debug("Chargement d'une trajectoire : "+k.toString());
        FileInputStream fichier = new FileInputStream("paths/"+k.toString()+".dat");
        ObjectInputStream ois = new ObjectInputStream(fichier);
        LinkedList<CinematiqueObs> path;
        try {
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
		
	/**
	 * Calcule un chemin et le suit jusqu'à un script
	 * @param shoot
	 * @throws PathfindingException
	 * @throws InterruptedException
	 * @throws UnableToMoveException
	 */
	public void computeAndFollowToScript(KeyPathCache c) throws PathfindingException, InterruptedException, UnableToMoveException
	{
		computeAndFollow(null, c.shoot, c);
	}
	
	public void computeAndFollowToPoint(Cinematique arrivee, boolean shoot) throws PathfindingException, InterruptedException, UnableToMoveException
	{
		computeAndFollow(arrivee, shoot, null);
	}
	
	/**
	 * Calcule un chemin et le suit jusqu'à un point
	 * @param arrivee
	 * @param shoot
	 * @throws PathfindingException
	 * @throws InterruptedException
	 * @throws UnableToMoveException
	 */
	private void computeAndFollow(Cinematique arrivee, boolean shoot, KeyPathCache c) throws PathfindingException, InterruptedException, UnableToMoveException
	{
		try {
			int essai = nbEssais;
			boolean restart = false;
			do {
				restart = false;
				try {
					if(c != null)
						prepareNewPathToScript(c);
					else
					{
						log.debug("Recherche de chemin pour "+arrivee+" depuis "+state.robot.getCinematique(), Verbose.CACHE.masque);
						astar.initializeNewSearch(arrivee, shoot, state);
						astar.process(realChemin);
					}
					log.debug("On va parcourir le chemin", Verbose.CACHE.masque);
					if(!simuleSerie)
						state.robot.followTrajectory(Speed.STANDARD);
				}
				catch(PathfindingException | UnableToMoveException e)
				{
					log.warning("Il y a eu un problème de pathfinding : "+e);
					essai--;
					if(essai == 0)
					{
						log.critical("Abandon de l'objectif.");
						throw e;
					}
					log.debug("On retente !");
					ObstacleRobot.setMarge(false);
					Thread.sleep(dureePeremption);
					restart = true;
				}
			} while(restart);
			log.debug("Compute and follow a terminé normalement", Verbose.CACHE.masque);
		} finally {
			ObstacleRobot.setMarge(true);
		}
	}
}
