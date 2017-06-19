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

	public Navmesh(Log log, StaticObstacles obs)
	{
		this.log = log;
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

	
	public Object[] generateNavMesh(StaticObstacles obs)
	{
		// TODO !
		return null;
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
