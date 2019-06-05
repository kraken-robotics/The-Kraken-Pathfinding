/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.memory;

import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.CinematiqueObs;

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

public final class CinemObsPool extends MemoryPool<CinematiqueObs>
{
	private RectangularObstacle vehicleTemplate;
	
	public CinemObsPool(Config config, RectangularObstacle vehicleTemplate)
	{
		super(CinematiqueObs.class);
		this.vehicleTemplate = vehicleTemplate;
		init(config.getInt(ConfigInfoKraken.OBSTACLES_MEMORY_POOL_SIZE));
	}

	@Override
	protected final void make(CinematiqueObs[] nodes)
	{
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new CinematiqueObs(vehicleTemplate);
	}
}
