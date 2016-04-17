package robot;

import java.util.ArrayList;

import permissions.ReadOnly;
import permissions.ReadWrite;
import container.Service;
import exceptions.FinMatchException;
import exceptions.UnableToMoveException;
import exceptions.UnexpectedObstacleOnPathException;
import hook.Hook;
import hook.types.HookDemiPlan;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import utils.Vec2;

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
    private final Vec2<ReadWrite> position = new Vec2<ReadWrite>();  // la position réelle du robot, pas la version qu'ont les robots
    
    private double orientation; // l'orientation réelle du robot, pas la version qu'ont les robots
    private LocomotionCardWrapper deplacements;
    private int sleep_boucle_acquittement = 10;
    private int distance_degagement_robot = 50;
    private double angle_degagement_robot;
    private ObstacleRotationRobot<ReadWrite> obstacleRotation;
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
    	Vec2<ReadOnly> consigne = new Vec2<ReadOnly>(
        (int) (position.x + 1000*Math.cos(angle)),
        (int) (position.y + 1000*Math.sin(angle))
        );

        // l'appel à cette méthode sous-entend que le robot ne tourne pas
        // il va donc en avant si la distance est positive, en arrière si elle est négative
        try {
			vaAuPointGestionExceptions(consigne, position.getReadOnly(), 0, hooks, true, false, true);
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
        log.debug("Avancer de "+Integer.toString(distance));
        
        Vec2<ReadOnly> consigne = new Vec2<ReadOnly>( 
        (int) (position.x + distance*Math.cos(orientation)),
        (int) (position.y + distance*Math.sin(orientation)));        

        // l'appel à cette méthode sous-entend que le robot ne tourne pas
        // il va donc en avant si la distance est positive, en arrière si elle est négative
        // si on est à 90°, on privilégie la marche avant
        try {
			vaAuPointGestionExceptions(consigne, position.getReadOnly(), 0, hooks, distance >= 0, mur, false);
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
    		Vec2<ReadOnly> intermediaire;
    		int differenceDistance;
    		if(i > 0)
    		{
    			intermediaire = chemin.get(i-1).objectifFinal.getCoordonnees();
    			differenceDistance = actuel.differenceDistance;
    		}
    		else
    		{
    			intermediaire = position.getReadOnly();
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
    		
            Vec2<ReadOnly> consigne = chemin.get(i).objectifFinal.getCoordonnees();
            
            try {
				vaAuPointGestionMarcheArriere(consigne, intermediaire, differenceDistance, hooks, false, directionstrategy, false);
			} catch (ChangeDirectionException e) {
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
    private void vaAuPointGestionMarcheArriere(Vec2<ReadOnly> consigne, Vec2<ReadOnly> intermediaire, int differenceDistance, ArrayList<Hook> hooks, boolean mur, DirectionStrategy strategy, boolean seulementAngle) throws UnableToMoveException, FinMatchException, ScriptHookException, ChangeDirectionException
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
	        Vec2<ReadWrite> delta = consigne.clone();
	        Vec2.minus(delta, position);
	        // Le coeff 1000 vient du fait que Vec2 est constitué d'entiers
	        Vec2<ReadWrite> orientationVec = new Vec2<ReadWrite>((int)(1000*Math.cos(orientation)), (int)(1000*Math.sin(orientation)));
	
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
    private void vaAuPointGestionExceptions(Vec2<ReadOnly> consigne, Vec2<ReadOnly> intermediaire, int differenceDistance, ArrayList<Hook> hooks, boolean marcheAvant, boolean mur, boolean seulementAngle) throws UnableToMoveException, FinMatchException, ScriptHookException, ChangeDirectionException
    {
        int attente_ennemi_max = 600; // combien de temps attendre que l'ennemi parte avant d'abandonner
        int nb_iterations_deblocage = 2; // combien de fois on réessaye si on se prend un mur
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
                        log.warning("On n'arrive plus à avancer. On se dégage");
                        if(seulementAngle)
                        {
                        	// on alterne rotation à gauche et à droite
                        	if((nb_iterations_deblocage & 1) == 0)
                        		deplacements.tourneRelatif(angle_degagement_robot);
                        	else
                        		deplacements.tourneRelatif(-angle_degagement_robot);
                        }
                        else if(marcheAvant)
                            deplacements.moveLengthwise(-distance_degagement_robot);
                        else
                            deplacements.moveLengthwise(distance_degagement_robot);
                        while(!deplacements.isMouvementFini());
                    	recommence = true; // si on est arrivé ici c'est qu'aucune exception n'a été levée
                    	// on peut donc relancer le mouvement
                    } catch (SerialConnexionException e1)
                    {
                        e1.printStackTrace();
                    } catch (BlockedException e1) {
                    	immobilise();
                        log.critical("On n'arrive pas à se dégager.");
					}
                    if(!recommence && nb_iterations_deblocage == 0)
                        throw new UnableToMoveException();
                }
            } catch (UnexpectedObstacleOnPathException e)
            {
            	immobilise();
            	long dateAvant = System.currentTimeMillis();
                log.critical("Détection d'un ennemi! Abandon du mouvement.");
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
                if(seulementAngle)
                	throw new UnableToMoveException();
                else
				try {
                	if(marcheAvant)
						deplacements.moveLengthwise(-distance_degagement_robot);
					else
	                    deplacements.moveLengthwise(distance_degagement_robot);
                    try {
						while(!deplacements.isMouvementFini());
					} catch (BlockedException e1) {
						throw new UnableToMoveException();
					}
                	recommence = true;
				} catch (SerialConnexionException e1) {
					e1.printStackTrace();
				}
                	
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
    private void vaAuPointGestionHookCorrectionEtDetection(Vec2<ReadOnly> consigne, Vec2<ReadOnly> intermediaire, int differenceDistance, ArrayList<Hook> hooks, boolean marcheAvant, boolean seulementAngle) throws BlockedException, UnexpectedObstacleOnPathException, FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException
    {
        // le fait de faire de nombreux appels permet de corriger la trajectoire
    	vaAuPointGestionDirection(consigne, intermediaire, differenceDistance, marcheAvant, seulementAngle, false);
        do
        {
        	// On attend la prochaine mise à jour de position
        	synchronized(this)
        	{
        		try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
            
            // en cas de détection d'ennemi, une exception est levée
            detectEnemy(!marcheAvant);

            for(Hook hook : hooks)
                hook.evaluate();

            corrigeAngle(consigne, marcheAvant);


//            Sleep.sleep(sleep_boucle_acquittement);
        } while(!deplacements.isMouvementFini());
        
    }

    private void corrigeAngle(Vec2<ReadOnly> consigne, boolean marcheAvant) throws BlockedException, FinMatchException, WallCollisionDetectedException
    {
    	vaAuPointGestionDirection(consigne, position.getReadOnly(), 0, marcheAvant, true, true);
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
    private void vaAuPointGestionDirection(Vec2<ReadOnly> consigne, Vec2<ReadOnly> intermediaire, int differenceDistance, boolean marcheAvant, boolean seulementAngle, boolean correction) throws BlockedException, FinMatchException, WallCollisionDetectedException
    {
        Vec2<ReadWrite> delta = consigne.clone();
        updateCurrentPositionAndOrientation();

        Vec2.minus(delta, intermediaire);
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
    private void vaAuPointGestionCourbe(Vec2<ReadOnly> consigne, Vec2<ReadOnly> intermediaire, double angle, double distance, boolean trajectoire_courbe, boolean seulementAngle, boolean correction) throws BlockedException, FinMatchException, WallCollisionDetectedException
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
            	if(obstacleRotation == null)
            		obstacleRotation = new ObstacleRotationRobot<ReadWrite>(position.clone(), orientation, angle);
            	else
            		ObstacleRotationRobot.update(obstacleRotation, position.getReadOnly(), orientation, angle);
            	if(obstacleRotation.isCollidingObstacleFixe())
	        	{
	        		log.debug("Le robot a demandé à tourner dans un obstacle. Ordre annulé.");
	        		throw new WallCollisionDetectedException();
	        	}
            }
            deplacements.tourneRelatif(delta);
            if(!trajectoire_courbe) // sans virage : la première rotation est bloquante
                while(!deplacements.isMouvementFini()) // on attend la fin du mouvement
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
        Vec2<ReadWrite> centre_detection = new Vec2<ReadWrite>((int)(signe * rayon_detection * Math.cos(orientation)), (int)(signe * rayon_detection * Math.sin(orientation)));
        Vec2.plus(centre_detection, position);
        if(obstaclemanager.isObstacleMobilePresent(centre_detection.getReadOnly(), distance_detection))
        {
            log.warning("Ennemi détecté en : " + centre_detection);
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
            orientation = infos[2];
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
    	obstaclemanager.updateConfig();
    	log.updateConfig();
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
        log.debug("Arrêt du robot en "+position);
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
    public void setPosition(Vec2<ReadOnly> position) throws FinMatchException {
        Vec2.copy(position, this.position);
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

    public Vec2<ReadOnly> getPosition() throws FinMatchException
    {
        updateCurrentPositionAndOrientation();
        return position.getReadOnly();
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
