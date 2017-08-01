/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite;

import pfg.config.Config;
import pfg.graphic.Fenetre;
import pfg.graphic.printable.Layer;
import pfg.graphic.printable.Printable;
import pfg.kraken.utils.XY;
import pfg.log.Log;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.LogCategoryKraken;
import pfg.kraken.SeverityCategoryKraken;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.container.StaticObstacles;

import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * La classe qui contient la grille utilisée par le pathfinding.
 * Utilisée uniquement pour le pathfinding DStarLite.
 * Notifie quand il y a un changement d'obstacles
 * 
 * @author pf
 *
 */

public class Navmesh implements Printable
{
	private static final long serialVersionUID = 3849267693380819201L;
	protected Log log;
	final NavmeshNode[] nodes;
	final NavmeshEdge[] edges;
	private int expansion;
	private List<NavmeshEdge> needFlipCheck = new ArrayList<NavmeshEdge>();

	public Navmesh(Log log, Config config, StaticObstacles obs)
	{
		this.log = log;
		expansion = config.getInt(ConfigInfoKraken.DILATATION_ROBOT_DSTARLITE);
		Object[] o = loadNavMesh(obs, "navmesh"+obs.hashCode()+".krk");
		nodes = (NavmeshNode[]) o[0];
		edges = (NavmeshEdge[]) o[1];
	}
	
	private Object[] loadNavMesh(StaticObstacles obs, String filename)
	{
		log.write("D* NavMesh loading…", LogCategoryKraken.PF);
		try
		{
			FileInputStream fichier = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fichier);
			Object[] o = (Object[]) ois.readObject();
			ois.close();
			return o;
		}
		catch(IOException | ClassNotFoundException | NullPointerException e)
		{
			assert false;
			log.write("The navmesh can't be loaded : generation of a new one.", SeverityCategoryKraken.WARNING, LogCategoryKraken.PF);
			Object[] o = generateNavMesh(obs);
			saveNavMesh(o, filename);
			return o;
		}
	}

	private void saveNavMesh(Object[] o, String filename)
	{
		try
		{
			FileOutputStream fichier;
			ObjectOutputStream oos;

			new File(filename).createNewFile();
			fichier = new FileOutputStream(filename);
			oos = new ObjectOutputStream(fichier);
			oos.writeObject(o);
			oos.flush();
			oos.close();
			log.write("Navmesh saved into "+filename, LogCategoryKraken.PF);
		}
		catch(IOException e)
		{
			log.write("Error during navmesh save ! " + e, SeverityCategoryKraken.CRITICAL, LogCategoryKraken.PF);
		}
	}

	
	private Object[] generateNavMesh(StaticObstacles obs)
	{
		List<Obstacle> obsList = obs.getObstacles();
		List<NavmeshNode> nodesList = new ArrayList<NavmeshNode>();
		for(Obstacle o : obsList)
		{
			XY[] hull = o.getExpandedConvexHull(expansion);
			for(XY pos : hull)
				nodesList.add(new NavmeshNode(pos));
		}
		addSteinerPoints(nodesList, obsList);
		triangulate(nodesList, obsList);
		return null;
	}
	
	// TODO : ou juste ajouter des points au milieu des plus grands triangles
	private void addSteinerPoints(List<NavmeshNode> nodesList, List<Obstacle> obsList)
	{
	}
	
	private boolean isCircumscribed(XY pointA, XY pointB, XY pointC, XY pointD)
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
	 * This is not the fastest algorithm… but it is enough for an off-line computation
	 * This is a Delaunay triangulation.
	 * @param nodesList
	 * @param obsList
	 */
	private void triangulate(List<NavmeshNode> nodesList, List<Obstacle> obsList)
	{
		/*
		 * No triangulation possible (nor needed)
		 */
		if(nodesList.size() < 3)
			return;
		
		// Initial triangle
		List<NavmeshTriangle> triangles = new ArrayList<NavmeshTriangle>();
		List<NavmeshEdge> edgesInProgress = new ArrayList<NavmeshEdge>();
		edgesInProgress.add(new NavmeshEdge(nodesList.get(0), nodesList.get(1)));
		edgesInProgress.add(new NavmeshEdge(nodesList.get(1), nodesList.get(2)));
		edgesInProgress.add(new NavmeshEdge(nodesList.get(2), nodesList.get(0)));
		triangles.add(new NavmeshTriangle(edgesInProgress.get(0), edgesInProgress.get(1), edgesInProgress.get(2)));

		for(int i = 0; i < 3; i++)
		{
			edgesInProgress.get(i).addTriangle(triangles.get(0));
			edgesInProgress.get(i).checkTriangle(1);
		}
		
		// We add the points one by one
		for(int index = 3; index < nodesList.size(); index++)
		{
			NavmeshNode nextNode = nodesList.get(index);
			boolean handled = false;
			
			// first we check if this point is in a triangle
			for(NavmeshTriangle t : triangles)
				if(t.isInside(nextNode.position))
				{
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

					NavmeshTriangle tr1 = new NavmeshTriangle(e[0], e[1], t.edges[2]);
					t.edges[2].replaceTriangle(t, tr1);
					NavmeshTriangle tr2 = new NavmeshTriangle(e[0], e[2], t.edges[1]);
					t.edges[1].replaceTriangle(t, tr2);
					t.setEdges(e[1], e[2], t.edges[0]);
					
					e[0].addTriangle(tr1);
					e[0].addTriangle(tr2);
					e[1].addTriangle(tr1);
					e[1].addTriangle(t);
					e[2].addTriangle(tr2);
					e[2].addTriangle(t);
					
					assert e[0].checkTriangle(2);
					assert e[1].checkTriangle(2);
					assert e[2].checkTriangle(2);
					
					triangles.add(tr1);
					triangles.add(tr2);
					
					flip();
					
					handled = true;
					break;
				}
			
			if(!handled)
			{
				// the point isn't in any triangle
			}
		}
	}
	
	private void flip()
	{
		while(!needFlipCheck.isEmpty())
		{
			// TODO
		}
		
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		// TODO
	}

	@Override
	public int getLayer()
	{
		return Layer.MIDDLE.ordinal();
	}

	public double getDistance(NavmeshNode n1, NavmeshNode n2)
	{
		return 0;
	}
	
	public NavmeshNode getNearest(XY position)
	{
		return null;
	}

}
