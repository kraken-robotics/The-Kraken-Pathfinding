package serial;

import exceptions.FinMatchException;
import exceptions.SerialConnexionException;
import exceptions.SerialManagerException;
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
import java.util.ArrayList;
import java.util.TooManyListenersException;

import planification.dstar.LocomotionNode;
import utils.Log;
import container.Service;

/**
 * Une connexion série
 * @author kayou
 * @author pf
 *
 */

public class SerialConnexion implements SerialPortEventListener, Service
{
	private SerialPort serialPort;
	protected Log log;
	private boolean isClosed = false;

	/**
	 * Constructeur pour la série de test
	 * @param log
	 * @param name
	 */
	SerialConnexion (Log log)
	{
		this.log = log;
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
	void initialize(CommPortIdentifier portId, int baudrate) throws SerialConnexionException
	{
		try
		{
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
		catch (PortInUseException | UnsupportedCommOperationException | IOException e2)
		{
			throw new SerialConnexionException();
		}
	}

	/**
	 * Envoie d'un itinéraire
	 * @param messages
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
	public synchronized void communiquer(ArrayList<LocomotionNode> messages) throws SerialConnexionException, FinMatchException
	{
		// TODO
	}

	/**
	 * Méthode pour envoyer un message à la carte
	 * @param messages
	 * @return
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
	public synchronized void communiquer(String[] messages) throws SerialConnexionException, FinMatchException
	{
		if(isClosed)
			throw new FinMatchException();
		
		try
		{
			for (String m : messages)
			{
				m += "\r";
				output.write(m.getBytes());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.critical("Ne peut pas parler à la carte");
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
	public synchronized String[] communiquer(String message, int nb_lignes_reponse) throws SerialConnexionException, FinMatchException
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
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.critical("Ne peut pas parler à la carte");
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
			log.critical("Ne peut pas parler à la carte");
			throw new SerialConnexionException();
		}

		return inputLines;
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
	}

	public synchronized String read()
	{
		try {
			while(!input.ready());
			return input.readLine();
		} catch (IOException e) {
			// Impossible car on sait qu'il y a des données
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * Lit sur la série. Cet appel doit être fait après la notification de données disponibles
	 * @throws IOException
	 */
	public synchronized ArrayList<String> readMore() throws IOException
	{
		ArrayList<String> output = new ArrayList<String>();

		while(!input.ready());
		int n = Integer.parseInt(input.readLine());
		for(int i = 0; i < n; i++)
			output.add(input.readLine());

		return output;
	}

	/**
	 * Gestion d'un évènement sur la série.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent)
	{
		notifyAll();
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
			//Evacuation de l'eventuel buffer indésirable
			output.flush();
//			output.write("$0P@L1Z7\r".getBytes());

			//ping
			output.write("?\r".getBytes());

			//recuperation de l'id de la carte
			while(!input.ready());
			return input.readLine().trim().compareTo("T3") == 0;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void updateConfig()
	{}

}
