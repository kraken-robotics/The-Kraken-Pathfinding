package robot;

import java.util.ArrayList;

import obstacles.ObstacleRotationRobot;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import container.Service;
import exceptions.BlockedException;
import exceptions.ChangeDirectionException;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.SerialConnexionException;
import exceptions.UnableToMoveException;
import exceptions.UnexpectedObstacleOnPathException;
import exceptions.WallCollisionDetectedException;
import hook.Hook;
import hook.types.HookDemiPlan;
import robot.cardsWrappers.LocomotionCardWrapper;
import table.ObstacleManager;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import utils.Vec2;

// TODO: ScriptHookException ne peut être lancé que sur un chemin, donc
// que par un suit_chemin

/**
 * Entre Deplacement (appels à la série) et RobotVrai (déplacements haut niveau), Locomotion
 * s'occupe de la position, de la symétrie, des hooks, des trajectoires courbes et des blocages.
 * Structure, du bas au haut niveau: symétrie, hook, trajectoire courbe et blocage.
 * Les méthodes "non-bloquantes" se finissent alors que le robot roule encore.
 * (les méthodes non-bloquantes s'exécutent très rapidement)
 * Les méthodes "bloquantes" se finissent alors que le robot est arrêté.
 * @author pf
 *
 */

public class Locomotion implements Service
{

    private Log log;
    private Config config;
    private ObstacleManager obstaclemanager;
    private int largeur_robot;
    private int distance_detection;
    private Vec2 position = new Vec2();  // la position réelle du robot, pas la version qu'ont les robots
    
    private double orientation; // l'orientation réelle du robot, pas la version qu'ont les robots
    private LocomotionCardWrapper deplacements;
    private int sleep_boucle_acquittement = 10;
    private int distance_degagement_robot = 50;
    private double angle_degagement_robot;
    
    private boolean directionPrecedente;
    
    public Locomotion(Log log, Config config, LocomotionCardWrapper deplacements, ObstacleManager obstaclemanager)
    {
        this.log = log;
        this.config = config;
        this.deplacements = deplacements;
        this.obstaclemanager = obstaclemanager;
        updateConfig();
    }
    
