/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.ArrayList;
import java.util.List;
import pfg.config.Config;
import pfg.graphic.Vec2RO;
import pfg.graphic.WindowFrame;
import pfg.log.Log;
import pfg.graphic.PrintBuffer;
import pfg.graphic.DebugTool;
import pfg.injector.Injector;
import pfg.injector.InjectorException;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.astar.tentacles.Tentacle;
import pfg.kraken.astar.tentacles.TentacleManager;
import pfg.kraken.astar.tentacles.types.*;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.EmptyDynamicObstacles;
import pfg.kraken.obstacles.container.StaticObstacles;

/**
 * 
 * Gestionnaire de la durée de vie des objets dans le code.
 * Permet à n'importe quelle classe implémentant l'interface "Service"
 * d'appeller d'autres instances de services via son constructeur.
 * Une classe implémentant Service n'est instanciée que par la classe
 * "Container"
 * 
 * @author pf
 */
public class Kraken
{
	private Log log;
	private Config config;
	private Injector injector;
	private TentacularAStar astar;

	private static Kraken instance;

	/**
	 * Fonction appelé automatiquement à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le
	 * log.
	 */
	public synchronized void destructor()
	{	
		PrintBuffer buffer = injector.getExistingService(PrintBuffer.class);
		// On appelle le destructeur du PrintBuffer
		if(buffer != null)
			buffer.destructor();

		// fermeture du log
		log.close();
		instance = null;
	}
	
	public static Kraken getKraken(List<Obstacle> fixedObstacles)
	{
		if(instance == null)
			instance = new Kraken(fixedObstacles, new EmptyDynamicObstacles(), null);
		return instance;
	}
	
	public static Kraken getKraken(List<Obstacle> fixedObstacles, DynamicObstacles dynObs, TentacleType tentacleTypes)
	{
		if(instance == null)
			instance = new Kraken(fixedObstacles, dynObs, tentacleTypes);
		return instance;
	}
	
	/**
	 * Instancie le gestionnaire de dépendances et quelques services critiques
	 * (log et config qui sont interdépendants)
	 */
	private Kraken(List<Obstacle> fixedObstacles, DynamicObstacles dynObs, TentacleType tentacleTypes)
	{	
		assert instance == null;
		List<TentacleType> tentacleTypesUsed = new ArrayList<TentacleType>();
		
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

		try {
			DebugTool debug = new DebugTool("graphic-kraken.conf", SeverityCategoryKraken.INFO);
			log = debug.getLog();
			config = new Config(ConfigInfoKraken.values(), "kraken.conf", false);

			injector.addService(Log.class, log);
			injector.addService(Config.class, config);
			injector.addService(DynamicObstacles.class, dynObs);
	
			injector.addService(Kraken.class, this);
	
			if(config.getBoolean(ConfigInfoKraken.GRAPHIC_ENABLE))
			{
				WindowFrame f = debug.getWindowFrame(new Vec2RO(0, 1000));
				injector.addService(WindowFrame.class, f);
//				if(config.getBoolean(ConfigInfoKraken.GRAPHIC_EXTERNAL))
//					injector.addService(PrintBufferInterface.class, f.getBu);
//				else
					injector.addService(PrintBuffer.class, f.getPrintBuffer());
			}
			else
			{
				ConfigInfoKraken.unsetGraphic();
				config.reload();
				injector.addService(PrintBuffer.class, null);
			}
	
			Obstacle.set(log, injector.getService(PrintBuffer.class));
			Obstacle.useConfig(config);
			Tentacle.useConfig(config);
			if(fixedObstacles != null)
				injector.getService(StaticObstacles.class).addAll(fixedObstacles);
			injector.getService(TentacleManager.class).setTentacle(tentacleTypesUsed);
			astar = injector.getService(TentacularAStar.class);
			
		}
		catch(InjectorException e)
		{
			throw new RuntimeException("Fatal error : "+e);
		}
	}

	public TentacularAStar getAStar()
	{
		return astar;
	}
	
	/**
	 * Used by the unit tests
	 * @return
	 */
	protected Injector getInjector()
	{
		return injector;
	}
}
