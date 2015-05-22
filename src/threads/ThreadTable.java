package threads;

import buffer.IncomingHook;
import buffer.IncomingHookBuffer;
import table.Table;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread de la table. Surveille IncomingHookBuffer
 * @author pf
 *
 */

// TODO: la table doit être surveillée

public class ThreadTable extends ThreadAvecStop implements Service
{
	private IncomingHookBuffer buffer;
	private Table table;
	protected Log log;
	
	public ThreadTable(Log log, IncomingHookBuffer buffer, Table table)
	{
		this.log = log;
		this.buffer = buffer;
		this.table = table;
	}
	
	@Override
	public void run()
	{
		while(!finThread)
		{
			IncomingHook e = null;
			synchronized(buffer)
			{
				try {
					buffer.wait();
					log.debug("Réveil de ThreadTable");
					e = buffer.poll();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			// Cet appel peut lancer un obstaclemanager.notifyAll()
			// Il n'est pas synchronized car il ne modifie pas le buffer
//			if(e != null)
//				table.apply(e); // TODO
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
