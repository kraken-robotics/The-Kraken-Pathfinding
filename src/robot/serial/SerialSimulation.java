package robot.serial;

import utils.Log;
import enums.ServiceNames;
import exceptions.serial.SerialConnexionException;

/**
 * Si on n'arrive pas à instancier les séries...
 * Et bien on écrit dans la console.
 * Une petite surcharge qui peut dépanner.
 * @author pf
 *
 */

public class SerialSimulation extends SerialConnexion {

	SerialSimulation(Log log, ServiceNames name) {
		super(log, name);
		log.critical("Simulation de la carte "+name.toString()+"!", this);
	}

	SerialSimulation(Log log, String name) {
		super(log, name);
		log.critical("Simulation de la carte "+name+"!", this);
	}
	
	void initialize(String port_name, int baudrate)
	{}
	
	public void close()
	{}
	
	public String[] communiquer(String message, int nb_lignes_reponse)
	{
		log.debug(name+" envoie: "+message, this);
		return new String[nb_lignes_reponse]; 
	}

	public String[] communiquer(String[] messages, int nb_lignes_reponse) throws SerialConnexionException
	{
		for(int i = 0; i < messages.length; i++)
		{
			log.debug(name+" envoie: "+messages[i], this);
		}
		return new String[nb_lignes_reponse]; 
	}

}
