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

package memory;

import pathfinding.ChronoGameState;
import pathfinding.astar.AStarCourbeNode;
import pathfinding.chemin.CheminPathfinding;
import robot.RobotChrono;
import robot.RobotReal;
import table.Table;
import utils.Log;
import config.Config;
import config.ConfigInfo;
import container.dependances.HighPFClass;
import exceptions.ContainerException;

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
	private RobotReal robot;
	private CheminPathfinding chemin;
	
	public NodeMM(Log log, Config config, RobotReal robot, CheminPathfinding chemin) throws ContainerException
	{
		super(AStarCourbeNode.class, log);
		this.robot = robot;
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
		return new AStarCourbeNode(new ChronoGameState(new RobotChrono(log, robot, chemin), new Table(log)), largeur, longueur_arriere, longueur_avant, marge);
	}

}
