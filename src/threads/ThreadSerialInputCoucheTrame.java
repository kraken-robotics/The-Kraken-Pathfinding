package threads;

import container.Service;
import serie.BufferIncomingOrder;
import serie.SerieCoucheTrame;
import utils.Config;
import utils.Log;

/**
 * Thread qui s'occupe de la partie bas niveau du protocole série
 * @author pf
 *
 */

public class ThreadSerialInputCoucheTrame extends Thread implements Service
{

	protected Log log;
	private SerieCoucheTrame serie;
	private BufferIncomingOrder buffer;
	
	public ThreadSerialInputCoucheTrame(Log log, SerieCoucheTrame serie, BufferIncomingOrder buffer)
	{
		this.log = log;
		this.serie = serie;
		this.buffer = buffer;
	}

	@Override
	public void run()
	{
		while(true)
			buffer.add(serie.readData());
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
