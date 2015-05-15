package threads;

import java.util.LinkedList;
import java.util.Queue;

import utils.Config;
import utils.Log;
import container.Service;

/**
 * Buffer qui contient les infos provenant des capteurs de la STM
 * @author pf
 *
 */

public class IncomingDataBuffer implements Service
{
	protected Config config;
	protected Log log;
	
	public IncomingDataBuffer(Log log, Config config)
	{
		this.log = log;
		this.config = config;
	}
	
	private Queue<IncomingData> buffer = new LinkedList<IncomingData>();
	
	public void add(IncomingData elem)
	{
		buffer.add(elem);
		synchronized(this)
		{
			notifyAll();
		}
	}

	public IncomingData poll()
	{
		return buffer.poll();
	}
	
	@Override
	public void updateConfig()
	{}
	
}
