/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.awt.Graphics;
import java.io.Serializable;

import pfg.graphic.GraphicPanel;
import pfg.graphic.printable.Printable;
import pfg.kraken.utils.XY;

/**
 * A triangle of the navmesh
 * @author pf
 *
 */

public class NavmeshTriangle implements Serializable, Printable
{
	private static final long serialVersionUID = 1L;
	transient NavmeshNode[] points = new NavmeshNode[3];
	transient NavmeshEdge[] edges = new NavmeshEdge[3];
	int[] edgesNb = new int[3];
	int area;
	
	NavmeshTriangle(NavmeshEdge a, NavmeshEdge b, NavmeshEdge c)
	{
		setEdges(a, b, c);
	}
	
	public void prepareToSave()
	{
		for(int i = 0; i < 3; i++)
			edgesNb[i] = edges[i].nb;
	}
	
	public void loadFromSave(NavmeshEdge[] allEdges)
	{
		points = new NavmeshNode[3];
		NavmeshEdge a = allEdges[edgesNb[0]], b = allEdges[edgesNb[1]], c = allEdges[edgesNb[2]];
		edges = new NavmeshEdge[3];
		edges[0] = a;
		edges[1] = b;
		edges[2] = c;
		
		if(a.points[0] == b.points[0] || a.points[1] == b.points[0])
			points[0] = b.points[1];
		else
			points[0] = b.points[0];
		
		if(b.points[0] == c.points[0] || b.points[1] == c.points[0])
			points[1] = c.points[1];
		else
			points[1] = c.points[0];
		
		if(c.points[0] == b.points[0] || c.points[1] == b.points[0])
			points[2] = b.points[1];
		else
			points[2] = b.points[0];
	}
	
	/**
	 * Returns true iff a, b and c are counterclockwise.
	 * Uses a cross product
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	boolean checkCounterclockwise()
	{
		return checkCounterclockwise(points[0].position, points[1].position, points[2].position);
	}
	
	static boolean checkCounterclockwise(XY pointA, XY pointB, XY pointC)
	{
		return crossProduct(pointA, pointB, pointC) >= 0;
	}

	
	boolean checkPoints()
	{
		return points[0] != points[1] && points[0] != points[2] && points[1] != points[2];
	}
	
	/**
	 * Check if the point i isn't in the edge i
	 * @return
	 */
	boolean checkDuality()
	{
		assert checkPoints();
		assert checkTriangle(edges[0], edges[1], edges[2]);
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 2; j++)
				if(edges[i].points[j] == points[i])
					return false;
		return true;
	}
	
	/**
	 * Return the triangle area
	 * @return
	 */
	private void updateArea()
	{
		assert checkCounterclockwise() : this;
		area = (int) crossProduct(points[0].position, points[1].position, points[2].position) / 2;
	}
	
	/**
	 * Well… a cross product.
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private static double crossProduct(XY a, XY b, XY c)
	{
		return (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
	}
	
	private boolean checkTriangle(NavmeshEdge a, NavmeshEdge b, NavmeshEdge c)
	{
		NavmeshEdge[] e = new NavmeshEdge[3];
		e[0] = a;
		e[1] = b;
		e[2] = c;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 2; j++)
			{
				int nb = 0;
				for(int i2 = 0; i2 < 3; i2++)
					for(int j2 = 0; j2 < 2; j2++)
						if(e[i].points[j] == e[i2].points[j2])
							nb++;
				assert nb == 2 : a.shortString()+" and "+b.shortString()+" and "+c.shortString(); // each point can be found in two edge ends
			}
		for(int i = 0; i < 3; i++)
			assert e[i].points[0] != e[i].points[1] : e[i].shortString(); // each edge connects two different points
		
		return true;
	}
	
	void setEdges(NavmeshEdge a, NavmeshEdge b, NavmeshEdge c)
	{
		assert a != null;
		assert b != null;
		assert c != null;
		assert checkTriangle(a, b, c);
		
		for(int i = 0; i < 3; i++)
			if(edges[i] != null)
				edges[i].removeTriangle(this);

		a.addTriangle(this);
		b.addTriangle(this);
		c.addTriangle(this);

		edges[0] = a;
		edges[1] = b;
		edges[2] = c;
		
		if(a.points[0] == b.points[0] || a.points[1] == b.points[0])
			points[0] = b.points[1];
		else
			points[0] = b.points[0];
		
		if(b.points[0] == c.points[0] || b.points[1] == c.points[0])
			points[1] = c.points[1];
		else
			points[1] = c.points[0];
		
		if(c.points[0] == b.points[0] || c.points[1] == b.points[0])
			points[2] = b.points[1];
		else
			points[2] = b.points[0];

		correctCounterclockwiseness();

		updateArea();
		assert checkDuality() : this;
		assert checkCounterclockwise() : this;
	}
	
	void correctCounterclockwiseness()
	{
		if(!checkCounterclockwise())
		{
			NavmeshNode tmp = points[0];
			points[0] = points[1];
			points[1] = tmp;
			
			NavmeshEdge tmp2 = edges[0];
			edges[0] = edges[1];
			edges[1] = tmp2;
		}
		
		assert checkDuality() : this;
		assert checkCounterclockwise() : this;
	}
	
	private static double sign(XY p1, XY p2, XY p3)
	{
	    return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
	}

	/**
	 * Source : https://stackoverflow.com/questions/2049582/how-to-determine-if-a-point-is-in-a-2d-triangle
	 * @param position
	 * @return
	 */
	public static boolean isInside(XY position, XY p1, XY p2, XY p3)
	{
	    boolean b1, b2, b3;

	    b1 = sign(position, p1, p2) < 0;
	    b2 = sign(position, p2, p3) < 0;
	    b3 = sign(position, p3, p1) < 0;

	    return ((b1 == b2) && (b2 == b3));
	}
	
	public boolean isInside(XY position)
	{
		return NavmeshTriangle.isInside(position, points[0].position, points[1].position, points[2].position);
	}
	
	public String toString()
	{
		return "Triangle with points "+points[0].position+", "+points[1].position+", "+points[2].position+" and edges "+edges[0].shortString()+", "+edges[1].shortString()+", "+edges[2].shortString();
	}

	@Override
	public void print(Graphics g, GraphicPanel f)
	{
		g.fillPolygon(new int[]{f.XtoWindow(points[0].position.getX()), f.XtoWindow(points[1].position.getX()), f.XtoWindow(points[2].position.getX())},
				new int[]{f.YtoWindow(points[0].position.getY()), f.YtoWindow(points[1].position.getY()), f.YtoWindow(points[2].position.getY())},
				3);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o == this;
	}

}
