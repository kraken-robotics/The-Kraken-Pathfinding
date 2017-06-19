/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.ArrayList;
import java.util.List;
import pfg.config.Config;
import pfg.graphic.Fenetre;
import pfg.graphic.AbstractPrintBuffer;
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
import pfg.kraken.utils.*;

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

	private static int nbInstances = 0;

	/**
	 * Fonction appelé automatiquement à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le
	 * log.
	 */
	public synchronized void destructor()
	{	
		/*
		 * Il ne faut pas appeler deux fois le destructeur
		 */
		if(nbInstances == 0)
			return;

		AbstractPrintBuffer buffer = injector.getExistingService(AbstractPrintBuffer.class);
		// On appelle le destructeur du PrintBuffer
		if(buffer != null)
			buffer.destructor();

		// fermeture du log
		log.debug("Fermeture du log");
		log.close();
		nbInstances--;
	}
	
	public Kraken(List<Obstacle> fixedObstacles)
	{
		this(fixedObstacles, new EmptyDynamicObstacles(), null);
	}
	
	/**
	 * Instancie le gestionnaire de dépendances et quelques services critiques
	 * (log et config qui sont interdépendants)
	 */
	public Kraken(List<Obstacle> fixedObstacles, DynamicObstacles dynObs, TentacleType tentacleTypes)
	{	
		/**
		 * On vérifie qu'il y ait un seul container à la fois
		 */
		if(nbInstances != 0)
		{
			log.critical("Un autre container existe déjà! Annulation du constructeur.");
			int z = 0;
			z = 1/z;
			return;
		}

		nbInstances++;

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
			log = injector.getService(Log.class);
			config = new Config(ConfigInfoKraken.values(), "kraken.conf", false);
			injector.addService(Config.class, config);
			injector.addService(DynamicObstacles.class, dynObs);
	
			log.useConfig(config);
	
			injector.addService(Kraken.class, this);
	
			if(config.getBoolean(ConfigInfoKraken.GRAPHIC_ENABLE))
			{
				Fenetre f = injector.getService(Fenetre.class);
//				if(config.getBoolean(ConfigInfoKraken.GRAPHIC_EXTERNAL))
//					injector.addService(PrintBufferInterface.class, f.getBu);
//				else
					injector.addService(AbstractPrintBuffer.class, f.getPrintBuffer());
			}
			else
			{
				ConfigInfoKraken.unsetGraphic();
				config.reload();
				injector.addService(AbstractPrintBuffer.class, null);
			}
	
			Obstacle.set(log, injector.getService(AbstractPrintBuffer.class));
			Obstacle.useConfig(config);
			Tentacle.useConfig(config);
			if(fixedObstacles != null)
				injector.getService(StaticObstacles.class).addAll(fixedObstacles);
			injector.getService(TentacleManager.class).setTentacle(tentacleTypesUsed);
			astar = injector.getService(TentacularAStar.class);
			
		}
		catch(InjectorException e)
		{
			System.err.println("Fatal error : "+e);
		}
	}

	public TentacularAStar getAStar()
	{
		return astar;
	}
	
	protected Injector getInjector()
	{
		return injector;
	}
}
