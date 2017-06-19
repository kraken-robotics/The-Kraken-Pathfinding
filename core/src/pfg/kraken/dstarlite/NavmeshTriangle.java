/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite;

import pfg.kraken.utils.XY;
import pfg.kraken.utils.XY_RW;

/**
 * A triangle of the navmesh
 * @author pf
 *
 */

public class NavmeshTriangle
{
	NavmeshNode[] points = new NavmeshNode[3];
	
	NavmeshTriangle(NavmeshNode a, NavmeshNode b, NavmeshNode c)
	{
		setPoints(a, b, c);
	}
	
	void setPoints(NavmeshNode a, NavmeshNode b, NavmeshNode c)
	{
		points[0] = a;
		points[1] = b;
		points[2] = c;
	}
	
	private XY_RW v0 = new XY_RW(), v1 = new XY_RW(), v2 = new XY_RW();
	
	/**
	 * Use the barycentric method. Source : http://www.blackpawn.com/texts/pointinpoly/default.html
	 * @param position
	 * @return
	 */
	public boolean isInside(XY position)
	{
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
