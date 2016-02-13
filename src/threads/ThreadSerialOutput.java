package threads;

import buffer.DataForSerialOutput;
import serie.SerialInterface;
import utils.Config;
import utils.Log;
import utils.Sleep;
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
	private SerialInterface serie;
	private DataForSerialOutput data;
	
	public ThreadSerialOutput(Log log, Config config, SerialInterface serie, DataForSerialOutput data)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.data = data;
	}

	@Override
	public void run()
	{
		byte[] message;
		while(true)
		{
			try {
				synchronized(data)
				{
					if(data.isEmpty()) // pas de message ? On attend
						data.wait(500);

					if(data.isEmpty()) // si c'est le timeout qui nous a réveillé, on envoie un ping
						message = data.getPing();
					else
						message = data.poll();
				}
				// communiquer est synchronized
				serie.communiquer(message);
				Sleep.sleep(5); // il faut un peu laisser la STM respirer…
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
