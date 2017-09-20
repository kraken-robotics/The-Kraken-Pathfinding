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
import pfg.kraken.obstacles.RectangularObstacle;
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
			return o2.length_um - o1.length_um;
		}
	}
	
	protected Log log;

	private LinkedList<NavmeshEdge> needFlipCheck;
	private PriorityQueue<NavmeshTriangle> triangles;
	private PriorityQueue<NavmeshEdge> edgesInProgress;
	private List<NavmeshNode> nodesList;

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
		needFlipCheck = new LinkedList<NavmeshEdge>();
		triangles = new PriorityQueue<NavmeshTriangle>(1000, new NavmeshTriangleComparator());
		edgesInProgress = new PriorityQueue<NavmeshEdge>(1000, new NavmeshEdgeComparator());
		 nodesList = new ArrayList<NavmeshNode>();

		List<Obstacle> obsList = obs.getObstacles();
		String s;
		
		XY bottomLeftCorner = obs.getBottomLeftCorner();
		XY topRightCorner = obs.getTopRightCorner();
		
		RectangularObstacle external = new RectangularObstacle(bottomLeftCorner.plusNewVector(topRightCorner).scalar(0.5), (int) (topRightCorner.getX() - bottomLeftCorner.getX()), (int) (topRightCorner.getY() - bottomLeftCorner.getY()), 0.);

		XY[] hull = external.getExpandedConvexHull(- expansion * 1.1, bottomLeftCorner.distance(topRightCorner));
		assert hull.length == 4 : hull.length;
		
		NavmeshNode br = new NavmeshNode(hull[0]);
		NavmeshNode tr = new NavmeshNode(hull[1]);
		NavmeshNode tl = new NavmeshNode(hull[2]);
		NavmeshNode bl = new NavmeshNode(hull[3]);

		nodesList.add(tl);
		nodesList.add(tr);
		nodesList.add(br);
		nodesList.add(bl);

		// Used to check if there are duplicate points
		List<XY> addedPoints = new ArrayList<XY>();
		for(NavmeshNode n : nodesList)
			addedPoints.add(n.position);

		hull = external.getExpandedConvexHull(- expansion * 1.1, longestAllowedLength * 0.7 / 1000.);
		for(int i = 0; i < hull.length; i++)
		{
			if(!addedPoints.contains(hull[i]))
			{
				NavmeshNode n = new NavmeshNode(hull[i]);
				nodesList.add(n);
				addedPoints.add(hull[i]);
			}
		}
		
		for(Obstacle o : obsList)
		{
			hull = o.getExpandedConvexHull(expansion * 1.1, longestAllowedLength * 0.7 / 1000.);
			assert hull.length >= 3;
			for(int i = 0; i < hull.length; i++)
			{
				// Si on trouve deux points très proches, on en retire un
				boolean abort = false;
				for(XY pos : addedPoints)
					if(hull[i].distance(pos) < longestAllowedLength / 5000)
					{
						abort = true;
						break;
					}
				if(abort)
					continue;
				
				// On n'inclut pas les nœuds en dehors du rectangle
				if(!addedPoints.contains(hull[i]) && hull[i].getX() >= bl.position.getX() && hull[i].getX() <= tr.position.getX()
						&& hull[i].getY() >= bl.position.getY() && hull[i].getY() <= tr.position.getY())
				{
					NavmeshNode n = new NavmeshNode(hull[i]);
					nodesList.add(n);
					addedPoints.add(hull[i]);
				}
			}
		}

		assert ((s = checkNoDuplicate()) == null) : s;
		
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
		
		// We add the points one by one
		for(int index = 4; index < nodesList.size(); index++)
			addNewNodeInitialization(nodesList.get(index));
		
		assert checkDelaunay();
		// We add other points in order to avoir long edges
		NavmeshEdge longestEdge = edgesInProgress.peek();
		while(longestEdge.length_um > longestAllowedLength)
		{
			assert ((s = checkLongestEdge()) == null) : s;
			edgesInProgress.poll();
			assert ((s = checkLongestEdge()) == null) : s;
			addMiddleEdgePoint(longestEdge);
			longestEdge = edgesInProgress.peek();
		}
		
		assert edgesInProgress.peek().length_um <= longestAllowedLength : edgesInProgress.peek().length_um + " > " + longestAllowedLength;
		
		// We add other points in order to avoid large triangle
		NavmeshTriangle largestTriangle = triangles.peek();
		while(largestTriangle.area > largestAllowedArea)
		{
			triangles.poll();
			addCenterPoint(largestTriangle);
			largestTriangle = triangles.peek();
		}
		
		assert edgesInProgress.peek().length_um <= longestAllowedLength : edgesInProgress.peek().length_um + " > " + longestAllowedLength;

		assert ((s = checkCrossingEdges()) == null) : s;
		assert ((s = checkNodeInTriangle()) == null) : s;
		assert ((s = checkNodeIncludedInEdge()) == null) : s;
		
