/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.autoreplanning;

import java.util.ArrayList;
import java.util.List;

import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.exceptions.NoPathException;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.CinematiqueObs;
import pfg.kraken.robot.ItineraryPoint;
import pfg.log.Log;
import static pfg.kraken.astar.tentacles.Tentacle.PRECISION_TRACE_MM;
/**
 * A path manager that can handle dynamic update
 * @author pf
 *
 */

public final class DynamicPath
{
	public enum State
	{
		STANDBY, // aucune recherche en cours
		MODE_WITHOUT_REPLANING, // signale qu'il n'y a pas de replanification à faire
		SEARCH_REQUEST, // on aimerait que le thread commence la recherche initiale
		SEARCHING, // le thread calcule la trajectoire initiale
		UPTODATE_WITH_NEW_PATH, // on a un NOUVEAU chemin vers la destination
		UPTODATE, // pas besoin de replanif
		REPLANNING, // replanification nécessaire / en cours
		WAITING_END; // attend que le thread soit bien au courant que la recherche est finie
	}
	
	protected Log log;
	
	private CinematiqueObs[] path = new CinematiqueObs[1000];
	private volatile State etat;
	private volatile int indexFirst; // index du point où est le robot
	private volatile int pathSize; // index du prochain point de la trajectoire
	private volatile int firstDifferentPoint; // index du premier point différent dans la replanification
	private final int margeNecessaire, margeInitiale, margeAvantCollision, margePreferable;
	private volatile PathfindingException e;
	
	public DynamicPath(Log log, Config config, RectangularObstacle vehicleTemplate)
	{
		this.log = log;
		margeNecessaire = (int) Math.ceil(config.getDouble(ConfigInfoKraken.NECESSARY_MARGIN) / PRECISION_TRACE_MM);
		margePreferable = (int) Math.ceil(config.getDouble(ConfigInfoKraken.PREFERRED_MARGIN) / PRECISION_TRACE_MM);
		margeAvantCollision = (int) Math.ceil(config.getInt(ConfigInfoKraken.MARGIN_BEFORE_COLLISION) / PRECISION_TRACE_MM);
		margeInitiale = (int) Math.ceil(config.getInt(ConfigInfoKraken.INITIAL_MARGIN) / PRECISION_TRACE_MM);
		if(margeNecessaire > margePreferable || margeInitiale < margePreferable)
			throw new IllegalArgumentException();
		
		for(int i = 0; i < path.length; i++)
			path[i] = new CinematiqueObs(vehicleTemplate);
		clear();
		etat = State.STANDBY;
	}
	
	public synchronized void initSearchWithoutPlanning()
	{
		assert pathSize == 0 : pathSize;
		assert etat == State.STANDBY : etat;
		etat = State.MODE_WITHOUT_REPLANING;
	}
	
	public synchronized List<ItineraryPoint> endSearchWithoutPlanning()
	{
		assert etat == State.MODE_WITHOUT_REPLANING : etat;
		List<ItineraryPoint> out = getPath();
		clear();
		etat = State.STANDBY;
		return out;
	}
	
	public synchronized void startContinuousSearch()
	{
		assert etat == State.STANDBY : etat;
		assert pathSize == 0 : pathSize;
//		log.write("Search request", LogCategoryKraken.REPLANIF);
		etat = State.SEARCH_REQUEST;
		notifyAll();
	}
	
