/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.ArrayList;
import java.util.List;
import pfg.config.Config;
import pfg.log.Log;
import pfg.injector.Injector;
import pfg.injector.InjectorException;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.astar.autoreplanning.CollisionDetectionThread;
import pfg.kraken.astar.autoreplanning.DynamicPath;
import pfg.kraken.astar.autoreplanning.ReplanningThread;
import pfg.kraken.astar.engine.QuadTreePhysicsEngine;
import pfg.kraken.astar.engine.DefaultPhysicsEngine;
import pfg.kraken.astar.engine.PhysicsEngine;
import pfg.kraken.astar.tentacles.ResearchProfile;
import pfg.kraken.astar.tentacles.ResearchProfileManager;
import pfg.kraken.astar.tentacles.TentacleManager;
import pfg.kraken.astar.tentacles.endCheck.EndWithXY;
import pfg.kraken.astar.tentacles.endCheck.EndWithXYO;
import pfg.kraken.astar.tentacles.endCheck.EndWithXYOC0;
import pfg.kraken.astar.tentacles.types.*;
import pfg.kraken.display.Display;
import pfg.kraken.exceptions.InvalidPathException;
import pfg.kraken.exceptions.NoPathException;
import pfg.kraken.exceptions.NotInitializedPathfindingException;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.EmptyDynamicObstacles;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;

/**
 * The manager of the tentacle pathfinder.
 * TentacularAStar wrapper.
 * @author pf
 *
 */
public final class Kraken
{
	private Config config;
	private Injector injector;
	private TentacularAStar astar;
	private ResearchProfileManager profiles;
	private TentacleManager tentaclemanager;
	private DynamicPath dpath;
	private boolean autoReplanningEnable = false;
	
	/**
	 * Get Kraken with :
	 * @param vehicleTemplate : the shape of the vehicle
	 * @param fixedObstacles : a list of fixed/permanent obstacles
	 * @param bottomLeftCorner : the bottom left corner of the search domain
	 * @param topRightCorner : the top right corner of the search domain
	 * @param configprofile : the config profiles
	 */
	public Kraken(RectangularObstacle vehicleTemplate, Iterable<Obstacle> fixedObstacles, XY bottomLeftCorner, XY topRightCorner, String configfile, String...profiles)
	{
		this(new KrakenParameters(vehicleTemplate, fixedObstacles, bottomLeftCorner, topRightCorner, configfile, profiles));
	}

	public Kraken(KrakenParameters param)
	{
		injector = new Injector();
		config = new Config(ConfigInfoKraken.values(), isJUnitTest(), param.configfile, param.configprofile);
		injector.addService(RectangularObstacle.class, param.vehicleTemplate);

		/*
		 * We adjust the maximal curvature in order to never be under the minimal speed
		 */
		double minSpeed = config.getDouble(ConfigInfoKraken.MINIMAL_SPEED);
		if(minSpeed > 0)
		{
			double maxLateralAcc = config.getDouble(ConfigInfoKraken.MAX_LATERAL_ACCELERATION);
			config.override(ConfigInfoKraken.MAX_CURVATURE, Math.min(config.getDouble(ConfigInfoKraken.MAX_CURVATURE), maxLateralAcc / (minSpeed * minSpeed)));
		}
		
		try {
			StaticObstacles so = injector.getService(StaticObstacles.class); 
			if(param.fixedObstacles != null)
				for(Obstacle o : param.fixedObstacles)
					so.add(o);
			so.setCorners(param.bottomLeftCorner, param.topRightCorner);

			Log log = new Log(SeverityCategoryKraken.INFO, param.configfile, param.configprofile);

			if(param.dynObs == null)
				param.dynObs = new EmptyDynamicObstacles();
			
			injector.addService(log);
			injector.addService(config);
			injector.addService(DynamicObstacles.class, param.dynObs);		
			injector.addService(this);
			injector.addService(injector);

			if(param.engine != null)
				injector.addService(param.engine);
			else if(config.getBoolean(ConfigInfoKraken.ENABLE_QUADTREE))
				injector.addService(PhysicsEngine.class, injector.getService(QuadTreePhysicsEngine.class));
			else
				injector.addService(PhysicsEngine.class, injector.getService(DefaultPhysicsEngine.class));
			
			injector.addService(Display.class, param.display); // may be null

			astar = injector.getService(TentacularAStar.class);
			tentaclemanager = injector.getService(TentacleManager.class);
			profiles = injector.getService(ResearchProfileManager.class);			
			dpath = injector.getService(DynamicPath.class);
			
			List<TentacleType> tentaclesXY = new ArrayList<TentacleType>();
			for(ClothoTentacle t : ClothoTentacle.values())
				tentaclesXY.add(t);
			tentaclesXY.add(BezierTentacle.BEZIER_XYOC_TO_XY);
			addMode(new ResearchProfile(tentaclesXY, "XY", 1.3, 1.3/3, 5, 0, -100, new EndWithXY()));
			
			List<TentacleType> tentaclesXYO = new ArrayList<TentacleType>();
			for(ClothoTentacle t : ClothoTentacle.values())
				tentaclesXYO.add(t);
			tentaclesXYO.add(BezierTentacle.BEZIER_XYO_TO_XYO);
			tentaclesXYO.add(BezierTentacle.BEZIER_XYOC_TO_XYOC0);
			addMode(new ResearchProfile(tentaclesXYO, "XYO", 1.3, 1.3/3, 0, 5, 500, new EndWithXYO()));
			
			List<TentacleType> tentaclesXYOC = new ArrayList<TentacleType>();
			for(ClothoTentacle t : ClothoTentacle.values())
				tentaclesXYOC.add(t);
			tentaclesXYOC.add(BezierTentacle.BEZIER_XYOC_TO_XYOC0); // arrive avec une courbure nulle
			addMode(new ResearchProfile(tentaclesXYOC, "XYOC0", 1.3, 1.3/3, 0, 5, 500, new EndWithXYOC0()));
		} catch (InjectorException e) {
			throw new RuntimeException("Fatal error", e);
		}
	}
	
