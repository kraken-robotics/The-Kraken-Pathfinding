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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import container.Service;
import container.dependances.HighPFClass;
import exceptions.PathfindingException;
import pathfinding.astar.AStarCourbe;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.chemin.FakeCheminPathfinding;
import pathfinding.chemin.IteratorCheminPathfinding;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.Speed;
import scripts.Script;
import scripts.ScriptDeposeMinerai;
import scripts.ScriptManager;
import utils.Log;

/**
 * Service qui contient les chemins précalculés
 * @author pf
 *
 */

public class PathCache implements Service, HighPFClass
{
	private Log log;
	private AStarCourbe astar;
	private CheminPathfinding realChemin;
	private FakeCheminPathfinding fakeChemin;
	public HashMap<Integer, HashMap<Script, LinkedList<CinematiqueObs>>> paths;
	
	public PathCache(Log log, ScriptManager smanager, ChronoGameState chrono, AStarCourbe astar, IteratorCheminPathfinding iterator, CheminPathfinding realChemin, FakeCheminPathfinding fakeChemin)
	{
		this.fakeChemin = fakeChemin;
		this.realChemin = realChemin;
		this.log = log;
		Cinematique start = new Cinematique(200, 1800, Math.PI, true, 0, Speed.STANDARD.translationalSpeed); // TODO
		chrono.robot.setCinematique(start);
		this.astar = astar;
		paths = new HashMap<Integer, HashMap<Script, LinkedList<CinematiqueObs>>>();
		if(!new File("paths/").exists())
			new File("paths/").mkdir();
//		loadAll(smanager, chrono, start, iterator);
	}
	
	private void savePath(String file, List<CinematiqueObs> path)
	{
    	log.debug("Sauvegarde d'une trajectoire : "+file);
        try {
            FileOutputStream fichier;
            ObjectOutputStream oos;

            new File(file).createNewFile();
            fichier = new FileOutputStream(file);
            oos = new ObjectOutputStream(fichier);
            oos.writeObject(path);
            oos.flush();
            oos.close();
        	log.debug("Sauvegarde terminée");
        }
        catch(IOException e)
        {
            log.critical("Erreur lors de la sauvegarde de la trajectoire ! "+e);
        }
	}
	
	/**
	 * Prépare un chemin et l'enregistre
	 * @param cinematiqueInitiale
	 * @param s
	 * @param shoot
	 * @throws PathfindingException
	 * @throws InterruptedException 
	 */
	public void prepareNewPathToScript(Script s, boolean shoot, ChronoGameState chrono) throws PathfindingException, InterruptedException
	{
		s.setUpCercleArrivee();
		astar.initializeNewSearchToCircle(shoot, chrono);

		HashMap<Script, LinkedList<CinematiqueObs>> hm = paths.get(chrono.robot.getCinematique().hashCode());

		if(hm != null && hm.get(s) != null)
		{
			LinkedList<CinematiqueObs> path = new LinkedList<CinematiqueObs>();
			// on fait une copie car la liste est modifiée par CheminPathfinding
			path.addAll(hm.get(s));
			fakeChemin.add(path);
		}
		else
			astar.process(fakeChemin);
	}
	
	/**
	 * Suit le chemin précédemment préparé
	 * @throws InterruptedException 
	 */
	public void followPreparedPath() throws InterruptedException, PathfindingException
	{
		/*
		 * Normalement, cette exception ne peut survenir que lors d'une replanification (donc pas là)
		 */
		synchronized(fakeChemin)
		{
			if(!fakeChemin.isReady())
				fakeChemin.wait();
			if(!fakeChemin.isReady()) // échec de la recherche TODO
				throw new PathfindingException();
			realChemin.add(fakeChemin.getPath());
		}
	}
	
	private void loadAll(ScriptManager smanager, ChronoGameState chrono, IteratorCheminPathfinding iterator) throws InterruptedException
	{
		smanager.reinit();
		boolean[] shoot = {true, false};
		for(int i = 0; i < 2; i++)
		{
			while(smanager.hasNext())
			{
				Script script = smanager.next();
				String fileName = "paths/"+chrono.robot.getCinematique().hashCode()+"->"+script+"-s="+shoot[i]+".dat";
				LinkedList<CinematiqueObs> path = loadPath(fileName);
				if(script instanceof ScriptDeposeMinerai)
					continue;
				
				log.debug(script);
				if(path == null)
				{
					try {
						prepareNewPathToScript(script, shoot[i], chrono);
						path = fakeChemin.getPath();
						savePath(fileName, path);
					} catch (PathfindingException e) {
						log.critical("Le précalcul du chemin a échoué");
					}
					finally
					{
						astar.stopSearch();
					}
				}
				if(path != null)
				{
					HashMap<Script, LinkedList<CinematiqueObs>> map = paths.get(chrono.robot.getCinematique().hashCode());
					if(map == null)
					{
						map = new HashMap<Script, LinkedList<CinematiqueObs>>();
						paths.put(chrono.robot.getCinematique().hashCode(), map);
					}
					map.put(script, path);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private LinkedList<CinematiqueObs> loadPath(String file)
	{
    	log.debug("Chargement d'une trajectoire : "+file);
        try {
            FileInputStream fichier = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fichier);
            LinkedList<CinematiqueObs> path = (LinkedList<CinematiqueObs>) ois.readObject();
            ois.close();
            return path;
        }
        catch(IOException | ClassNotFoundException e)
        {
        	log.critical("Chargement échoué !");
        }
        return null;
	}
	
	/**
	 * Le chemin a été entièrement parcouru.
	 */
	public synchronized void stopSearch()
	{
		astar.stopSearch();
	}
}