	public synchronized void endContinuousSearch()
	{
		if(etat == State.STANDBY)
			return;
		
		clear();
		notifyAll();
		try
		{
			waitThread();
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		notifyAll();
	}
	
	public synchronized void addToEnd(List<CinematiqueObs> points, boolean partial)
	{
		for(int i = 0; i < points.size(); i++)
			points.get(i).copy(path[pathSize + i]);
		updateFirstDifferentPoint(pathSize);
		
		pathSize += points.size();
		
		if(!partial && isModeWithReplanning())
			etat = State.UPTODATE_WITH_NEW_PATH;

		notifyAll();
	}

	public synchronized void importPath(List<ItineraryPoint> pathInitial)
	{
		assert etat == State.STANDBY;
		pathSize = 0;
		updateFirstDifferentPoint(pathSize);
		for(ItineraryPoint p : pathInitial)
		{
			path[pathSize].update(p);
			pathSize++;
		}
		etat = State.UPTODATE_WITH_NEW_PATH;
		notifyAll();
	}
	
	public synchronized int margeSupplementaireDemandee()
	{
		/*
		 * Si on a moins de MARGE_PREFERABLE points, on demande à Kraken de compléter jusqu'à avoir MARGE_INITIALE points (c'est un hystérésis)
		 */
		if(etat == State.REPLANNING && pathSize - indexFirst < margePreferable)
			return margeInitiale - (pathSize - indexFirst);
		
		/*
		 * Si on est à jour, pas besoin de regarder le nombre de points restant (car il peut être normalement faible quand on arrive à destination)
		 */
		else
			return 0;
	}

	public synchronized CinematiqueObs setCurrentTrajectoryIndex(int index)
	{
		if(pathSize > 0 && index >= pathSize)
			return path[pathSize-1]; // ça peut potentiellement arrivé à cause de la latence de la communication…
		
		indexFirst = index;
		return path[index];
	}
	
	public synchronized void checkException() throws PathfindingException
	{
		if(e != null)
		{
			assert etat == State.STANDBY || etat == State.WAITING_END : etat;
			PathfindingException tmp = e;
			e = null;
			throw tmp;
		}
		if(etat == State.REPLANNING && pathSize - indexFirst < margeNecessaire)
			throw new NoPathException("Pas assez de marge !");
	}
	
	public boolean needToStopReplaning()
	{
		return (etat == State.REPLANNING && pathSize - indexFirst < margeNecessaire);
	}
	
	public synchronized void threadReady()
	{
		assert etat == State.WAITING_END;
		etat = State.STANDBY;
		notifyAll();
	}
	
	private synchronized void waitThread() throws InterruptedException
	{
		while(etat == State.WAITING_END)
			wait();
		assert etat == State.STANDBY;
	}
	
	/**
	 * La recherche est terminée, on retourne en STANDBY
	 */
	private synchronized void clear()
	{
//		log.write("Search ended, returns to STANDBY", LogCategoryKraken.REPLANIF);
		firstDifferentPoint = Integer.MAX_VALUE;
		pathSize = 0;
		assert e == null : e;
		etat = State.WAITING_END;
	}
	
	public synchronized boolean isThereDiff() throws PathfindingException
	{
		checkException();
		return firstDifferentPoint != Integer.MAX_VALUE;
	}
	
	public synchronized PathDiff getDiff()
	{
		if(!isStarted())
			return null;

		PathDiff diff = new PathDiff(firstDifferentPoint, getPossiblyPartialPath(), etat == State.UPTODATE || etat == State.UPTODATE_WITH_NEW_PATH);
		firstDifferentPoint = Integer.MAX_VALUE;
		return diff;
	}
	
	public synchronized boolean isThereACompletePath()
	{
		return etat == State.UPTODATE || etat == State.UPTODATE_WITH_NEW_PATH;
	}
	
	public List<ItineraryPoint> getPath()
	{
		assert etat == State.UPTODATE_WITH_NEW_PATH || etat == State.MODE_WITHOUT_REPLANING : etat;
		etat = State.UPTODATE;
		return getPossiblyPartialPath();
	}
	
	private List<ItineraryPoint> getPossiblyPartialPath()
	{
		List<ItineraryPoint> pathIP = new ArrayList<ItineraryPoint>();
		for(int i = 0; i < pathSize; i++)
			pathIP.add(new ItineraryPoint(path[i]));
		return pathIP;
	}

	public synchronized void updateCollision(DynamicObstacles dynObs) throws InterruptedException
	{
		// dans tous les cas, on vérifie les collisions afin de vider la liste des nouveaux obstacles
		int firstDifferentPoint = dynObs.isThereCollision(path, indexFirst, pathSize);
		
		if(!needCollisionCheck())
			return;

		if(firstDifferentPoint != pathSize)
		{
			// on retire des points corrects mais trop proche de la collision
			firstDifferentPoint -= margeAvantCollision;
			
			// Si la trajectoire restante est plus petite que la marge initiale désirée, il faut s'arrêter
			if(firstDifferentPoint - indexFirst <= margeInitiale)
			{
				endContinuousSearchWithException(new NoPathException("Pas assez de marge"));
				pathSize = indexFirst;
			}
			else
			{
				// Sinon on prévient qu'il faut une replanification
				etat = State.REPLANNING;
				pathSize = firstDifferentPoint;
				updateFirstDifferentPoint(firstDifferentPoint);
			}
			notifyAll();
		}
	}

	/**
	 * Nouveau départ en replanification (après avoir calculé un bout de chemin partiel)
	 * @return
	 */
	public Cinematique getNewStart()
	{
		assert etat == State.REPLANNING : etat;
		return path[pathSize - 1];
	}

	/**
	 * Is a new, complete path available ?
	 * @return
	 * @throws PathfindingException 
	 */
	public synchronized boolean isNewPathAvailable() throws PathfindingException
	{
		checkException();
		return etat == State.UPTODATE_WITH_NEW_PATH;
	}

	public synchronized boolean needReplanning()
	{
		return etat == State.REPLANNING;
	}
	
	public synchronized boolean shouldThreadStopSearch()
	{
		return etat == State.WAITING_END;
	}
	
	public synchronized boolean isStarted()
	{
		return etat != State.STANDBY;
	}
	
	public boolean isModeWithReplanning()
	{
		assert etat != State.STANDBY : etat;
		// La replanification est désactivée lors d'une recherche "manuelle"
		return etat != State.MODE_WITHOUT_REPLANING;
	}

	public boolean isInitialSearch()
	{
		return etat == State.SEARCHING;
	}
	
	public boolean isThereSearchRequest()
	{
		return etat == State.SEARCH_REQUEST;
	}

	public void setSearchInProgress()
	{
		assert etat == State.SEARCH_REQUEST : etat;
//		log.write("Search begin", LogCategoryKraken.REPLANIF);
		etat = State.SEARCHING;
	}

	public boolean needCollisionCheck()
	{
		return etat == State.REPLANNING || etat == State.UPTODATE_WITH_NEW_PATH || etat == State.UPTODATE;
	}
	
	public synchronized List<ItineraryPoint> waitNewPath() throws InterruptedException, PathfindingException
	{
		while(!isNewPathAvailable())
			wait();

		return getPath();
	}
	
	public synchronized PathDiff waitDiff() throws InterruptedException, PathfindingException
	{
		while(!isThereDiff())
			wait();

		return getDiff();
	}

	private void updateFirstDifferentPoint(int firstDifferentPoint)
	{
		this.firstDifferentPoint = Math.min(this.firstDifferentPoint, firstDifferentPoint);
	}
	
	public void endContinuousSearchWithException(PathfindingException e) throws InterruptedException
	{
		if(etat == State.STANDBY)
			return;

		this.e = e;
		endContinuousSearch();
	}
}
