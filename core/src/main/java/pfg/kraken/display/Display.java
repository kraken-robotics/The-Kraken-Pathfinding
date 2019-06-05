package pfg.kraken.display;

import java.awt.Color;
import java.awt.image.ImageObserver;
import java.util.PriorityQueue;
import pfg.kraken.struct.XY;

public interface Display extends ImageObserver
{
	public void refresh();
	public void updatePrintable(PriorityQueue<ColoredPrintable> l);
	public void clearTemporaryPrintables();
	public void addTemporaryPrintable(Printable o, Color c, int layer);
	public void addPrintable(Printable o, Color c, int layer);
	public boolean removePrintable(Printable o);
	

	public XY getCurrentCoinHautDroite();
	
	public XY getCurrentCoinBasGauche();
	
	public int distanceXtoWindow(int dist);

	public int distanceYtoWindow(int dist);

	public int XtoWindow(double x);

	public int YtoWindow(double y);

}
