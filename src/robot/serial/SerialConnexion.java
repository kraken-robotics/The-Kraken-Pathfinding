package robot.serial;

import exceptions.FinMatchException;
import exceptions.SerialConnexionException;
import exceptions.SerialManagerException;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import utils.Log;
import container.Service;
import container.ServiceNames;

/**
 * Une connexion série
 * @author kayou
 *
 */

public class SerialConnexion implements SerialPortEventListener, Service
{
	private SerialPort serialPort;
	protected Log log;
	protected String name;
	private boolean isClosed = false;

	/**
	 * Construction pour les séries trouvées
	 * @param log
	 * @param name
	 */
	SerialConnexion (Log log, ServiceNames name)
	{
		this(log, name.toString());
	}

	/**
	 * Constructeur pour la série de test
	 * @param log
	 * @param name
	 */
	SerialConnexion (Log log, String name)
	{
		this.log = log;
		this.name = name;
	}

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
	 * Appelé par le SerialManager, il donne à la série tout ce qu'il faut pour fonctionner
	 * @param port_name
	 * 					Le port où est connecté la carte
	 * @param baudrate
	 * 					Le baudrate que la carte utilise
	 * @throws SerialManagerException 
	 * @throws SerialConnexionException 
	 */
	void initialize(String port_name, int baudrate) throws SerialConnexionException
	{
		CommPortIdentifier portId = null;
		try
		{
			portId = CommPortIdentifier.getPortIdentifier(port_name);
			serialPort = (SerialPort) portId.open("TechTheTroll", TIME_OUT);
			// set port parameters
			serialPort.setSerialPortParams(baudrate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// Configuration du Listener
			try {
				serialPort.addEventListener(this);
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}
			serialPort.notifyOnDataAvailable(true);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			/*
			 * A tester, permet d'avoir un readLine non bloquant! (valeur à rentrée en ms)
			 */
			serialPort.enableReceiveTimeout(1000);
		}
		catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e2)
		{
			throw new SerialConnexionException();
		}
	}

	/**
	 * Méthode pour parler à l'avr
	 * @param messages Messages à envoyer
	 * @param nb_lignes_reponse Nombre de lignes que l'avr va répondre (sans compter les acquittements)
	 * @return Un tableau contenant le message
	 * @throws SerialConnexionException 
	 * @throws FinMatchException 
	 */
	public String[] communiquer(String message, int nb_lignes_reponse) throws SerialConnexionException, FinMatchException
	{
		String[] messages = {message};
		return communiquer(messages, nb_lignes_reponse);
	}
	
	/**
	 * Méthode pour parler à l'avr
	 * @param messages Messages à envoyer
	 * @param nb_lignes_reponse Nombre de lignes que l'avr va répondre (sans compter les acquittements)
	 * @return Un tableau contenant le message
	 * @throws SerialConnexionException 
	 * @throws FinMatchException 
	 */
	public synchronized String[] communiquer(String[] messages, int nb_lignes_reponse) throws SerialConnexionException, FinMatchException
	{
		if(isClosed)
			throw new FinMatchException();
		
		String inputLines[] = new String[nb_lignes_reponse];
		try
		{
			for (String m : messages)
			{
				m += "\r";
				output.write(m.getBytes());
				int nb_tests = 0;
				char acquittement;

				while(nb_tests < 10)
				{
					nb_tests++;

					while(!input.ready());
					acquittement = input.readLine().charAt(0);

					if (acquittement != '_')
						output.write(m.getBytes());
					else
						break;
				}
				if(nb_tests == 10)
					log.critical("La série" + this.name + " ne répond plus", this);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.critical("Ne peut pas parler à la carte " + this.name, this);
			throw new SerialConnexionException();
		}

		try
		{
			for (int i = 0 ; i < nb_lignes_reponse; i++)
			{
				while(!input.ready());
				inputLines[i] = input.readLine();
			}
		}
		catch (Exception e)
		{
			log.critical("Ne peut pas parler à la carte " + this.name, this);
			throw new SerialConnexionException();
		}

		return inputLines;
	}

	/**
	 * Doit être appelé quand on arrête de se servir de la série
	 */
	public void close()
	{
		if (!isClosed && serialPort != null)
		{
			log.debug("Fermeture de "+name, this);
			serialPort.close();
			isClosed = true;
		}
	}
	
	/**
	 * Lit sur la série. Cet appel doit être fait après la notification de données disponibles
	 * @throws IOException
	 */
	public synchronized String[] read(int n) throws IOException
	{
		String[] output = new String[n];

		while(!input.ready());
		
		for(int i = 0; i < n; i++)
			output[i] =input.readLine();

		return output;
	}

	/**
	 * Handle an event on the serial port.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent)
	{
		notify();
	}

	/**
	 * Ping de la carte.
	 * Utilisé que par createSerial de SerialManager
	 * @return l'id de la carte
	 */
	public synchronized String ping()
	{
		String ping = null;
		try
		{
		
			//Evacuation de l'eventuel buffer indésirable
			output.write("$0P@L1Z7\r".getBytes());
			//Evacuation de l'acquittement
			while(!input.ready());
			input.readLine();
		
			//ping
			output.write("?\r".getBytes());
			//evacuation de l'acquittement
			while(!input.ready());
			input.readLine();

			//recuperation de l'id de la carte
			while(!input.ready());
			ping = input.readLine();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ping;
	}
	
	public void updateConfig()
	{
	}

}
