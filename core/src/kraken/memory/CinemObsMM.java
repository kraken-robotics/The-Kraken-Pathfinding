/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package kraken.memory;

import config.Config;
import kraken.ConfigInfoKraken;
import kraken.pathfinding.astar.arcs.ArcCourbeDynamique;
import kraken.robot.CinematiqueObs;
import kraken.utils.Log;

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

public class CinemObsMM extends MemoryManager<CinematiqueObs>
{
	private int largeur, longueur_arriere, longueur_avant;
	
	public CinemObsMM(Log log, Config config)
	{
		super(CinematiqueObs.class, log);
		largeur = config.getInt(ConfigInfoKraken.LARGEUR_NON_DEPLOYE) / 2;
		longueur_arriere = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		longueur_avant = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		init(config.getInt(ConfigInfoKraken.NB_INSTANCES_OBSTACLES));
	}

	@Override
	protected final CinematiqueObs make()
	{
		return new CinematiqueObs(largeur, longueur_arriere, longueur_avant);
	}
	
	// TODO : optimisable : la mémoire est contigue
	public void destroyNode(ArcCourbeDynamique arc)
	{
		for(int i = 0; i < arc.getNbPoints(); i++)
			destroyNode(arc.getPoint(i));
	}

}
