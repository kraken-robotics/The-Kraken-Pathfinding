package threads;

import container.Service;
import serie.BufferIncomingOrder;
import serie.SerialInterface;
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
	protected Config config;
	private SerieCoucheTrame serie;
	private BufferIncomingOrder buffer;
	private SerialInterface seriePhysique;
	
	public ThreadSerialInputCoucheTrame(Log log, Config config, SerieCoucheTrame serie, SerialInterface seriePhysique, BufferIncomingOrder buffer)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		this.seriePhysique = seriePhysique;
	}

	@Override
	public void run()
	{
		while(true)
		{
			try {
				synchronized(seriePhysique)
				{
					// dès que la série reçoit un octet, on demande au bas niveau de le traiter
					if(seriePhysique.available())
						buffer.add(serie.readData());
					else
						seriePhysique.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
