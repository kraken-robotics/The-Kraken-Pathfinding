package robot.serial;
import utils.Config;
import utils.Log;
import enums.ServiceNames;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import container.Service;
import exceptions.serial.SerialManagerException;

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

	//Series a instancier
	public SerialConnexion serieAsservissement = null;
	public SerialConnexion serieCapteursActionneurs = null;

	//On stock les series dans une liste
	private SerialConnexion[] series = new SerialConnexion[2];

	//Pour chaque carte, on connait a l'avance son nom, son ping et son baudrate
	private SpecificationCard carteAsservissement = new SpecificationCard(ServiceNames.SERIE_ASSERVISSEMENT, 0, 9600);
	private SpecificationCard carteCapteursActionneurs = new SpecificationCard(ServiceNames.SERIE_CAPTEURS_ACTIONNEURS, 3, 9600);

	//On stock les cartes dans une liste
	private ArrayList <SpecificationCard> cards = new ArrayList <SpecificationCard>();

	//Liste pour stocker les series qui sont connectees au pc 
	private ArrayList<String> connectedSerial = new ArrayList<String>();

	//Liste pour stocker les baudrates des differentes serie

	private ArrayList<Integer> baudrate = new ArrayList<Integer>();
	
	/**
	 * Recuperation de toutes les cartes dans cards et des baudrates dans baudrate
	 * @param config 
	 */
	public SerialManager(Log log, Config config) throws SerialManagerException
	{
		this.log = log;

		cards.add(this.carteAsservissement);
		cards.add(this.carteCapteursActionneurs);

		Iterator<SpecificationCard> e = cards.iterator();
		while (e.hasNext())
		{
			int baud = e.next().baudrate;
			if (!this.baudrate.contains(baud))
				this.baudrate.add(baud);
		}

		this.serieAsservissement = new SerialConnexion(log, this.carteAsservissement.name);
		this.serieCapteursActionneurs = new SerialConnexion(log, this.carteCapteursActionneurs.name);

		this.series[this.carteAsservissement.name.getNbSerie()] = this.serieAsservissement;
		this.series[this.carteCapteursActionneurs.name.getNbSerie()] = this.serieCapteursActionneurs;
		
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
			this.connectedSerial.add(port.getName());
		}
	}
	/**
	 * Création des series (il faut au prealable faire un checkSerial())
	 */
	public void createSerial() throws SerialManagerException
	{
		int id = -1;
		//Liste des series deja attribues
		ArrayList<Integer> deja_attribues = new ArrayList<Integer>();
		String pings[] = new String[20];
		for (int baudrate : this.baudrate)
		{
			log.debug("liste des pings pour le baudrate " + baudrate, this);

			for(int k = 0; k < this.connectedSerial.size(); k++)
			{
				if (!deja_attribues.contains(k))
				{
					//Creation d'une serie de test
					SerialConnexion serialTest = new SerialConnexion(log, "Carte de test");
					serialTest.initialize(this.connectedSerial.get(k), baudrate);
					
					
					if(serialTest.ping() != null)
						id = Integer.parseInt(serialTest.ping());
					else 
					{
						serialTest.close();
						continue;
					}

					if(!isKnownPing(id))
					{
						serialTest.close();
						continue;
					}

					if (!goodBaudrate(baudrate, id))
					{
						serialTest.close();
						continue;
					}
					//On stock le port de la serie (connectedSerial) dans le tabeau à la case [id]
					pings[id] = this.connectedSerial.get(k);

					//Après les tests de pings sur la serie, on ferme la communication
					serialTest.close();

					deja_attribues.add(k);

					log.debug(id + " sur: " + connectedSerial.get(k),this);

				}
			}
		}

		//Association de chaque serie a son port
		Iterator<SpecificationCard> e = cards.iterator();
		while (e.hasNext())
		{
			SpecificationCard serial = e.next();
			if(serial.id == 0 && pings[serial.id] != null)
			{
				this.serieAsservissement.initialize(pings[serial.id], serial.baudrate);
			}
			else if(serial.id == 3 && pings[serial.id] != null)
			{
				this.serieCapteursActionneurs.initialize(pings[serial.id], serial.baudrate);
			}	

			if (pings[serial.id] == null)
			{
				log.critical("La carte " + serial.name + " n'est pas détectée", this);
				throw new SerialManagerException();
			}
		}
	}
	
	/**
	 * 
	 * @param baudrate
	 * @param id
	 * @return
	 */
	private boolean goodBaudrate(int baudrate, int id)
	{
		Iterator<SpecificationCard> e = cards.iterator();
		while(e.hasNext())
		{
			SpecificationCard serial = e.next();
			if((id == serial.id) && (baudrate == serial.baudrate))
				return true;
		}
		return false;
	}

	/**
	 * Permet de savoir si une carte a déjà été pingée, utilisé que par SerialManager
	 * @param id
	 * @return
	 */
	private boolean isKnownPing(int id)
	{
		Iterator<SpecificationCard> e = cards.iterator();
		while(e.hasNext())
		{
			if(id == e.next().id)
				return true;
		}
		return false;
	}

	/**
	 * Permet d'obtenir une série
	 * @param name
	 * 				Nom de la série
	 * @return
	 * 				L'instance de la série
	 */
	public SerialConnexion getSerial(ServiceNames name)	throws SerialManagerException
	{
		if (this.series[name.getNbSerie()] != null)
		{
			return this.series[name.getNbSerie()];
		}
		else
		{
			log.critical("Aucune série du nom : " + name.toString() + " n'existe", this);
			log.critical("Vérifiez les branchements ou l'interface+simulateur (redémarrez si besoin).", this);
			log.critical("Vérifiez aussi que tous les processus Java exécutant ce code sont éteints.", this);
			throw new SerialManagerException("serie non trouvée");
		}
	}
	
	public void closeAll()
	{
		for(SerialConnexion s: series)
			s.close();
	}

	@Override
	public void updateConfig() {
	}
}
