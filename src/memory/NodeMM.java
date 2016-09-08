package memory;

import pathfinding.astarCourbe.AStarCourbeNode;
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

	public NodeMM(Log log, Container container) throws ContainerException
	{
		super(AStarCourbeNode.class, log, container, 1000);
	}

}
