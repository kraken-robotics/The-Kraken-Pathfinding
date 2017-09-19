/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
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
		String s;
		
		XY bottomLeftCorner = obs.getBottomLeftCorner();
		XY topRightCorner = obs.getTopRightCorner();
		XY bottomRightCorner = new XY(topRightCorner.getX(), bottomLeftCorner.getY());
		XY topLeftCorner = new XY(bottomLeftCorner.getX(), topRightCorner.getY());
		
		NavmeshNode bl = new NavmeshNode(bottomLeftCorner);
		NavmeshNode tr = new NavmeshNode(topRightCorner);
		NavmeshNode br = new NavmeshNode(bottomRightCorner);
		NavmeshNode tl = new NavmeshNode(topLeftCorner);
		
/*		tl.neighbourInConvexHull = bl;
		tr.neighbourInConvexHull = tl;
		br.neighbourInConvexHull = tr;
		bl.neighbourInConvexHull = bl;*/
		
		nodesList.add(tl);
		nodesList.add(tr);
		nodesList.add(br);
		nodesList.add(bl);
		
		for(Obstacle o : obsList)
		{
			XY[] hull = o.getExpandedConvexHull(expansion, longestAllowedLength / 1000.);
			assert hull.length >= 3;
			NavmeshNode n = null;
			for(int i = 0; i < hull.length; i++)
			{
				// On n'inclut pas les nœuds en dehors du rectangle
				if(hull[i].getX() >= bottomLeftCorner.getX() && hull[i].getX() <= topRightCorner.getX()
						&& hull[i].getY() >= bottomLeftCorner.getY() && hull[i].getY() <= topRightCorner.getY())
				{
					n = new NavmeshNode(hull[i]);
					nodesList.add(n);					
				}
			}
//			first.neighbourInConvexHull = last;
		}

		/*
		 * This is not the fastest algorithm… but it is enough for an off-line computation
		 * This is a Delaunay triangulation.
		 */
		
		// Initial triangles
		NavmeshEdge e1 = new NavmeshEdge(tl, tr);
		edgesInProgress.add(e1);
		NavmeshEdge e2 = new NavmeshEdge(tr, br);
		edgesInProgress.add(e2);
		NavmeshEdge e3 = new NavmeshEdge(br, tl);
		edgesInProgress.add(e3);
		NavmeshEdge e4 = new NavmeshEdge(br, bl);
		edgesInProgress.add(e4);
		NavmeshEdge e5 = new NavmeshEdge(bl, tl);
		edgesInProgress.add(e5);
		triangles.add(new NavmeshTriangle(e1, e2, e3));
		triangles.add(new NavmeshTriangle(e3, e4, e5));
		
//		log.write("First triangle : "+triangles.get(0), LogCategoryKraken.TEST);
		
		// We add the points one by one
		for(int index = 4; index < nodesList.size(); index++)
			addNewNodeInitialization(nodesList.get(index));
		
		/**
		 * Flip the edges (unnecessary)
		 */
		assert checkDelaunay();