/*		try {
		// Suppression des nœuds à l'intérieur d'obstacle
		Iterator<NavmeshNode> iterN = nodesList.iterator();
		while(iterN.hasNext())
		{
			NavmeshNode node = iterN.next();
			for(Obstacle o : obsList)
				if(o.squaredDistance(node.position) < expansion * expansion)
				{
					// On flippe le plus d'arêtes possible
					boolean fliped;
					do {
						fliped = false;
						for(int i = 0; i < node.getNbNeighbours(); i++)
						{
							NavmeshEdge edge = node.getNeighbourEdge(i);
	
							assert checkAll();
							assert ((s = checkCrossingEdges()) == null) : s+", fliped edge : "+edge;
							if(edge.forceFlip()) // flip a marché
							{
								edgesInProgress.remove(edge);
								edgesInProgress.add(edge);
								needFlipCheck.add(edge);
								fliped = true;
								assert checkAll();
								assert ((s = checkCrossingEdges()) == null) : s+", fliped edge : "+edge+", node to remove : "+node;
								assert ((s = checkNodeInTriangle()) == null) : s+", fliped edge : "+edge+", node to remove : "+node;
								assert ((s = checkNodeIncludedInEdge()) == null) : s;

								break;
							}
						}
					} while(fliped);
					
					int nbVoisins = node.getNbNeighbours();
					assert nbVoisins <= 4 : "Nb voisins : "+nbVoisins+" "+node;

					NavmeshNode[] voisins = new NavmeshNode[nbVoisins];
					
					for(int i = 0; i < nbVoisins; i++)
						voisins[i] = node.getNeighbour(i);					
					System.out.println(nbVoisins);*/
/*					NavmeshEdge[][] edges = null;
					
					// TODO trouver les conditions exactes
					
					// Première étape : on prépare les arêtes des futurs triangles
					if(nbVoisins == 3)
					{
						edges = new NavmeshEdge[1][3];
						// edges[0] can contain "null" value if the neighbours of node don't form a triangle
						edges[0] = getEdges(new NavmeshNode[] {node.getNeighbour(0), node.getNeighbour(1), node.getNeighbour(2)});
					}
					else if(nbVoisins == 4)
					{
						edges = new NavmeshEdge[2][3];
						NavmeshNode premier = node.getNeighbour(3);
						for(int i = 0; i < 3; i++)
							if(!premier.isNeighbourOf(node.getNeighbour(i)))
							{
								NavmeshEdge newEdge = new NavmeshEdge(premier, node.getNeighbour(i));
								edgesInProgress.add(newEdge);
								edges[0] = getEdges(new NavmeshNode[] {premier, node.getNeighbour(i), node.getNeighbour((i+1) % 3)});
								edges[1] = getEdges(new NavmeshNode[] {premier, node.getNeighbour(i), node.getNeighbour((i+2) % 3)});
								break;
							}
						assert edges[1] != null;
					}*/
					/*
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
					if(nbVoisins == 3)
					{
						boolean check = true;
						for(NavmeshNode n : nodesList)
							if(n != voisins[0] && n != voisins[1] && n != voisins[2] &&
									(NavmeshTriangle.isInside(n.position, voisins[0].position, voisins[1].position, voisins[2].position)
											|| NavmeshEdge.containsNode(voisins[0].position, n.position, voisins[1].position)
											|| NavmeshEdge.containsNode(voisins[1].position, n.position, voisins[2].position)
											|| NavmeshEdge.containsNode(voisins[2].position, n.position, voisins[0].position)))
							{
								check = false;
								break;
							}

						if(check)
						{
							System.out.println("On construit");
							NavmeshEdge[] edgesTr = new NavmeshEdge[3];
							for(int i = 0; i < 3; i++)
							{
								NavmeshNode voisinCherche = voisins[(i+1) % 3];
								for(int j = 0; j < voisins[i].getNbNeighbours(); j++)
								{
									NavmeshEdge candidat = voisins[i].getNeighbourEdge(j);
									if(candidat.points[0] == voisinCherche || candidat.points[1] == voisinCherche)
									{
										edgesTr[i] = candidat;
										break;
									}
								}
								if(edgesTr[i] == null)
								{
									NavmeshEdge newEdge = new NavmeshEdge(voisins[i], voisinCherche);
									edgesInProgress.add(newEdge);
									edgesTr[i] = newEdge;
								}
							}
							for(int j = 0; j < 3; j++)
								needFlipCheck.add(edgesTr[j]);
							triangles.add(new NavmeshTriangle(edgesTr[0], edgesTr[1], edgesTr[2]));

						}
					}
					
					assert ((s = checkNodeInTriangle()) == null) : s;
			
					flip();
					assert checkDelaunay();
					break;
				}
		}
				}catch(AssertionError e){e.printStackTrace();}
					 */
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

		assert checkAll();
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
			e[i].nb = i;
			e[i].updateOrientation();
		}
		
		for(int i = 0; i < n.length; i++)
			n[i].prepareToSave();

		for(int i = 0; i < e.length; i++)
			e[i].prepareToSave();
		
		NavmeshTriangle[] t = new NavmeshTriangle[triangles.size()];
		for(int i = 0; i < t.length; i++)
		{
			t[i] = triangles.poll();
			t[i].prepareToSave();
		}
		
		return new TriangulatedMesh(n, e, t, obs.hashCode());
	}
	
	/**
	 * A partir de trois points qui forment un triangle, retourne les trois arêtes de ce triangle
	 * @return
	 */
