package threads;

import serie.DataForSerialOutput;
import serie.SerialLowLevel;
import serie.trame.Order;
import utils.Config;
import utils.ConfigInfo;
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
	private SerialLowLevel serie;
	private DataForSerialOutput data;
	private int sleep;
	
	public ThreadSerialOutput(Log log, SerialLowLevel serie, DataForSerialOutput data)
	{
		this.log = log;
		this.serie = serie;
		this.data = data;
	}

	@Override
	public void run()
	{
		Order message;
		while(true)
		{
			try {
				synchronized(data)
				{
					if(data.isEmpty()) // pas de message ? On attend
						data.wait(500);

					if(data.isEmpty()) // si c'est le timeout qui nous a réveillé, on envoie un ping
					{
						message = data.getPing();
						if(Config.debugSerie)
							log.debug("Envoi d'un ping pour vérifier la connexion");
					}
					else
						message = data.poll();
				}
				serie.sendOrder(message);
				Sleep.sleep(sleep); // laisse un peu de temps entre deux trames si besoin est
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		sleep = config.getInt(ConfigInfo.SLEEP_ENTRE_TRAMES);
	}

}
