/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.utils.XY;
import pfg.log.Log;

/**
 * A navmesh computer
 * @author pf
 *
 */

public class NavmeshComputer
{
	public class NavmeshTriangleComparator implements Comparator<NavmeshTriangle>
	{
		@Override
		public int compare(NavmeshTriangle o1, NavmeshTriangle o2)
		{
			return o2.area - o1.area;
		}
	}
	
	public class NavmeshEdgeComparator implements Comparator<NavmeshEdge>
	{
		@Override
		public int compare(NavmeshEdge o1, NavmeshEdge o2)
		{
			return o2.length - o1.length;
		}
	}
	
	protected Log log;

	private LinkedList<NavmeshEdge> needFlipCheck = new LinkedList<NavmeshEdge>();
	private PriorityQueue<NavmeshTriangle> triangles = new PriorityQueue<NavmeshTriangle>(1000, new NavmeshTriangleComparator());
	private PriorityQueue<NavmeshEdge> edgesInProgress = new PriorityQueue<NavmeshEdge>(1000, new NavmeshEdgeComparator());
	private List<NavmeshNode> nodesList = new ArrayList<NavmeshNode>();

	private int expansion;
	private int largestAllowedArea, longestAllowedLength;
	
	public NavmeshComputer(Log log, Config config)
	{
		this.log = log;
		expansion = config.getInt(ConfigInfoKraken.DILATATION_ROBOT_DSTARLITE);
		largestAllowedArea = config.getInt(ConfigInfoKraken.LARGEST_TRIANGLE_AREA_IN_NAVMESH);
		longestAllowedLength = config.getInt(ConfigInfoKraken.LONGEST_EDGE_IN_NAVMESH);
	}
	
	public TriangulatedMesh generateNavMesh(StaticObstacles obs)
	{
		List<Obstacle> obsList = obs.getObstacles();
		
		for(Obstacle o : obsList)
		{
			XY[] hull = o.getExpandedConvexHull(expansion, longestAllowedLength);
			assert hull.length >= 3;
			NavmeshNode first = null;
			NavmeshNode last = null;
			for(int i = 0; i < hull.length; i++)
			{
				if(i == 0)
				{
					last = first = new NavmeshNode(hull[i]);
					nodesList.add(first);					
				}
				else
				{
					last = new NavmeshNode(hull[i], last);
					nodesList.add(last);
				}
			}
			first.neighbourInConvexHull = last;
		}

		/*
		 * No possible triangulation (nor needed)
		 */
		if(nodesList.size() < 3)
			return new TriangulatedMesh(new NavmeshNode[] {new NavmeshNode(new XY(0,0))}, new NavmeshEdge[0], new NavmeshTriangle[0]);

		/*
		 * This is not the fastest algorithm… but it is enough for an off-line computation
		 * This is a Delaunay triangulation.
		 */
		
		// Initial triangle
		NavmeshEdge e1 = new NavmeshEdge(nodesList.get(0), nodesList.get(1));
		edgesInProgress.add(e1);
		NavmeshEdge e2 = new NavmeshEdge(nodesList.get(1), nodesList.get(2));
		edgesInProgress.add(e2);
		NavmeshEdge e3 = new NavmeshEdge(nodesList.get(2), nodesList.get(0));
		edgesInProgress.add(e3);
		triangles.add(new NavmeshTriangle(e1, e2, e3));

//		log.write("First triangle : "+triangles.get(0), LogCategoryKraken.TEST);

		
		// No flip until the constraints have been added !
		
		// We add the points one by one
		for(int index = 3; index < nodesList.size(); index++)
		{
			NavmeshNode nextNode = nodesList.get(index);
			addNewNodeInitialization(nextNode);
		}
		
		/**
		 * Flip and add the constraints
		 */
		for(NavmeshEdge e : edgesInProgress)
			e.flipIfNecessary(true);

		// We add other points in order to avoir long edges
		NavmeshEdge longestEdge = edgesInProgress.peek();
		while(longestEdge.length > longestAllowedLength)
		{
			edgesInProgress.poll();
			addMiddleEdgePoint(longestEdge);
			longestEdge = edgesInProgress.peek();
		}

		// We add other points in order to avoid large triangle
		NavmeshTriangle largestTriangle = triangles.peek();
		while(largestTriangle.area > largestAllowedArea)
		{
			triangles.poll();
			addCenterPoint(largestTriangle);
			largestTriangle = triangles.peek();
		}
		
		assert edgesInProgress.peek().length <= longestAllowedLength;
		
		assert checkTrianglesAndEdges();
		
		NavmeshNode[] n = new NavmeshNode[nodesList.size()];
		for(int i = 0; i < n.length; i++)
		{
			n[i] = nodesList.get(i);
			n[i].updateNeighbours();
		}
		
		NavmeshEdge[] e = new NavmeshEdge[edgesInProgress.size()];
		for(int i = 0; i < e.length; i++)
		{
			e[i] = edgesInProgress.poll();
			for(Obstacle o : obsList)
				if(o.isColliding(e[i].points[0].position, e[i].points[1].position))
					e[i].obstructingObstacles.add(o);
			e[i].hasChanged();
		}

		NavmeshTriangle[] t = new NavmeshTriangle[triangles.size()];
		for(int i = 0; i < t.length; i++)
			t[i] = triangles.poll();
		
		return new TriangulatedMesh(n, e, t);
	}
	
