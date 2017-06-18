/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.pathfinding.dstarlite.navmesh;

import graphic.Fenetre;
import graphic.printable.Layer;
import graphic.printable.Printable;
import pfg.kraken.utils.Log;
import pfg.kraken.utils.XY;

import java.awt.Graphics;

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
	private NavmeshNode[] nodes;
	private NavmeshEdge[] edges;

	public Navmesh(Log log)
	{
		this.log = log;
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

	public NavmeshEdge[] getEdges()
	{
		return edges;
	}
	
	public int getNodeNb()
	{
		return nodes.length;
	}
	
	public double getDistance(NavmeshNode n1, NavmeshNode n2)
	{
		return 0;
	}
	
	public NavmeshNode getNearest(XY position)
	{
		return null;
	}

	public NavmeshNode getNode(int i)
	{
		return nodes[i];
	}

}
