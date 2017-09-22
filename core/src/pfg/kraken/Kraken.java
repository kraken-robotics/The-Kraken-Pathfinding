/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import pfg.config.Config;
import pfg.config.ConfigInfo;
import pfg.graphic.Vec2RO;
import pfg.graphic.WindowFrame;
import pfg.log.Log;
import pfg.graphic.PrintBuffer;
import pfg.graphic.ConfigInfoGraphic;
import pfg.graphic.DebugTool;
import pfg.injector.Injector;
import pfg.injector.InjectorException;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.astar.tentacles.Tentacle;
import pfg.kraken.astar.tentacles.TentacleManager;
import pfg.kraken.astar.tentacles.types.*;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.EmptyDynamicObstacles;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.utils.XY;

/**
 * The manager of the tentacular pathfinder.
 * @author pf
 *
 */
public class Kraken
{
	private Config config;
	private Injector injector;
	private List<TentacleType> tentacleTypesUsed;
	private boolean initialized = false;
	private XY bottomLeftCorner, topRightCorner;
	private DynamicObstacles dynObs;

	private static Kraken instance;

	/**
	 * Call this function if you want to create a new Kraken.
	 * The graphic interface is stopped.
	 */
	public synchronized void destructor()
	{	
		if(instance != null)
		{
			PrintBuffer buffer = injector.getExistingService(PrintBuffer.class);
			// On appelle le destructeur du PrintBuffer
			if(buffer != null)
				buffer.destructor();
	
			// fermeture du log
			Log log = injector.getExistingService(Log.class);
			if(log != null)
				log.close();
			instance = null;
		}
	}
	
	/**
	 * Get Kraken with permanent obstacles. Note that Kraken won't be able to deal with dynamic obstacles.
	 * @param fixedObstacles : a list of fixed/permanent obstacles
	 * @return the instance of Kraken
	 */
	public static Kraken getKraken(RectangularObstacle vehicleTemplate, List<Obstacle> fixedObstacles, XY bottomLeftCorner, XY topRightCorner, String...profiles)
	{
		if(instance == null)
			instance = new Kraken(vehicleTemplate, fixedObstacles, new EmptyDynamicObstacles(), null, bottomLeftCorner, topRightCorner, profiles);
		return instance;
	}
	
	/**
	 * Get Kraken with :
	 * @param fixedObstacles : a list of fixed/permanent obstacles
	 * @param dynObs : a dynamic/temporary obstacles manager that implements the DynamicObstacles interface
	 * @param tentacleTypes : 
	 * @return
	 */
	public static Kraken getKraken(RectangularObstacle vehicleTemplate, List<Obstacle> fixedObstacles, DynamicObstacles dynObs, XY bottomLeftCorner, XY topRightCorner, String...configprofile)
	{
		if(instance == null)
			instance = new Kraken(vehicleTemplate, fixedObstacles, dynObs, null, bottomLeftCorner, topRightCorner, configprofile);
		return instance;
	}
	
	/**
	 * Instancie le gestionnaire de dépendances et quelques services critiques
	 * (log et config qui sont interdépendants)
	 */
	private Kraken(RectangularObstacle vehicleTemplate, List<Obstacle> fixedObstacles, DynamicObstacles dynObs, TentacleType tentacleTypes, XY bottomLeftCorner, XY topRightCorner, String...configprofile)
	{	
		assert instance == null;
		this.bottomLeftCorner = bottomLeftCorner;
		this.topRightCorner = topRightCorner;
		this.dynObs = dynObs;
		
		tentacleTypesUsed = new ArrayList<TentacleType>();
		if(tentacleTypes == null)
		{
			for(BezierTentacle t : BezierTentacle.values())
				tentacleTypesUsed.add(t);
			for(ClothoTentacle t : ClothoTentacle.values())
				tentacleTypesUsed.add(t);
			for(TurnoverTentacle t : TurnoverTentacle.values())
				tentacleTypesUsed.add(t);
			for(StraightingTentacle t : StraightingTentacle.values())
				tentacleTypesUsed.add(t);
		}
		
		injector = new Injector();
		config = new Config(ConfigInfoKraken.values(), false, "kraken.conf", configprofile);
		try {
			injector.addService(RectangularObstacle.class, vehicleTemplate);
			StaticObstacles so = injector.getService(StaticObstacles.class); 
			if(fixedObstacles != null)
				so.addAll(fixedObstacles);
			so.setCorners(bottomLeftCorner, topRightCorner);
		} catch (InjectorException e) {
			throw new RuntimeException("Fatal error", e);
		}
		initialize();
	}
	
