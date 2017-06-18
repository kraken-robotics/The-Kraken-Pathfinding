/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.pathfinding.dstarlite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pfg.kraken.obstacles.types.Obstacle;

/**
 * Une structure utilisée par le GridSpace
 * 
 * @author pf
 *
 */

class NavmeshEdge implements Serializable
{
	private static final long serialVersionUID = 7904466980326128967L;
	final int distance;
	private boolean lastConsultedState = false;
	final NavmeshNode[] points = new NavmeshNode[2];
	final List<Obstacle> obstructingObstacle = new ArrayList<Obstacle>();
	
	NavmeshEdge(NavmeshNode p1, NavmeshNode p2)
	{
		points[0] = p1;
		points[1] = p2;
		distance = (int) (1000 * p1.position.distance(p2.position));
	}

	/**
	 * Add an obstacle
	 * @param o
	 * @return
	 */
	public void addObstacle(Obstacle o)
	{
		// TODO
	}
	
	public boolean hasChanged()
	{
		return lastConsultedState != !obstructingObstacle.isEmpty();
	}
	
	public boolean isBlocked()
	{
		return lastConsultedState = !obstructingObstacle.isEmpty();
	}
	
	@Override
	public int hashCode()
	{
		// TODO : on peut trouver mieux ? est-ce beaucoup utilisé ?
		return points[0].nb * points[1].nb;
	}

	@Override
	public boolean equals(Object d)
	{
		return d instanceof NavmeshEdge && hashCode() == ((NavmeshEdge) d).hashCode();
	}

	@Override
	public String toString()
	{
		return "Edge between "+points[0]+" and "+points[1];
	}

}
