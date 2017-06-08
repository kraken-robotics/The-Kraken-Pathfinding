package robot.cardsWrappers;

import java.util.Hashtable;

import robot.serial.SerialConnexion;
import utils.*;
import container.Service;
import exceptions.Locomotion.BlockedException;
import exceptions.serial.SerialConnexionException;

/**
 *  Dialogue avec la carte d'asservissement en position du robot.
 *  Pour les déplacements intelligents, voir Locomotion
 * @author PF, marsu
 */

public class LocomotionCardWrapper implements Service
{

	// Dépendances
	private Log log;
	private SerialConnexion serie;

	private Hashtable<String, Integer> feedbackLoopStatistics;
		
	private long blockageStartTimestamp;
	
    private boolean wasBlockedAtPreviousCall = false;

    /**
	 * Constructeur
	 */
	public LocomotionCardWrapper(Log log, SerialConnexion serie)
	{
		this.log = log;
		this.serie = serie;
		
		feedbackLoopStatistics = new Hashtable<String, Integer>();
		feedbackLoopStatistics.put("PWMmoteurGauche", 0);
		feedbackLoopStatistics.put("PWMmoteurDroit", 0);
		feedbackLoopStatistics.put("erreur_rotation", 0);
		feedbackLoopStatistics.put("erreur_translation", 0);
		feedbackLoopStatistics.put("derivee_erreur_rotation", 0);
		feedbackLoopStatistics.put("derivee_erreur_translation", 0);
	}
	
	public void updateConfig()
	{
	}	
	
