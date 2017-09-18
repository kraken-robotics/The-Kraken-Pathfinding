/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pfg.graphic.Chart;
import pfg.graphic.GraphicPanel;
import pfg.graphic.printable.Layer;
import pfg.graphic.printable.Printable;
import pfg.kraken.utils.XY;

/**
 * A node of the navmesh
 * 
 * @author pf
 *
 */

public class NavmeshNode implements Printable, Serializable
{
	private static final long serialVersionUID = -6588410126587155794L;

	public NavmeshNode neighbourInConvexHull;
	public final int nb; // index in memory pool
	public final XY position;
	List<NavmeshEdge> edges = new ArrayList<NavmeshEdge>();
	List<NavmeshNode> neighbours = new ArrayList<NavmeshNode>();
	private static int nbStatic = 0;

	/**
	 * Construit à partir du hashCode
	 * 
	 * @param i
	 */
	NavmeshNode(XY position)
	{
		this.position = position;
		this.nb = nbStatic++;
		neighbourInConvexHull = null;
	}
	
	NavmeshNode(XY position, NavmeshNode neighbourInConvexHull)
	{
		this.position = position;
		this.nb = nbStatic++;
		this.neighbourInConvexHull = neighbourInConvexHull;
	}

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
	public void print(Graphics g, GraphicPanel f, Chart a)
	{
		int rayon = 15;
		g.fillOval(f.XtoWindow(position.getX()) - f.distanceXtoWindow(rayon) / 2, f.YtoWindow(position.getY()) - f.distanceYtoWindow(rayon) / 2, f.distanceXtoWindow((int) (rayon)), f.distanceYtoWindow((int) (rayon)));
	}

	@Override
	public String toString()
	{
		return "NavmeshNode at "+position;
	}

	@Override
	public int getLayer()
	{
		return Layer.FOREGROUND.ordinal();
	}

	void updateNeighbours()
	{
		for(NavmeshEdge e : edges)
		{
			if(e.points[0] == this)
			{
				assert !neighbours.contains(e.points[1]);
				neighbours.add(e.points[1]);
			}
			else
			{
				assert e.points[1] == this;
				assert !neighbours.contains(e.points[0]);
				neighbours.add(e.points[0]);
			}
		}	
	}
	
	public int getNbNeighbours()
	{
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
}
