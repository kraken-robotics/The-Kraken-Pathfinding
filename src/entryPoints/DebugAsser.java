package entryPoints;

import org.jfree.ui.RefineryUtilities;

import container.Container;
import container.ServiceNames;
import debug.AffichageDebug;
import debug.IncomingDataDebugBuffer;
import exceptions.ContainerException;
import exceptions.PointSortieException;

/**
 * Debug l'asser en affichant les grandeurs
 * @author pf
 *
 */

public class DebugAsser
{
	public static void main(String[] args) throws ContainerException, InterruptedException, PointSortieException
	{
		Container container = new Container();
		IncomingDataDebugBuffer buffer = (IncomingDataDebugBuffer) container.getService(ServiceNames.INCOMING_DATA_DEBUG_BUFFER);
		AffichageDebug aff = new AffichageDebug();
		aff.pack();
		RefineryUtilities.centerFrameOnScreen(aff);
		aff.setVisible(true);
		while(true)
		{
			synchronized(buffer)
			{
				buffer.wait();
				aff.add(buffer.poll());
//				aff.add(new IncomingDataDebug((new Random()).nextInt(10), (new Random()).nextInt(7), 7, 2, 1, 2, 5, 6));
			}
		}
	}
}
