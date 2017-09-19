/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import pfg.graphic.PrintBuffer;

/**
 * The mesh itself
 * @author pf
 *
 */

public class TriangulatedMesh implements Serializable
{
	private static final long serialVersionUID = 5706228066190218520L;
	public final int obsHashCode;
	public final NavmeshNode[] nodes;
	public final NavmeshEdge[] edges;
	public final NavmeshTriangle[] triangles;
	
	public TriangulatedMesh(NavmeshNode[] nodes, NavmeshEdge[] edges, NavmeshTriangle[] triangles, int obsHashCode)
	{
		this.nodes = nodes;
		this.edges = edges;
		this.triangles = triangles;
		this.obsHashCode = obsHashCode;
	}

	public static TriangulatedMesh loadNavMesh(String filename) throws IOException, ClassNotFoundException
	{
		FileInputStream fichier = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fichier);
		TriangulatedMesh o = (TriangulatedMesh) ois.readObject();
		ois.close();
		return o;
	}

	public void saveNavMesh(String filename) throws IOException
	{
		FileOutputStream fichier;
		ObjectOutputStream oos;

		new File(filename).createNewFile();
		fichier = new FileOutputStream(filename);
		oos = new ObjectOutputStream(fichier);
		oos.writeObject(this);
		oos.flush();
		oos.close();
	}
	
	@Override
	public String toString()
	{
		String out = "Triangulated mesh : \n";
		for(int i = 0; i < triangles.length; i++)
			out += triangles[i].toString()+"\n";
		return out;
	}

	public void addToBuffer(PrintBuffer buffer)
	{
		for(NavmeshTriangle t : triangles)
			buffer.add(t);
		for(NavmeshEdge e : edges)
			buffer.add(e);
		for(NavmeshNode n : nodes)
			buffer.add(n);
	}
}
