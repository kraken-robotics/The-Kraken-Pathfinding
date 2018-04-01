/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import pfg.config.Config;
import pfg.config.ConfigInfo;
import pfg.graphic.Vec2RO;
import pfg.log.Log;
import pfg.graphic.printable.Layer;
import pfg.graphic.GraphicDisplay;
import pfg.graphic.ConfigInfoGraphic;
import pfg.graphic.DebugTool;
import pfg.injector.Injector;
import pfg.injector.InjectorException;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.astar.tentacles.EndOfTrajectoryCheck;
import pfg.kraken.astar.tentacles.EndWithXY;
import pfg.kraken.astar.tentacles.EndWithXYO;
import pfg.kraken.astar.tentacles.ResearchProfileManager;
import pfg.kraken.astar.tentacles.TentacleManager;
import pfg.kraken.astar.tentacles.types.*;
import pfg.kraken.exceptions.NoPathException;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.EmptyDynamicObstacles;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;

/**
 * The manager of the tentacle pathfinder.
 * TentacularAStar wrapper.
 * @author pf
 *
 */
public class Kraken
{
	private Config config;
	private Injector injector;
	private TentacularAStar astar;
	private ResearchProfileManager profiles;
	private TentacleManager tentaclemanager;
	private static final String version = "1.3.0";
	
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
		this(vehicleTemplate, fixedObstacles, new EmptyDynamicObstacles(), bottomLeftCorner, topRightCorner, configfile, profiles);
	}
	
	/**
	 * Get Kraken with :
	 * @param vehicleTemplate : the shape of the vehicle
	 * @param fixedObstacles : a list of fixed/permanent obstacles
	 * @param dynObs : a dynamic/temporary obstacles manager that implements the DynamicObstacles interface
	 * @param bottomLeftCorner : the bottom left corner of the search domain
	 * @param topRightCorner : the top right corner of the search domain
	 * @param configprofile : the config profiles
	 */
