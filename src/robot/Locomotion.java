package robot;

import java.util.ArrayList;

import obstacles.ObstacleRectangular;
import obstacles.ObstacleRotationRobot;
import astar.arc.PathfindingNodes;
import container.Service;
import exceptions.BlockedException;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.SerialConnexionException;
import exceptions.UnableToMoveException;
import exceptions.UnexpectedObstacleOnPathException;
import exceptions.WallCollisionDetectedException;
import hook.Hook;
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
    private Vec2 consigne = new Vec2(); // La consigne est un attribut car elle peut être modifiée au sein d'un même mouvement.
    
    private double orientation; // l'orientation réelle du robot, pas la version qu'ont les robots
    private LocomotionCardWrapper deplacements;
    private boolean symetrie;
    private int sleep_boucle_acquittement = 10;
    private int distance_degagement_robot = 50;
    private double angle_degagement_robot;
    
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
            setOrientation(0f);
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
    public void turn(double angle, ArrayList<Hook> hooks) throws UnableToMoveException, FinMatchException, ScriptHookException
    {
        consigne.x = (int) (position.x + 1000*Math.cos(angle));
        consigne.y = (int) (position.y + 1000*Math.sin(angle));

        // l'appel à cette méthode sous-entend que le robot ne tourne pas
        // il va donc en avant si la distance est positive, en arrière si elle est négative
        vaAuPointGestionExceptions(hooks, false, true, false, true);
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
    public void moveLengthwise(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException, FinMatchException, ScriptHookException
    {
        log.debug("Avancer de "+Integer.toString(distance), this);
        
        Vec2 consigneNonInversee = new Vec2(); 
        consigneNonInversee.x = (int) (position.x + distance*Math.cos(orientation));
        consigneNonInversee.y = (int) (position.y + distance*Math.sin(orientation));        

        // En fait, ici on prend en compte que la symétrie va inverser la consigne...
        if(symetrie)
        {
        	consigne.x = -consigneNonInversee.x;
            consigne.y = consigneNonInversee.y;
        }
        else
        {
        	consigne.x = consigneNonInversee.x;
            consigne.y = consigneNonInversee.y;
        }

        // l'appel à cette méthode sous-entend que le robot ne tourne pas
        // il va donc en avant si la distance est positive, en arrière si elle est négative
        // si on est à 90°, on privilégie la marche avant
        vaAuPointGestionExceptions(hooks, false, distance >= 0, mur, false);
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
    public void followPath(ArrayList<PathfindingNodes> chemin, ArrayList<Hook> hooks, DirectionStrategy directionstrategy) throws UnableToMoveException, FinMatchException, ScriptHookException
    {
    	// TODO: trajectoire courbe
        for(PathfindingNodes point: chemin)
        {
            consigne = point.getCoordonnees().clone();
            vaAuPointGestionMarcheArriere(hooks, false, false, directionstrategy, false);
        }
    }

    /**
     * Bloquant. Gère la marche arrière automatique selon la stratégie demandée.
     * @param hooks
     * @param insiste
     * @throws UnableToMoveException
     * @throws FinMatchException 
     * @throws ScriptHookException 
     */
    private void vaAuPointGestionMarcheArriere(ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean mur, DirectionStrategy strategy, boolean seulementAngle) throws UnableToMoveException, FinMatchException, ScriptHookException
    {
    	if(strategy == DirectionStrategy.FORCE_BACK_MOTION)
            vaAuPointGestionExceptions(hooks, trajectoire_courbe, false, mur, seulementAngle);
    	else if(strategy == DirectionStrategy.FORCE_FORWARD_MOTION)
            vaAuPointGestionExceptions(hooks, trajectoire_courbe, true, mur, seulementAngle);
    	else
    	{
    		// Calcul du moyen le plus rapide
	        Vec2 delta = consigne.clone();
	        if(symetrie)
	            delta.x *= -1;
	        delta.minus(position);
	        // Le coeff 1000 vient du fait que Vec2 est constitué d'entiers
	        Vec2 orientationVec = new Vec2((int)(1000*Math.cos(orientation)), (int)(1000*Math.sin(orientation)));
	
	        // On regarde le produit scalaire; si c'est positif, alors on est dans le bon sens, et inversement
	        vaAuPointGestionExceptions(hooks, trajectoire_courbe, delta.dot(orientationVec) > 0, mur, seulementAngle);
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
     */
    private void vaAuPointGestionExceptions(ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean marcheAvant, boolean mur, boolean seulementAngle) throws UnableToMoveException, FinMatchException, ScriptHookException
    {
        int attente_ennemi_max = 600; // combien de temps attendre que l'ennemi parte avant d'abandonner
        int nb_iterations_deblocage = 2; // combien de fois on réessayer si on se prend un mur
        boolean recommence;
        do {
            recommence = false;
            try
            {
                vaAuPointGestionHookCorrectionEtDetection(hooks, trajectoire_courbe, marcheAvant, seulementAngle);
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
                        		deplacements.turn(orientation+angle_degagement_robot);
                        	else
                        		deplacements.turn(orientation-angle_degagement_robot);
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
     */
    private void vaAuPointGestionHookCorrectionEtDetection(ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean marcheAvant, boolean seulementAngle) throws BlockedException, UnexpectedObstacleOnPathException, FinMatchException, ScriptHookException, WallCollisionDetectedException
    {
        // le fait de faire de nombreux appels permet de corriger la trajectoire
        vaAuPointGestionSymetrie(trajectoire_courbe, marcheAvant, seulementAngle, false);
        do
        {
            updateCurrentPositionAndOrientation();
            
            // en cas de détection d'ennemi, une exception est levée
            detectEnemy(!marcheAvant);

            for(Hook hook : hooks)
                hook.evaluate();

            corrigeAngle(trajectoire_courbe, marcheAvant);


            Sleep.sleep(sleep_boucle_acquittement);
        } while(!isMotionEnded());
        
    }

    private void corrigeAngle(boolean trajectoire_courbe, boolean marcheAvant) throws BlockedException, FinMatchException, WallCollisionDetectedException
    {
    	vaAuPointGestionSymetrie(trajectoire_courbe, marcheAvant, true, true);
    }

    /**
     * Non bloquant. Gère la symétrie et la marche arrière.
     * @param point
     * @param sans_lever_exception
     * @param trajectoire_courbe
     * @param marche_arriere
     * @throws BlockedException 
     * @throws FinMatchException 
     * @throws WallCollisionDetectedException 
     */
    private void vaAuPointGestionSymetrie(boolean trajectoire_courbe, boolean marcheAvant, boolean seulementAngle, boolean correction) throws BlockedException, FinMatchException, WallCollisionDetectedException
    {
        Vec2 delta = consigne.clone();
        if(symetrie)
            delta.x = -delta.x;
        
        updateCurrentPositionAndOrientation();

        delta.minus(position);
        double distance = delta.length();
        
        double angle =  Math.atan2(delta.y, delta.x);
        // on suit ce que demande le boolean marcheAvant, en se retournant si besoin
        if(marcheAvant && distance < 0 || (!marcheAvant && distance > 0))
        {
            distance *= -1;
            angle += Math.PI;
        }
        
        vaAuPointGestionCourbe(angle, distance, trajectoire_courbe, seulementAngle, correction);
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
    private void vaAuPointGestionCourbe(double angle, double distance, boolean trajectoire_courbe, boolean seulementAngle, boolean correction) throws BlockedException, FinMatchException, WallCollisionDetectedException
    {
		double delta = orientation-angle % (2*Math.PI);
		delta = Math.abs(delta);
		if(delta > Math.PI)
			delta = 2*Math.PI - delta;
		
        // On interdit la trajectoire courbe si on doit faire un virage trop grand.
		if(delta > Math.PI/2)
			trajectoire_courbe = false;

		/**
		 * Si on fait une correction, il faut vérifier l'angle de correction.
		 * S'il est petit, alors on fait la correction en angle sans s'arrêter
		 * S'il n'est pas petit, on annule la correction (par exemple, si le robot
		 * dépasse la consigne, la correction va le faire se retourner ce qui
		 * n'est pas le résultat demandé)
		 */
		if(correction)
		{
			if(delta < 3*Math.PI/180)
				trajectoire_courbe = true;
			return;
		}
        try
        {
            deplacements.turn(angle);
            if(!correction && !trajectoire_courbe)
            {
            	ObstacleRotationRobot obstacle = new ObstacleRotationRobot(position, orientation, angle);
	        	if(obstacle.isCollidingObstacleFixe())
	        	{
	        		log.debug("Le robot a demandé à tourner dans un obstacle. Ordre annulé.", this);
	        		throw new WallCollisionDetectedException();
	        	}
            }
            if(!trajectoire_courbe) // sans virage : la première rotation est bloquante
                while(!isMotionEnded()) // on attend la fin du mouvement
                    Sleep.sleep(sleep_boucle_acquittement);
            
            // TODO: passer en hook
            ObstacleRectangular obstacle = new ObstacleRectangular(position, consigne);
        	if(obstacle.isCollidingObstacleFixe())
        	{
        		log.debug("Le robot a demandé à avancer dans un obstacle. Ordre annulé.", this);
        		throw new WallCollisionDetectedException();
        	}

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
            position.x = (int)infos[0];
            position.y = (int)infos[1];
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
        distance_detection = config.getInt(ConfigInfo.DISTANCE_DETECTION);
        distance_degagement_robot = config.getInt(ConfigInfo.DISTANCE_DEGAGEMENT_ROBOT);
        sleep_boucle_acquittement = config.getInt(ConfigInfo.SLEEP_BOUCLE_ACQUITTEMENT);
        angle_degagement_robot = config.getDouble(ConfigInfo.ANGLE_DEGAGEMENT_ROBOT);
        symetrie = config.getSymmetry();
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
        if(symetrie)
        	this.position.x = -this.position.x;
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
        if(symetrie)
        	this.orientation = Math.PI-this.orientation;
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
        if(symetrie)
        	out.x = -out.x;
        return out;
    }

    public double getOrientation() throws FinMatchException
    {
        updateCurrentPositionAndOrientation();
        if(symetrie)
        	return Math.PI-orientation;
        else
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
