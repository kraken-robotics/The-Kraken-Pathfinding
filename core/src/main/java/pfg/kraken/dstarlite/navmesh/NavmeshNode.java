/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import pfg.kraken.display.Display;
import pfg.kraken.display.Printable;
import pfg.kraken.struct.XY;

/**
 * A node of the navmesh
 * 
 * @author pf
 *
 */

public final class NavmeshNode implements Printable, Serializable
{
	private static final long serialVersionUID = -6588410126587155794L;

//	public NavmeshNode neighbourInConvexHull;
	public int nb; // index in memory pool
	public final XY position;
	private transient List<NavmeshEdge> edges = new ArrayList<NavmeshEdge>();
	private List<Integer> edgesNb = new ArrayList<Integer>();
	transient List<NavmeshNode> neighbours = new ArrayList<NavmeshNode>();
	private static int nbStatic = 0;

	public void prepareToSave()
	{
		for(NavmeshEdge e : edges)
			edgesNb.add(e.nb);
	}
	
	public void loadFromSave(NavmeshEdge[] allEdges)
	{
		edges = new ArrayList<NavmeshEdge>();
		neighbours = new ArrayList<NavmeshNode>();
		for(Integer nb : edgesNb)
			edges.add(allEdges[nb]);
		updateNeighbours();
	}
	
	public void addEdge(NavmeshEdge e)
	{
		assert !edges.contains(e) : "Can't add an edge already added";
		edges.add(e);
		updateNeighbours();
	}
	
	public void removeEdge(NavmeshEdge e)
	{
		assert edges.contains(e) : "Can't remove an edge already removed";
		edges.remove(e);
		updateNeighbours();
	}
	
	/**
	 * Construit à partir du hashCode
	 * 
	 * @param i
	 */
	NavmeshNode(XY position)
	{
		this.position = position;
		this.nb = nbStatic++;
//		neighbourInConvexHull = null;
	}
	
/*	NavmeshNode(XY position, NavmeshNode neighbourInConvexHull)
	{
		this.position = position;
		this.nb = nbStatic++;
		this.neighbourInConvexHull = neighbourInConvexHull;
	}*/

	@Override
	public int hashCode()
	{
		return nb;
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof NavmeshNode && nb == o.hashCode();
	}
	
	@Override
	public void print(Graphics g, Display f)
	{
		int rayon = 15;
		g.fillOval(f.XtoWindow(position.getX()) - f.distanceXtoWindow(rayon) / 2, f.YtoWindow(position.getY()) - f.distanceYtoWindow(rayon) / 2, f.distanceXtoWindow((int) (rayon)), f.distanceYtoWindow((int) (rayon)));
	}

	@Override
	public String toString()
	{
		return "NavmeshNode at "+position;
	}

	public void updateNeighbours()
	{
		neighbours.clear();
		for(NavmeshEdge e : edges)
		{
			if(e.points[0] == this)
			{
				assert !neighbours.contains(e.points[1]) : "Double edge !";
				neighbours.add(e.points[1]);
			}
			else
			{
				assert e.points[1] == this;
				assert !neighbours.contains(e.points[0]) : "Double edge !";
				neighbours.add(e.points[0]);
			}
		}	
	}
	
	public int getNbNeighbours()
	{
//		updateNeighbours();
		assert neighbours.size() == edges.size();
		return edges.size();
	}

	public NavmeshEdge getNeighbourEdge(int index)
	{
		return edges.get(index);
	}
	
	public NavmeshNode getNeighbour(int index)
	{
		return neighbours.get(index);
	}
	
	public boolean isNeighbourOf(NavmeshNode other)
	{
		return neighbours.contains(other);
	}
}
