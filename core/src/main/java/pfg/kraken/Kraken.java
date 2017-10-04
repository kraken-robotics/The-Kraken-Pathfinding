/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import pfg.config.Config;
import pfg.config.ConfigInfo;
import pfg.graphic.Vec2RO;
import pfg.graphic.WindowFrame;
import pfg.graphic.log.Log;
import pfg.graphic.GraphicDisplay;
import pfg.graphic.ConfigInfoGraphic;
import pfg.graphic.DebugTool;
import pfg.injector.Injector;
import pfg.injector.InjectorException;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.astar.tentacles.ResearchProfileManager;
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
	
	/**
	 * Get Kraken with :
	 * @param vehicleTemplate : the shape of the vehicle
	 * @param fixedObstacles : a list of fixed/permanent obstacles
	 * @param bottomLeftCorner : the bottom left corner of the search domain
	 * @param topRightCorner : the top right corner of the search domain
	 * @param configprofile : the config profiles
	 */
	public Kraken(RectangularObstacle vehicleTemplate, List<Obstacle> fixedObstacles, XY bottomLeftCorner, XY topRightCorner, String...profiles)
	{
		this(vehicleTemplate, fixedObstacles, new EmptyDynamicObstacles(), bottomLeftCorner, topRightCorner, profiles);
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
	public Kraken(RectangularObstacle vehicleTemplate, List<Obstacle> fixedObstacles, DynamicObstacles dynObs, XY bottomLeftCorner, XY topRightCorner, String...configprofile)
	{	
		List<TentacleType> tentacleTypesUsed = new ArrayList<TentacleType>();
		for(BezierTentacle t : BezierTentacle.values())
			tentacleTypesUsed.add(t);
		for(ClothoTentacle t : ClothoTentacle.values())
			tentacleTypesUsed.add(t);
/*			for(TurnoverTentacle t : TurnoverTentacle.values())
				tentacleTypesUsed.add(t);
			for(StraightingTentacle t : StraightingTentacle.values())
				tentacleTypesUsed.add(t);*/
		
		injector = new Injector();
		config = new Config(ConfigInfoKraken.values(), false, "kraken.conf", configprofile);
		injector.addService(RectangularObstacle.class, vehicleTemplate);
		
		try {
			ResearchProfileManager profiles = injector.getService(ResearchProfileManager.class);
			profiles.addProfile(tentacleTypesUsed);
			
			StaticObstacles so = injector.getService(StaticObstacles.class); 
			if(fixedObstacles != null)
				so.addAll(fixedObstacles);
			so.setCorners(bottomLeftCorner, topRightCorner);


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
			
			Log log = Log.getLog(SeverityCategoryKraken.INFO);

			injector.addService(log);
			injector.addService(config);
			injector.addService(DynamicObstacles.class, dynObs);		
			injector.addService(this);
			injector.addService(injector);

			if(config.getBoolean(ConfigInfoKraken.GRAPHIC_ENABLE))
			{
				DebugTool debug = DebugTool.getDebugTool(overrideGraphic, new Vec2RO((topRightCorner.getX() + bottomLeftCorner.getX()) / 2, (topRightCorner.getY() + bottomLeftCorner.getY()) / 2), SeverityCategoryKraken.INFO, null);
				WindowFrame f = debug.getWindowFrame();
				injector.addService(f.getPrintBuffer());
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
		astar.initializeNewSearch(start, arrival, directionstrategy);
	}
	
	/**
	 * Initialize a new search from :
	 * - a position and an orientation, to
	 * - a position
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
}
