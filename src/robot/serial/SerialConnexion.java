package robot.serial;

import enums.ServiceNames;
import exceptions.FinMatchException;
import exceptions.serial.SerialConnexionException;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import utils.Log;
import container.Service;

public class SerialConnexion implements SerialPortEventListener, Service
{
	private SerialPort serialPort;
	protected Log log;
	protected String name;
	private boolean isClosed = false;

	SerialConnexion (Log log, ServiceNames name)
	{
		this(log, name.toString());
	}

	SerialConnexion (Log log, String name)
	{
		super();
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
	 */
	void initialize(String port_name, int baudrate)
	{
		CommPortIdentifier portId = null;
		try
		{
			portId = CommPortIdentifier.getPortIdentifier(port_name);
		}
		catch (NoSuchPortException e2)
		{
			e2.printStackTrace();
		}

		// open serial port, and use class name for the appName.
		try {
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);
		} 
		catch (PortInUseException e1)
		{
			e1.printStackTrace();
		}
		try
		{
			// set port parameters
			serialPort.setSerialPortParams(baudrate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

		}
		catch (Exception e)
		{
			System.err.println(e.toString());
		}
		
		/*
		 * A tester, permet d'avoir un readLine non bloquant! (valeur à rentrée en ms)
		 */
		try {
			serialPort.enableReceiveTimeout(1000);
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Méthode pour parler à l'avr
	 * @param message
	 * 					Message à envoyer
	 * @param nb_lignes_reponse
	 * 					Nombre de lignes que l'avr va répondre (sans compter les acquittements)
	 * @return
	 * 					Un tableau contenant le message
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
	 * @param messages
	 * 					Messages à envoyer
	 * @param nb_lignes_reponse
	 * 					Nombre de lignes que l'avr va répondre (sans compter les acquittements)
	 * @return
	 * 					Un tableau contenant le message
	 * @throws SerialConnexionException 
	 * @throws FinMatchException 
	 */
	public String[] communiquer(String[] messages, int nb_lignes_reponse) throws SerialConnexionException, FinMatchException
	{
		if(isClosed)
			throw new FinMatchException();
		
		synchronized(output)
		{
			String inputLines[] = new String[nb_lignes_reponse];
			try
			{
				for (String m : messages)
				{
					m += "\r";
					output.write(m.getBytes());
					int nb_tests = 0;
					char acquittement = ' ';
	
					while (acquittement != '_')
					{
						nb_tests++;
						acquittement = input.readLine().charAt(0);
						if (acquittement != '_')
						{
							output.write(m.getBytes());
						}
						if (nb_tests > 10)
						{
							log.critical("La série" + this.name + " ne répond pas après " + nb_tests + " tentatives", this);
							break;
						}
					}
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
	 * Handle an event on the serial port.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent)
	{
	}

	/**
	 * Ping de la carte.
	 * Utilisé que par createSerial de SerialManager
	 * @return l'id de la carte
	 */
	synchronized String ping()
	{
		synchronized(output) {
			String ping = null;
			try
			{
			
				//Evacuation de l'eventuel buffer indésirable
				output.write("dfsfdsgdfgfd\r".getBytes());
				//Evacuation de l'acquittement
				input.readLine();
			
				//ping
				output.write("?\r".getBytes());
				//evacuation de l'acquittement
				input.readLine();
	
				//recuperation de l'id de la carte
				ping = input.readLine();
	
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return ping;
		}
	}
	
	public void updateConfig()
	{
	}

}
