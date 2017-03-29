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
import robot.RobotChrono;
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
	
	/**
	 * Les chemins précalculés.
	 */
	public HashMap<KeyPathCache, LinkedList<CinematiqueObs>> paths;
	
	public PathCache(Log log, ScriptManager smanager, ChronoGameState chrono, AStarCourbe astar, CheminPathfinding realChemin, FakeCheminPathfinding fakeChemin) throws InterruptedException
	{
		this.fakeChemin = fakeChemin;
		this.realChemin = realChemin;
		this.log = log;
		Cinematique start = new Cinematique(200, 1800, Math.PI, true, 0, Speed.STANDARD.translationalSpeed); // TODO
		chrono.robot.setCinematique(start);
		this.astar = astar;
		paths = new HashMap<KeyPathCache, LinkedList<CinematiqueObs>>();
		if(!new File("paths/").exists())
			new File("paths/").mkdir();
		loadAll(smanager, chrono, start);
	}
	
	private void savePath(KeyPathCache k, List<CinematiqueObs> path)
	{
    	log.debug("Sauvegarde d'une trajectoire : "+k.toString());
        try {
            FileOutputStream fichier;
            ObjectOutputStream oos;

            new File(k.toString()).createNewFile();
            fichier = new FileOutputStream("paths/"+k.toString());
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
	public void prepareNewPathToScript(KeyPathCache k) throws PathfindingException, InterruptedException
	{
		k.s.setUpCercleArrivee();
		astar.initializeNewSearchToCircle(k.shoot, k.chrono);
		astar.process(fakeChemin);
	}
	
	/**
	 * Envoie le chemin précédemment préparé
	 * @throws InterruptedException 
	 */
	public void sendPreparedPath() throws InterruptedException, PathfindingException
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
	
	private void loadAll(ScriptManager smanager, ChronoGameState chrono, Cinematique start) throws InterruptedException
	{
		smanager.reinit();
		for(int i = 0; i < 2; i++)
		{
			while(smanager.hasNext())
			{
				KeyPathCache k = new KeyPathCache(chrono, smanager.next(), i == 0);
				LinkedList<CinematiqueObs> path = loadPath(k);

				// TODO
				if(k.s instanceof ScriptDeposeMinerai)
					continue;
				
				log.debug(k);
				if(path == null)
				{
					try {
						k.chrono.robot.setCinematique(start);
						prepareNewPathToScript(k);
						path = fakeChemin.getPath();
//						savePath(k, path); // TODO
						
						// TODO : affichage à virer
						realChemin.add(path);
					} catch (PathfindingException e) {
						log.critical("Le précalcul du chemin a échoué : "+e);
					}
					finally
					{
						Thread.sleep(2000);
						astar.stopContinuousSearch();
					}
				}
				if(path != null)
					paths.put(k, path);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private LinkedList<CinematiqueObs> loadPath(KeyPathCache k)
	{
    	log.debug("Chargement d'une trajectoire : "+k.toString());
        try {
            FileInputStream fichier = new FileInputStream("paths/"+k.toString());
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
		astar.stopContinuousSearch();
	}
}
