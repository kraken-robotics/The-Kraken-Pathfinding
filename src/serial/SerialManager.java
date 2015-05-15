package serial;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import gnu.io.CommPortIdentifier;

import java.util.Enumeration;

import container.Service;
import exceptions.SerialConnexionException;
import exceptions.SerialManagerException;

/**
 * Instancie la série STM
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
		log.debug("Recherche de la série à "+baudrate+" baud");
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		while(ports.hasMoreElements())
		{
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();

			//Creation d'une serie de test
			serie = new SerialConnexion(log);
			try {
				serie.initialize(port, baudrate);
			} catch (SerialConnexionException e) {
				throw new SerialManagerException();
			}
			
			if(serie.ping())
			{
				log.debug("STM sur " + port.getName());
				return;
			}
			else
				log.debug(port.getName()+": non");
				
			// Ce n'est pas cette série, on la ferme donc
			serie.close();
		}
		// La série n'a pas été trouvée
		throw new SerialManagerException();
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
