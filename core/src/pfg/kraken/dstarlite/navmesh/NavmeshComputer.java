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
		
		// Used to check if there are duplicate points
		List<XY> addedPoints = new ArrayList<XY>();
		
		for(Obstacle o : obsList)
		{
			XY[] hull = o.getExpandedConvexHull(expansion * 1.1, longestAllowedLength / 1000.);
			assert hull.length >= 3;
			NavmeshNode n = null;
			for(int i = 0; i < hull.length; i++)
			{
				// On n'inclut pas les nœuds en dehors du rectangle
				if(!addedPoints.contains(hull[i]) && hull[i].getX() >= bottomLeftCorner.getX() && hull[i].getX() <= topRightCorner.getX()
						&& hull[i].getY() >= bottomLeftCorner.getY() && hull[i].getY() <= topRightCorner.getY())
				{
					n = new NavmeshNode(hull[i]);
					nodesList.add(n);
					addedPoints.add(hull[i]);
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
		System.out.println("Tout ajouté");
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
try {
		// Suppression des nœuds à l'intérieur d'obstacle
		Iterator<NavmeshNode> iterN = nodesList.iterator();
		while(iterN.hasNext())
		{
			NavmeshNode node = iterN.next();
			for(Obstacle o : obsList)
				if(o.squaredDistance(node.position) < expansion * expansion)
				{
					System.out.println("nbVoisins : "+node.getNbNeighbours());
					
					// On flippe le plus d'arêtes possible
					boolean fliped;
					do {
						fliped = false;
						for(int i = 0; i < node.getNbNeighbours(); i++)
						{
							NavmeshEdge edge = node.getNeighbourEdge(i);
							System.out.println(i+" "+edge);
	
							if(edge.forceFlip()) // flip a marché
							{
								System.out.println("Flip forcé !");
								edgesInProgress.remove(edge);
								edgesInProgress.add(edge);
								fliped = true;
								break;
							}
						}
					} while(fliped);
					

					
					int nbVoisins = node.getNbNeighbours();
					assert nbVoisins <= 4 : "Nb voisins : "+nbVoisins;
					
					NavmeshEdge[][] edges = null;
					
					// Première étape : on prépare les arêtes des futurs triangles
					if(nbVoisins == 3)
					{
						edges = new NavmeshEdge[1][3];
						edges[0] = getEdges(new NavmeshNode[] {node.getNeighbour(0), node.getNeighbour(1), node.getNeighbour(2)});
					}
					else if(nbVoisins == 4)
					{
						edges = new NavmeshEdge[2][3];
						edges[0] = getEdges(new NavmeshNode[] {node.getNeighbour(0), node.getNeighbour(1), node.getNeighbour(2)});
						NavmeshNode premier = node.getNeighbour(3);
						for(int i = 0; i < 3; i++)
							if(!premier.isNeighbourOf(node.getNeighbour(i)))
							{
								edges[1] = getEdges(new NavmeshNode[] {premier, node.getNeighbour((i+1) % 3), node.getNeighbour((i+2) % 3)});
								break;
							}
					}
					
					// Seconde étape : on retire toutes les arêtes qui restent ainsi que le nœud
					for(int i = 0; i < nbVoisins; i++)
					{
						NavmeshEdge edge = node.getNeighbourEdge(i);
						edgesInProgress.remove(edge);
						for(int j = 0; j < edge.nbTriangles; j++)
							if(triangles.remove(edge.triangles[j]))
								for(int k = 0; k < edge.triangles[j].edges.length; k++)
									if(edge.triangles[j].edges[k] != edge) // on itère sur ses triangles, donc il ne faut pas les modifier
										edge.triangles[j].edges[k].removeTriangle(edge.triangles[j]);
						node.getNeighbour(i).removeEdge(edge);
					}
					
					iterN.remove();

					// Troisième étape : on construit le ou les triangles
					if(edges != null)
						for(int i = 0; i < edges.length; i++)
							triangles.add(new NavmeshTriangle(edges[i][0], edges[i][1], edges[i][2]));
											
					break;
				}
		}
} catch(AssertionError e){e.printStackTrace();}

		// Suppression des arêtes qui coupent des obstacles
		LinkedList<NavmeshEdge> needDestruction = new LinkedList<NavmeshEdge>();
		for(NavmeshEdge e : edgesInProgress)
			for(Obstacle o : obsList)
				if(o.isColliding(e.points[0].position, e.points[1].position))
					needDestruction.add(e);
		
		/*
		 * On ne veut garder que des nœuds qui ont au moins deux voisins.
		 * Retirer ces nœuds retire des arêtes, ce qui peut entraîner une réaction en chaîne
		 * (une chaîne de nœuds sera détruite par exemple) 
		 */
		while(!needDestruction.isEmpty())
		{
			NavmeshEdge e = needDestruction.poll();
			if(edgesInProgress.remove(e)) // on vérifie que l'arête n'a pas déjà été traitée
			{
				for(int i = 0; i < 2; i++)
				{
					e.points[i].removeEdge(e);
					int nbVoisins = e.points[i].getNbNeighbours(); 
					// If the node isn't connected anymore
					if(nbVoisins == 0)
						nodesList.remove(e.points[i]);
					else if(nbVoisins == 1)
						needDestruction.add(e.points[i].getNeighbourEdge(0));
				}
			}
		}

		// Si on a supprimé l'arête d'un triangle, on supprime ce triangle, et on retire ce triangle de ses autres côtés
		Iterator<NavmeshTriangle> iterT = triangles.iterator();
		while(iterT.hasNext())
		{
			NavmeshTriangle triangle = iterT.next();
			for(int i = 0; i < 3; i++)
				if(!edgesInProgress.contains(triangle.edges[i]))
				{
					iterT.remove();
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
			n[i].nb = i; // since we deleted some nodes, the numbers may not be adjoining anymore
		}

		assert checkNodes();

		NavmeshEdge[] e = new NavmeshEdge[edgesInProgress.size()];
		for(int i = 0; i < e.length; i++)
		{
			e[i] = edgesInProgress.poll();
			e[i].updateOrientation();
		}
		
		NavmeshTriangle[] t = new NavmeshTriangle[triangles.size()];
		for(int i = 0; i < t.length; i++)
			t[i] = triangles.poll();
		
		return new TriangulatedMesh(n, e, t);
	}
	
	/**
	 * A partir de trois points qui forment un triangle, retourne les trois arêtes de ce triangle
	 * @return
	 */
	private NavmeshEdge[] getEdges(NavmeshNode[] nodes)
	{
		NavmeshEdge[] edges = new NavmeshEdge[3];
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < nodes[i].getNbNeighbours(); j++)
			{
				NavmeshEdge candidat = nodes[i].getNeighbourEdge(j);
				NavmeshNode voisinCherche = nodes[(i+1) % 3];
				if(candidat.points[0] == voisinCherche || candidat.points[1] == voisinCherche)
				{
					edges[i] = candidat;
					break;
				}
			}
		return edges;
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
		
		
		edge.points[1].removeEdge(edge);
		NavmeshEdge newEdge = new NavmeshEdge(newNode, edge.points[1]);
//		if(edge.constrained)
//			newEdge.constrained = true;
		
		edgesInProgress.add(newEdge);

		edge.points[1] = newNode;
		assert !edgesInProgress.contains(edge);
		edge.update();
		
		edgesInProgress.add(edge);

		newNode.addEdge(edge);

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
		System.out.println("Ajout");
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
		System.out.println("Add inside");
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
		System.out.println("Fin add inside");
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
		System.out.println("Flip");
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
			assert !e.flipIfNecessary();
		}
		System.out.println("Fin flip");
		// All triangles should be Delaunay
		assert checkDelaunay();
	}

	private boolean checkNodes()
	{
		for(NavmeshNode n : nodesList)
		{
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
