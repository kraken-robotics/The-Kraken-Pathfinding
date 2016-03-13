package entryPoints;

import org.jfree.ui.RefineryUtilities;

import utils.Config;
import utils.ConfigInfo;
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
		Config config = (Config) container.getService(ServiceNames.CONFIG);
		synchronized(config)
		{
			config.set(ConfigInfo.MATCH_DEMARRE, true);
			config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		}
		int nb = 0;
		long avant = System.currentTimeMillis();
		while(true)
		{
			synchronized(buffer)
			{
				if(buffer.isEmpty())
					buffer.wait();
				aff.add(buffer.poll());
//				aff.add(new IncomingDataDebug((new Random()).nextInt(10), (new Random()).nextInt(7), 7, 2, 1, 2, 5, 6));
			}
			nb++;
			if(nb % 100 == 0)
				System.out.println((System.currentTimeMillis() - avant)/((double)nb));
		}
	}
}
