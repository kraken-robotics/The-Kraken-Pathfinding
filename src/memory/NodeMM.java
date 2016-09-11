/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package memory;

import pathfinding.astarCourbe.AStarCourbeNode;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import container.Container;
import exceptions.ContainerException;

/**
 * Memory Manager des nœuds du pathfinding courbe
 * @author pf
 *
 */

public class NodeMM extends MemoryManager<AStarCourbeNode>
{

	public NodeMM(Log log, Config config, Container container) throws ContainerException
	{
		super(AStarCourbeNode.class, log, container, config.getInt(ConfigInfo.NB_INSTANCES_NODE));
	}

}
