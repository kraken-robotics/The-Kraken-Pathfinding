package utils;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import container.Service;

/**
 * La connexion série vers la STM ou la XBEE
 * @author pf
 *
 */

public class SerialConnexion implements SerialPortEventListener, Service
{
	private SerialPort serialPort;
	protected Log log;
	
	private boolean isClosed;
	private int baudrate;
	private int canBeRead = 0;
	private String question, reponse;
	
	private byte[] retourLigne = new String("\r").getBytes();
	
	/**
	 * A BufferedReader which will be fed by a InputStreamReader 
	 * converting the bytes into characters 
	 * making the displayed results codepage independent
	 */
	private BufferedReader input;

	/** The output stream to the port */
	private OutputStream output;

	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;

	/**
	 * Constructeur pour la série de test
	 * @param log
	 */
	public SerialConnexion(Log log, String question, String reponse, int baudrate)
	{
		this.log = log;
		this.question = question;
		this.reponse = reponse;
		this.baudrate = baudrate;
		if(!searchPort())
		{
			/**
			 * Suppression des verrous qui empêchent parfois la connexion
			 */
			try {
				log.critical("Port série non trouvé, suppression des verrous");
				Runtime.getRuntime().exec("sudo rm -f /var/lock/LCK..tty*");
			} catch (IOException e) {
				e.printStackTrace();
			}
			while(!searchPort())
			{
				log.critical("Port série non trouvé, réessaie dans 500 ms");
				Sleep.sleep(500);
			}
		}
	}

	private synchronized boolean searchPort()
	{
		log.debug("Recherche de la série à "+baudrate+" baud");
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();

		while(ports.hasMoreElements())
		{
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();

			log.debug("Essai du port " + port.getName());
			
			// Test du port
			if(!initialize(port, baudrate))
				continue;
			
			if(ping())
			{
				log.debug("STM sur " + port.getName());
				// Il ne faut activer le listener que maintenant, sinon
				// ça pose des problèmes avec le ping
				serialPort.notifyOnDataAvailable(true);
//				notifyAll();
				return true;
			}
			else
				log.debug(port.getName()+": non");
				
			// Ce n'est pas cette série, on la ferme donc
			serialPort.close();
		}
		// La série n'a pas été trouvée
		return false;
	}
	
	/**
	 * Il donne à la série tout ce qu'il faut pour fonctionner
	 * @param port_name
	 * 					Le port où est connecté la carte
	 * @param baudrate
	 * 					Le baudrate que la carte utilise
	 */
	private boolean initialize(CommPortIdentifier portId, int baudrate)
	{
		try
		{
			serialPort = (SerialPort) portId.open("TechTheTroll", TIME_OUT);
			// set port parameters
			serialPort.setSerialPortParams(baudrate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			serialPort.enableReceiveTimeout(100);
			// Configuration du Listener
			try {
				serialPort.addEventListener(this);
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
			
			isClosed = false;
			return true;
		}
		catch (PortInUseException | UnsupportedCommOperationException | IOException e2)
		{
			log.critical(e2);
			return false;
		}
	}
	
	/**
	 * Méthode pour envoyer un message à la carte
	 * @param messages
	 */
	public synchronized void communiquer(byte[] out)
	{
		/**
		 * Un appel à une série fermée ne devrait jamais être effectué.
		 */
		if(isClosed)
		{
			log.debug("La série est fermée et ne peut envoyer "+out);
			return;
		}

		try
		{
			if(Config.debugSerie)
				log.debug("OUT: "+out);

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

	/**
	 * Doit être appelé quand on arrête de se servir de la série
	 */
	public synchronized void close()
	{
		if (!isClosed && serialPort != null)
		{
			log.debug("Fermeture de la carte");
			serialPort.close();
			isClosed = true;
		}
		else if(isClosed)
			log.warning("Carte déjà fermée");
		else
			log.warning("Carte jamais ouverte");
	}

	/**
	 * Lit une ligne.
	 * @return
	 */
	public synchronized String read()
	{
		try {
			/**
			 * On sait qu'une donnée arrive, donc l'attente est très faible.
			 */
			if(!input.ready()) // série pas prête ? (au cas où)
				return "";

			canBeRead--;
			String m = input.readLine();
			if(Config.debugSerie)
				log.debug("IN: "+m);
			return m;
		} catch (IOException e) {
			// Impossible car on sait qu'il y a des données
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Gestion d'un évènement sur la série.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent)
	{
		try {
			if(input.ready())
			{
				canBeRead++;
				notify();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean canBeRead()
	{
		return canBeRead > 0;
	}

	/**
	 * Ping de la carte.
	 * Utilisé que par createSerial de SerialManager
	 * @return l'id de la carte
	 */
	public synchronized boolean ping()
	{
		try
		{		
			//Evacuation de l'éventuel buffer indésirable
			output.flush();

			//ping
			output.write(question.getBytes());
			output.write(retourLigne);

			//recuperation de l'id de la carte avec un timeout
			long timeout = 500;
			long debut = System.currentTimeMillis();
			while(!input.ready())
				if(System.currentTimeMillis() - debut > timeout)
					return false;
			
			String lu = input.readLine().trim();
//			log.debug("Lu : "+lu+", attendu : "+reponse);
			if(lu.compareTo(reponse) == 0)
			{
				log.debug("Série trouvée. Estimation de la latence…");
				long avant = System.currentTimeMillis();
				for(int i = 0; i < 10; i++)
				{
					output.write(question.getBytes());
					output.write(retourLigne);
					while(!input.ready());
					input.readLine();
				}
				log.debug("Latence de la série : "+((System.currentTimeMillis() - avant)*50)+" ns.");
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void useConfig(Config config)
	{

	}
	
	@Override
	public void updateConfig(Config config)
	{}
}
