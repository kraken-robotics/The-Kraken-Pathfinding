/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.io.Serializable;

import pfg.kraken.utils.XY;
import pfg.kraken.utils.XY_RW;

/**
 * A triangle of the navmesh
 * @author pf
 *
 */

public class NavmeshTriangle implements Serializable
{
	private static final long serialVersionUID = 1L;
	NavmeshNode[] points = new NavmeshNode[3];
	NavmeshEdge[] edges = new NavmeshEdge[3];
	
	NavmeshTriangle(NavmeshEdge a, NavmeshEdge b, NavmeshEdge c)
	{
		setEdges(a, b, c);
	}
	
	/**
	 * Returns true iff a, b and c are counterclockwise.
	 * Uses a cross product
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private boolean checkCounterclockwise(NavmeshNode a, NavmeshNode b, NavmeshNode c)
	{
		return crossProduct(a,b,c) >= 0;
	}
	
	/**
	 * Check if the point i isn't in the edge i
	 * @return
	 */
	private boolean checkDuality()
	{
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
	public double getArea()
	{
		assert checkCounterclockwise(points[0],points[1],points[2]);
		return crossProduct(points[0],points[1],points[2]) / 2;
	}
	
	private double crossProduct(NavmeshNode a, NavmeshNode b, NavmeshNode c)
	{
		return (b.position.getX() - a.position.getX()) * (c.position.getY() - a.position.getY()) - (b.position.getY() - a.position.getY()) * (c.position.getX() - a.position.getX());
	}
	
	void setEdges(NavmeshEdge a, NavmeshEdge b, NavmeshEdge c)
	{
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

		if(!checkCounterclockwise(points[0],points[1],points[2]))
		{
			NavmeshNode tmp = points[0];
			points[0] = points[1];
			points[1] = tmp;
		}
				
		assert checkDuality();
		assert checkCounterclockwise(points[0],points[1],points[2]);
	}
	
	private XY_RW v0 = new XY_RW(), v1 = new XY_RW(), v2 = new XY_RW();
	
	/**
	 * Use the barycentric method. Source : http://www.blackpawn.com/texts/pointinpoly/default.html
	 * @param position
	 * @return
	 */
	public boolean isInside(XY position)
	{
		assert checkCounterclockwise(points[0],points[1],points[2]);
		
		// Compute vectors     
		points[2].position.copy(v0);
		v0.minus(points[0].position);
		
		points[1].position.copy(v1);
		v1.minus(points[0].position);
		
		position.copy(v2);
		v2.minus(points[0].position);

		// Compute dot products
		double dot00 = v0.dot(v0);
		double dot01 = v0.dot(v1);
		double dot02 = v0.dot(v2);
		double dot11 = v1.dot(v1);
		double dot12 = v1.dot(v2);

		// Compute barycentric coordinates
		double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		// Check if point is in triangle
		return (u >= 0) && (v >= 0) && (u + v < 1);
	}
}
