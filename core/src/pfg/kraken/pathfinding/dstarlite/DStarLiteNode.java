/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.pathfinding.dstarlite;

import java.awt.Graphics;
import graphic.Fenetre;
import graphic.printable.Printable;
import pfg.kraken.Couleur;
import pfg.kraken.pathfinding.dstarlite.navmesh.NavmeshNode;

/**
 * Un nœud du D* Lite.
 * 
 * @author pf
 *
 */

public class DStarLiteNode implements Printable
{
	private static final long serialVersionUID = -6800876007134374180L;
	public final NavmeshNode gridpoint;
	public final Cle cle = new Cle();
	public int g = Integer.MAX_VALUE, rhs = Integer.MAX_VALUE;
	public Double heuristiqueOrientation = null;
	public int indexPriorityQueue;

	/**
	 * "done" correspond à l'appartenance à U dans l'algo du DStarLite
	 */
	public boolean inOpenSet = false;
	public long nbPF = 0;

	public DStarLiteNode(NavmeshNode gridpoint)
	{
		this.gridpoint = gridpoint;
	}

	@Override
	public final int hashCode()
	{
		return gridpoint.nb;
	}

	@Override
	public final boolean equals(Object o)
	{
		return gridpoint.nb == o.hashCode();
	}

	@Override
	public String toString()
	{
		return gridpoint + " (" + cle + ")";
	}

	/**
	 * Initialisation du nœud s'il n'a pas encore été utilisé pour ce
	 * pathfinding
	 * 
	 * @param nbPF
	 */
	public void update(long nbPF)
	{
		if(this.nbPF != nbPF)
		{
			g = Integer.MAX_VALUE;
			rhs = Integer.MAX_VALUE;
			inOpenSet = false;
			heuristiqueOrientation = null;
			this.nbPF = nbPF;
		}
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		g.setColor(Couleur.HEURISTIQUE.couleur);
/*		if(heuristiqueOrientation != null)
		{
			double n = NavmeshNode.DISTANCE_ENTRE_DEUX_POINTS / 2;
			XY_RW point1 = new XY_RW(n, 0), point2 = new XY_RW(-n / 2, n / 2), point3 = new XY_RW(-n / 2, -n / 2);
			point1.rotate(heuristiqueOrientation).plus(gridpoint.computeVec2());
			point2.rotate(heuristiqueOrientation).plus(gridpoint.computeVec2());
			point3.rotate(heuristiqueOrientation).plus(gridpoint.computeVec2());
			int[] X = { f.XtoWindow((int) point1.getX()), f.XtoWindow((int) point2.getX()), f.XtoWindow((int) point3.getX()) };
			int[] Y = { f.YtoWindow((int) point1.getY()), f.YtoWindow((int) point2.getY()), f.YtoWindow((int) point3.getY()) };

			g.drawPolygon(X, Y, 3);
		}*/
	}

	@Override
	public int getLayer()
	{
		return Couleur.HEURISTIQUE.l.ordinal();
	}

}