	/**
	 * lève BlockedException si le robot bloque (c'est-à-dire que les moteurs forcent mais que le robot ne bouge pas). Blocage automatique au bout de 500ms
	 * @param PWMmoteurGauche
	 * @param PWMmoteurDroit
	 * @param derivee_erreur_rotation
	 * @param derivee_erreur_translation
	 * @throws BlockedException 
	 */
	public void raiseExeptionIfBlocked() throws BlockedException, SerialConnexionException
	{
		// nombre de miliseconde de tolérance entre la détection d'un patinage et la levée de l'exeption.
		//  trop basse il y a des faux positifs, trop haute on va forcer dans les murs pendant longtemps
		int blockedTolerancy = 200;//TODO: mettre dans le fichier de config
		
		// demande des information sur l'asservissement du robot
		int pwmLeftMotor = feedbackLoopStatistics.get("PWMmoteurGauche");
		int pwmRightMotor = feedbackLoopStatistics.get("PWMmoteurDroit");
		int derivatedRotationnalError = feedbackLoopStatistics.get("derivee_erreur_rotation");
		int derivatedTranslationnalError = feedbackLoopStatistics.get("derivee_erreur_translation");
		
		// on décrète que les moteurs forcent si la puissance qu'ils demandent est trop grande
		boolean areMotorsActive = Math.abs(pwmLeftMotor) > 40 || Math.abs(pwmRightMotor) > 40;
		
		// on décrète que le robot est immobile si l'écart entre la position demandée et la position actuelle est (casi) constant
		boolean isRobotImmobile = Math.abs(derivatedRotationnalError) <= 10 && Math.abs(derivatedTranslationnalError) <= 10;

		// si on patine
		if(isRobotImmobile && areMotorsActive)
		{
			// si on patinais déja auparavant, on fait remonter le patinage au code de haut niveau (via BlocageExeption)
			if(wasBlockedAtPreviousCall)
			{
                // la durée de tolérance au patinage est fixée ici (200ms)
				// mais cette fonction n'étant appellée qu'a une fréquance de l'ordre du Hertz ( la faute a une saturation de la série)
				// le robot mettera plus de temps a réagir ( le temps de réaction est égal au temps qui sépare 2 appels successifs de cette fonction)
				if((System.currentTimeMillis() - blockageStartTimestamp) > blockedTolerancy)
				{
					log.warning("raiseExeptionIfBlocked : le robot a dû s'arrêter suite à un patinage. (levage de BlockedException)", this);
					immobilise();
					
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
	 * Regarde si le robot bouge effectivement.
	 * @param erreur_rotation
	 * @param erreur_translation
	 * @param derivee_erreur_rotation
	 * @param derivee_erreur_translation
	 * @return
	 */
	public boolean isRobotMoving()
	{
		// obtient les infos de l'asservissement
		int erreur_rotation = feedbackLoopStatistics.get("erreur_rotation");
		int erreur_translation = feedbackLoopStatistics.get("erreur_translation");
		int derivee_erreur_rotation = feedbackLoopStatistics.get("derivee_erreur_rotation");
		int derivee_erreur_translation = feedbackLoopStatistics.get("derivee_erreur_translation");
		
		// ces 2 booléens checkent la précision de l'asser. Ce n'est pas le rôle de cette fonction, 
		// et peut causer des bugs (erreurs d'aquitement) de java si l'asser est mla fait		
		//donc, on vire !
		
		
		// VALEURS A REVOIR
		boolean rotation_stoppe = Math.abs(erreur_rotation) <= 60;
		boolean translation_stoppe = Math.abs(erreur_translation) <= 60;
		boolean bouge_pas = Math.abs(derivee_erreur_rotation) <= 20 && Math.abs(derivee_erreur_translation) <= 20;
		return !(rotation_stoppe && translation_stoppe && bouge_pas);
	}
	
	/** 
	 * Fait avancer le robot. Méthode non bloquante
	 * @param distance
	 */
	public void moveForward(double distance) throws SerialConnexionException
	{
		String chaines[] = {"d", Double.toString(distance)};
		serie.communiquer(chaines, 0);
	}

	/** 
	 * Fait tourner le robot. Méthode non bloquante
	 * @param angle
	 */
	public void turn(double angle) throws SerialConnexionException
	{
		String chaines[] = {"t", Double.toString(angle)};
		serie.communiquer(chaines, 0);		
	}
	
	/**
	 * Arrête le robot
	 */
	public void immobilise() throws SerialConnexionException
	{
        disableTranslationnalFeedbackLoop();
        disableRotationnalFeedbackLoop();
		serie.communiquer("stop", 0);
        enableTranslationnalFeedbackLoop();
        enableRotationnalFeedbackLoop();
	}
	
	/**
	 * Ecrase la position x du robot au niveau de la carte
	 * @param x
	 */
	public void setX(int x) throws SerialConnexionException
	{
		String chaines[] = {"cx", Integer.toString(x)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Ecrase la position y du robot au niveau de la carte
	 * @param y
	 */
	public void setY(int y) throws SerialConnexionException
	{
		String chaines[] = {"cy", Integer.toString(y)};
		serie.communiquer(chaines, 0);	
	}
	
	/**
	 * Ecrase l'orientation du robot au niveau de la carte
	 * @param orientation
	 */
	public void setOrientation(double orientation) throws SerialConnexionException
	{
		String chaines[] = {"co", Double.toString(orientation)};
		serie.communiquer(chaines, 0);
	}
	
	/**
	 * Active l'asservissement en translation du robot
	 */
	public void enableTranslationnalFeedbackLoop() throws SerialConnexionException
	{
		serie.communiquer("ct1", 0);
	}

	/**
	 * Active l'asservissement en rotation du robot
	 */
	public void enableRotationnalFeedbackLoop() throws SerialConnexionException
	{
		serie.communiquer("cr1", 0);
	}

	/**
	 * Désactive l'asservissement en translation du robot
	 */
	public void disableTranslationnalFeedbackLoop() throws SerialConnexionException
	{
		serie.communiquer("ct0", 0);
	}

	/**
	 * Désactive l'asservissement en rotation du robot
	 */
	public void disableRotationnalFeedbackLoop() throws SerialConnexionException
	{
		serie.communiquer("cr0", 0);
	}

	/**
	 * Modifie la vitesse en translation
	 * @param pwm_max
	 */
	public void setTranslationnalSpeed(int pwm_max) throws SerialConnexionException
	{
		double kp, kd;
		if(pwm_max >= 195)
		{
			kp = 0.55;
			kd = 27.0;
		}
		else if(pwm_max >= 165)
		{
			kp = 0.52;
			kd = 17.0;
		}
		else if(pwm_max >= 145)
		{
			kp = 0.52;
			kd = 17.0;
		}
		else if(pwm_max >= 115)
		{
			kp = 0.45;
			kd = 12.0;
		}
		else if(pwm_max >= 85)
		{
			kp = 0.45;
			kd = 12.5;
		}
		else if(pwm_max >= 55)
		{
			kp = 0.5;
			kd = 4.0;
		}
		else
		{
			kp = 1.15;
			kd = 3.0;
		}
		
		String chaines[] = {"ctv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);			
	}

	/**
	 * Modifie la vitesse en rotation
	 * @param pwm_max
	 */
	public void setRotationnalSpeed(int pwm_max) throws SerialConnexionException
	{
		double kp, kd;
		if(pwm_max > 155)
		{
			kp = 2.0;
			kd = 50.0;
		}
		else if(pwm_max > 115)
		{
			kp = 0.85;
			kd = 25.0;
		}
		else if(pwm_max > 85)
		{
			kp = 1.0;
			kd = 15.0;
		}
		else
		{
			kp = 2.0;
			kd = 14.0;
		}
		
		String chaines[] = {"crv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}
	
	public void changeTranslationnalFeedbackParameters(double kp, double kd, int pwm_max) throws SerialConnexionException
	{
		String chaines[] = {"ctv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}
	
	public void changeRotationnalFeedbackParameters(double kp, double kd, int pwm_max) throws SerialConnexionException
	{
		String chaines[] = {"crv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Met à jour PWMmoteurGauche, PWMmoteurDroit, erreur_rotation, erreur_translation, derivee_erreur_rotation, derivee_erreur_translation
	 * les nouvelles valeurs sont stokées dans la map
	 */
	public void refreshFeedbackLoopStatistics() throws SerialConnexionException
	{
		// on envois "?infos" et on lis les 4 int (dans l'ordre : PWM droit, PWM gauche, erreurRotation, erreurTranslation)
		String[] infos_string = serie.communiquer("?infos", 4);
		int[] infos_int = new int[4];
		for(int i = 0; i < 4; i++)
			infos_int[i] = Integer.parseInt(infos_string[i]);
		
		// calcul des dérivées des erreurs en translation et en rotation :
		// on fait la différence entre la valeur actuelle de l'erreur et le valeur précédemment mesurée.
		// on divise par un dt unitaire (non mentionné dans l'expression)
		int deriv_erreur_rot = infos_int[2] - feedbackLoopStatistics.get("erreur_rotation");
		int deriv_erreur_tra = infos_int[3] - feedbackLoopStatistics.get("erreur_translation");
		
		
		// infos_stoppage_enMouvement est une map dont les clés sont des strings et les valeurs des int
		
		// on stocke la puissance consommée par les moteurs
        feedbackLoopStatistics.put("PWMmoteurGauche", infos_int[0]);
        feedbackLoopStatistics.put("PWMmoteurDroit", infos_int[1]);
        
        // l'erreur de translation mesurée par les codeuses
        feedbackLoopStatistics.put("erreur_rotation", infos_int[2]);
        feedbackLoopStatistics.put("erreur_translation", infos_int[3]);
        
        // stocke les dérivées des erreurs, calculés 10 lignes plus haut
        feedbackLoopStatistics.put("derivee_erreur_rotation", deriv_erreur_rot);
        feedbackLoopStatistics.put("derivee_erreur_translation", deriv_erreur_tra);

        
	}

	/**
	 * Renvoie x, y et orientation du robot
	 * @return un tableau de 3 cases: [x, y, orientation]
	 */
	public double[] getCurrentPositionAndOrientation() throws SerialConnexionException
	{
		String[] infos_string = serie.communiquer("?xyo", 3);
		double[] infos_double = new double[3];
		
		for(int i = 0; i < 3; i++)
		    infos_double[i] = Double.parseDouble(infos_string[i]);

		return infos_double;
	}

	/**
	 * Arrêt de la série
	 */
	public void closeLocomotion()
	{
		serie.close();
	}
	
}
