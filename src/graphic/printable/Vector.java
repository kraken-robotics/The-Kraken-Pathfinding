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

package graphic.printable;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import graphic.Fenetre;
import robot.RobotReal;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Un vecteur affichable
 * 
 * @author pf
 *
 */

public class Vector implements Printable, Serializable
{
	private static final long serialVersionUID = 3887897521575363643L;
	private Vec2RO a, b;
	private double orientation;
	private Layer l;
	private Couleur c;
	private AffineTransform tx = new AffineTransform();
	private Polygon arrowHead = new Polygon();  
	
	public Vector(Vec2RO pos, double orientation, Couleur c)
	{
		a = pos;
		b = new Vec2RW(50, orientation, true).plus(a);
		this.orientation = orientation;
		this.l = c.l;
		this.c = c;
		arrowHead.addPoint(0,5);
		arrowHead.addPoint(-5,-5);
		arrowHead.addPoint(5,-5);
	}
	
	public void update(Vec2RO pos, double orientation)
	{
		a = pos;
		b = new Vec2RW(50, orientation, true).plus(a);
		this.orientation = orientation;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		g.setColor(c.couleur);
		g.drawLine(f.XtoWindow(a.getX()), f.YtoWindow(a.getY()), f.XtoWindow(b.getX()), f.YtoWindow(b.getY()));
	    tx.setToIdentity();
	    tx.translate(f.XtoWindow(b.getX()), f.YtoWindow(b.getY()));
	    tx.rotate((-orientation-Math.PI/2d));  

	    Graphics2D g2d = (Graphics2D) g.create();
	    g2d.setTransform(tx);   
	    g2d.fill(arrowHead);
	    g2d.dispose();
	}

	@Override
	public Vector clone()
	{
		return new Vector(a.clone(), orientation, c);
	}
	
	@Override
	public Layer getLayer()
	{
		return l;
	}

	@Override
	public String toString()
	{
		return "Vecteur entre " + a + " et " + b;
	}

}