//		for(NavmeshEdge e : edgesInProgress)
//			e.flipIfNecessary();		

		// We add other points in order to avoir long edges
		NavmeshEdge longestEdge = edgesInProgress.peek();
		while(longestEdge.length > longestAllowedLength)
		{
			assert ((s = checkLongestEdge()) == null) : s;
			edgesInProgress.poll();
			assert ((s = checkLongestEdge()) == null) : s;
			addMiddleEdgePoint(longestEdge);
			longestEdge = edgesInProgress.peek();
		}
		
		assert edgesInProgress.peek().length <= longestAllowedLength : edgesInProgress.peek().length + " > " + longestAllowedLength;
		
		// We add other points in order to avoid large triangle
		NavmeshTriangle largestTriangle = triangles.peek();
		while(largestTriangle.area > largestAllowedArea)
		{
			triangles.poll();
			addCenterPoint(largestTriangle);
			largestTriangle = triangles.peek();
		}
		
		assert edgesInProgress.peek().length <= longestAllowedLength : edgesInProgress.peek().length + " > " + longestAllowedLength;

		// Suppression des nœuds à l'intérieur d'obstacle
		Iterator<NavmeshNode> iter = nodesList.iterator();
		while(iter.hasNext())
		{
			NavmeshNode node = iter.next();
			for(Obstacle o : obsList)
				if(o.isInObstacle(node.position))
				{
					node.updateNeighbours();
					int nbVoisins = node.getNbNeighbours();
					
					// On retire toutes les arêtes qui en partent					
					for(int i = 0; i < nbVoisins; i++)
					{
						edgesInProgress.remove(node.getNeighbourEdge(i));
						node.getNeighbour(i).edges.remove(node.getNeighbourEdge(i));
						// Il n'existe plus pour ses voisins
						node.getNeighbour(i).updateNeighbours();
					}
					iter.remove();
					break;
				}
		}
		
		// Suppression des arêtes qui coupent des obstacles
		// TODO
		

		// Si on a supprimé l'arête d'un triangle, on supprime ce triangle, et on retire ce triangle de ses autres côtés
		Iterator<NavmeshTriangle> iter2 = triangles.iterator();
		while(iter2.hasNext())
		{
			NavmeshTriangle triangle = iter2.next();
			for(int i = 0; i < 3; i++)
				if(!edgesInProgress.contains(triangle.edges[i]))
				{
					iter2.remove();
					for(int j = 0; j < 3; j++)
						triangle.edges[j].removeTriangle(triangle);
					break;
				}
		}
	
		assert ((s = checkTrianglesAndEdges()) == null) : s;

		NavmeshNode[] n = new NavmeshNode[nodesList.size()];
		for(int i = 0; i < n.length; i++)
		{
			n[i] = nodesList.get(i);
			n[i].updateNeighbours();
			n[i].nb = i; // since we deleted some nodes, the numbers may not be adjoining anymore
		}

		assert checkNodes();

		NavmeshEdge[] e = new NavmeshEdge[edgesInProgress.size()];
		for(int i = 0; i < e.length; i++)
			e[i] = edgesInProgress.poll();
		
		NavmeshTriangle[] t = new NavmeshTriangle[triangles.size()];
		for(int i = 0; i < t.length; i++)
			t[i] = triangles.poll();
		
		return new TriangulatedMesh(n, e, t);
	}
	
	private String checkLongestEdge()
	{
		int longestEdge = edgesInProgress.peek().length;
		for(NavmeshEdge e : edgesInProgress)
			if(e.length > longestEdge)
				return e.toString()+" : "+e.length+" > "+longestEdge;
		return null;
	}

	private void addMiddleEdgePoint(NavmeshEdge edge)
	{
		assert needFlipCheck.isEmpty();
		assert edge.nbTriangles > 0;
		
		XY a = edge.points[0].position;
		XY b = edge.points[1].position;
		XY c = a.plusNewVector(b).scalar(0.5);
		// if we split a constrained edge, make the new edges constrained
		
/*		NavmeshNode first = null, second = null;
		if(edge.points[0].neighbourInConvexHull == edge.points[1])
		{
			first = edge.points[1];
			second = edge.points[0];
		}
		else if(edge.points[1].neighbourInConvexHull == edge.points[0])
		{
			first = edge.points[0];
			second = edge.points[1];
		}*/
		
		NavmeshNode newNode = new NavmeshNode(c/*, first*/);
//		if(second != null)
//			second.neighbourInConvexHull = newNode;
		
		nodesList.add(newNode);
		
		NavmeshTriangle tr0 = edge.triangles[0];
		NavmeshTriangle tr1 = edge.triangles[1];
		
		edge.points[1].edges.remove(edge);
		NavmeshEdge newEdge = new NavmeshEdge(newNode, edge.points[1]);
//		if(edge.constrained)
//			newEdge.constrained = true;
		
		edgesInProgress.add(newEdge);

		edge.points[1] = newNode;
		assert !edgesInProgress.contains(edge);
		edge.update();
		
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

//		System.out.println("Ajout de "+nextNode);
		// first we check if this point is in a triangle
		for(NavmeshTriangle t : triangles)
			if(t.isInside(nextNode.position))
			{
				triangles.remove(t);
				addInsideNode(nextNode, t);
				flip();
				return;
			}
		
		// All points must be within the initial rectangle
		assert false : "The node "+nextNode+" is outside the initial rectangle !";
		
		// The point is outside the navmesh. We create a new triangle with the closest external edge
		NavmeshEdge best = null;
		double distance = 0, distanceTie = 0;
		for(NavmeshEdge e : edgesInProgress)
			if(e.getNbTriangles() == 1) // we want an external edge (it participates in only one triangle)
			{
				double distanceCandidate = e.distanceToPoint(nextNode);
				double distanceCandidateTie = e.distanceToPointTie(nextNode);
				if(best == null || distanceCandidate < distance || (distanceCandidate == distance && distanceCandidateTie < distanceTie))
				{
					best = e;
					distance = distanceCandidate;
					distanceTie = distanceCandidateTie;
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
		needFlipCheck.add(best);
		
		flip();
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

		String s;
		edgesInProgress.add(transversalEdge);
		assert ((s = checkLongestEdge()) == null) : s;
		
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
			assert !e.flipIfNecessary() : e;
		return true;
	}
	
	private void flip()
	{
		while(!needFlipCheck.isEmpty())
		{
			NavmeshEdge e = needFlipCheck.removeFirst();
			if(e.flipIfNecessary())
			{
				// the areas have changed
				triangles.remove(e.triangles[0]);
				triangles.remove(e.triangles[1]);
				triangles.add(e.triangles[0]);				
				triangles.add(e.triangles[1]);
				
				// the length has changed
				edgesInProgress.remove(e);
				edgesInProgress.add(e);
				
				// We add the four external edges
				for(int i = 0; i < 2; i++)
					for(int j = 0; j < 3; j++)
						needFlipCheck.add(e.triangles[i].edges[j]);
			}
		}

		// All triangles should be Delaunay
		assert checkDelaunay();
	}

	private boolean checkNodes()
	{
		for(NavmeshNode n : nodesList)
		{
			n.updateNeighbours();
			int nbVoisins = n.getNbNeighbours();
			for(int i = 0; i < nbVoisins; i++)
				if(!nodesList.contains(n.getNeighbour(i)) || !edgesInProgress.contains(n.getNeighbourEdge(i)))
				{
					if(!nodesList.contains(n.getNeighbour(i)))
						System.err.println("Unknown node : "+n.getNeighbour(i));
					else
						System.err.println("Unknown edge : "+n.getNeighbourEdge(i));
						
					return false;
				}
		}
		return true;
	}

	
	private String checkTrianglesAndEdges()
	{
		for(NavmeshTriangle tr : triangles)
			for(int i = 0; i < 3; i++)
				if(!edgesInProgress.contains(tr.edges[i]))
					return tr.toString();
		
		for(NavmeshEdge e : edgesInProgress)
			for(int i = 0; i < e.nbTriangles; i++)
				if(!triangles.contains(e.triangles[i]))
					return e.toString();
		return null;
	}
}
