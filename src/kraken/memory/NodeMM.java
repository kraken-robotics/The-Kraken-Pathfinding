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
import kraken.pathfinding.ChronoGameState;
import kraken.container.dependances.HighPFClass;
import kraken.exceptions.ContainerException;
import kraken.pathfinding.astar.AStarCourbeNode;
import kraken.robot.RobotChrono;
import kraken.utils.Log;

/**
 * Memory Manager des nœuds du pathfinding courbe
 * 
 * @author pf
 *
 */

public class NodeMM extends MemoryManager<AStarCourbeNode>
{
	private int largeur, longueur_arriere, longueur_avant, marge;
	private Log log;
	
	public NodeMM(Log log, Config config)
	{
		super(AStarCourbeNode.class, log);
		largeur = config.getInt(ConfigInfoKraken.LARGEUR_NON_DEPLOYE) / 2;
		longueur_arriere = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		longueur_avant = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		marge = config.getInt(ConfigInfoKraken.DILATATION_OBSTACLE_ROBOT);
		init(config.getInt(ConfigInfoKraken.NB_INSTANCES_NODE));
	}

	@Override
	protected final AStarCourbeNode make()
	{
		return new AStarCourbeNode(new RobotChrono(log), largeur, longueur_arriere, longueur_avant, marge);
	}

}
