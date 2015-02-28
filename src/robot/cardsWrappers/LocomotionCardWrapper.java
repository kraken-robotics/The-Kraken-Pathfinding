package robot.cardsWrappers;

import robot.Speed;
import robot.serial.SerialConnexion;
import utils.*;
import container.Service;
import exceptions.BlockedException;
import exceptions.FinMatchException;
import exceptions.SerialConnexionException;

/**
 *  Dialogue avec la carte d'asservissement en position du robot.
 *  Pour les déplacements intelligents, voir Locomotion
 * @author PF, marsu
 */

public class LocomotionCardWrapper implements Service
{

	protected static final int INFO_X = 0;
	protected static final int INFO_Y = 1;
	protected static final int INFO_O = 2;
	
	/**
	 *  pour écrire dans le log en cas de problème
	 */
	protected Log log;
	protected Config config;

	/**
	 * connexion série avec la carte d'asservissement
	 */
	private SerialConnexion locomotionCardSerial;

	private boolean symetrie;
	
	/**
	 * Construit la surchouche de la carte d'asservissement
	 * @param log le système de log ou écrire  
	 * @param serial la connexion série avec la carte d'asservissement
	 */
	public LocomotionCardWrapper(Log log, Config config, SerialConnexion serial)
	{
		this.log = log;
		this.config = config;
		this.locomotionCardSerial = serial;
		updateConfig();
	}
	
	@Override
	public void updateConfig()
	{
        symetrie = config.getSymmetry();
        log.updateConfig();
        locomotionCardSerial.updateConfig();
	}

