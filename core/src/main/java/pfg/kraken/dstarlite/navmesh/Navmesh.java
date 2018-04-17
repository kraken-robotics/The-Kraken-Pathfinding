/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import pfg.config.Config;
import pfg.graphic.GraphicDisplay;
import pfg.kraken.utils.XY;
import pfg.log.Log;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.LogCategoryKraken;
import pfg.kraken.SeverityCategoryKraken;
import pfg.kraken.obstacles.container.StaticObstacles;

import java.io.IOException;

/**
 * A navmesh, used by the D* Lite.
 * It can load and save a navmesh. If necessary, it generate a new one.
 * 
 * @author pf
 *
 */

public final class Navmesh
{
	protected Log log;
	public TriangulatedMesh mesh;
	
	public Navmesh(Log log, Config config, StaticObstacles obs, GraphicDisplay buffer, NavmeshComputer computer)
	{
		this.log = log;
		String filename = config.getString(ConfigInfoKraken.NAVMESH_FILENAME);
		try {
			log.write("D* NavMesh loading…", LogCategoryKraken.PF);
			mesh = TriangulatedMesh.loadNavMesh(filename);
			if(mesh.obsHashCode != obs.hashCode())
				throw new NullPointerException("different obstacles ("+mesh.obsHashCode+" != "+obs.hashCode()+")"); // l'objectif est juste d'entrer dans le catch ci-dessous…
			if(!computer.checkNavmesh(mesh))
				throw new NullPointerException("invalid navmesh");
		}
		catch(IOException | ClassNotFoundException | NullPointerException e)
		{
			log.write("The navmesh can't be loaded ("+e.getMessage()+") : generation of a new one.", SeverityCategoryKraken.WARNING, LogCategoryKraken.PF);
			mesh = computer.generateNavMesh(obs);
			try {
				mesh.saveNavMesh(filename);
				log.write("Navmesh saved into "+filename, LogCategoryKraken.PF);
			}
			catch(IOException e1)
			{
				log.write("Error during navmesh save ! " + e1, SeverityCategoryKraken.CRITICAL, LogCategoryKraken.PF);
			}
		}
		assert mesh != null;
		if(config.getBoolean(ConfigInfoKraken.GRAPHIC_NAVMESH))
			mesh.addToBuffer(buffer);
	}
	
	@Override
	public String toString()
	{
		return mesh.toString();
	}
	
	public NavmeshNode getNearest(XY position)
	{
		NavmeshNode bestNode = null;
		double smallestDistance = 0;
		for(NavmeshNode n : mesh.nodes)
		{
			double candidateDistance = position.squaredDistance(n.position);
			if(bestNode == null || candidateDistance < smallestDistance)
			{
				bestNode = n;
				smallestDistance = candidateDistance;
			}
		}
		assert bestNode != null;
		return bestNode;
	}

}
