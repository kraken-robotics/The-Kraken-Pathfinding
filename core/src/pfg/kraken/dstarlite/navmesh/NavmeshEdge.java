/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.utils.XY;

/**
 * An edge of the navmesh
 * 
 * @author pf
 *
 */

public class NavmeshEdge implements Serializable
{
	private static final long serialVersionUID = 7904466980326128967L;
	final int distance;
	private boolean lastConsultedState = false;
	public final NavmeshNode[] points = new NavmeshNode[2];
	final transient NavmeshTriangle[] triangles = new NavmeshTriangle[2]; // transient because it is used only at the building of the navmesh
	transient int nbTriangles = 0;
	final List<Obstacle> obstructingObstacle = new ArrayList<Obstacle>();
	
	private boolean checkNbTriangles()
	{
		if(nbTriangles == 0 && (triangles[0] != null || triangles[1] != null))
			return false;

		if(nbTriangles == 1 && (triangles[0] == null || triangles[1] != null))
			return false;
		
		if(nbTriangles == 2 && (triangles[0] == null || triangles[1] == null))
			return false;
		
		return true;
	}
	
	public int getNbTriangles()
	{
		assert checkNbTriangles();
		return nbTriangles;
	}
	
	boolean checkTriangle(int expected)
	{
		assert checkNbTriangles();
		assert nbTriangles == expected;
		
		if(expected == 1)
		{
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
		nbTriangles++;
		assert checkNbTriangles();
	}
	

	public void removeTriangle(NavmeshTriangle tr)
	{
		if(triangles[0] == tr)
		{
			triangles[0] = triangles[1];
			triangles[1] = null;	
		}
		else
		{
			assert triangles[1] == tr;
			triangles[1] = null;
		}
		nbTriangles--;
		assert checkNbTriangles();
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
		assert checkNbTriangles();
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
		return lastConsultedState == obstructingObstacle.isEmpty();
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

	private static boolean isCircumscribed(XY pointA, XY pointB, XY pointC, XY pointD)
	{
		double a = pointA.getX() - pointD.getX();
		double b = pointA.getY() - pointD.getY();
		double c = (pointA.getX() * pointA.getX() - pointD.getX() * pointD.getX()) + (pointA.getY() * pointA.getY() - pointD.getY() * pointD.getY());

		double d = pointB.getX() - pointD.getX();
		double e = pointB.getY() - pointD.getY();
		double f = (pointB.getX() * pointB.getX() - pointD.getX() * pointD.getX()) + (pointB.getY() * pointB.getY() - pointD.getY() * pointD.getY());
		
		double g = pointC.getX() - pointD.getX();
		double h = pointC.getY() - pointD.getY();
		double i = (pointC.getX() * pointC.getX() - pointD.getX() * pointD.getX()) + (pointC.getY() * pointC.getY() - pointD.getY() * pointD.getY());

		return (a * e * i + d * h * c + g * b * f) - (g * e * c + a * h * f + d * b * i) > 0;
	}
	
	/**
	 * Returns true iff a flip is done
	 * @return
	 */
	public boolean flipIfNecessary()
	{
		if(nbTriangles < 2)
			return false;
				
		int edgeIn0 = -1, edgeIn1 = -1;
		for(int j = 0; j < 3; j++)
		{
			if(triangles[0].edges[j] == this)
				edgeIn0 = j;
			if(triangles[1].edges[j] == this)
				edgeIn1 = j;
		}
		
		NavmeshNode alpha = triangles[0].points[edgeIn0];
		NavmeshNode beta = triangles[0].points[(edgeIn0 + 1) % 3];
		NavmeshNode gamma = triangles[0].points[(edgeIn0 + 2) % 3];
		NavmeshNode delta = triangles[1].points[edgeIn1];

		
		if(!isCircumscribed(alpha.position, beta.position, gamma.position, delta.position))
			return false;
		
		points[0] = alpha;
		points[1] = delta;
			
		triangles[0].setEdges(triangles[1].edges[(edgeIn1 + 1) % 3], this, triangles[0].edges[(edgeIn0 + 2) % 3]);
		triangles[1].setEdges(triangles[0].edges[(edgeIn0 + 1) % 3], this, triangles[1].edges[(edgeIn0 + 2) % 3]);
		
		return true;
	}

	double distanceToPoint(NavmeshNode nextNode)
	{
		return (nextNode.position.distance(points[0].position) + nextNode.position.distance(points[1].position)) / 2.;
	}

}