	/** 
	 * Fait avancer le robot. Méthode non bloquante
	 * @param distance distance a parcourir par le robot. Une valeur négative fera reculer le robot, une valeur positive le fera avancer.
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void moveLengthwise(double distance) throws SerialConnexionException, FinMatchException
	{
		String chaines[] = {"d", Double.toString(distance)};
		locomotionCardSerial.communiquer(chaines, 0);
	}

	/** 
	 * Fait tourner le robot d'un certain angle. Méthode non bloquante
	 * @param angle
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void tourneRelatif(double angle) throws SerialConnexionException, FinMatchException
	{
		// Gestion de la symétrie
		if(symetrie)
			angle = -angle;
		String chaines[] = {"t3", Double.toString(angle)};
		locomotionCardSerial.communiquer(chaines, 0);		
	}
	
	public boolean isMouvementFini() throws BlockedException, FinMatchException
	{
		String[] infosBuffer;
		try {
			infosBuffer = locomotionCardSerial.communiquer("f", 2);
			// 0: le robot est-il en train de bouger?
			// 1: le robot est-il bloqué?
//			log.debug(Boolean.parseBoolean(infosBuffer[0])+", "+Boolean.parseBoolean(infosBuffer[1]), this);
			if(Boolean.parseBoolean(infosBuffer[1]))
				throw new BlockedException();
			return !Boolean.parseBoolean(infosBuffer[0]);
		} catch (SerialConnexionException e) {
			// TODO gérer coupure série
			e.printStackTrace();
		}
		return false;
	}

	public void turn(double angle) throws SerialConnexionException, FinMatchException
	{
		String chaines[] = {"t", Double.toString(angle)};
		locomotionCardSerial.communiquer(chaines, 0);		
	}

	/**
	 * Arrête le robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void immobilise() throws SerialConnexionException, FinMatchException
	{
		locomotionCardSerial.communiquer("stop", 0);
	}
	
	/**
	 * Ecrase la position x du robot au niveau de la carte
	 * @param x la nouvelle abscisse que le robot doit considérer avoir sur la table
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void setX(int x) throws SerialConnexionException, FinMatchException
	{
		if(symetrie)
			x = -x;
		String chaines[] = {"cx", Integer.toString(x)};
		locomotionCardSerial.communiquer(chaines, 0);
	}

	/**
	 * Ecrase la position y du robot au niveau de la carte
	 * @param x la nouvelle ordonnée que le robot doit considérer avoir sur la table
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void setY(int y) throws SerialConnexionException, FinMatchException
	{
		String chaines[] = {"cy", Integer.toString(y)};
		locomotionCardSerial.communiquer(chaines, 0);	
	}
	
	/**
	 * Ecrase l'orientation du robot au niveau de la carte
	 * @param x la nouvelle orientation que le robot doit considérer avoir sur la table
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void setOrientation(double orientation) throws SerialConnexionException, FinMatchException
	{
        if(symetrie)
        	orientation = Math.PI-orientation;
		String chaines[] = {"co", Double.toString(orientation)};
		locomotionCardSerial.communiquer(chaines, 0);
	}
	
	/**
	 * Active l'asservissement en translation du robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void enableTranslationnalFeedbackLoop() throws SerialConnexionException, FinMatchException
	{
		locomotionCardSerial.communiquer("ct1", 0);
	}

	/**
	 * Active l'asservissement en rotation du robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void enableRotationnalFeedbackLoop() throws SerialConnexionException, FinMatchException
	{
		locomotionCardSerial.communiquer("cr1", 0);
	}

	/**
	 * Désactive l'asservissement en translation du robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void disableTranslationnalFeedbackLoop() throws SerialConnexionException, FinMatchException
	{
		locomotionCardSerial.communiquer("ct0", 0);
	}

	/**
	 * Désactive l'asservissement en rotation du robot
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void disableRotationnalFeedbackLoop() throws SerialConnexionException, FinMatchException
	{
		locomotionCardSerial.communiquer("cr0", 0);
	}

	/**
	 * Modifie la vitesse en translation du robot sur la table
	 * @param pwmMax la nouvelle valeur maximum que peut prenvent prendre les pwm des moteurs lors d'une translation
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void setTranslationnalSpeed(Speed speed) throws SerialConnexionException, FinMatchException
	{
		// envois a la carte d'asservissement les nouvelles valeurs des correcteurs et le nouveau maximum des pwm
		String chaines[] = {"ctv", Double.toString(speed.kp_trans), Double.toString(speed.kd_trans), Integer.toString(speed.PWMTranslation)};
		locomotionCardSerial.communiquer(chaines, 0);			
	}

	/**
	 * Modifie la vitesse en rotation du robot sur la table
	 * @param pwmMax la nouvelle valeur maximum que peut prenvent prendre les pwm des moteurs lors d'une rotation
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void setRotationnalSpeed(Speed speed) throws SerialConnexionException, FinMatchException
	{
		// envois a la carte d'asservissement les nouvelles valeurs des correcteurs et le nouveau maximum des pwm
		String chaines[] = {"crv", Double.toString(speed.kp_rot), Double.toString(speed.kd_rot), Integer.toString(speed.PWMRotation)};
		locomotionCardSerial.communiquer(chaines, 0);
	}
	
	/**
	 * envois a la carte d'asservissement de nouvelles valeurs pour les correcteurs et un nouveau maximum pour les pwm lors d'une translation
	 * @param kp nouvelle valeur du correcteur proportionnel
	 * @param kd nouvelle valeur du correcteur dérivé 
	 * @param pwm_max a nouvelle valeur maximum que peut prenvent prendre les pwm des moteurs lors d'une translation
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void changeTranslationnalFeedbackParameters(double kp, double kd, int pwm_max) throws SerialConnexionException, FinMatchException
	{
		String chaines[] = {"ctv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		locomotionCardSerial.communiquer(chaines, 0);
	}

	/**
	 * envois a la carte d'asservissement de nouvelles valeurs pour les correcteurs et un nouveau maximum pour les pwm lors d'une rotation
	 * @param kp nouvelle valeur du correcteur proportionnel
	 * @param kd nouvelle valeur du correcteur dérivé 
	 * @param pwm_max a nouvelle valeur maximum que peut prenvent prendre les pwm des moteurs lors d'une rotation
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void changeRotationnalFeedbackParameters(double kp, double kd, int pwm_max) throws SerialConnexionException, FinMatchException
	{
		String chaines[] = {"crv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		locomotionCardSerial.communiquer(chaines, 0);
	}

	/**
	 * Demande a la carte d'asservissement la position et l'orientation courrante du robot sur la table.
	 * Renvoie x, y et orientation du robot
	 * @return un tableau de 3 cases: [x, y, orientation]
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public double[] getCurrentPositionAndOrientation() throws SerialConnexionException, FinMatchException
	{
		// on demande a la carte des information a jour
		// on envois "?infos" et on lis double (dans l'ordre : abscisse, ordonnée, orientation)
		String[] infosBuffer = locomotionCardSerial.communiquer("?xyo", 3);
		double[] parsedInfos = new double[3];
		for(int i = 0; i < 3; i++)
		    parsedInfos[i] = Double.parseDouble(infosBuffer[i]);
        if(symetrie)
        {
        	parsedInfos[INFO_X] = -parsedInfos[INFO_X];
        	parsedInfos[INFO_O] = Math.PI-parsedInfos[INFO_O];
        }

		return parsedInfos;
	}

	/**
	 * Ferme la connexion série avec la carte d'asservissements
	 */
	public void closeLocomotion()
	{
		locomotionCardSerial.close();
	}
	
}
