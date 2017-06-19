/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite;

import pfg.config.Config;
import pfg.graphic.Fenetre;
import pfg.graphic.printable.Layer;
import pfg.graphic.printable.Printable;
import pfg.kraken.utils.Log;
import pfg.kraken.utils.XY;
import pfg.kraken.ConfigInfoKraken;
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
		log.debug("D* NavMesh loading…");
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
			log.warning("The navmesh can't be loaded : generation of a new one.");
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
			log.debug("Navmesh saved into "+filename);
		}
		catch(IOException e)
		{
			log.critical("Error during navmesh save ! " + e);
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
		triangles.add(new NavmeshTriangle(nodesList.get(0), nodesList.get(1), nodesList.get(2)));

		for(int index = 3; index < nodesList.size(); index++)
		{
			NavmeshNode nextNode = nodesList.get(index);
			boolean handled = false;
			for(NavmeshTriangle t : triangles)
				if(t.isInside(nextNode.position))
				{
					// the point is in the triangle t
					// TODO
					handled = true;
					break;
				}
			
			if(!handled)
			{
				// the point isn't in any triangle
			}
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
