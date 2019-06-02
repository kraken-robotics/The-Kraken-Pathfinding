package pfg.kraken.display;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

/**
 * A printable with its color
 * @author pf
 *
 */

public class ColoredPrintable implements Serializable
{
	private static final long serialVersionUID = 1007485107824898388L;
	public final Printable p;
	public final Color c;
	public final int l;
	public final boolean temporary;
	
	public ColoredPrintable(Printable p, Color c, int l, boolean temporary)
	{
		this.p = p;
		this.c = c;
		this.l = l;
		this.temporary = temporary;
	}
	
	public void print(Graphics g, Display f)
	{
		if(c != null)
			g.setColor(c);
		p.print(g, f);
	}

	@Override
	public int hashCode()
	{
		return p.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o == p;
	}
	
	@Override
	public String toString()
	{
		return p+" "+c+" "+l;
	}
}
