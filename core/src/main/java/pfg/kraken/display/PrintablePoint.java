/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.display;

import java.awt.Graphics;
import java.io.Serializable;
import pfg.kraken.struct.XY;

/**
 * Un point
 * 
 * @author pf
 *
 */

public class PrintablePoint implements Printable, Serializable
{
	private static final long serialVersionUID = 3887897521575363643L;
	private XY a;
	private int taille = 2;
	
	public PrintablePoint(double x, double y)
	{
		this.a = new XY(x, y);
	}

	public PrintablePoint(double x, double y, int taille)
	{
		this.a = new XY(x, y);
		this.taille = taille;
	}

	@Override
	public void print(Graphics g, Display f)
	{
		g.fillOval(f.XtoWindow(a.getX())-taille/2, f.YtoWindow(a.getY())-taille/2, taille, taille);
	}

	@Override
	public String toString()
	{
		return "Point en " + a;
	}
}
