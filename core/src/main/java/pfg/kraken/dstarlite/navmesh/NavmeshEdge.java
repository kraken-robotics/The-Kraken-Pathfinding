/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pfg.kraken.ColorKraken;
import pfg.kraken.display.Display;
import pfg.kraken.display.Printable;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XY_RW;

/**
 * An edge of the navmesh
 * 
 * @author pf
 *
 */

public final class NavmeshEdge implements Serializable, Printable
{
	private static final long serialVersionUID = 7904466980326128967L;
	int length;
	int nb;
	private double orientation;
	public transient NavmeshNode[] points = new NavmeshNode[2];
	public final int[] pointsNb = new int[2];
	final transient NavmeshTriangle[] triangles = new NavmeshTriangle[2]; // transient because it is used only at the building of the navmesh
	int nbTriangles = 0;
	private boolean highlight = false;
	final List<Obstacle> obstructingObstacles = new ArrayList<Obstacle>();
	
	public void prepareToSave()
	{
		pointsNb[0] = points[0].nb;
		pointsNb[1] = points[1].nb;
	}
	
	public void loadFromSave(NavmeshNode[] allNodes)
	{
		points = new NavmeshNode[2];
		points[0] = allNodes[pointsNb[0]];
		points[1] = allNodes[pointsNb[1]];
		assert length == (int) (1000 * points[0].position.distance(points[1].position));
	}
	
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
		assert p1 != p2;
		points[0] = p1;
		points[1] = p2;
		p1.addEdge(this);
		p2.addEdge(this);
		update();
	}
	
	public void updateOrientation()
	{
		orientation = Math.atan2(points[1].position.getY() - points[0].position.getY(), points[1].position.getX() - points[0].position.getX());		
	}
	
	public void update()
	{
		length = (int) (1000 * points[0].position.distance(points[1].position));
	}
	
	public void addTriangle(NavmeshTriangle tr)
	{
		assert nbTriangles < 2 : "Il y a déjà "+nbTriangles+" triangles !";
		if(triangles[0] == tr || triangles[1] == tr)
			return;
		
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
		if(triangles[0] != tr && triangles[1] != tr)
			return;
		
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
	 * Update the "blocked" state
	 * @param currentList
	 */
	public void updateState(List<Obstacle> currentList)
	{
		// Add the new obstacles that collide the edge  
		for(Obstacle o : currentList)
			if(!obstructingObstacles.contains(o) && o.isColliding(points[0].position, points[1].position))
				obstructingObstacles.add(o);
		
		// Remove the obstacles that are *absent* from the list or the obstacles that moved (not colliding anymore)
		Iterator<Obstacle> iter = obstructingObstacles.iterator();
		while(iter.hasNext())
		{
			Obstacle o = iter.next();
			if(!currentList.contains(o) || !o.isColliding(points[0].position, points[1].position))
				iter.remove();
		}
	}
	
	@Override
	public int hashCode()
	{
		return (8 * points[0].nb + 7) * (8 * points[1].nb + 7);
	}

	@Override
	public boolean equals(Object d)
	{
		return d == this;
	}

	@Override
	public String toString()
	{
		return "Edge between "+points[0]+" and "+points[1];
	}
	
	public String shortString() 
	{
		return points[0].position+" -- "+points[1].position;
	}

	/**
	 * Returns true iff D is (strictly) in the circumcircle of A, B and C.
	 * In that case, a flip is needed 
	 * @param pointA
	 * @param pointB
	 * @param pointC
	 * @param pointD
	 * @return
	 */
	private static boolean isCircumscribed(XY pointA, XY pointB, XY pointC, XY pointD)
	{
		assert NavmeshTriangle.checkCounterclockwise(pointA, pointB, pointC);
		double a = pointA.getX() - pointD.getX();
		double b = pointA.getY() - pointD.getY();
		double c = (pointA.getX() * pointA.getX() - pointD.getX() * pointD.getX()) + (pointA.getY() * pointA.getY() - pointD.getY() * pointD.getY());

		double d = pointB.getX() - pointD.getX();
		double e = pointB.getY() - pointD.getY();
		double f = (pointB.getX() * pointB.getX() - pointD.getX() * pointD.getX()) + (pointB.getY() * pointB.getY() - pointD.getY() * pointD.getY());
		
		double g = pointC.getX() - pointD.getX();
		double h = pointC.getY() - pointD.getY();
		double i = (pointC.getX() * pointC.getX() - pointD.getX() * pointD.getX()) + (pointC.getY() * pointC.getY() - pointD.getY() * pointD.getY());

		return (a * e * i + d * h * c + g * b * f) - (g * e * c + a * h * f + d * b * i) > 0.1; // pour ajouter un peu plus de robustesse
	}
	
	/**
	 * Returns true iff a flip is done
	 * A constrained edge cannot flip
	 * @return
	 */
	public boolean flipIfNecessary()
	{
		return flip(false);
	}
	
	public boolean forceFlip()
	{
		return flip(true);
	}
	
	private boolean flip(boolean force)
	{
		if(nbTriangles < 2)
			return false;
				
		NavmeshTriangle tr0 = triangles[0];
		NavmeshTriangle tr1 = triangles[1];
		
		int edgeIn0 = -1, edgeIn1 = -1;
		for(int j = 0; j < 3; j++)
		{
			if(tr0.edges[j] == this)
				edgeIn0 = j;
			if(tr1.edges[j] == this)
				edgeIn1 = j;
		}
				
		// beta et gamma sont les points communs
		NavmeshNode alpha = tr0.points[edgeIn0];
		NavmeshNode beta = tr0.points[(edgeIn0 + 1) % 3]; // a node of this edge
		NavmeshNode gamma = tr0.points[(edgeIn0 + 2) % 3]; // the other node of this edge
		NavmeshNode delta = tr1.points[edgeIn1];
		
/*		System.out.println("Les quatre points sont : ");
		System.out.println(alpha);
		System.out.println(beta);
		System.out.println(gamma);
		System.out.println(delta);*/

		// On vérifie la convexité des deux triangles (nécessaire seulement si on force)
		// On refuse aussi les cas où l'intersection n'est pas stricte (trois points alignés)
		// Ce dernier cas n'est pas rare du tout car il y a souvent plusieurs points sur une même arête
		if(NavmeshEdge.containsNode(beta.position, alpha.position, gamma.position) ||
				NavmeshEdge.containsNode(beta.position, delta.position, gamma.position) ||
				NavmeshEdge.containsNode(alpha.position, beta.position, delta.position) ||
				NavmeshEdge.containsNode(alpha.position, gamma.position, delta.position) ||
				!XY.segmentIntersection(alpha.position, delta.position, beta.position, gamma.position))
			return false;
		
		// Une conséquence de la convexité
		assert !alpha.neighbours.contains(delta) : alpha+" is already a neighbour of "+delta;
					
		// force overrides the circumscribed check
		if(!force)
		{
			if(!isCircumscribed(alpha.position, beta.position, gamma.position, delta.position))
				return false;
			assert !isCircumscribed(alpha.position, beta.position, delta.position, gamma.position);
		}
		
		// Flip
		
		points[0].removeEdge(this);
		points[1].removeEdge(this);
		
		points[0] = alpha;
		points[1] = delta;
		update();
		
		points[0].addEdge(this);
		points[1].addEdge(this);
		
		NavmeshEdge tmp = tr0.edges[(edgeIn0 + 1) % 3];
		tr1.edges[(edgeIn1 + 1) % 3].removeTriangle(tr1);

		tr0.setEdges(tr1.edges[(edgeIn1 + 1) % 3], this, tr0.edges[(edgeIn0 + 2) % 3]);
		tr1.setEdges(tmp, this, tr1.edges[(edgeIn1 + 2) % 3]);
		return true;
	}

	private transient XY_RW tmp = new XY_RW(), tmp2 = new XY_RW();
	
	/**
	 * Returns the distance between the node and the edge
	 * @param nextNode
	 * @return
	 */
	double distanceToPoint(NavmeshNode nextNode)
	{
		// First, we need to check if the node projection lies in the edge
		points[1].position.copy(tmp);
		tmp.minus(points[0].position);
		nextNode.position.copy(tmp2);
		tmp2.minus(points[0].position);
		boolean check = tmp.dot(tmp2) > 0;
		if(check)
		{
			nextNode.position.copy(tmp2);
			tmp2.minus(points[1].position);
			check = tmp.dot(tmp2) < 0;
			if(check)
			{
				// distance from a point to a line
				double tmpY = tmp.getY();
				tmp.setY(tmp.getX());
				tmp.setX(-tmpY);
				return Math.abs(tmp2.dot(tmp)) / tmp.norm();
			}
			// the closest point of nextNode in the edge in points[1]
			return nextNode.position.distance(points[1].position);
		}
		// the closest point of nextNode in the edge in points[0]
		return nextNode.position.distance(points[0].position);
	}
	
	/**
	 * Used for breaking tie
	 * @param nextNode
	 * @return
	 */
	double distanceToPointTie(NavmeshNode nextNode)
	{
		// the closest point of nextNode in the edge in points[0]
		return nextNode.position.distance(points[0].position) + nextNode.position.distance(points[1].position);
	}

	/**
	 * Returns the distance of the edge.
	 * If the edge is blocked, returns infinity
	 * @return
	 */
	public int getDistance()
	{
		if(!obstructingObstacles.isEmpty())
			return Integer.MAX_VALUE;
		return length;
	}
	
	public int getUnblockedDistance()
	{
		return length;
	}

	public boolean isBlocked()
	{
		return !obstructingObstacles.isEmpty();
	}

	@Override
	public void print(Graphics g, Display f)
	{
		if(highlight)
			g.setColor(ColorKraken.RED.color);
		else if(isBlocked())
			g.setColor(ColorKraken.NAVMESH_BLOCKED.color);
		g.drawLine(f.XtoWindow(points[0].position.getX()), f.YtoWindow(points[0].position.getY()), f.XtoWindow(points[1].position.getX()), f.YtoWindow(points[1].position.getY()));
	}

	public void highlight(boolean state)
	{
		highlight = state;
	}
	
	public boolean isAdjacent(NavmeshEdge other)
	{
		for(int i = 0; i < 2; i++)
			for(int j = 0; j < 2; j++)
				if(points[i] == other.points[j])
					return true;
		return false;
	}

	public double getOrientation(NavmeshNode origin)
	{
		if(origin == points[0])
			return orientation;
		assert origin == points[1];
		return orientation + Math.PI;
	}

	/**
	 * Returns true iff the node is inside the edge
	 * @param nextNode
	 * @return
	 */
	public static boolean containsNode(XY pointA, XY pointB, XY pointC)
	{
		// A-----B----C (arête : AC, nœud : B)
		XY_RW ab = pointB.minusNewVector(pointA);
		XY_RW ac = pointC.minusNewVector(pointA);
		
		// vérification de l'alignement
		if(ab.getX() * ac.getY() - ab.getY() * ac.getX() != 0)
			return false;
		
		// on vérifie que B n'est pas à gauche de A
		if(ab.dot(ac) < 0)
			return false;
		
		// on vérifie que B n'est pas à droite de C
		if(ab.dot(ac) > ac.dot(ac))
			return false;

		return true;
	}
	
	public boolean containsNode(NavmeshNode nextNode)
	{
		return containsNode(points[0].position, nextNode.position, points[1].position);
	}

}
