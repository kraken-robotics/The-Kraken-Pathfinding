/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import pfg.config.Config;
import pfg.graphic.Fenetre;
import pfg.graphic.printable.Layer;
import pfg.graphic.printable.Printable;
import pfg.kraken.utils.XY;
import pfg.log.Log;
import pfg.kraken.LogCategoryKraken;
import pfg.kraken.SeverityCategoryKraken;
import pfg.kraken.obstacles.container.StaticObstacles;

import java.awt.Graphics;
import java.io.IOException;

/**
 * A navmesh, used by the D* Lite.
 * It can load and save a navmesh. If necessary, it generate a new one
 * 
 * @author pf
 *
 */

public class Navmesh implements Printable
{
	
	private static final long serialVersionUID = 3849267693380819201L;
	protected Log log;
	public TriangulatedMesh en;
	
	public Navmesh(Log log, Config config, StaticObstacles obs)
	{
		this.log = log;
		String filename = "navmesh-"+obs.hashCode()+".krk";
		try {
			log.write("D* NavMesh loading…", LogCategoryKraken.PF);
			en = TriangulatedMesh.loadNavMesh(filename);
		}
		catch(IOException | ClassNotFoundException | NullPointerException e)
		{
			log.write("The navmesh can't be loaded : generation of a new one.", SeverityCategoryKraken.WARNING, LogCategoryKraken.PF);
			NavmeshComputer computer = new NavmeshComputer(log, config);
			en = computer.generateNavMesh(obs);
			try {
				en.saveNavMesh(filename);
				log.write("Navmesh saved into "+filename, LogCategoryKraken.PF);
			}
			catch(IOException e1)
			{
				log.write("Error during navmesh save ! " + e, SeverityCategoryKraken.CRITICAL, LogCategoryKraken.PF);
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
