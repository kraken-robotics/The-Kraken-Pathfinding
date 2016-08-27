package threads;

import serie.BufferOutgoingOrder;
import serie.SerieCoucheTrame;
import serie.trame.Order;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import container.Service;
import enums.SerialProtocol.OutOrder;

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
		try {
			synchronized(serie)
			{
				serie.init();
				Sleep.sleep(50); // on attend que la série soit bien prête
				serie.sendOrder(new Order(OutOrder.PING));
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
					/**
					 * Pour désactiver le ping automatique, remplacer "data.wait(500)" par "data.wait()"
					 */
						
					if(data.isEmpty()) // pas de message ? On attend
//						data.wait(500);
						data.wait();

					if(data.isEmpty()) // si c'est le timeout qui nous a réveillé, on envoie un ping
					{
						message = new Order(OutOrder.PING);
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
