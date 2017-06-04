/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.dstarlite.gridspace;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import graphic.Fenetre;
import graphic.printable.Layer;
import graphic.printable.Printable;
import kraken.Couleur;

/**
 * Le masque d'un obstacle de proximité
 * 
 * @author pf
 *
 */

public class Masque implements Printable, Serializable
{
	private static final long serialVersionUID = 3358374399044123020L;
	public List<PointDirige> masque = new ArrayList<PointDirige>();
	private transient PointGridSpaceManager pm;

	public Masque(PointGridSpaceManager pm, List<PointDirige> masque)
	{
		this.pm = pm;
		this.masque = masque;
	}

	@Override
	public int hashCode()
	{
		return masque.get(0).hashCode();
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		for(PointDirige p : masque)
		{
			g.setColor(Couleur.NOIR.couleur);
			pm.getGridPointVoisin(p).print(g, f);
		}
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

}
