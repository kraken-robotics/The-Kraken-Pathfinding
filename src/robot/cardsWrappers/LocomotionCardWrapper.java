package robot.cardsWrappers;

import robot.FeedbackLoopStatisticsElement;
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
	private Log log;
	protected Config config;

	/**
	 * connexion série avec la carte d'asservissement
	 */
	private SerialConnexion locomotionCardSerial;

	/**
	 * Stockage des informations courrantes de l'asservissement. 
	 * Dès la fin du constructeur, les clefs sont: 
	 *  - PWMmoteurGauche
	 *  - PWMmoteurDroit
	 *  - erreur_rotation
	 *  - erreur_translation
	 *  - derivee_erreur_rotation
	 *  - derivee_erreur_translation
	 */
	private int[] feedbackLoopStatistics = new int[FeedbackLoopStatisticsElement.values().length];
		
	/**
	 *  en cas de bloquage, date a laquelle le blocage a commencé
	 */
	private long blockageStartTimestamp;
	
	/**
	 *  utilisé par raiseExceptionIfBlocked, pour savoir si lors du dernier appel de raiseExceptionIfBlocked, la robot était déja bloqué (auquel cas il ne faut plus considérer que c'est le début du bloquage)
	 */
    private boolean wasBlockedAtPreviousCall = false;
    

	/**
	 *  nombre de miliseconde de tolérance entre la détection d'un patinage et la levée de l'exception. Trop basse il y aura des faux positifs, trop haute on va forcer dans les murs pendant longtemps
	 */
	private int blockedTolerancy;

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
		
		feedbackLoopStatistics[FeedbackLoopStatisticsElement.PWMmoteurGauche.ordinal()] = 0;
		feedbackLoopStatistics[FeedbackLoopStatisticsElement.PWMmoteurDroit.ordinal()] = 0;
		feedbackLoopStatistics[FeedbackLoopStatisticsElement.erreur_rotation.ordinal()] = 0;
		feedbackLoopStatistics[FeedbackLoopStatisticsElement.erreur_translation.ordinal()] = 0;
		feedbackLoopStatistics[FeedbackLoopStatisticsElement.derivee_erreur_rotation.ordinal()] = 0;
		feedbackLoopStatistics[FeedbackLoopStatisticsElement.derivee_erreur_translation.ordinal()] = 0;
