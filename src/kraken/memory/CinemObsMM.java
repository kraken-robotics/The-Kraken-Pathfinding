/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package kraken.memory;

import config.Config;
import kraken.config.ConfigInfoKraken;
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
