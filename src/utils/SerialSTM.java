package utils;

import container.Service;
import enums.SerialProtocol;

public class SerialSTM extends SerialConnexion implements Service
{
	private byte[] question, reponse;
	
	public SerialSTM(Log log, int baudrate) {
		super(log, baudrate);
		
		// le problème du ping, c'est que SerialConnexion ne connaît pas l'id du paquet à envoyer
		// du coup, on met l'id 0. Comme ça, la STM pensera que c'est un vieux paquet mais y répondra quand même,
		// et ça ne posera pas problème.
		question = new byte[4];
		question[0] = 0;
		question[1] = 0;
		question[2] = SerialProtocol.OUT_PING.code;
		question[3] = (byte) ~question[2]; // checksum
		
		reponse = new byte[3];
		reponse[0] = SerialProtocol.IN_PONG1.code;
		reponse[1] = SerialProtocol.IN_PONG2.code;
		reponse[2] = (byte) ~(SerialProtocol.IN_PONG1.code+SerialProtocol.IN_PONG2.code); // checksum

	}
	

	/**
	 * Ping de la carte.
	 * @return l'id de la carte
	 */
	public synchronized boolean ping()
	{
		try
		{
			//Evacuation de l'éventuel buffer indésirable
			output.flush();

			//ping
			output.write(question);

			// on laisse le temps au périphérique de réagir
			Sleep.sleep(50);
						
			byte[] lu = new byte[reponse.length];
			int nbLu = input.read(lu);
			
			// le +4 vient du fait qu'on ne vérifie pas l'id du paquet qui arrive ni l'entete
			if(nbLu != reponse.length + 4)	// vérification du nombre de byte lu
				return false;

			if(lu[0] != 0x55 || lu[1] != 0xAA) // vérification de l'entete
				return false;
			
			for(int i = 0; i < reponse.length; i++)
				if(reponse[i] != lu[i+4]) // on ne vérifie pas l'ID
					return false;
			
			log.debug("Série trouvée.");
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	protected synchronized void estimeLatence()
	{
		try {
			log.debug("Estimation de la latence…");
			long avant = System.currentTimeMillis();
			for(int i = 0; i < 10; i++)
			{
				output.write(question);
				while(input.available() == 0);
				input.skip(input.available());
			}
			log.debug("Latence de la série : "+((System.currentTimeMillis() - avant)*50)+" ns.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void afficheMessage(byte[] out)
	{
		String m = "";
		for(int i = 0; i < out.length; i++)
			m += Integer.toHexString(out[i]).toUpperCase()+" ";
		log.debug(m);
	}
	
	public synchronized void communiquer(byte[] out)
	{
		/**
		 * Un appel à une série fermée ne devrait jamais être effectué.
		 */
		if(isClosed)
		{
			log.debug("La série est fermée et ne peut envoyer :");
			afficheMessage(out);
			return;
		}

		try
		{
			if(Config.debugSerie)
				afficheMessage(out);

			output.write(0x55);
			output.write(0xAA);
			output.write(out);
		}
		catch (Exception e)
		{
			/**
			 * Si la STM ne répond vraiment pas, on recommence de manière infinie.
			 * De toute façon, on n'a pas d'autre choix...
			 */
			log.critical("Ne peut pas parler à la STM. Tentative de reconnexion.");
			while(!searchPort())
			{
				log.critical("Pas trouvé... On recommence");
				// On laisse la série respirer un peu
				Sleep.sleep(200);
			}
			// On a retrouvé la série, on renvoie le message
			communiquer(out);
		}
	}
	
	@Override
	public void useConfig(Config config)
	{}
	
	@Override
	public void updateConfig(Config config)
	{}

}
