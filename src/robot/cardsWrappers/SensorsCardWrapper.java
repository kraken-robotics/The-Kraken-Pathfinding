package robot.cardsWrappers;

import java.io.IOException;

import robot.serial.SerialConnexion;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import vec2.ReadOnly;
import vec2.Vec2;
import container.Service;
import exceptions.FinMatchException;
import exceptions.SerialConnexionException;

/**
 * Classe simplifiant le dialogue avec les capteurs
 * @author PF
 */

public class SensorsCardWrapper implements Service
{
	// Dépendances
	private Log log;
	private SerialConnexion serie;
	private Config config;

	private boolean capteursOn = true;
	private int nbCapteurs;

	public SensorsCardWrapper(Config config, Log log, SerialConnexion serie)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		updateConfig();
	}
	
	public void updateConfig()
	{
		capteursOn = config.getBoolean(ConfigInfo.CAPTEURS_ON);
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
		log.updateConfig();
		serie.updateConfig();
	}

	/**
	 * Méthode bloquante
	 * Renvoie la liste des positions des obstacles vus par les capteurs
	 * @return renvoie la position brute puis position de l'ennemi, pour chaque capteur
	 * @throws FinMatchException */
	public Vec2<ReadOnly>[] mesurer() throws FinMatchException
	{
		@SuppressWarnings("unchecked")
		Vec2<ReadOnly>[] positions = (Vec2<ReadOnly>[]) new Vec2[2*nbCapteurs];
		if(!capteursOn)
    		return positions;
		String[] positionsString = new String[4*nbCapteurs];
		synchronized(serie)
		{
			try {
				serie.wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
				positionsString = serie.read(4*nbCapteurs);
				for(int i = 0; i < 2*nbCapteurs; i++)
					positions[i] = new Vec2<ReadOnly>(Integer.parseInt(positionsString[2*i]), Integer.parseInt(positionsString[2*i+1]));
				return positions;
			} catch (IOException e) {
				e.printStackTrace();
				return positions;
			}
		}
	}
	
	/**
	 * Le match a-t-il démarré? Demande à la STM l'état du jumper.
	 * @return
	 * @throws SerialConnexionException
	 * @throws FinMatchException
	 */
    public boolean demarrageMatch() throws SerialConnexionException, FinMatchException
    {
    	try {
    		return Integer.parseInt(serie.communiquer("j", 1)[0]) != 0;
    	}
    	catch(NumberFormatException e)
    	{
    		log.critical("Réponse du jumper non comprise", this);
    		return false;
    	}
    }
    
    /**
     * Active ou désactive les capteurs. Les capteurs sont désactivés avant le début du match.
     * @param capteurs_on
     */
    public void setCapteursOn(boolean capteursOn)
    {
    	this.capteursOn = capteursOn;
    }
     
}
