package robot.serial;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import gnu.io.CommPortIdentifier;

import java.util.Enumeration;

import container.Service;
import exceptions.SerialConnexionException;
import exceptions.SerialManagerException;

/**
 * Instancie toutes les s�ries, si on lui demande gentillement!
 * @author pierre
 * @author pf
 *
 */
public class SerialManager implements Service
{
	// Dépendances
	private Log log;

	private SerialConnexion serie = null;
	private int baudrate;
	
	/**
	 * Recuperation de toutes les cartes dans cards et des baudrates dans baudrate
	 * @param config 
	 */
	public SerialManager(Log log, Config config)
	{
		this.log = log;
		baudrate = config.getInt(ConfigInfo.BAUDRATE);
	}

	/**
	 * Création de la série
	 */
	public void createSerial() throws SerialManagerException
	{
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements())
		{
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();

			//Creation d'une serie de test
			serie = new SerialConnexion(log, "STM32");
			try {
				serie.initialize(port.getName(), baudrate);
			} catch (SerialConnexionException e) {
				throw new SerialManagerException();
			}
			
			String ping = serie.ping();
			if(ping == "T3")
			{
				log.debug("STM sur: " + port.getName());
				break;
			}
				
			// Ce n'est pas cette série, on la ferme donc
			serie.close();
		}
	}

	/**
	 * Renvoie la série
	 * @return
	 * @throws SerialManagerException 
	 */
	public SerialConnexion getSerial() throws SerialManagerException
	{
		if(serie == null)
			createSerial();
		return serie;
	}
	
	/**
	 * Ferme la série
	 */
	public void close()
	{
		if(serie != null)
			serie.close();
	}

	@Override
	public void updateConfig()
	{
		
	}
}