	private void addMiddleEdgePoint(NavmeshEdge edge)
	{
		assert needFlipCheck.isEmpty();
		assert edge.nbTriangles > 0;
		
		XY a = edge.points[0].position;
		XY b = edge.points[1].position;
		XY c = a.plusNewVector(b).scalar(0.5);
		// if we split a constrained edge, make the new edges constrained
		
		NavmeshNode first = null, second = null;
		if(edge.points[0].neighbourInConvexHull == edge.points[1])
		{
			first = edge.points[1];
			second = edge.points[0];
		}
		else if(edge.points[1].neighbourInConvexHull == edge.points[0])
		{
			first = edge.points[0];
			second = edge.points[1];
		}
		
		NavmeshNode newNode = new NavmeshNode(c, first);
		if(second != null)
			second.neighbourInConvexHull = newNode;
		
		nodesList.add(newNode);
		
		NavmeshTriangle tr0 = edge.triangles[0];
		NavmeshTriangle tr1 = edge.triangles[1];
		
		edge.points[1].edges.remove(edge);
		NavmeshEdge newEdge = new NavmeshEdge(newNode, edge.points[1]);
		if(edge.constrained)
			newEdge.constrained = true;
		
		edgesInProgress.add(newEdge);
		
		edge.points[1] = newNode;
		edge.updateLength();
		
		edgesInProgress.add(edge);

		newNode.edges.add(edge);

		addEdgeNode(newNode, edge, newEdge, tr0);
		
		if(edge.nbTriangles == 2)
		{
			assert tr1 != null;
			addEdgeNode(newNode, edge, newEdge, tr1);
		}
		
		flip();
	}
	
	private void addCenterPoint(NavmeshTriangle enclosingTriangle)
	{
		XY a = enclosingTriangle.points[0].position;
		XY b = enclosingTriangle.points[1].position;
		XY c = enclosingTriangle.points[2].position;
		XY d = a.plusNewVector(b).plus(c).scalar(1./3.);
		NavmeshNode newNode = new NavmeshNode(d);
//		log.write("New center node : "+newNode+" within "+enclosingTriangle, LogCategoryKraken.TEST);
		nodesList.add(newNode);
		addInsideNode(newNode, enclosingTriangle);
		flip();
	}
	
	/**
	 * Add a new node to the navmesh.
	 * There must be at least one triangle.
	 * @param nextNode
	 */
	private void addNewNodeInitialization(NavmeshNode nextNode)
	{
		assert !edgesInProgress.isEmpty();
		assert !triangles.isEmpty();
		System.out.println("Ajout de "+nextNode);
		// first we check if this point is in a triangle
		for(NavmeshTriangle t : triangles)
			if(t.isInside(nextNode.position))
			{
				triangles.remove(t);
				addInsideNode(nextNode, t);
				needFlipCheck.clear();
				return;
			}
		
		// The point is outside the navmesh. We create a new triangle with the closest external edge
		NavmeshEdge best = null;
		double distance = 0;
		for(NavmeshEdge e : edgesInProgress)
			if(e.getNbTriangles() == 1) // we want an external edge (it participates in only one triangle)
			{
				double distanceCandidate = e.distanceToPoint(nextNode);
				if(best == null || distanceCandidate < distance)
				{
					best = e;
					distance = distanceCandidate;
				}
			}
		
		assert best != null;
		
		NavmeshEdge[] e = new NavmeshEdge[2];
		e[0] = new NavmeshEdge(nextNode, best.points[0]);
		e[1] = new NavmeshEdge(nextNode, best.points[1]);
		
		edgesInProgress.add(e[0]);
		edgesInProgress.add(e[1]);
		
		NavmeshTriangle t = new NavmeshTriangle(e[0], e[1], best);
		triangles.add(t);
		
		assert e[0].checkTriangle(1) : e[0];
		assert e[1].checkTriangle(1) : e[1];
		assert best.checkTriangle(2) : best;
		assert needFlipCheck.isEmpty() : needFlipCheck;
	}

