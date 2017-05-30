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

import kraken.config.Config;
import kraken.config.ConfigInfo;
import kraken.container.dependances.HighPFClass;
import kraken.exceptions.ContainerException;
import kraken.pathfinding.ChronoGameState;
import kraken.pathfinding.astar.AStarCourbeNode;
import kraken.pathfinding.chemin.CheminPathfinding;
import kraken.robot.RobotChrono;
import kraken.table.Table;
import kraken.utils.Log;

/**
 * Memory Manager des nœuds du pathfinding courbe
 * 
 * @author pf
 *
 */

public class NodeMM extends MemoryManager<AStarCourbeNode> implements HighPFClass
{
	private int largeur, longueur_arriere, longueur_avant, marge;
	private Log log;
	private CheminPathfinding chemin;
	
	public NodeMM(Log log, Config config, CheminPathfinding chemin) throws ContainerException
	{
		super(AStarCourbeNode.class, log);
		this.chemin = chemin;
		largeur = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE) / 2;
		longueur_arriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		longueur_avant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		marge = config.getInt(ConfigInfo.DILATATION_OBSTACLE_ROBOT);
		init(config.getInt(ConfigInfo.NB_INSTANCES_NODE));
	}

	@Override
	protected final AStarCourbeNode make()
	{
		return new AStarCourbeNode(new ChronoGameState(new RobotChrono(log, chemin), new Table(log)), largeur, longueur_arriere, longueur_avant, marge);
	}

}
