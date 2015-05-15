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
	public class Elem{}
	
	protected Config config;
	protected Log log;
	
	public IncomingDataBuffer(Log log, Config config)
	{
		this.log = log;
		this.config = config;
	}
	
	private Queue<Elem> buffer = new LinkedList<Elem>();
	
	public void add(Elem elem)
	{
		buffer.add(elem);
		buffer.notifyAll();
	}

	public Elem poll()
	{
		return buffer.poll();
	}
	
	@Override
	public void updateConfig()
	{}
	
}
