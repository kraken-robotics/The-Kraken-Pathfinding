/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package pathfinding.dstarlite.gridspace;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import graphic.Fenetre;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import robot.RobotReal;

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
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		for(PointDirige p : masque)
		{
			g.setColor(Couleur.NOIR.couleur);
			pm.getGridPointVoisin(p).print(g, f, robot);
		}
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

}
