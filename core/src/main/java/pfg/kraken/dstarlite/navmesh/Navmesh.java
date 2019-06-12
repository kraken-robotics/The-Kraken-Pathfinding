/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.display.Display;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.struct.XY;
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
	public TriangulatedMesh mesh;
	
	public Navmesh(Config config, StaticObstacles obs, Display buffer, NavmeshComputer computer)
	{
		String filename = config.getString(ConfigInfoKraken.NAVMESH_FILENAME);
		try {
			mesh = TriangulatedMesh.loadNavMesh(filename);
			if(mesh.obsHashCode != obs.hashCode())
				throw new NullPointerException("different obstacles ("+mesh.obsHashCode+" != "+obs.hashCode()+")"); // l'objectif est juste d'entrer dans le catch ci-dessous…
			if(!computer.checkNavmesh(mesh))
				throw new NullPointerException("invalid navmesh");
		}
		catch(IOException | ClassNotFoundException | NullPointerException e)
		{
			System.err.println("The navmesh can't be loaded ("+e.getMessage()+") : generation of a new one.");
			mesh = computer.generateNavMesh(obs);
			try {
				mesh.saveNavMesh(filename);
			}
			catch(IOException e1)
			{
				System.err.println("Error during navmesh save ! " + e1);
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
	
	/**
	 * Returns the nearest navmesh node not in an obstacle
	 * @param position
	 * @return
	 */
	public NavmeshNode getNearestAvailable(XY position)
	{
		NavmeshNode bestNode = null;
		double smallestDistance = 0;
		for(NavmeshNode n : mesh.nodes)
		{
			boolean anyAvailableEdge = false;
			for(int i = 0; i < n.getNbNeighbours(); i++)
				if(n.getNeighbourEdge(i).obstructingObstacles.isEmpty())
				{
					anyAvailableEdge = true;
					break;
				}
			if(!anyAvailableEdge)
				continue;
				
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
