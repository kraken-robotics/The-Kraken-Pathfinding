package robot.cardsWrappers;

import robot.serial.SerialConnexion;
import utils.Log;
import utils.Config;
import container.Service;
import enums.ActuatorOrder;
import exceptions.FinMatchException;
import exceptions.serial.SerialConnexionException;


/**
 * Utilisée par robot pour bouger les actionneurs.
 * @author pf
 */
public class ActuatorCardWrapper implements Service
{
	
	// pour parler aux cartes du robot
	private Log log;
	private SerialConnexion serie;

	public ActuatorCardWrapper(Config config, Log log, SerialConnexion serie)
	{
		this.log = log;
		this.serie = serie;		
	}

	public void updateConfig()
	{
	}
	
	/**
	 * Envoie un ordre à la série. Le protocole est défini dans l'enum ActuatorOrder
	 * @param order
	 * @throws SerialConnexionException
	 * @throws FinMatchException 
	 */
	public void useActuator(ActuatorOrder order) throws SerialConnexionException, FinMatchException
	{
		log.debug("Ordre série: "+order.toString(), this);
		serie.communiquer(order.getSerialOrder(), 0);
	}

}
