package threads;

import serie.BufferOutgoingOrder;
import serie.SerieCoucheTrame;
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
	private SerieCoucheTrame serie;
	private BufferOutgoingOrder data;
	private int sleep;
	
	public ThreadSerialOutput(Log log, SerieCoucheTrame serie, BufferOutgoingOrder data)
	{
		this.log = log;
		this.serie = serie;
		this.data = data;
	}

	@Override
	public void run()
	{
		Order message;
		
		// On envoie d'abord le ping long initial
		Order initPing = data.getInitialLongPing();
		try {
			synchronized(serie)
			{
				serie.sendOrder(initPing);
				serie.wait(); // on est notifié dès qu'on reçoit quelque chose sur la série
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while(true)
		{
			try {
				synchronized(data)
				{
					if(data.isEmpty()) // pas de message ? On attend
						data.wait(500);
//						data.wait();

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