/*	public Kraken(RectangularObstacle vehicleTemplate, List<Obstacle> fixedObstacles, DynamicObstacles dynObs, XY bottomLeftCorner, XY topRightCorner, String...configprofile)
	{
		this(vehicleTemplate, fixedObstacles, dynObs, bottomLeftCorner, topRightCorner, configprofile);
	}*/
	
	/**
	 * Get Kraken with :
	 * @param vehicleTemplate : the shape of the vehicle
	 * @param fixedObstacles : a list of fixed/permanent obstacles
	 * @param dynObs : a dynamic/temporary obstacles manager that implements the DynamicObstacles interface
	 * @param bottomLeftCorner : the bottom left corner of the search domain
	 * @param topRightCorner : the top right corner of the search domain
	 * @param configprofile : the config profiles
	 */
	public Kraken(RectangularObstacle vehicleTemplate, Iterable<Obstacle> fixedObstacles, DynamicObstacles dynObs, XY bottomLeftCorner, XY topRightCorner, String configfile, String...configprofile)
	{	
		injector = new Injector();
		config = new Config(ConfigInfoKraken.values(), isJUnitTest(), configfile, configprofile);
		injector.addService(RectangularObstacle.class, vehicleTemplate);
		
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
			if(fixedObstacles != null)
				for(Obstacle o : fixedObstacles)
					so.add(o);
			so.setCorners(bottomLeftCorner, topRightCorner);

			Log log = new Log(SeverityCategoryKraken.INFO, configfile, configprofile);

			injector.addService(log);
			injector.addService(config);
			injector.addService(DynamicObstacles.class, dynObs);		
			injector.addService(this);
			injector.addService(injector);

			/*
			 * Override the graphic config
			 */
			HashMap<ConfigInfo, Object> overrideGraphic = new HashMap<ConfigInfo, Object>();
			overrideGraphic.put(ConfigInfoGraphic.SIZE_X_WITH_UNITARY_ZOOM, (int) (topRightCorner.getX() - bottomLeftCorner.getX()));
			overrideGraphic.put(ConfigInfoGraphic.SIZE_Y_WITH_UNITARY_ZOOM, (int) (topRightCorner.getY() - bottomLeftCorner.getY()));
			
			DebugTool debug = DebugTool.getDebugTool(overrideGraphic, new Vec2RO((topRightCorner.getX() + bottomLeftCorner.getX()) / 2, (topRightCorner.getY() + bottomLeftCorner.getY()) / 2), SeverityCategoryKraken.INFO, configfile, configprofile);
			injector.addService(debug.getGraphicDisplay());
			
			if(config.getBoolean(ConfigInfoKraken.GRAPHIC_ENABLE))
			{
				if(config.getBoolean(ConfigInfoKraken.GRAPHIC_SERVER))
					debug.startPrintServer();
				
				if(fixedObstacles != null && config.getBoolean(ConfigInfoKraken.GRAPHIC_FIXED_OBSTACLES))
				{
					GraphicDisplay display = injector.getExistingService(GraphicDisplay.class);
					for(Obstacle o : fixedObstacles)
						display.addPrintable(o, Color.BLACK, Layer.MIDDLE.layer);
				}
			}


			astar = injector.getService(TentacularAStar.class);
			tentaclemanager = injector.getService(TentacleManager.class);
			profiles = injector.getService(ResearchProfileManager.class);			

			List<TentacleType> tentaclesXY = new ArrayList<TentacleType>();
			for(ClothoTentacle t : ClothoTentacle.values())
				tentaclesXY.add(t);
			tentaclesXY.add(BezierTentacle.BEZIER_XYOC_TO_XY);
			addMode("XY", tentaclesXY, new EndWithXY());
			
			List<TentacleType> tentaclesXYO = new ArrayList<TentacleType>();
			for(ClothoTentacle t : ClothoTentacle.values())
				tentaclesXYO.add(t);
			tentaclesXYO.add(BezierTentacle.BEZIER_XYO_TO_XYO);
			tentaclesXYO.add(BezierTentacle.BEZIER_XYOC_TO_XYO);
			addMode("XYO", tentaclesXYO, new EndWithXYO());
		} catch (InjectorException e) {
			throw new RuntimeException("Fatal error", e);
		}
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
	public void initializeNewSearch(XYO start, XY arrival, DirectionStrategy directionstrategy, double maxSpeed) throws NoPathException
	{
		astar.initializeNewSearch(new Cinematique(start), new Cinematique(new XYO(arrival.clone(), 0)), directionstrategy, "XY", maxSpeed);
	}
	
	/**
	 * Initialize a new search from :
	 * - a position and an orientation, to
	 * - a position and an orientation
	 * @param start
	 * @param arrival
	 * @param directionstrategy
	 * @throws NoPathException
	 */
	public void initializeNewSearch(XYO start, XYO arrival, DirectionStrategy directionstrategy, double maxSpeed) throws NoPathException
	{
		astar.initializeNewSearch(new Cinematique(start), new Cinematique(arrival), directionstrategy, "XYO", maxSpeed);
	}
	
	/**
	 * Initialize a new search from :
	 * - a position and an orientation, to
	 * - a position and an orientation
	 * @param start
	 * @param arrival
	 * @param directionstrategy
	 * @throws NoPathException
	 */
	public void initializeNewSearch(Cinematique start, Cinematique arrival, DirectionStrategy directionstrategy, String mode, double maxSpeed) throws NoPathException
	{
		astar.initializeNewSearch(start, arrival, directionstrategy, mode, maxSpeed);
	}
	
	/**
	 * Initialize a new search from :
	 * - a position and an orientation, to
	 * - a position and an orientation
	 * Use the default direction strategy
	 * @param start
	 * @param arrival
	 * @throws NoPathException
	 */
	public void initializeNewSearch(XYO start, XYO arrival) throws NoPathException
	{
		astar.initializeNewSearch(start, arrival);
	}
	
	/**
	 * Initialize a new search from :
	 * - a position and an orientation, to
	 * - a position
	 * Use the default direction strategy
	 * @param start
	 * @param arrival
	 * @throws NoPathException
	 */
	public void initializeNewSearch(XYO start, XY arrival) throws NoPathException
	{
		astar.initializeNewSearch(start, arrival);
	}

	/**
	 * Start the search. You must have called "initializeNewSearch" before the search.
	 * @return
	 * @throws PathfindingException
	 */
	public LinkedList<ItineraryPoint> search() throws PathfindingException
	{
		return astar.search();
	}
	
	/**
	 * Used by the unit tests
	 * @return
	 */
	protected Injector getInjector()
	{
		return injector;
	}

	/**
	 * Get the graphic display
	 * @return
	 */
	public GraphicDisplay getGraphicDisplay()
	{
		return injector.getExistingService(GraphicDisplay.class);
	}
	
	public static String getVersion()
	{
		return version;
	}
	
	public void addMode(String name, List<TentacleType> tentacles, EndOfTrajectoryCheck end)
	{
		profiles.addProfile(name, tentacles, end);
		tentaclemanager.updateProfiles(name);
	}
	
	/**
	 * Print the values overridden by the configuration file
	 */
	public void displayOverriddenConfigValues()
	{
		config.printChangedValues();
	}
}
