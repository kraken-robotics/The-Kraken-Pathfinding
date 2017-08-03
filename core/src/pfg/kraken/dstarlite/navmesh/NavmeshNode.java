/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import pfg.graphic.Fenetre;
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
	public void print(Graphics g, Fenetre f)
	{
//		g.fillOval(f.XtoWindow(p.getX()) - f.distanceXtoWindow((int) DISTANCE_ENTRE_DEUX_POINTS) / 2, f.YtoWindow(p.getY()) - f.distanceYtoWindow((int) DISTANCE_ENTRE_DEUX_POINTS) / 2, f.distanceXtoWindow((int) (DISTANCE_ENTRE_DEUX_POINTS * 0.7)), f.distanceYtoWindow((int) (DISTANCE_ENTRE_DEUX_POINTS * 0.7)));
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
