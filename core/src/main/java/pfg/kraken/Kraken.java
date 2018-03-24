/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
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
import pfg.graphic.WindowFrame;
import pfg.log.Log;
import pfg.graphic.printable.Layer;
import pfg.graphic.GraphicDisplay;
import pfg.graphic.ConfigInfoGraphic;
import pfg.graphic.DebugTool;
import pfg.injector.Injector;
import pfg.injector.InjectorException;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.ResearchMode;
import pfg.kraken.astar.TentacularAStar;
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
	private boolean stopped = false;
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
	 * Stop Kraken
	 */
	public void stop()
	{
		TentacleManager tm = injector.getExistingService(TentacleManager.class);
		if(tm != null)
			tm.stopThreads();
		stopped = true;
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
	public Kraken(RectangularObstacle vehicleTemplate, Iterable<Obstacle> fixedObstacles, DynamicObstacles dynObs, XY bottomLeftCorner, XY topRightCorner, String configfile, String...configprofile)
	{	
		injector = new Injector();
		config = new Config(ConfigInfoKraken.values(), isJUnitTest(), configfile, configprofile);
		injector.addService(RectangularObstacle.class, vehicleTemplate);
		
		List<TentacleType> tentacleTypesUsed = new ArrayList<TentacleType>();
		for(BezierTentacle t : BezierTentacle.values())
			tentacleTypesUsed.add(t);
		for(ClothoTentacle t : ClothoTentacle.values())
			tentacleTypesUsed.add(t);
		for(TurnoverTentacle t : TurnoverTentacle.values())
			tentacleTypesUsed.add(t);
		for(StraightingTentacle t : StraightingTentacle.values())
			tentacleTypesUsed.add(t);
		if(config.getBoolean(ConfigInfoKraken.ALLOW_SPINNING))
			for(SpinTentacle t : SpinTentacle.values())
				tentacleTypesUsed.add(t);
		
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
			ResearchProfileManager profiles = injector.getService(ResearchProfileManager.class);
			
			for(ResearchMode m : ResearchMode.values())
			{
				List<TentacleType> tentacles = new ArrayList<TentacleType>();
				for(TentacleType t : tentacleTypesUsed)
					if(t.usableFor(m))
						tentacles.add(t);
				profiles.addProfile(tentacles);
			}
			
			StaticObstacles so = injector.getService(StaticObstacles.class); 
			if(fixedObstacles != null)
				for(Obstacle o : fixedObstacles)
					so.add(o);
			so.setCorners(bottomLeftCorner, topRightCorner);

			Log log = new Log(SeverityCategoryKraken.INFO, configfile, "log");

			injector.addService(log);
			injector.addService(config);
			injector.addService(DynamicObstacles.class, dynObs);		
			injector.addService(this);
			injector.addService(injector);

			if(config.getBoolean(ConfigInfoKraken.GRAPHIC_ENABLE))
			{
				/*
				 * Override the graphic config
				 */
				HashMap<ConfigInfo, Object> overrideGraphic = new HashMap<ConfigInfo, Object>();
				for(ConfigInfoGraphic infoG : ConfigInfoGraphic.values())
					for(ConfigInfoKraken infoK : ConfigInfoKraken.values())
						if(infoG.toString().equals(infoK.toString()))
							overrideGraphic.put(infoG, config.getObject(infoK));
				overrideGraphic.put(ConfigInfoGraphic.SIZE_X_WITH_UNITARY_ZOOM, (int) (topRightCorner.getX() - bottomLeftCorner.getX()));
				overrideGraphic.put(ConfigInfoGraphic.SIZE_Y_WITH_UNITARY_ZOOM, (int) (topRightCorner.getY() - bottomLeftCorner.getY()));
				
				DebugTool debug = DebugTool.getDebugTool(overrideGraphic, new Vec2RO((topRightCorner.getX() + bottomLeftCorner.getX()) / 2, (topRightCorner.getY() + bottomLeftCorner.getY()) / 2), SeverityCategoryKraken.INFO, null);
				WindowFrame f = debug.getWindowFrame();
				injector.addService(f.getPrintBuffer());
				
				if(config.getBoolean(ConfigInfoKraken.GRAPHIC_SERVER))
					debug.startPrintServer();
				
				if(fixedObstacles != null && config.getBoolean(ConfigInfoKraken.GRAPHIC_FIXED_OBSTACLES))
				{
					GraphicDisplay display = injector.getExistingService(GraphicDisplay.class);
					for(Obstacle o : fixedObstacles)
						display.addPrintable(o, Color.BLACK, Layer.MIDDLE.layer);
				}
			}
			else
			{
				injector.addService(GraphicDisplay.class, new GraphicDisplayPlaceholder());
				HashMap<ConfigInfo, Object> override = new HashMap<ConfigInfo, Object>();
				List<ConfigInfo> graphicConf = ConfigInfoKraken.getGraphicConfigInfo();
				for(ConfigInfo c : graphicConf)
					override.put(c, false);
				config.override(override);
			}
	
//				injector.getService(TentacleManager.class).setTentacle(tentacleTypesUsed);	
			astar = injector.getService(TentacularAStar.class);
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
	public void initializeNewSearch(XYO start, XY arrival, DirectionStrategy directionstrategy) throws NoPathException
	{
		if(!stopped)
			astar.initializeNewSearch(start, new XYO(arrival.clone(), 0), directionstrategy, ResearchMode.XYO2XY);
		else
			throw new RuntimeException("Kraken is stopped !");
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
	public void initializeNewSearch(XYO start, XYO arrival, DirectionStrategy directionstrategy) throws NoPathException
	{
		if(!stopped)
			astar.initializeNewSearch(start, arrival, directionstrategy, ResearchMode.XYO2XYO);
		else
			throw new RuntimeException("Kraken is stopped !");
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
		if(!stopped)
			astar.initializeNewSearch(start, arrival);
		else
			throw new RuntimeException("Kraken is stopped !");
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
		if(!stopped)
			astar.initializeNewSearch(start, arrival);
		else
			throw new RuntimeException("Kraken is stopped !");
	}

	/**
	 * Start the search. You must have called "initializeNewSearch" before the search.
	 * @return
	 * @throws PathfindingException
	 */
	public LinkedList<ItineraryPoint> search() throws PathfindingException
	{
		if(!stopped)
			return astar.search();
		else
			throw new RuntimeException("Kraken is stopped !");
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
}