	/**
	 * Initialize Kraken
	 * @return
	 */
	private void initialize()
	{
		try {
			if(!initialized)
			{
				initialized = true;

				/*
				 * Override the graphic config
				 */
				HashMap<ConfigInfo, Object> overrideGraphic = new HashMap<ConfigInfo, Object>();
				overrideGraphic.put(ConfigInfoGraphic.SIZE_X_WITH_UNITARY_ZOOM, (int) (topRightCorner.getX() - bottomLeftCorner.getX()));
				overrideGraphic.put(ConfigInfoGraphic.SIZE_Y_WITH_UNITARY_ZOOM, (int) (topRightCorner.getY() - bottomLeftCorner.getY()));
				overrideGraphic.put(ConfigInfoGraphic.BACKGROUND_PATH, config.getString(ConfigInfoKraken.BACKGROUND_PATH));
				overrideGraphic.put(ConfigInfoGraphic.DISPLAY_GRID, config.getBoolean(ConfigInfoKraken.DISPLAY_GRID));
				overrideGraphic.put(ConfigInfoGraphic.SIZE_X_WINDOW, config.getInt(ConfigInfoKraken.SIZE_X_WINDOW));
				overrideGraphic.put(ConfigInfoGraphic.SIZE_Y_WINDOW, config.getInt(ConfigInfoKraken.SIZE_Y_WINDOW));
				overrideGraphic.put(ConfigInfoGraphic.GRAPHIC_SERVER_PORT_NUMBER, config.getInt(ConfigInfoKraken.GRAPHIC_SERVER_PORT_NUMBER));
				overrideGraphic.put(ConfigInfoGraphic.CONSOLE_NB_ROWS, config.getInt(ConfigInfoKraken.CONSOLE_NB_ROWS));
				overrideGraphic.put(ConfigInfoGraphic.CONSOLE_NB_COLUMNS, config.getInt(ConfigInfoKraken.CONSOLE_NB_COLUMNS));
				overrideGraphic.put(ConfigInfoGraphic.FAST_LOG, config.getBoolean(ConfigInfoKraken.FAST_LOG));
				overrideGraphic.put(ConfigInfoGraphic.STDOUT_LOG, config.getBoolean(ConfigInfoKraken.STDOUT_LOG));
				
				DebugTool debug = new DebugTool(overrideGraphic, SeverityCategoryKraken.INFO, null);
				Log log = debug.getLog();

				injector.addService(log);
				injector.addService(config);
				injector.addService(DynamicObstacles.class, dynObs);		
				injector.addService(this);
		
				if(config.getBoolean(ConfigInfoKraken.GRAPHIC_ENABLE))
				{
					WindowFrame f = debug.getWindowFrame(new Vec2RO((topRightCorner.getX() + bottomLeftCorner.getX()) / 2, (topRightCorner.getY() + bottomLeftCorner.getY()) / 2));
					injector.addService(f);
					injector.addService(f.getPrintBuffer());
				}
				else
				{
					injector.addService(PrintBuffer.class, new PrintBufferPlaceholder());
					HashMap<ConfigInfo, Object> override = new HashMap<ConfigInfo, Object>();
					List<ConfigInfo> graphicConf = ConfigInfoKraken.getGraphicConfigInfo();
					for(ConfigInfo c : graphicConf)
						override.put(c, false);
					config.override(override);
				}
		
				Obstacle.set(log, injector.getService(PrintBuffer.class));
				Tentacle.useConfig(config);
				injector.getService(TentacleManager.class).setTentacle(tentacleTypesUsed);	
				injector.getService(TentacularAStar.class);
			}
		} catch (InjectorException e) {
			throw new RuntimeException("Fatal error", e);
		}
	}
	
	/**
	 * Return the tentacular pathfinder
	 * @return
	 */
	public TentacularAStar getAStar()
	{
		return injector.getExistingService(TentacularAStar.class);
	}

	/**
	 * Used by the unit tests
	 * @return
	 */
	protected Injector getInjector()
	{
		return injector;
	}

	public PrintBuffer getPrintBuffer()
	{
		return injector.getExistingService(PrintBuffer.class);
	}
}