	public void stop()
	{
		astar.stop();
	}
	
	private boolean isJUnitTest()
	{
	    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
	    for (StackTraceElement element : stackTrace)
	        if (element.getClassName().startsWith("org.junit."))
	            return true;
	    return false;
	}
	
	/**
	 * Initialize a new search from :
	 * - a position and an orientation, to
	 * - a position
	 * @param start
	 * @param arrival
	 * @param directionstrategy
	 * @throws NoPathException
	 */
	public void initializeNewSearch(SearchParameters sp) throws PathfindingException
	{
		if(!autoReplanningEnable)
			astar.initializeNewSearch(sp.start, sp.arrival, sp.directionstrategy, sp.mode, sp.maxSpeed, sp.timeout);
		else
			throw new NotInitializedPathfindingException("initializeNewSearch() should be called before enabling the autoreplanning mode.");
	}
	
	/**
	 * Start the search. You must have called "initializeNewSearch" before the search.
	 * @return
	 * @throws PathfindingException
	 */
	public List<ItineraryPoint> search() throws PathfindingException
	{
		if(!autoReplanningEnable)
			return astar.searchWithoutReplanning();
		else
			throw new NotInitializedPathfindingException("search() isn't permitted in autoreplanning mode.");
	}
	
	/**
	 * Used by the unit tests
	 * @return
	 */
	protected Injector getInjector()
	{
		return injector;
	}

	public void addMode(ResearchProfile p)
	{
		profiles.addProfile(p);
		tentaclemanager.updateProfiles(p);
	}
	
	/**
	 * Print the values overridden by the configuration file
	 */
	public void displayOverriddenConfigValues()
	{
		config.printChangedValues();
	}
	
	public DynamicPath enableAutoReplanning()
	{
		try {
			CollisionDetectionThread rt = injector.getService(CollisionDetectionThread.class);
			// On le démarre (ou on le redémarre) si besoin est
			if(!rt.isAlive())
				rt.start();
			
			ReplanningThread rep = injector.getService(ReplanningThread.class);
			// idem
			if(!rep.isAlive())
				rep.start();
			
			autoReplanningEnable = true;
			return dpath;
		} catch (InjectorException e) {
			e.printStackTrace();
			assert false;
			return null;
		}
	}
	
	public void endAutoReplanning()
	{
		if(autoReplanningEnable)
		{
			if(dpath.isStarted())
				try {
					endContinuousSearch();
				} catch (NotInitializedPathfindingException e) {
					e.printStackTrace();
				}
			injector.getExistingService(CollisionDetectionThread.class).interrupt();
			injector.getExistingService(ReplanningThread.class).interrupt();
			autoReplanningEnable = false;
		}
	}
	
	public boolean checkPath(List<ItineraryPoint> initialPath)
	{
		try {
			astar.checkAfterInitialization(initialPath);
			return true;
		} catch (InvalidPathException e) {
			return false;
		}
	}
	
	public void startContinuousSearchWithInitialPath(SearchParameters sp, List<ItineraryPoint> initialPath) throws PathfindingException
	{
		if(dpath.isStarted())
			throw new NotInitializedPathfindingException("You should end the previous continuous search before starting a new one.");
		else if(autoReplanningEnable)
		{
			astar.initializeNewSearch(sp.start, sp.arrival, sp.directionstrategy, sp.mode, sp.maxSpeed, sp.timeout);
			astar.searchWithReplanningAndInitialPath(initialPath);
		}
		else
			throw new NotInitializedPathfindingException("You should enable the continuous search before starting it.");
	}

	public void startContinuousSearch(SearchParameters sp) throws PathfindingException
	{
		if(dpath.isStarted())
			throw new NotInitializedPathfindingException("You should end the previous continuous search before starting a new one.");
		else if(autoReplanningEnable)
		{
			astar.initializeNewSearch(sp.start, sp.arrival, sp.directionstrategy, sp.mode, sp.maxSpeed, sp.timeout);
			dpath.startContinuousSearch();
		}
		else
			throw new NotInitializedPathfindingException("You should enable the continuous search before starting it.");
	}

	public void endContinuousSearch() throws NotInitializedPathfindingException
	{
		if(!dpath.isStarted())
			throw new NotInitializedPathfindingException("");
		else if(autoReplanningEnable)
			dpath.endContinuousSearch();
		else
			throw new NotInitializedPathfindingException("You should enable the continuous search before starting it.");
	}
	
	
}
