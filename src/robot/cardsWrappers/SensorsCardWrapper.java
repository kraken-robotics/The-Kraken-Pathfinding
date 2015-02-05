package robot.cardsWrappers;

import java.io.IOException;

import robot.serial.SerialConnexion;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import container.Service;
import exceptions.FinMatchException;
import exceptions.SerialConnexionException;

/**
 * Classe simplifiant le dialogue avec les capteurs
 * @author PF, marsu
 */

public class SensorsCardWrapper implements Service
{
	// TODO: faire en sortes que les capteurs renvoie toutes les valeurs, avant et arrière

	// Dépendances
	private Log log;
	private SerialConnexion serie;
	private Config config;

	private boolean capteurs_on = true;

	public SensorsCardWrapper(Config config, Log log, SerialConnexion serie)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		updateConfig();
	}
	
	public void updateConfig()
	{
		capteurs_on = config.getBoolean(ConfigInfo.CAPTEURS_ON);
	}

	/**
	-	 * Retourne la valeur la plus optimiste des capteurs de type capteur dans 
	-	 * la direction voulue
	-	 * Par rapport à la fonction suivante, c'est mieux de renvoyer séparément 
	-	 * les données des capteurs qund c'est pas du même type.
	-	 * @param capteur (soit "ir", soit "us")
	-	 * @return la valeur la plus optimiste des capteurs
	-	 
	 * @throws FinMatchException */
	public int mesurer() throws FinMatchException
	{
		if(!capteurs_on)
    		return 3000;
		String[] distances_string;
		synchronized(serie)
		{
			try {
				serie.wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
				distances_string = serie.read(1);
			} catch (IOException e) {
				e.printStackTrace();
				return 3000;
			}
		}
		
		int[] distances = new int[1];
		distances[0] = Integer.parseInt(distances_string[0]);
		
	    return distances[0];
	}
	
    public boolean demarrage_match() throws SerialConnexionException, FinMatchException
    {
    	try {
    		return Integer.parseInt(serie.communiquer("j", 1)[0]) != 0;
    	}
    	catch(Exception e)
    	{
    		log.critical("Aucune réponse du jumper", this);
    		return false;
    	}
    }
    
    public void setCapteursOn(boolean capteurs_on)
    {
    	this.capteurs_on = capteurs_on;
    }
     
}
