package threads;

import container.Service;
import table.GridSpace;
import table.GridSpaceWayPoints;
import table.Table;
import utils.Config;
import utils.Log;

/**
 * S'occupe de la mise à jour du cache. Surveille la table
 * @author pf
 *
 */

public class ThreadGridSpace2 extends Thread implements Service {

	protected Log log;
	private Table table;
	private GridSpace gridspace;
	
	public ThreadGridSpace2(Log log, Table table, GridSpace gridspace)
	{
		this.log = log;
		this.table = table;
		this.gridspace = gridspace;
	}

	@Override
	public void run()
	{
		while(true)
		{
			synchronized(table)
			{
				try {
					table.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			log.debug("Réveil de ThreadGridSpace");	
			
//			gridspace.reinitConnections();
		}
//		log.debug("Fermeture de ThreadGridSpace2");

	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