    public void readjust()
    {
        try {
            setOrientation(0);
            setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Fait tourner le robot (méthode bloquante)
     * Une manière de tourner qui réutilise le reste du code, car tourner
     * n'en devient plus qu'un cas particulier (celui où... on n'avance pas)
     * @param angle
     * @param hooks
     * @throws UnableToMoveException
     * @throws FinMatchException
     * @throws ScriptHookException
     */
    public void turn(double angle, ArrayList<Hook> hooks) throws UnableToMoveException, FinMatchException
    {
    	Vec2 consigne = new Vec2(
        (int) (position.x + 1000*Math.cos(angle)),
        (int) (position.y + 1000*Math.sin(angle))
        );

        // l'appel à cette méthode sous-entend que le robot ne tourne pas
        // il va donc en avant si la distance est positive, en arrière si elle est négative
        try {
			vaAuPointGestionExceptions(consigne, position, 0, hooks, true, false, true);
		} catch (ChangeDirectionException e) {
			// Normalement impossible
			e.printStackTrace();
		} catch (ScriptHookException e) {
			// Normalement impossible
			e.printStackTrace();
		}
    }
    
    /**
     * Fait avancer le robot de "distance" (en mm).
     * @param distance
     * @param hooks
     * @param insiste
     * @throws UnableToMoveException
     * @throws FinMatchException 
     * @throws ScriptHookException 
     */
    public void moveLengthwise(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException, FinMatchException
    {
        log.debug("Avancer de "+Integer.toString(distance), this);
        
        Vec2 consigne = new Vec2(); 
        consigne.x = (int) (position.x + distance*Math.cos(orientation));
        consigne.y = (int) (position.y + distance*Math.sin(orientation));        

        // l'appel à cette méthode sous-entend que le robot ne tourne pas
        // il va donc en avant si la distance est positive, en arrière si elle est négative
        // si on est à 90°, on privilégie la marche avant
        try {
			vaAuPointGestionExceptions(consigne, position, 0, hooks, distance >= 0, mur, false);
		} catch (ChangeDirectionException e) {
			// Normalement impossible
			e.printStackTrace();
		} catch (ScriptHookException e) {
			// Normalement impossible
			e.printStackTrace();
		}
    }
        
    /**
     * Suit un chemin. Crée les hooks de trajectoire courbe si besoin est.
     * @param chemin
     * @param hooks
     * @param insiste
     * @throws UnableToMoveException
     * @throws FinMatchException 
     * @throws ScriptHookException 
     */
    public void followPath(ArrayList<SegmentTrajectoireCourbe> chemin, HookDemiPlan hookTrajectoireCourbe, ArrayList<Hook> hooks, DirectionStrategy directionstrategy) throws UnableToMoveException, FinMatchException, ScriptHookException
    {
    	hooks.add(hookTrajectoireCourbe);
    	int size = chemin.size();
    	for(int i = 0; i < size; i++)
        {
    		SegmentTrajectoireCourbe actuel = chemin.get(i);
    		Vec2 intermediaire;
    		int differenceDistance;
    		if(i > 0)
    		{
    			intermediaire = chemin.get(i-1).objectifFinal.getCoordonnees().clone();
    			differenceDistance = actuel.differenceDistance;
    		}
    		else
    		{
    			intermediaire = position.clone();
    			// Cas particulier du premier départ: pas de trajectoire courbe
    			differenceDistance = 0;
    		}
    		
    		// On annule tout hook sur l'ultime point d'arrivée.
    		if(i == size-1 || chemin.get(i+1).differenceDistance == 0 )
    		{
    			hookTrajectoireCourbe.setDisabled();
    		}
    		else
    		{
    			SegmentTrajectoireCourbe prochain = chemin.get(i+1);
    			// On annule le prochain hook si on a déjà dépassé le hook
    			if(prochain.differenceDistance == 0 || position.minusNewVector(prochain.pointDepart).dot(prochain.directionHook) > 0)
    			{
    				prochain.differenceDistance = 0;
    				prochain.distanceAnticipation = 0;
        			hookTrajectoireCourbe.setDisabled();
    			}
	    		else
	    		{
//	    			log.debug("Création d'un hook pour la trajectoire courbe en "+chemin.get(i).objectifFinal, this);
	    			hookTrajectoireCourbe.update(prochain.directionHook, prochain.pointDepart);
	//    			log.debug("Bas: "+PathfindingNodes.BAS.getCoordonnees()+", hook = "+prochain.directionHook, this);
	    		}
    		}
    		
            Vec2 consigne = chemin.get(i).objectifFinal.getCoordonnees().clone();
            
            try {
            	// TODO: retirer trajectoire_courbe qui n'apporte aucune information
				vaAuPointGestionMarcheArriere(consigne, intermediaire, differenceDistance, hooks, false, directionstrategy, false);
			} catch (ChangeDirectionException e) {
				// On change de direction!
//				log.debug("Changement de direction!", this);
			}
        }
    }

    /**
     * Bloquant. Gère la marche arrière automatique selon la stratégie demandée.
     * @param hooks
     * @param insiste
     * @throws UnableToMoveException
     * @throws FinMatchException 
     * @throws ScriptHookException 
     * @throws ChangeDirectionException 
     */
    private void vaAuPointGestionMarcheArriere(Vec2 consigne, Vec2 intermediaire, int differenceDistance, ArrayList<Hook> hooks, boolean mur, DirectionStrategy strategy, boolean seulementAngle) throws UnableToMoveException, FinMatchException, ScriptHookException, ChangeDirectionException
    {
    	// Si on est en trajectoire courbe, on continue comme la fois précédente
    	boolean trajectoire_courbe = differenceDistance != 0;
    	if(trajectoire_courbe)
            vaAuPointGestionExceptions(consigne, intermediaire, differenceDistance, hooks, directionPrecedente, mur, seulementAngle);
    	else if(strategy == DirectionStrategy.FORCE_BACK_MOTION)
    	{
    		directionPrecedente = false;
            vaAuPointGestionExceptions(consigne, intermediaire, differenceDistance, hooks, false, mur, seulementAngle);
    	}
    	else if(strategy == DirectionStrategy.FORCE_FORWARD_MOTION)
    	{
    		directionPrecedente = true;
            vaAuPointGestionExceptions(consigne, intermediaire, differenceDistance, hooks, true, mur, seulementAngle);
    	}
    	else //if(strategy == DirectionStrategy.FASTEST)
    	{
    		// Calcul du moyen le plus rapide
	        Vec2 delta = consigne.clone();
	        delta.minus(position);
	        // Le coeff 1000 vient du fait que Vec2 est constitué d'entiers
	        Vec2 orientationVec = new Vec2((int)(1000*Math.cos(orientation)), (int)(1000*Math.sin(orientation)));
	
	        directionPrecedente = delta.dot(orientationVec) > 0;
	        // On regarde le produit scalaire; si c'est positif, alors on est dans le bon sens, et inversement
	        vaAuPointGestionExceptions(consigne, intermediaire, differenceDistance, hooks, directionPrecedente, mur, seulementAngle);
    	}
    }
    
    /**
     * Gère les exceptions, c'est-à-dire les rencontres avec l'ennemi et les câlins avec un mur.
     * @param hooks
     * @param trajectoire_courbe
     * @param marche_arriere
     * @param insiste
     * @throws UnableToMoveException 
     * @throws FinMatchException 
     * @throws ScriptHookException 
     * @throws ChangeDirectionException 
     */
    private void vaAuPointGestionExceptions(Vec2 consigne, Vec2 intermediaire, int differenceDistance, ArrayList<Hook> hooks, boolean marcheAvant, boolean mur, boolean seulementAngle) throws UnableToMoveException, FinMatchException, ScriptHookException, ChangeDirectionException
    {
        int attente_ennemi_max = 600; // combien de temps attendre que l'ennemi parte avant d'abandonner
        int nb_iterations_deblocage = 2; // combien de fois on réessayer si on se prend un mur
        boolean recommence;
        do {
            recommence = false;
            try
            {
                vaAuPointGestionHookCorrectionEtDetection(consigne, intermediaire, differenceDistance, hooks, marcheAvant, seulementAngle);
            } catch (BlockedException e)
            {
                nb_iterations_deblocage--;
                immobilise();
                /*
                 * En cas de blocage, on recule (si on allait tout droit) ou on avance.
                 */
                // Si on s'attendait à un mur, c'est juste normal de se le prendre.
                if(!mur)
                {
                    try
                    {
                        log.warning("On n'arrive plus à avancer. On se dégage", this);
                        if(seulementAngle)
                        {
                        	// TODO: les appels à déplacements sont non bloquants, il faut rajouter des sleeps
                        	// on alterne rotation à gauche et à droite
                        	if((nb_iterations_deblocage & 1) == 0)
                        		deplacements.tourneRelatif(angle_degagement_robot);
                        	else
                        		deplacements.tourneRelatif(-angle_degagement_robot);
                        }
                        else if(marcheAvant)
                            deplacements.moveLengthwise(distance_degagement_robot);
                        else
                            deplacements.moveLengthwise(-distance_degagement_robot);
                        while(!isMotionEnded());
                    	recommence = true; // si on est arrivé ici c'est qu'aucune exception n'a été levée
                    } catch (SerialConnexionException e1)
                    {
                        e1.printStackTrace();
                    } catch (BlockedException e1) {
                    	immobilise();
                        log.critical("On n'arrive pas à se dégager.", this);
					}
                    if(!recommence)
                        throw new UnableToMoveException();
                }
            } catch (UnexpectedObstacleOnPathException e)
            {
            	immobilise();
            	long dateAvant = System.currentTimeMillis();
                log.critical("Détection d'un ennemi! Abandon du mouvement.", this);
            	while(System.currentTimeMillis() - dateAvant < attente_ennemi_max)
            	{
            		try {
            			detectEnemy(marcheAvant);
            			recommence = true; // si aucune détection
            			break;
            		}
            		catch(UnexpectedObstacleOnPathException e2)
            		{}
            	}
                if(!recommence)
                    throw new UnableToMoveException();
            } catch (WallCollisionDetectedException e) {
            	immobilise();
            	e.printStackTrace();
            	throw new UnableToMoveException();
            	// TODO: et ensuite?
			}

        } while(recommence); // on recommence tant qu'il le faut

    // Tout s'est bien passé
    }
    
    /**
     * Bloquant. Gère les hooks, la correction de trajectoire et la détection.
     * @param point
     * @param hooks
     * @param trajectoire_courbe
     * @throws BlockedException 
     * @throws UnexpectedObstacleOnPathException 
     * @throws FinMatchException 
     * @throws ScriptHookException 
     * @throws WallCollisionDetectedException 
     * @throws ChangeDirectionException 
     */
    private void vaAuPointGestionHookCorrectionEtDetection(Vec2 consigne, Vec2 intermediaire, int differenceDistance, ArrayList<Hook> hooks, boolean marcheAvant, boolean seulementAngle) throws BlockedException, UnexpectedObstacleOnPathException, FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException
    {
        // le fait de faire de nombreux appels permet de corriger la trajectoire
    	vaAuPointGestionDirection(consigne, intermediaire, differenceDistance, marcheAvant, seulementAngle, false);
        do
        {
            updateCurrentPositionAndOrientation();
            
            // en cas de détection d'ennemi, une exception est levée
            detectEnemy(!marcheAvant);

            for(Hook hook : hooks)
                hook.evaluate();

            corrigeAngle(consigne, marcheAvant);


//            Sleep.sleep(sleep_boucle_acquittement);
        } while(!isMotionEnded());
        
    }

    private void corrigeAngle(Vec2 consigne, boolean marcheAvant) throws BlockedException, FinMatchException, WallCollisionDetectedException
    {
    	vaAuPointGestionDirection(consigne, position, 0, marcheAvant, true, true);
    }

    /**
     * Non bloquant. Gère l'utilisation marche arrière.
     * @param point
     * @param sans_lever_exception
     * @param trajectoire_courbe
     * @param marche_arriere
     * @throws BlockedException 
     * @throws FinMatchException 
     * @throws WallCollisionDetectedException 
     */
    private void vaAuPointGestionDirection(Vec2 consigne, Vec2 intermediaire, int differenceDistance, boolean marcheAvant, boolean seulementAngle, boolean correction) throws BlockedException, FinMatchException, WallCollisionDetectedException
    {
        Vec2 delta = consigne.clone();
        updateCurrentPositionAndOrientation();

        delta.minus(intermediaire);
//        log.debug("Distance directe: "+delta.length()+", differenceDistance: "+differenceDistance, this);
        double distance = delta.length() - differenceDistance;
        
        double angle =  Math.atan2(delta.y, delta.x);
        // on suit ce que demande le boolean marcheAvant, en se retournant si besoin
        if(marcheAvant && distance < 0 || (!marcheAvant && distance > 0))
        {
            distance *= -1;
            angle += Math.PI;
        }
        
        vaAuPointGestionCourbe(consigne, intermediaire, angle, distance, differenceDistance != 0, seulementAngle, correction);
    }
    
    /**
     * Non bloquant. Avance, de manière courbe ou non.
     * @param angle
     * @param distance
     * @param trajectoire_courbe
     * @throws BlockedException 
     * @throws FinMatchException 
     * @throws WallCollisionDetectedException 
     */
    private void vaAuPointGestionCourbe(Vec2 consigne, Vec2 intermediaire, double angle, double distance, boolean trajectoire_courbe, boolean seulementAngle, boolean correction) throws BlockedException, FinMatchException, WallCollisionDetectedException
    {
		double delta = (angle-orientation) % (2*Math.PI);
		if(delta > Math.PI)
			delta -= 2*Math.PI;
		else if(delta < -Math.PI)
			delta += 2*Math.PI;
		
		/**
		 * Si on fait une correction, il faut vérifier la distance à la consigne et la correction
		 * Si elles sont grandes, alors on fait la correction en angle sans s'arrêter
		 * Si une au moins est petite, on annule la correction (par exemple, si le robot
		 * dépasse un peu la consigne, la correction le ferait se retourner ce qui
		 * n'est pas le résultat demandé)
		 */
		if(correction)
		{
			// 5 cm
			if(intermediaire.squaredDistance(consigne) > 2500 && Math.abs(delta) < Math.PI/2)
				trajectoire_courbe = true;
			else
				return;
		}
        try
        {
            if(!correction && !trajectoire_courbe)
            {
            	ObstacleRotationRobot obstacle = new ObstacleRotationRobot(position, orientation, angle);
	        	if(obstacle.isCollidingObstacleFixe())
	        	{
	        		log.debug("Le robot a demandé à tourner dans un obstacle. Ordre annulé.", this);
	        		throw new WallCollisionDetectedException();
	        	}
            }
            deplacements.tourneRelatif(delta);
            if(!trajectoire_courbe) // sans virage : la première rotation est bloquante
                while(!isMotionEnded()) // on attend la fin du mouvement
                    Sleep.sleep(sleep_boucle_acquittement);
            
/*            // TODO: passer en hook
            ObstacleRectangular obstacle = new ObstacleRectangular(position, consigne);
        	if(obstacle.isCollidingObstacleFixe())
        	{
        		log.debug("Le robot a demandé à avancer dans un obstacle. Ordre annulé.", this);
        		throw new WallCollisionDetectedException();
        	}
*/
            if(!seulementAngle)
            	deplacements.moveLengthwise(distance);
        } catch (SerialConnexionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Boucle d'acquittement générique. Retourne des valeurs spécifiques en cas d'arrêt anormal (blocage, capteur)
     *  	
     *  	false : si on roule
     *  	true : si on est arrivé à destination
     *  	exception : si patinage
     * 
     * 
     * @param detection_collision
     * @param sans_lever_exception
     * @return oui si le robot est arrivé à destination, non si encore en mouvement
     * @throws BlockedException
     * @throws FinMatchException 
     * @throws UnexpectedObstacleOnPathException
     */
    private boolean isMotionEnded() throws BlockedException, FinMatchException
    {
    	// TODO: débugger!
        // récupérations des informations d'acquittement
        try {
        	
        	// met a jour: 	l'écart entre la position actuelle et la position sur laquelle on est asservi
        	//				la variation de l'écart a la position sur laquelle on est asservi
        	//				la puissance demandée par les moteurs 	
            deplacements.refreshFeedbackLoopStatistics();
            
            // lève une exeption de blocage si le robot patine (ie force sur ses moteurs sans bouger) 
            deplacements.raiseExceptionIfBlocked();
            
            // robot arrivé?
            return !deplacements.isRobotMoving();

        } 
        catch (SerialConnexionException e) 
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isEnemyHere()
    {
		try {
			detectEnemy(true);
			return false;
		} catch (UnexpectedObstacleOnPathException e) {
			return true;
		}
    }
    
    /**
     * fonction vérifiant que l'on ne va pas taper dans le robot adverse. 
     * @param devant: fait la détection derrière le robot si l'on avance à reculons 
     * @throws UnexpectedObstacleOnPathException si obstacle sur le chemin
     */
    private void detectEnemy(boolean devant) throws UnexpectedObstacleOnPathException
    {
        int signe = -1;
        if(devant)
            signe = 1;
        
        int rayon_detection = largeur_robot/2 + distance_detection;
        Vec2 centre_detection = new Vec2((int)(signe * rayon_detection * Math.cos(orientation)), (int)(signe * rayon_detection * Math.sin(orientation)));
        centre_detection.plus(position);
        if(obstaclemanager.isObstacleMobilePresent(centre_detection, distance_detection))
        {
            log.warning("Ennemi détecté en : " + centre_detection, this);
            throw new UnexpectedObstacleOnPathException();
        }

    }

    /**
     * Met à jour position et orientation via la carte d'asservissement.
     * @throws FinMatchException 
     * @throws SerialConnexionException
     */
    private void updateCurrentPositionAndOrientation() throws FinMatchException
    {
        try {
            double[] infos = deplacements.getCurrentPositionAndOrientation();
            synchronized(position)
            {
	            position.x = (int)infos[0];
	            position.y = (int)infos[1];
            }
            orientation = infos[2]/1000; // car getCurrentPositionAndOrientation renvoie des milliradians
        }
        catch(SerialConnexionException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void updateConfig()
    {
    	deplacements.updateConfig();
        distance_detection = config.getInt(ConfigInfo.DISTANCE_DETECTION);
        distance_degagement_robot = config.getInt(ConfigInfo.DISTANCE_DEGAGEMENT_ROBOT);
        sleep_boucle_acquittement = config.getInt(ConfigInfo.SLEEP_BOUCLE_ACQUITTEMENT);
        angle_degagement_robot = config.getDouble(ConfigInfo.ANGLE_DEGAGEMENT_ROBOT);
    }

    /**
     * Arrête le robot.
     * @throws FinMatchException 
     */
    public void immobilise() throws FinMatchException
    {
        log.debug("Arrêt du robot en "+position, this);
        try {
            deplacements.immobilise();
        } catch (SerialConnexionException e) {
            e.printStackTrace();
        }           
    }

    /**
     * Met à jour la position. A ne faire qu'en début de match.
     * @param position
     * @throws FinMatchException 
     */
    public void setPosition(Vec2 position) throws FinMatchException {
        this.position = position.clone();
        try {
    		deplacements.setX(this.position.x);
            deplacements.setY(this.position.y);
        } catch (SerialConnexionException e) {
            e.printStackTrace();
        }
        Sleep.sleep(300);
    }

    /**
     * Met à jour l'orientation. A ne faire qu'en début de match.
     * @param orientation
     * @throws FinMatchException 
     */
    public void setOrientation(double orientation) throws FinMatchException {
        this.orientation = orientation;
        try {
    		deplacements.setOrientation(this.orientation);
        } catch (SerialConnexionException e) {
            e.printStackTrace();
        }
    }

    public Vec2 getPosition() throws FinMatchException
    {
        updateCurrentPositionAndOrientation();
        Vec2 out = position.clone();
        return out;
    }

    public double getOrientation() throws FinMatchException
    {
        updateCurrentPositionAndOrientation();
        	return orientation;
    }

    public void desasservit() throws FinMatchException
    {
        try
        {
            deplacements.disableRotationnalFeedbackLoop();
            deplacements.disableTranslationnalFeedbackLoop();
        } catch (SerialConnexionException e)
        {
            e.printStackTrace();
        }
    }

    public void setRotationnalSpeed(Speed speed) throws FinMatchException
    {
        try
        {
            deplacements.setRotationnalSpeed(speed);
        } catch (SerialConnexionException e)
        {
            e.printStackTrace();
        }
    }

    public void setTranslationnalSpeed(Speed speed) throws FinMatchException
    {
        try
        {
            deplacements.setTranslationnalSpeed(speed);
        } catch (SerialConnexionException e)
        {
            e.printStackTrace();
        }
    }

    
    public void asservit() throws FinMatchException
    {
        try
        {
            deplacements.enableRotationnalFeedbackLoop();
            deplacements.enableTranslationnalFeedbackLoop();
        } catch (SerialConnexionException e)
        {
            e.printStackTrace();
        }
    }
    
    public void initialiser_deplacements()
    {}
    
    public void disableRotationnalFeedbackLoop() throws FinMatchException
    {
    	try
		{
			deplacements.disableRotationnalFeedbackLoop();
		}
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
    }

    public void enableRotationnalFeedbackLoop() throws FinMatchException
    {
    	try
		{
			deplacements.enableRotationnalFeedbackLoop();
		}
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
    }

	public void disableTranslationalFeedbackLoop() throws FinMatchException {
    	try
		{
			deplacements.disableTranslationnalFeedbackLoop();
		}
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}

	public void close()
	{
		deplacements.closeLocomotion();
	}

}
