package pfg.kraken;

import java.awt.Color;
import java.io.ObjectOutputStream;

import pfg.graphic.PrintBuffer;
import pfg.graphic.printable.Printable;

public class PrintBufferPlaceholder extends PrintBuffer
{
	public synchronized void clearSupprimables()
	{}
	
	public synchronized void addSupprimable(Printable o, Color c, int layer)
	{}
	
	public synchronized void add(Printable o, Color c, int layer)
	{}
	
	public synchronized boolean removeSupprimable(Printable o)
	{
		return true;
	}
	
	public boolean needRefresh()
	{
		return false;
	}
	
	public synchronized void saveState()
	{}
	
	public synchronized void send(ObjectOutputStream out)
	{}
	
	public synchronized void destructor()
	{}
	
	public void refresh()
	{}
}
