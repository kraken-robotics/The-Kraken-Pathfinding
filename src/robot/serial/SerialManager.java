package robot.serial;
import utils.Config;
import utils.Log;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;

import container.Service;
import container.ServiceNames;
import container.ServiceNames.TypeService;
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

	//On stock les series dans une liste
	private SerialConnexion[] series = new SerialConnexion[10];

	//On stock les cartes dans une liste
	private SpecificationCard[] cards = new SpecificationCard[10];

	//Liste pour stocker les series qui sont connectees au pc 
	private ArrayList<String> connectedSerial = new ArrayList<String>();

	//Liste pour stocker les baudrates des differentes serie
	private ArrayList<Integer> baudrates = new ArrayList<Integer>();
	
	/**
	 * Recuperation de toutes les cartes dans cards et des baudrates dans baudrate
	 * @param config 
	 */
	public SerialManager(Log log, Config config) throws SerialManagerException
	{
		this.log = log;

		for(ServiceNames s: ServiceNames.values())
			if(s.getType() == TypeService.SERIE)
			{
				int baudrate = s.getBaudrate();
				cards[s.getNbSerie()] = new SpecificationCard(s, s.getNbSerie(), baudrate);
				series[s.getNbSerie()] = new SerialConnexion(log, s);
				if (!baudrates.contains(baudrate))
					baudrates.add(baudrate);
			}
		
		checkSerial();
		createSerial();
	}

	/**
	 * Regarde toutes les series qui sont branchees dans /dev/ttyUSB*
	 */
	public  void checkSerial()
	{
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements())
		{
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
			connectedSerial.add(port.getName());
		}
	}

	/**
	 * Création des series (il faut au préalable faire un checkSerial())
	 */
	public void createSerial() throws SerialManagerException
	{
		//Liste des series deja attribues
		ArrayList<Integer> deja_attribues = new ArrayList<Integer>();
		int pings[] = new int[20];
		for(int baudrate : baudrates)
		{
			log.debug("Liste des pings pour le baudrate " + baudrate, this);

			for(int k = 0; k < connectedSerial.size(); k++)
			{
				if (!deja_attribues.contains(k))
				{
					//Creation d'une serie de test
					SerialConnexion serialTest = new SerialConnexion(log, "Carte de test");
					try {
						serialTest.initialize(connectedSerial.get(k), baudrate);
					} catch (SerialConnexionException e) {
						throw new SerialManagerException();
					}
					
					if(serialTest.ping() != null)
					{
						int id = Integer.parseInt(serialTest.ping());
						if(cards[id] != null && cards[id].baudrate == baudrate)
						{
							pings[id] = k;
							deja_attribues.add(k);
							log.debug(id + " sur: " + connectedSerial.get(k),this);
						}
					}
						
					//Après les tests de pings sur la serie, on ferme la communication
					serialTest.close();
				}
			}
		}

		//Association de chaque serie a son port
		for(SpecificationCard serial: cards)
		{
			if(serial != null)
			{
				String port = connectedSerial.get(pings[serial.id]);
	
				if(port == null)
				{
					log.critical("La carte " + serial.name + " n'est pas détectée", this);
					throw new SerialManagerException();
				}
	
				try {
					series[serial.id].initialize(port, serial.baudrate);
				} catch (SerialConnexionException e) {
					throw new SerialManagerException();
				}
			}
		}
	}

	/**
	 * Permet d'obtenir une série
	 * @param name
	 * 				Nom de la série
	 * @return
	 * 				L'instance de la série
	 */
	public SerialConnexion getSerial(ServiceNames name)
	{
		return series[name.getNbSerie()];
	}
	
	public void closeAll()
	{
		for(SerialConnexion s: series)
			if(s != null)
				s.close();
	}

	@Override
	public void updateConfig()
	{}
}
