package robot.cardsWrappers;

import robot.cardsWrappers.enums.ActuatorOrder;
import robot.serial.SerialConnexion;
import utils.Log;
import utils.Config;
import container.Service;
import exceptions.FinMatchException;
import exceptions.SerialConnexionException;

/**
 * Surcouche user-friendly pour parler a la carte actionneurs.
 * Utilisée par le package robot pour bouger les actionneurs.
 * @author pf
 */
public class ActuatorCardWrapper implements Service
{

	protected Log log;
	private SerialConnexion actuatorCardSerial;

	/**
	 * Construit la surchouche de la carte actionneurs
	 * @param config
	 * @param log  
	 * @param serial la connexion série avec la STM
	 */
	public ActuatorCardWrapper(Config config, Log log, SerialConnexion serial)
	{
		this.log = log;
		this.actuatorCardSerial = serial;
		updateConfig();
	}

	@Override
	public void updateConfig()
	{
	}
	
	/**
	 * Envoie un ordre à la série. Le protocole est défini dans l'enum ActuatorOrder
	 * @param order l'ordre à envoyer
	 * @throws SerialConnexionException en cas de problème de communication avec la carte actionneurs
	 * @throws FinMatchException 
	 */
	public void useActuator(ActuatorOrder order) throws SerialConnexionException, FinMatchException
	{
		actuatorCardSerial.communiquer(order.getSerialOrder(), 0);
	}

	public void close()
	{
		actuatorCardSerial.close();
	}

}