/*	private NavmeshEdge[] getEdges(NavmeshNode[] nodes)
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
	}*/
	
	private String checkLongestEdge()
	{
		int longestEdge = edgesInProgress.peek().length_um;
		for(NavmeshEdge e : edgesInProgress)
			if(e.length_um > longestEdge)
				return e.toString()+" : "+e.length_um+" > "+longestEdge;
		return null;
	}
	
	private boolean checkAll()
	{
		String s;
		assert ((s = checkEdgesInTriangle()) == null) : s;
		assert ((s = checkPointsInTriangle()) == null) : s;
		assert ((s = checkTrianglesInEdge()) == null) : s;
		assert ((s = checkEdgesInNodes()) == null) : s;
		assert ((s = checkNodesInEdges()) == null) : s;
		return true;
	}
	
	private String checkEdgesInTriangle()
	{
		for(NavmeshTriangle t : triangles)
			for(NavmeshEdge e : t.edges)
				if(!edgesInProgress.contains(e))
					return "A triangle has an unknown edge : "+t+" "+e;
		return null;
	}
	
	private String checkPointsInTriangle()
	{
		for(NavmeshTriangle t : triangles)
			for(NavmeshNode n : t.points)
				if(!nodesList.contains(n))
					return "A triangle has an unknown node : "+t+" "+n;
		return null;
	}
	
	private String checkTrianglesInEdge()
	{
		for(NavmeshEdge e : edgesInProgress)
			for(NavmeshTriangle t : e.triangles)
				if(t != null && !triangles.contains(t))
					return "An edge has an unknown triangle : "+e+" "+t;
		return null;
	}
	
	private String checkEdgesInNodes()
	{
		for(NavmeshNode n : nodesList)
			for(int i = 0; i < n.getNbNeighbours(); i++)
				if(!edgesInProgress.contains(n.getNeighbourEdge(i)))
					return "A node has an unknown edge : "+n+" "+n.getNeighbourEdge(i);
		return null;
	}
	
	private String checkNodesInEdges()
	{
		for(NavmeshEdge e : edgesInProgress)
			for(int i = 0; i < 2; i++)
				if(!nodesList.contains(e.points[i]))
					return "An edge has an unknown points : "+e+" "+e.points[i];
		return null;
	}
	
	
	private String checkNodeInTriangle()
	{
		for(NavmeshNode n : nodesList)
			for(NavmeshTriangle t : triangles)
				if(n != t.points[0] && n != t.points[1] && n != t.points[2] && t.isInside(n.position))
					return "There is a node inside a triangle ! "+n+" "+t;
		return null;
	}
	
	private String checkNodeIncludedInEdge()
	{
		for(NavmeshEdge e : edgesInProgress)
			for(NavmeshNode n : nodesList)
				if(e.points[0] != n && e.points[1] != n && e.containsNode(n))
					return "A node is included in an edge : "+e+" "+n;
		return null;

	}
	
	private String checkCrossingEdges()
	{
		for(NavmeshEdge e1 : edgesInProgress)
			for(NavmeshEdge e2 : edgesInProgress)
				if(e1 != e2 && e1.points[0] != e2.points[0] && e1.points[0] != e2.points[1]
						&& e1.points[1] != e2.points[0] && e1.points[1] != e2.points[1]
								&& XY.segmentIntersection(e1.points[0].position, e1.points[1].position,
										e2.points[0].position, e2.points[1].position))
					return "Two edges intersect ! "+e1+" and "+e2+" "+XY.segmentIntersection(e1.points[0].position, e1.points[1].position,
							e2.points[0].position, e2.points[1].position);
		return null;
	}

	private void addMiddleEdgePoint(NavmeshEdge edge)
	{
		assert needFlipCheck.isEmpty();
		assert edge.nbTriangles > 0;
		
		XY a = edge.points[0].position;
		XY b = edge.points[1].position;
		XY c = a.plusNewVector(b).scalar(0.5);
		
		NavmeshNode newNode = new NavmeshNode(c);
		nodesList.add(newNode);

		addPointInEdge(newNode, edge);
	}
	
	/**
	 * Add a node (already created) within an edge
	 * @param newNode
	 * @param edge
	 */
	private void addPointInEdge(NavmeshNode newNode, NavmeshEdge edge)
	{		
		// Il ne faut pas que ce soit sur
		assert !newNode.position.equals(edge.points[0].position);
		assert !newNode.position.equals(edge.points[1].position);
		
		NavmeshTriangle tr0 = edge.triangles[0];
		NavmeshTriangle tr1 = edge.triangles[1];
		
		edge.points[1].removeEdge(edge);
		NavmeshEdge newEdge = new NavmeshEdge(newNode, edge.points[1]);
		
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
		assert !edgesInProgress.isEmpty();
		assert !triangles.isEmpty();

		// first we check if this point is in a triangle
				
		NavmeshTriangle tr = null;
		NavmeshEdge edge = null;
		for(NavmeshTriangle t : triangles)
			if(t.isInside(nextNode.position))
			{
				tr = t;
				for(NavmeshEdge e : t.edges)
					if(e.containsNode(nextNode))
					{
						assert edge == null : "The node "+nextNode+" belongs to "+edge+" and "+e+" "+tr;
						edge = e;
					}
				break;
			}
		
		if(tr != null)
		{
			if(edge == null)
			{
				// The node is (strictly) inside a triangle
				triangles.remove(tr);
				addInsideNode(nextNode, tr);
				String s;
				assert ((s = checkNoDuplicate()) == null) : s+", nextNode = "+nextNode;
			}
			else
			{
				// Le point est sur une arête. On utilise l'autre procédure qui s'occupe de ça
				edgesInProgress.remove(edge);
				addPointInEdge(nextNode, edge);
				String s;
				assert ((s = checkNoDuplicate()) == null) : s+", nextNode = "+nextNode;
			}
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
	
	private String checkNoDuplicate()
	{
		for(int i = 0; i < nodesList.size(); i++)
			for(int j = 0; j < nodesList.size(); j++)
				if(i != j)
				{
					NavmeshNode n1 = nodesList.get(i);
					NavmeshNode n2 = nodesList.get(j);
					if(n1 == n2)
						return "Duplicated node : "+n1;
					if(n1.position.equals(n2.position))
						return "Duplicated position : "+n1.position;
				}
		return null;
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
			assert !e.flipIfNecessary() : e;
		}
		// All triangles should be Delaunay
		String s;
		assert ((s = checkCrossingEdges()) == null) : s;
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

	public boolean checkNavmesh(TriangulatedMesh mesh)
	{
		for(NavmeshEdge e : mesh.edges)
			if(e.length_um > longestAllowedLength)
				return false;

		for(NavmeshTriangle t : mesh.triangles)
			if(t.area > largestAllowedArea)
				return false;

		return true;
	}
}
