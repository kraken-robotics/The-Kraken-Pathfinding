/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import pfg.kraken.obstacles.Obstacle;

/**
 * A node in the navmesh
 * 
 * @author pf
 *
 */

public class NavmeshEdge implements Serializable
{
	private static final long serialVersionUID = 7904466980326128967L;
	final int distance;
	private boolean lastConsultedState = false;
	final NavmeshNode[] points = new NavmeshNode[2];
	final transient NavmeshTriangle[] triangles = new NavmeshTriangle[2]; // transient because it is used only at the building of the navmesh
	final List<Obstacle> obstructingObstacle = new ArrayList<Obstacle>();
	
	boolean checkTriangle(int expected)
	{
		if(expected == 0 && (triangles[0] != null || triangles[1] != null))
			return false;
		if(expected == 1)
		{
			if(triangles[0] == null || triangles[1] != null)
				return false;
			boolean ok = false;
			for(int i = 0; i < 3; i++)
				if(triangles[0].edges[i] == this)
				{
					ok = true;
					break;
				}
			assert ok;
			return true;
		}
		if(expected == 2)
		{
			if(triangles[0] == null || triangles[1] == null)
				return false;
			boolean ok;
			for(int j = 0; j < 2; j++)
			{
				ok = false;
				for(int i = 0; i < 3; i++)
					if(triangles[j].edges[i] == this)
					{
						ok = true;
						break;
					}
				assert ok;
			}
			return true;
		}
		else
			return false;
	}
	
	NavmeshEdge(NavmeshNode p1, NavmeshNode p2)
	{
		points[0] = p1;
		points[1] = p2;
		distance = (int) (1000 * p1.position.distance(p2.position));
	}

	public void addTriangle(NavmeshTriangle tr)
	{
		if(triangles[0] == null)
			triangles[0] = tr;
		else
		{
			assert triangles[1] == null;
			triangles[1] = tr;
		}
	}
	
	public void replaceTriangle(NavmeshTriangle oldTr, NavmeshTriangle newTr)
	{
		if(triangles[0] == oldTr)
			triangles[0] = newTr;
		else
		{
			assert triangles[1] == oldTr;
			triangles[1] = newTr;
		}
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
