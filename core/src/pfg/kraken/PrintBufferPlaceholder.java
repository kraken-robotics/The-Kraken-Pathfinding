/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.awt.Color;

import pfg.graphic.PrintBuffer;
import pfg.graphic.printable.Plottable;
import pfg.graphic.printable.Printable;

/**
 * Just a placeholder to disable the graphic display
 * @author pf
 *
 */

public class PrintBufferPlaceholder extends PrintBuffer
{
	public synchronized void clearTemporaryPrintables()
	{}
	
	public synchronized void addTemporaryPrintable(Printable o, Color c, int layer)
	{}
	
	public synchronized void addPrintable(Printable o, Color c, int layer)
	{}
	
	public synchronized boolean removePrintable(Printable o)
	{
		return true;
	}

	public synchronized void addPlottable(Plottable p)
	{}

	public synchronized boolean removePlottable(Plottable p)
	{
		return true;
	}
	
	public void refresh()
	{}
}