	private void addInsideNode(NavmeshNode nextNode, NavmeshTriangle t)
	{
		assert t.isInside(nextNode.position);
		assert needFlipCheck.isEmpty();

		for(int i = 0; i < 3; i++)
			needFlipCheck.add(t.edges[i]);
		
		// We divide this triangle into three triangles
		NavmeshEdge[] e = new NavmeshEdge[3];
		e[0] = new NavmeshEdge(nextNode, t.points[0]);
		e[1] = new NavmeshEdge(nextNode, t.points[1]);
		e[2] = new NavmeshEdge(nextNode, t.points[2]);

		for(int i = 0; i < 3; i++)
			needFlipCheck.add(e[i]);
		
		edgesInProgress.add(e[0]);
		edgesInProgress.add(e[1]);
		edgesInProgress.add(e[2]);
		
		assert t.checkDuality() : t;
		assert t.checkCounterclockwise() : t;

		NavmeshEdge tedges1 = t.edges[1];
		NavmeshEdge tedges2 = t.edges[2];
		t.setEdges(e[2], e[1], t.edges[0]);
		triangles.add(t);
		NavmeshTriangle tr1 = new NavmeshTriangle(e[1], e[0], tedges2);
		NavmeshTriangle tr2 = new NavmeshTriangle(e[0], e[2], tedges1);
		
		assert e[0].checkTriangle(2) : e[0];
		assert e[1].checkTriangle(2) : e[1];
		assert e[2].checkTriangle(2) : e[2];
		
		assert tr1.checkCounterclockwise() : tr1;
		assert tr2.checkCounterclockwise() : tr2;
		assert t.checkCounterclockwise() : t;
		
		triangles.add(tr1);
		triangles.add(tr2);
	}
	
	private void addEdgeNode(NavmeshNode nextNode, NavmeshEdge originalEdge, NavmeshEdge newEdge, NavmeshTriangle tr)
	{
		int indexEdge = -1;
		for(int i = 0; i < 3; i++)
			if(tr.edges[i] == originalEdge)
				indexEdge = i;
		
		assert indexEdge != -1;
		
		triangles.remove(tr);
		assert originalEdge.points[0] != tr.points[indexEdge] && originalEdge.points[1] != tr.points[indexEdge] &&
				newEdge.points[0] != tr.points[indexEdge] && newEdge.points[1] != tr.points[indexEdge];
		
		NavmeshEdge transversalEdge = new NavmeshEdge(nextNode, tr.points[indexEdge]);

		edgesInProgress.add(transversalEdge);
		
		NavmeshEdge edgeNextToNewEdge, edgeNextToOriginalEdge;
		
		if(tr.edges[(indexEdge + 1) % 3].isAdjacent(newEdge))
		{
			edgeNextToNewEdge = tr.edges[(indexEdge + 1) % 3];
			edgeNextToOriginalEdge = tr.edges[(indexEdge + 2) % 3];
		}
		else
		{
			assert tr.edges[(indexEdge + 2) % 3].isAdjacent(newEdge);
			edgeNextToNewEdge = tr.edges[(indexEdge + 2) % 3];
			edgeNextToOriginalEdge = tr.edges[(indexEdge + 1) % 3];
		}
		
		edgeNextToNewEdge.removeTriangle(tr);
		NavmeshTriangle tr1 = new NavmeshTriangle(transversalEdge, newEdge, edgeNextToNewEdge);

		tr.setEdges(transversalEdge, originalEdge, edgeNextToOriginalEdge);
		
		triangles.add(tr1);
		triangles.add(tr);

		needFlipCheck.add(transversalEdge);
		needFlipCheck.add(originalEdge);
		needFlipCheck.add(newEdge);
		needFlipCheck.add(edgeNextToOriginalEdge);
		needFlipCheck.add(edgeNextToNewEdge);		
	}

	private boolean checkDelaunay()
	{
		for(NavmeshEdge e : edgesInProgress)
			assert !e.flipIfNecessary(false) : e;
		return true;
	}
	
	private void flip()
	{
		while(!needFlipCheck.isEmpty())
		{
			NavmeshEdge e = needFlipCheck.removeFirst();
			if(e.flipIfNecessary(false))
			{
				// the areas have changed
				triangles.remove(e.triangles[0]);
				triangles.remove(e.triangles[1]);
				triangles.add(e.triangles[0]);
				triangles.add(e.triangles[1]);
				// We add the four external edges
				for(int i = 0; i < 2; i++)
					for(int j = 0; j < 3; j++)
						needFlipCheck.add(e.triangles[i].edges[j]);
			}
		}

		// All triangles should be Delaunay
		assert checkDelaunay();
	}
	
	private boolean checkTrianglesAndEdges()
	{
		for(NavmeshTriangle tr : triangles)
			for(int i = 0; i < 3; i++)
				if(!edgesInProgress.contains(tr.edges[i]))
					return false;
		
		for(NavmeshEdge e : edgesInProgress)
			for(int i = 0; i < e.nbTriangles; i++)
				if(!triangles.contains(e.triangles[i]))
					return false;
		return true;
	}
}
