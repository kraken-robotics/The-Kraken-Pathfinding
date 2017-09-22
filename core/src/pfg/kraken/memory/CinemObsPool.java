/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.memory;

import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.tentacles.DynamicTentacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.CinematiqueObs;
import pfg.log.Log;

/**
 * Classe qui fournit des objets CinematiqueObs
 * Ces CinematiqueObs ne sont utilisés QUE pas les arcs courbes cubiques, qui
 * ont un nombre de CinematiqueObs pas connu à l'avance
 * Les arcs courbes de clothoïde contiennent des CinematiqueObs et sont gérés
 * par le NodeMM
 * 
 * @author pf
 *
 */

public class CinemObsPool extends MemoryPool<CinematiqueObs>
{
	private RectangularObstacle vehicleTemplate;
	
	public CinemObsPool(Log log, Config config, RectangularObstacle vehicleTemplate)
	{
		super(CinematiqueObs.class, log);
		this.vehicleTemplate = vehicleTemplate;
		init(config.getInt(ConfigInfoKraken.NB_INSTANCES_OBSTACLES));
	}

	@Override
	protected final void make(CinematiqueObs[] nodes)
	{
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new CinematiqueObs(vehicleTemplate);
	}
	
	// TODO : optimisable : la mémoire est probablement contigue
	public final void destroyNode(DynamicTentacle arc)
	{
		for(int i = 0; i < arc.getNbPoints(); i++)
			destroyNode(arc.getPoint(i));
	}

}
