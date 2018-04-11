/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.robot.CinematiqueObs;
import pfg.kraken.robot.ItineraryPoint;

import static pfg.kraken.astar.tentacles.Tentacle.PRECISION_TRACE_MM;
/**
 * A path manager that can handle dynamic update
 * @author pf
 *
 */

public class DynamicPath
{
	private enum State
	{
		DISABLE, // pas de recherche en cours
		UPTODATE, // recherche en cours, tout va bien
		NEED_REPLANNING, // a besoin d'une replanif
		STOP; // le robot doit s'arrêter
	}
	
	private LinkedList<CinematiqueObs> path = new LinkedList<CinematiqueObs>();
	private volatile State etat = State.DISABLE;
	private volatile int indexFirst; // index du point où est le robot
//	private volatile int firstDifferentPoint; // index du premier point différent dans la replanification
	private final int margeNecessaire, margeInitiale, margeAvantCollision, margePreferable;
	
	public DynamicPath(Config config)
	{
		margeNecessaire = (int) Math.ceil(config.getDouble(ConfigInfoKraken.NECESSARY_MARGIN) / PRECISION_TRACE_MM);
		margePreferable = (int) Math.ceil(config.getDouble(ConfigInfoKraken.PREFERRED_MARGIN) / PRECISION_TRACE_MM);
		margeAvantCollision = (int) Math.ceil(config.getInt(ConfigInfoKraken.MARGIN_BEFORE_COLLISION) / PRECISION_TRACE_MM);
		margeInitiale = (int) Math.ceil(config.getInt(ConfigInfoKraken.INITIAL_MARGIN) / PRECISION_TRACE_MM);
	}
	
	public synchronized void addToEnd(LinkedList<CinematiqueObs> points)
	{
		path.addAll(points);
		notifyAll();
	}

	public void setUptodate()
	{
		etat = State.UPTODATE;
	}

	public LinkedList<CinematiqueObs> getPath()
	{
		return path;
	}

	public synchronized int margeSupplementaireDemandee()
	{
		/*
		 * Si on est à jour, pas besoin de regarder le nombre de points restant (car il peut être normalement faible quand on arrive à destination)
		 */
		if(etat == State.UPTODATE || path.size() - indexFirst >= margePreferable)
			return 0;
		else
			/*
			 * Si on a moins de MARGE_PREFERABLE points, on demande à Kraken de compléter jusqu'à avoir MARGE_INITIALE points (c'est un hystérésis)
			 */
			return margeInitiale - (path.size() - indexFirst);
	}

	public synchronized CinematiqueObs setCurrentTrajectoryIndex(int index)
	{
		if(index >= path.size())
			return path.getLast(); // ça peut potentiellement arrivé à cause de la latence de la communication…
		
		indexFirst = index;
		return path.get(index);
	}
	
	public boolean needStop()
	{
		return etat == State.STOP || path.size() - indexFirst < margeNecessaire;
	}
	
	public synchronized void clear()
	{
		// Appelé au début d'une recherche
		etat = State.UPTODATE;
		path.clear();
	}
	
	public List<ItineraryPoint> getPathItineraryPoint()
	{
		List<ItineraryPoint> pathIP = new ArrayList<ItineraryPoint>();
		for(CinematiqueObs o : path)
			pathIP.add(new ItineraryPoint(o));

		return pathIP;
	}

	public synchronized void updateCollision(DynamicObstacles dynObs)
	{
		// on ne vérifie pas les collisions si on est éteint ou en état STOP
		if(etat == State.STOP || etat == State.DISABLE)
			return;
		
		int firstDifferentPoint = dynObs.isThereCollision(path.subList(indexFirst, path.size())) + indexFirst;
		if(firstDifferentPoint != path.size())
		{
			// on retire des points corrects mais trop proche de la collision
			firstDifferentPoint -= margeAvantCollision;
			
			// Si la trajectoire restante est plus petite que la marge initiale désirée, il faut s'arrêter
			if(firstDifferentPoint - indexFirst <= margeInitiale)
			{
				etat = State.STOP;
				path.subList(indexFirst, path.size()).clear();
			}
			else
			{
				// Sinon on prévient qu'il faut une replanification et on détruit
				etat = State.NEED_REPLANNING;
				path.subList(firstDifferentPoint, path.size()).clear();
				assert !needStop();
			}
			notify();
		}
	}
}