//		feedbackLoopStatistics[FeedbackLoopStatisticsElement.inverse_erreur_translation_integrale.ordinal()] = 100;
		updateConfig();
	}
	
	@Override
	public void updateConfig()
	{
		blockedTolerancy = config.getInt(ConfigInfo.TEMPS_AVANT_BLOCAGE);
        symetrie = config.getSymmetry();
	}	
	
	/**
	 * lève BlockedException si le robot bloque (c'est-à-dire que les moteurs forcent mais que le robot ne bouge pas).
	 * @throws BlockedException si le robot est mécaniquement bloqué contre un obstacle qui l'empèche d'avancer plus loin
	 * @throws FinMatchException 
	 */
	public void raiseExceptionIfBlocked() throws BlockedException, FinMatchException
	{
		
		// demande des information sur l'asservissement du robot
		int pwmLeftMotor = feedbackLoopStatistics[FeedbackLoopStatisticsElement.PWMmoteurGauche.ordinal()];
		int pwmRightMotor = feedbackLoopStatistics[FeedbackLoopStatisticsElement.PWMmoteurDroit.ordinal()];
		int derivatedRotationnalError = feedbackLoopStatistics[FeedbackLoopStatisticsElement.derivee_erreur_rotation.ordinal()];
		int derivatedTranslationnalError = feedbackLoopStatistics[FeedbackLoopStatisticsElement.derivee_erreur_translation.ordinal()];
		
		// on décrète que les moteurs forcent si la puissance qu'ils demandent est trop grande
		boolean areMotorsActive = Math.abs(pwmLeftMotor) > 40 || Math.abs(pwmRightMotor) > 40;
		
		// on décrète que le robot est immobile si l'écart entre la position demandée et la position actuelle est (casi) constant
		boolean isRobotImmobile = Math.abs(derivatedRotationnalError) <= 10 && Math.abs(derivatedTranslationnalError) <= 10;

		// si on patine
		if(isRobotImmobile && areMotorsActive)
		{
			// si on patinais déja auparavant, on fait remonter le patinage au code de haut niveau (via BlocageException)
			if(wasBlockedAtPreviousCall)
			{
                // la durée de tolérance au patinage est fixée ici (200ms)
				// mais cette fonction n'étant appellée qu'a une fréquance de l'ordre du Hertz ( la faute a une saturation de la série)
				// le robot mettera plus de temps a réagir ( le temps de réaction est égal au temps qui sépare 2 appels successifs de cette fonction)
				if((System.currentTimeMillis() - blockageStartTimestamp) > blockedTolerancy)
				{
					log.warning("raiseExceptionIfBlocked : le robot a dû s'arrêter suite à un patinage. (levage de BlockedException)", this);
					try
					{
						immobilise();
					} 
					catch (SerialConnexionException e)
					{
						log.critical("raiseExceptionIfBlocked : Impossible d'immobiliser le robot: la carte d'asser ne répond plus.", this);
						e.printStackTrace();
					}
					
					throw new BlockedException("l'écart a la consigne ne bouge pas alors que les moteurs sont en marche");
				}
			}

			// si on détecte pour la première fois le patinage, on continue de forcer
			else
			{
				blockageStartTimestamp = System.currentTimeMillis();
				wasBlockedAtPreviousCall  = true;
			}
		}
		// si tout va bien
		else
		{
			wasBlockedAtPreviousCall = false;
			blockageStartTimestamp = System.currentTimeMillis();
		}

	}

	/** 
	 * Renvoie "faux" si le robot est arrivé et s'il est à la bonne position, vrai sinon.
	 * Provoque un appel série pour avoir des information a jour. Cette méthode demande donc un peu de temps. 
	 * @return vrai si le robot bouge, faux si le robot est immobile
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public boolean isRobotMoving() throws SerialConnexionException, FinMatchException
	{
		refreshFeedbackLoopStatistics();
		
		// petits alias sur les infos de l'asservissement
		int rotationnalError = feedbackLoopStatistics[FeedbackLoopStatisticsElement.erreur_rotation.ordinal()];
		int translationnalError = feedbackLoopStatistics[FeedbackLoopStatisticsElement.erreur_translation.ordinal()];
		int derivedRotationnalError = feedbackLoopStatistics[FeedbackLoopStatisticsElement.derivee_erreur_rotation.ordinal()];
		int derivedTranslationnalError = feedbackLoopStatistics[FeedbackLoopStatisticsElement.derivee_erreur_translation.ordinal()];
		
		// TODO:VALEURS A REVOIR
		// Décide si on considère le robot immobile ou non.
		boolean rotationStopped = Math.abs(rotationnalError) <= 60;
		boolean translationStopped = Math.abs(translationnalError) <= 60;
		boolean isImmobile = Math.abs(derivedRotationnalError) <= 20 && Math.abs(derivedTranslationnalError) <= 20;
		
		
		
		return !(rotationStopped && translationStopped && isImmobile);
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
	 * Met à jour PWMmoteurGauche, PWMmoteurDroit, erreur_rotation, erreur_translation, derivee_erreur_rotation, derivee_erreur_translation
	 * les nouvelles valeurs sont stokées dans feedbackLoopStatistics (feedbackLoopStatistics est une map privée de la classe)
	 * @throws SerialConnexionException en cas de problème de communication avec la carte d'asservissement
	 * @throws FinMatchException 
	 */
	public void refreshFeedbackLoopStatistics() throws SerialConnexionException, FinMatchException
	{
		// on demande à la carte des informations a jour
		// on envoit "?infos" et on lit 4 int (dans l'ordre : PWM droit, PWM gauche, erreurRotation, erreurTranslation)
		String[] infosBuffer = locomotionCardSerial.communiquer("?infos", 4);
		int[] parsedInfos = new int[4];
		for(int i = 0; i < 4; i++)
			parsedInfos[i] = Integer.parseInt(infosBuffer[i]);
		
		// calcul des dérivées des erreurs en translation et en rotation :
		// on fait la différence entre la valeur actuelle de l'erreur et le valeur précédemment mesurée.
		// on divise par un dt unitaire (non mentionné dans l'expression)
		int derivedRotationnalError = parsedInfos[2] - feedbackLoopStatistics[FeedbackLoopStatisticsElement.erreur_rotation.ordinal()];
		int derivedTranslationnalError = parsedInfos[3] - feedbackLoopStatistics[FeedbackLoopStatisticsElement.erreur_translation.ordinal()];
		
		
		// on stocke la puissance consommée par les moteurs
        feedbackLoopStatistics[FeedbackLoopStatisticsElement.PWMmoteurGauche.ordinal()] = parsedInfos[0];
        feedbackLoopStatistics[FeedbackLoopStatisticsElement.PWMmoteurDroit.ordinal()] = parsedInfos[1];
        
        // l'erreur de translation mesurée par les codeuses
        feedbackLoopStatistics[FeedbackLoopStatisticsElement.erreur_rotation.ordinal()] = parsedInfos[2];
        feedbackLoopStatistics[FeedbackLoopStatisticsElement.erreur_translation.ordinal()] = parsedInfos[3];
        
        // stocke les dérivées des erreurs, calculés 10 lignes plus haut
        feedbackLoopStatistics[FeedbackLoopStatisticsElement.derivee_erreur_rotation.ordinal()] = derivedRotationnalError;
        feedbackLoopStatistics[FeedbackLoopStatisticsElement.derivee_erreur_translation.ordinal()] = derivedTranslationnalError;

        
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
