/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	protected Log log;

	private LinkedList<NavmeshEdge> needFlipCheck = new LinkedList<NavmeshEdge>();
	private List<NavmeshTriangle> triangles = new ArrayList<NavmeshTriangle>();
	private List<NavmeshEdge> edgesInProgress = new ArrayList<NavmeshEdge>();
	private List<NavmeshNode> nodesList = new ArrayList<NavmeshNode>();

	private int expansion;
	private double largestAllowedArea;
	
	public NavmeshComputer(Log log, Config config)
	{
		this.log = log;
		expansion = config.getInt(ConfigInfoKraken.DILATATION_ROBOT_DSTARLITE);
		largestAllowedArea = config.getInt(ConfigInfoKraken.LARGEST_TRIANGLE_AREA_IN_NAVWESH);
	}
	
	public TriangulatedMesh generateNavMesh(StaticObstacles obs)
	{
		List<Obstacle> obsList = obs.getObstacles();
		
		for(Obstacle o : obsList)
		{
			XY[] hull = o.getExpandedConvexHull(expansion);
			for(XY pos : hull)
				nodesList.add(new NavmeshNode(pos));
			
//			log.write(nodesList, LogCategoryKraken.TEST);
		}

		/*
		 * No possible triangulation (nor needed)
		 */
		if(nodesList.size() < 3)
			return new TriangulatedMesh(new NavmeshNode[0], new NavmeshEdge[0], new NavmeshTriangle[0]);

		/*
		 * This is not the fastest algorithm… but it is enough for an off-line computation
		 * This is a Delaunay triangulation.
		 */
		
		// Initial triangle
		edgesInProgress.add(new NavmeshEdge(nodesList.get(0), nodesList.get(1)));
		edgesInProgress.add(new NavmeshEdge(nodesList.get(1), nodesList.get(2)));
		edgesInProgress.add(new NavmeshEdge(nodesList.get(2), nodesList.get(0)));
		triangles.add(new NavmeshTriangle(edgesInProgress.get(0), edgesInProgress.get(1), edgesInProgress.get(2)));

//		log.write("First triangle : "+triangles.get(0), LogCategoryKraken.TEST);

		
		// We add the points one by one
		for(int index = 3; index < nodesList.size(); index++)
		{
			NavmeshNode nextNode = nodesList.get(index);
			addNewNode(nextNode);
			assert needFlipCheck.isEmpty();
		}
		
		// We add other points in order to avoid large triangle
		NavmeshTriangle largestTriangle = getLargestTriangle();
		double largestArea = largestTriangle.getArea();
		while(largestArea > largestAllowedArea)
		{
			addCenterPoint(largestTriangle);
			largestTriangle = getLargestTriangle();
			largestArea = largestTriangle.getArea();
		}

		for(NavmeshEdge e : edgesInProgress)
			e.hasChanged(); // TODO ??
		
		NavmeshNode[] n = new NavmeshNode[nodesList.size()];
		for(int i = 0; i < n.length; i++)
			n[i] = nodesList.get(i);
		
		NavmeshEdge[] e = new NavmeshEdge[edgesInProgress.size()];
		for(int i = 0; i < e.length; i++)
			e[i] = edgesInProgress.get(i);

		NavmeshTriangle[] t = new NavmeshTriangle[triangles.size()];
		for(int i = 0; i < t.length; i++)
			t[i] = triangles.get(i);
		
		return new TriangulatedMesh(n, e, t);
	}
	
	private NavmeshTriangle getLargestTriangle()
	{
		assert !triangles.isEmpty();
		NavmeshTriangle out = triangles.get(0);
		double largestArea = out.getArea();
		for(NavmeshTriangle t : triangles)
		{
			double candidateArea = t.getArea();
			if(candidateArea > largestArea)
			{
				largestArea = candidateArea;
				out = t;
			}
		}
		return out;
	}
	
	private void addCenterPoint(NavmeshTriangle enclosingTriangle)
	{
		assert triangles.contains(enclosingTriangle) : enclosingTriangle+" not in "+triangles;
		XY a = enclosingTriangle.points[0].position;
		XY b = enclosingTriangle.points[1].position;
		XY c = enclosingTriangle.points[2].position;
		XY d = a.plusNewVector(b).plus(c).scalar(1./3.);
		NavmeshNode newNode = new NavmeshNode(d);
//		log.write("New center node : "+newNode+" within "+enclosingTriangle, LogCategoryKraken.TEST);
		nodesList.add(newNode);
		addInsideNode(newNode, enclosingTriangle);
	}
	
	/**
	 * Add a new node to the navmesh.
	 * There must be at least one triangle.
	 * @param nextNode
	 */
	private void addNewNode(NavmeshNode nextNode)
	{
		assert !edgesInProgress.isEmpty();
		assert !triangles.isEmpty();
		
		// first we check if this point is in a triangle
		for(NavmeshTriangle t : triangles)
			if(t.isInside(nextNode.position))
			{
				addInsideNode(nextNode, t);
				return;
			}
		
		// The point is outside the navwesh. We create a new triangle with the closest external edge
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
		
//		log.write("A new triangle has been created from an outer point", LogCategoryKraken.TEST);
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

		edgesInProgress.add(e[0]);
		edgesInProgress.add(e[1]);
		edgesInProgress.add(e[2]);
		
		assert t.checkDuality() : t;
		assert t.checkCounterclockwise() : t;
		
		NavmeshEdge tedges1 = t.edges[1];
		NavmeshEdge tedges2 = t.edges[2];
		t.setEdges(e[2], e[1], t.edges[0]);
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
		
		flip();
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
				// We add the four external edges
				for(int i = 0; i < 2; i++)
					for(int j = 0; j < 3; j++)
						if(e.triangles[i].edges[j] != e)
							needFlipCheck.add(e.triangles[i].edges[j]);
			}
		}
		
		// All triangles should be Delaunay
		assert checkDelaunay();
	}
}
