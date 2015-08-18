package threads;

import java.util.ArrayList;

import buffer.DataForSerialOutput;
import serial.SerialConnexion;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread qui vérifie s'il faut envoyer des choses sur la série
 * @author pf
 *
 */

public class ThreadSerialOutput extends Thread implements Service
{
	protected Log log;
	protected Config config;
	private SerialConnexion serie;
	private DataForSerialOutput data;
	
	public ThreadSerialOutput(Log log, Config config, SerialConnexion serie, DataForSerialOutput data)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.data = data;
	}

	@Override
	public void run()
	{
		ArrayList<String> message;
		while(true)
		{
			try {
				synchronized(data)
				{
					while(data.isEmpty())
					{
						data.wait(500);
					}
					message = data.poll();
				}
				// communiquer est synchronized
				serie.communiquer(message);

			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
		}
//		log.debug("Fermeture de ThreadSerialOutput");
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
