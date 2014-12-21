package robot;

import java.util.ArrayList;

import obstacles.ObstacleManager;
import container.Service;
//import hook.Callback;
//import hook.Executable;
import hook.Hook;
import enums.ConfigInfo;
import enums.PathfindingNodes;
import enums.Speed;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
//import hook.methodes.ChangeConsigne;
//import hook.sortes.HookGenerator;
import exceptions.Locomotion.BlockedException;
import exceptions.Locomotion.UnexpectedObstacleOnPathException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import robot.cardsWrappers.LocomotionCardWrapper;
import smartMath.Vec2;
import utils.Log;
import utils.Config;
import utils.Sleep;

/**
 * Entre LocomtionCardWrapper (appels à la série) et RobotReal (Interface avec les utilisateurs de haut niveau), Locomotion
 * s'occupe de la position, de la symétrie, des hooks, des trajectoires courbes et tente de réagir en cas de problème
 * .
 * Structure, du bas au haut niveau: symétrie, hook, trajectoire courbe et blocage.
 * 
 * Les méthodes "non-bloquantes" se finissent alors que le robot roule encore. (les méthodes non-bloquantes s'exécutent très rapidement)
 * Les méthodes "bloquantes" se finissent quand le robot a atteind le point et l'orientation désirée en fin de mouvment.
 * @author pf, marsu
 *
 */
// TODO: les utilisateurs de cette classe utilisent la méthode turn. Mais turn n'utilise pas les fonction moveForwardInDirection, il faut retravailler la série de fonction moveForwardInDirection
// afin de la rendre plus générique ( notamment, moveInDirection est semi-bloquante: la rotation pour se mettre dans la bonne direction est bloquante, mais pas la tranlation qui la suit)
// il faudrait que toutes les rotations (celles de moveForwardInDirection et celles appellées directement par l'utilisateur via la méthode turn) passent idéalement par les moveForwardInDirection
// ca mermetterait d'avoir une gestion propre des hooks, des capteurs, etc.


// TODO: gestion propre des exceptions de communication série: Les exceptions de séries sont toujours mise dans des try/catch avec des printStackTrace. Le problème c'est que le comportement du
// java n'est pas déterminé en cas de coupure de la connexion. Il faudrait que les exceptions soient remontées quand nécessaire, voir s'il faut interrompre le mouvement dans le code, et même
// le remonter au code de très haut niveau si par exemeple l'asservissement meurs en plein match. (histoire que l'IA puisse prendre une décision en conséquence)
public class Locomotion implements Service
{

	/** système de log sur lequel écrire*/
	private Log log;
	
	/** endroit ou lire la configuration du robot */
	private Config config;
	
	private ObstacleManager obstaclemanager;
	
	/** la longueur du robot (ie la distance qui sépare son devant de son arrière)
	 * Cette valeur est utilisée pour placer le disque devant le robot ou l'on va vérifier qu'il n'y a pas d'obstacle */
	private int robotLengh; //TODO: cette variable n'a pas sa place ici. Elle n'est même pas initialisée ici
	
	/** Rayon du disque que l'on place devant le robot et ou l'on vérifie auprès de la table qu'il n'y a pas d'obstacle si on doit avancer dans ce disque.  */
	private int obstacleDetectionDiscRadius;
	
	/** Position courante du robot sur la table. La coordonnée X est multipliée par -1 si on est équipe jaune */
	private Vec2 position = new Vec2(); //TODO: spécifier dans la doc a un endroit logique ou est défini le système d'axe (plus le système d'orientation)
	
	/** Position de la table que le robot cherche a ateindre. Elle peut être modifiée au sein d'un même mouvement. */
	private Vec2 aim = new Vec2();
	
	/** Le système de trajectoire courbe ou de "tourner en avançant" fait rêver des génération d'INTechiens,
	 * mais ça a toujours fais perdre du temps pour un truc qui ne marche pas */
	private boolean allowCurvedPath = false;

	/** orientation actuelle du robot. L'orientation est multipliée par -1 si on est équipe jaune */
	private double orientation;
	
	/** interface de communication avec la carte d'asservissement */
	private LocomotionCardWrapper mLocomotionCardWrapper;
	
	/** la table est symétrisée si on est équipe jaune */
	private boolean symmetry;
	
	/** Temps d'attente e miliseconde entre deux vérification de l'état du déplacement quand le robot bouge. (arrivée, blocage, etc.) */
	private int minimumDelayBetweenMovementStatusCheck = 50;
	
	/** nombre maximum d'excpetions levés d'un certain type lors d'un déplacement */
	private int maxAllowedExceptionCount = 5;
	
	/** distance en mm sur laquelle on revient sur nos pas avant de réessayer d'atteindre le point d'arrivée lorsque le robot faire face a un obstacle qui immobilise mécaniquement le robot */
	private int blockedExceptionRetraceDistance = 50;
	
	/** temps en ms que l'on attends après avoir vu un ennemi avant de réessayer d'atteindre le point d'arrivée lorsque le robot détecte que sa route est obstruée */
	private int unexpectedObstacleOnPathRetryDelay = 200; 
	
	/** angle en radiant de dégagement du robot utilisé lorsque l'utilisateur demande une rotation (via la méthode turn), et que le robot rencontre un obstacle: le robot tourne alors de cet angle
	 * dans l'autre sens que celui demandé pour se dégager de l'obstacle rencontré  */
	private double pullOutAngleInCaseOfBlockageWhileTurning; // TODO: passer la réaction du robot en cas de blocage mécanique en cours de rotation dans BlockedExceptionReaction
	
	/** ancienne valeur de l'assevissement, dans l'ordre: X, Y, orientation. */
	private double[] oldInfos;

	/**
	 * Instancie le service de d�placement haut niveau du robot.
	 * Appell� par le container
	 * @param log : la sortie de log à utiliser
	 * @param config : sur quel objet lire la configuration du match
	 * @param table : l'aire de jeu sur laquelle on se d�place
	 * @param mLocomotion : service de d�placement de bas niveau
	 */	
	public Locomotion(Log log, Config config, LocomotionCardWrapper mLocomotion, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.config = config;
		this.mLocomotionCardWrapper = mLocomotion;
		//        this.hookgenerator = hookgenerator;
		this.obstaclemanager = obstaclemanager;
		updateConfig();
	}

	/**
	 * Recale le robot pour qu'il sache ou il est sur la table et dans quel sens il se trouve.
	 * La méthode est de le faire pecuter contre les coins de la table, ce qui lui donne des repères.
	 * TODO: réécrire, et documenter en fonction de la table de cette année.
	 */
	public void readjust()
	{
		try
		{
	        setTranslationnalSpeed(Speed.READJUSTMENT.PWMTranslation);
	        setRotationnalSpeed(Speed.READJUSTMENT.PWMRotation);
	        
			// Retrouve l'abscisse du robot en foncant dans un mur d'abscisse connue
			log.debug("recale X",this);

			moveLengthwise(-200, null, true);
			mLocomotionCardWrapper.setTranslationnalSpeed(200);
			mLocomotionCardWrapper.disableRotationnalFeedbackLoop();
			Sleep.sleep(1000);
			moveLengthwise(-200, null, true);
			mLocomotionCardWrapper.enableRotationnalFeedbackLoop();
			mLocomotionCardWrapper.setTranslationnalSpeed(Speed.READJUSTMENT.PWMTranslation);

			position.x = 1500 - 165;
			if(symmetry)
			{
				setOrientation(0f);
				mLocomotionCardWrapper.setX(-1500+165);
			}
			else
			{
				mLocomotionCardWrapper.setX(1500-165);
				setOrientation(Math.PI);
			}


			Sleep.sleep(500);
			moveLengthwise(40, null, true);
			turn(-Math.PI/2, null, false);


			log.debug("recale Y",this);
			moveLengthwise(-600, null, true);
			mLocomotionCardWrapper.setTranslationnalSpeed(200);
			mLocomotionCardWrapper.disableRotationnalFeedbackLoop();
			Sleep.sleep(1000);
			moveLengthwise(-200, null, true);
			mLocomotionCardWrapper.enableRotationnalFeedbackLoop();
			mLocomotionCardWrapper.setTranslationnalSpeed(Speed.READJUSTMENT.PWMTranslation);
			position.y = 2000 - 165;
			mLocomotionCardWrapper.setY(2000 - 165);


			log.debug("Done !",this);
			Sleep.sleep(500);
			moveLengthwise(100, null, false);
			orientation = -Math.PI/2;
			setOrientation(-Math.PI/2);
			//Normalement on se trouve à (1500 - 165 - 100 = 1225 ; 2000 - 165 - 100 = 1725)
			mLocomotionCardWrapper.enableRotationnalFeedbackLoop();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Fait tourner le robot (méthode bloquante)
	 * @param angle : valeur absolue en radiant de l'orientation que le robot doit avoir après cet appel
	 * @param hooksToConsider hooks a considérer lors de ce déplacement. Le hook n'est déclenché que s'il est dans cette liste et que sa condition d'activation est remplie
	 * @param expectsWallImpact true si le robot doit s'attendre a percuter un mur au cours de la rotation. false si les alentours du robot sont sensés être dégagés.
	 * @throws UnableToMoveException losrque quelque chose sur le chemin cloche et que le robot ne peut s'en défaire simplement: bloquage mécanique immobilisant le robot ou obstacle percu par les capteurs
	 * @throws FinMatchException 
	 * @throws ScriptHookException 
	 */
	// TODO: refactor massif de la facon dont le robot tourne en haut niveau. La rotation ne passe pas du tout par le système de moveForwardInDirection. 
	// C'est la faute au système de moveForwardInDirection, qui n'est pas complètement générique. notamment BlockedExceptionReaction qui est en réalité spécialisé dans une réaction a un blocage lors d'une translation.
	public void turn(double angle, ArrayList<Hook> hooksToConsider, boolean expectsWallImpact) throws UnableToMoveException, FinMatchException, ScriptHookException
	{

		// prends en compte la symétrie: si on est équipe jaune, et non équipe verte on doit tourner de PI moins l'angle
		if(symmetry)
			angle = Math.PI-angle;
		
		// on souhaite rester ou l'on est : la position d'arrivée est le position courrante
		position.copy(aim);

		// Tourne-t-on dans le sens trigonométrique?
		// C'est important de savoir pour se dégager.
		boolean isTurnCCW = angle > orientation; // CCw pour  Counter-ClocWise, ie sens trigonométrique

		// On donnera l'odre de se déplacer au robot
		boolean haveToGiveOrderToMove = true;
		
		try
		{
			// boucle surveillant que tout se passe bien lors de la rotation
			while(!isMovementFinished()) // on attend la fin du mouvement
			{
				// donne (éventuellement de nouveau) l'ordre de se déplacer
				if(haveToGiveOrderToMove)
				{
					oldInfos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();
					mLocomotionCardWrapper.turn(angle);
				}
				
				// Vérifie si les hooks fournis doivent être déclenchés, et les déclenche si besoin
				haveToGiveOrderToMove = false;
				if(hooksToConsider != null)
					for(Hook currentHook : hooksToConsider)
					{
						// savegarde la consige de position, ca si un hook fait appel a cette classe, il écrase l'ancienne valeur de aim
						Vec2 oldAim = aim.clone();
						
						// vérifie si ce hook doit être déclenché, le déclenche si c'est le cas, et fera renvoyer au prochain tour de while l'ordre de déplacement si le hook a fait bouger le robot
						currentHook.evaluate();
						
						// restaure le aim de ce déplacement (au lieu ce celui d'un hook)
						aim = oldAim;
					}
				
				//attends un peu entre deux tours de boucle histoire de ne pas trop spammer la connexion série
				Sleep.sleep(minimumDelayBetweenMovementStatusCheck);
			}
		}
		catch(BlockedException e)	// TODO: a fusionner avec BlockedExceptionReaction
		{
			updatePositionAndOrientation();
			
			// Si on ne devait pas tapper dans un obstacle, il faut essayer de s'en dégager puis faire rementer le problème
			if(!expectsWallImpact)
			{
				// essaye de s'en dégager
				try
				{
					// on se dégage en sens trigo si on tournait initialement en sens horraire, et réciproquement
					if(isTurnCCW ^ symmetry)
						mLocomotionCardWrapper.turn(orientation+pullOutAngleInCaseOfBlockageWhileTurning);
					else
						mLocomotionCardWrapper.turn(orientation-pullOutAngleInCaseOfBlockageWhileTurning);
				}

				catch(SerialConnexionException e1)
				{
					e1.printStackTrace();
				}

				// informe l'utilisateur qu'un problème est survenu
				throw new UnableToMoveException();
			}
		}
		catch(SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Fait avancer le robot de la distance spécifiée. Le robot garde son orientation actuelle et va simplement avancer
	 * C'est la méthode que les utilisateurs (externes au développement du système de locomotion) vont utiliser
	 * Cette méthode est bloquante: son exécution ne se termine que lorsque le robot a atteint le point d'arrivée
	 * @param distance en mm que le robot doit franchir
	 * @param hooksToConsider hooks a considérer lors de ce déplacement. Le hook n'est déclenché que s'il est dans cette liste et que sa condition d'activation est remplie	 
	 * @param expectsWallImpact true si le robot doit s'attendre a percuter un mur au cours du déplacement. false si la route est sensée être dégagée.
	 * @throws UnableToMoveException losrque quelque chose sur le chemin cloche et que le robot ne peut s'en défaire simplement: bloquage mécanique immobilisant le robot ou obstacle percu par les capteurs
	 * @throws FinMatchException 
	 * @throws ScriptHookException 
	 */
	public void moveLengthwise(int distance, ArrayList<Hook> hooksToConsider, boolean expectsWallImpact) throws UnableToMoveException, FinMatchException, ScriptHookException
	{
		// demande une position et une oriantation a jour du robot
		updatePositionAndOrientation();

		
		// calcule la position a atteindre en fin de mouvement
		aim.x = (int) (position.x + distance*Math.cos(orientation));
		aim.y = (int) (position.y + distance*Math.sin(orientation));
		if(symmetry)	// on considère le point d'abscisse opposée si on est équipe jaune et non verte
			aim.x = -aim.x;
		  
		
		// choisit de manière intelligente la marche arrière ou non
		// mais cette année, on ne peut aller, de manière automatique, que tout droit (pas de capteur d'obstacle à l'arrière).
		// Donc on force marche_arriere à true. (ie pas de décision intelligente)

		/*
		 * Ce qui suit est une méthode qui permet de choisir si la marche arrière
		 * est plus rapide que la marche avant. Non utilisé, mais ca peut servir un jour.
		 */
		
		/*
		 // TODO : refactor
        Vec2 delta = aim.clone();
        if(symmetry)
            delta.x *= -1;
        delta.minus(position);
        // Le coeff 1000 vient du fait que Vec2 est constitué d'entiers
        Vec2 orientationVec = new Vec2((int)(1000*Math.cos(orientation)), (int)(1000*Math.sin(orientation)));

        // On regarde le produit scalaire; si c'est positif, alors on est dans le bon sens, et inversement
        boolean marche_arriere = delta.dot(orientationVec) > 0;
		*/
		
		
		// fais le déplacement
		moveInDirectionExceptionHandler(hooksToConsider, false, distance < 0, expectsWallImpact);

		// demande une position et une oriantation a jour du robot
		updatePositionAndOrientation();
	}

	/**
	 * Fais suivre un chemin au robot décrit par une liste de point.
	 * @param path liste des points sur la table a atteindre, dans l'ordre. Le robot parcourera une ligne brisée dont les sommets sont ces points.
	 * @param hooksToConsider hooks a considérer lors de ce déplacement. Le hook n'est déclenché que s'il est dans cette liste et que sa condition d'activation est remplie
	 * @throws UnableToMoveException losrque quelque chose sur le chemin cloche et que le robot ne peut s'en défaire simplement: bloquage mécanique immobilisant le robot ou obstacle percu par les capteurs
	 * @throws FinMatchException 
	 * @throws ScriptHookException 
	 */
	public void followPath(ArrayList<PathfindingNodes> path_pathfinding, ArrayList<Hook> hooksToConsider) throws UnableToMoveException, FinMatchException, ScriptHookException
	{
		ArrayList<Vec2> path = new ArrayList<Vec2>();
		for(PathfindingNodes n: path_pathfinding)
			path.add(n.getCoordonnees());
		
		// en cas de coup de folie a INTech, on active la trajectoire courbe.
		if(allowCurvedPath)
		{
			log.critical("Désactive la trajectoire courbe, pauvre fou! (appel a followPath ignoré)", this);
			// TODO: refactor
			/*            consigne = chemin.get(0).clone();
            ArrayList<Hook> hooks_trajectoire = new ArrayList<Hook>();
            for(int i = 0; i < chemin.size()-2; i++)
            {
                Hook hook_trajectoire_courbe = hookgenerator.hook_position(chemin.get(i), anticipation_trajectoire_courbe);
                Executable change_consigne = new ChangeConsigne(chemin.get(i+1), this);
                hook_trajectoire_courbe.ajouter_callback(new Callback(change_consigne, true));
                hooks_trajectoire.add(hook_trajectoire_courbe);
            }

            // TODO: en cas de choc avec un bord, recommencer sans trajectoire courbe?

            // Cette boucle est utile si on a "raté" des hooks.
            boolean nouvel_essai = false;
            do {
                if(nouvel_essai)
                    va_au_point_marche_arriere(hooks, hooks_trajectoire, false, false);
                va_au_point_marche_arriere(hooks, hooks_trajectoire, true, false);
                nouvel_essai = false;
                if(hooks_trajectoire.size() != 0)
                    nouvel_essai = true;
            } while(nouvel_essai);

            log.debug("Fin en: "+position, this);
            // Le dernier trajet est exact (sans trajectoire courbe)
            // afin d'arriver exactement au bon endroit.
            consigne = chemin.get(chemin.size()-1).clone();
            va_au_point_marche_arriere(hooks, null, false, false);         */   
		}
		
		// sinon on fait rotation puis translation pour chaque point du chemin
		else
			for(Vec2 point: path)
			{
				point.copy(aim);
				moveInDirectionExceptionHandler(hooksToConsider, false, false, false);
			}
	}

	/**
	 * Intercepte les exceptions des capteurs (on va rentrer dans un ennemi) et les exceptions de l'asservissement (le robot est mécaniquement bloqué)
	 * Déclenche différentes réactions sur ces évènements, et si les réactions mises en places ici sont insuffisantes (on n'arrive pas a se dégager)
	 * fais remonter l'exception a l'utilisateur de la classe
	 * @param hooksToConsider hooks a considérer lors de ce déplacement. Un hook n'est déclenché que s'il est dans cette liste et que sa condition d'activation est remplie
	 * @param allowCurvedPath true si l'on autorise le robot à se déplacer le long d'une trajectoire curviligne.  false pour une simple ligne brisée
	 * @param isBackward true si le déplacement doit se faire en marche arrière, false si le robot doit avancer en marche avant.
	 * @param expectsWallImpact true si le robot doit s'attendre a percuter un mur au cours du déplacement. false si la route est sensée être dégagée.
	 * @throws UnableToMoveException losrque quelque chose sur le chemin cloche et que le robot ne peut s'en défaire simplement: bloquage mécanique immobilisant le robot ou obstacle percu par les capteurs
	 * @throws FinMatchException 
	 * @throws ScriptHookException 
	 */
	private void moveInDirectionExceptionHandler(ArrayList<Hook> hooksToConsider, boolean allowCurvedPath, boolean isBackward, boolean expectsWallImpact) throws UnableToMoveException, FinMatchException, ScriptHookException
	{
		// nombre d'exception (et donc de nouvels essais) que l'on va lever avant de prévenir
		// l'utilisateur en cas de bloquage mécanique du robot l'empéchant d'avancer plus loin
		int blockedExceptionStillAllowed = 2;
		if (blockedExceptionStillAllowed > maxAllowedExceptionCount)
			blockedExceptionStillAllowed = maxAllowedExceptionCount;
		
		// nombre d'exception (et donc de nouvels essais) que l'on va lever avant de prévenir
		// l'utilisateur en cas de découverte d'un obstacle imprévu (robot adverse ou autre) sur la route
		int unexpectedObstacleOnPathExceptionStillAllowed = 2;
		if (unexpectedObstacleOnPathExceptionStillAllowed > maxAllowedExceptionCount)
			unexpectedObstacleOnPathExceptionStillAllowed = maxAllowedExceptionCount;
		
		
		// drapeau indiquant s'il faut retenter d'atteindre le point d'arrivée. Initialisé à vrai pour l'essai initial.
		boolean tryAgain = true;
		// on essaye (de nouveau) d'aller jusqu'au point d'arrivée du mouvement
		// cette boucle prend fin soit quand on est arrivée, soit lors d'une exception de type UnableToMoveException
		while(tryAgain)
		{
		
			tryAgain = false;
			try
			{
				moveInDirectionEventWatcher(hooksToConsider, allowCurvedPath, isBackward);
			}
			
			// Si on remarque que le robot a percuté un obstacle l'empéchant d'avancer plus loin
			catch (BlockedException e)
			{
				// Réaction générique aux exceptions de déplacement du robot
				generalLocomotionExceptionReaction(blockedExceptionStillAllowed);
				
				//On tolère une exception de moins
				blockedExceptionStillAllowed--;
				
				// On réagit spécifiquement à la présence d'un bloquage mécanique du robot
				tryAgain = BlockedExceptionReaction(e, isBackward, expectsWallImpact);
			}
			
			// Si on a vu un obstacle inattendu sur notre chemin (robot ennemi ou autre)
			catch (UnexpectedObstacleOnPathException e)
			{
				// Réaction générique aux exceptions de déplacement du robot
				generalLocomotionExceptionReaction(unexpectedObstacleOnPathExceptionStillAllowed);
				
				//On tolère une exception de moins
				unexpectedObstacleOnPathExceptionStillAllowed--;
				
				// Réagit spécifiquement à la présence d'un obstacle
				tryAgain = unexpectedObstacleOnPathExceptionReaction(e);
			}
			catch (SerialConnexionException e)
			{
				// TODO: faire une réaction propre si la carte d'asser déconne
				log.critical("La carte d'asservissement a cessé de répondre !", this);
				e.printStackTrace();
			}
		} // while

		// si on arrive ici, c'est que l'on est au point d'arrivée
	}
	
	/**
	 * Réaction générique face a un souci dans le déplacment du robot. (arreter les moteurs de propultion par exemple)
	 * @param exceptionCountStillAllowed nombre d'exception pour ce déplacement que l'on tolère encore avant de remonter le problème a l'utilisateur de la classe
	 * @throws UnableToMoveException exception lancée quand on ne tolère plus de soucis interne au déplacement. Elle indique soit un obstacle détecté sur la route, soit que le robot a un blocage mécanique pour continuer a avancer
	 * @throws FinMatchException 
	 */
	private void generalLocomotionExceptionReaction(int exceptionCountStillAllowed) throws UnableToMoveException, FinMatchException
	{
		log.warning("Exception de déplacement lancée, Immobilisation du robot.", this);
		/* TODO: si on veut pouvoir enchaîner avec un autre chemin, il ne faut pas arrêter le robot.
		 * ATTENTION! ceci peut être dangereux, car si aucun autre chemin ne lui est donné, le robot va continuer sa course et percuter l'obstacle ! */
		immobilise();
		
		// Si cette exception fait dépasser le quota autorisé, on la remonte a l'utilisateur de la classe
		if(exceptionCountStillAllowed <= 0)
		{
			log.critical("Abandon du déplacement.", this);
			throw new UnableToMoveException();
		}
	}
	
	
	/**
	 * Réaction face a un bloquage mécanique du robot l'empéchant d'avancer. (les codeuses ne tournent plus, donc le robot ne bouge pas, alors que les moteurs de propultion sont en marche)
	 * @param e l'execption à laquelle on réagit
	 * @param isBackward true si le déplacement doit se faire en marche arrière, false si le robot doit avancer en marche avant.
	 * @param expectsWallImpact true si le robot doit s'attendre a percuter un mur au cours du déplacement. false si la route est sensée être dégagée.
	 * @return true si l'on doit essayer de nouveau d'atteindre le point d'arrivée du mouvement, faux si l'on doit rester sur place.
	 * @throws FinMatchException 
	 */
	private boolean BlockedExceptionReaction(BlockedException e, boolean isBackward, boolean expectsWallImpact) throws FinMatchException
	{
		// valeur de retour: faut-t-il réessayer d'atteindre le point d'arrivée ? Par défaut, non.
		boolean out  = false;
		
		// si on a indiqué qu'il fallait s'attendre a percuter quelque chose, l'exception est normale. Sinon, on doit réagir.
		if(!expectsWallImpact)
		{
			
			log.warning("On n'arrive plus à avancer. On se dégage et on retente d'aller jusqu'au point demandé.", this);
			try
			{
				// On cherche a se dégager de cet obstacle : on reviens sur nos pas, et on reprend le mouvement
				// si le mouvement était initialent en marche arrière, revenir sur nos pas est en marche avant, sinon c'est en marche arrière
				if(isBackward)
					mLocomotionCardWrapper.moveLengthwise(blockedExceptionRetraceDistance);
				else
					mLocomotionCardWrapper.moveLengthwise(-blockedExceptionRetraceDistance);
				
				// on suppose qu'on s'est dégagé, alors on va réessayer d'atteindre le point d'arrivée
				out = true;
			}
			catch (SerialConnexionException e1)
			{
				e1.printStackTrace();
			}
			
		}
		
		return out;
	}
	

	/**
	 * Réaction du robot lorsque l'on détecte a distance sur notre route un obstacle qui l'obstrue
	 * @param e l'execption à laquelle on réagit
	 * @param isBackward true si le déplacement doit se faire en marche arrière, false si le robot doit avancer en marche avant.
	 * @param expectsWallImpact true si le robot doit s'attendre a percuter un mur au cours du déplacement. false si la route est sensée être dégagée.
	 * @return true si l'on doit essayer de nouveau d'atteindre le point d'arrivée du mouvement, faux si l'on doit rester sur place.
	 */
	private boolean unexpectedObstacleOnPathExceptionReaction(UnexpectedObstacleOnPathException e) throws UnableToMoveException
	{
		log.warning("Détection d'un obstacle innatendu sur notre route !", this);
		
		// On suppose avoir vu un robot ennemi passer sous notre nez.
		// On attends donc juste un peu avant de réessayer
		Sleep.sleep(unexpectedObstacleOnPathRetryDelay);
		
		return true;
	}

	
	/**
	 * Méthode bloquante surveillant tout le long du déplacement s'il y a des évènement qui méritent un traitement.
	 * Les hooks et la détection d'obstacle a distance font partie de ces évènnements.
	 * Les évènements de type hooks seront traités en interne, les évènements empéchant le robot de continuer le déplacement sont signalé à l'utilisateur par des exceptions 
	 * @param hooksToConsider hooks a considérer lors de ce déplacement. Le hook n'est déclenché que s'il est dans cette liste et que sa condition d'activation est remplie
	 * @param allowCurvedPath true si l'on autorise le robot à se déplacer le long d'une trajectoire curviligne.  false pour une simple ligne brisée
	 * @param isBackward true si le déplacement doit se faire en marche arrière, false si le robot doit avancer en marche avant.
	 * @throws BlockedException en cas de blocage mécanique du robot: un obstacle non détecté par les capteurs a distance immobilise le robot
	 * @throws UnexpectedObstacleOnPathException en cas de détection par les capteurs a distance d'un obstacle sur la route que le robot s'apprête a suivre
	 * @throws SerialConnexionException si la carte d'asservissement cesse de répondre
	 * @throws FinMatchException 
	 * @throws ScriptHookException 
	 */
	private void moveInDirectionEventWatcher(ArrayList<Hook> hooksToConsider, boolean allowCurvedPath, boolean isBackward) throws BlockedException, UnexpectedObstacleOnPathException, SerialConnexionException, FinMatchException, ScriptHookException
	{	
		// On donnera l'odre de se déplacer au robot
		boolean haveToGiveOrderToMove = true;
		
		// Surveille les évènements qui peuvent survenir durant le déplacement
		// le mouvement dure tant qu'il n'est pas fini
		while(!isMovementFinished() || haveToGiveOrderToMove)	
		{
			// donne (éventuellement de nouveau) l'ordre de se déplacer
			if(haveToGiveOrderToMove)
				moveInDirectionPlanner(allowCurvedPath, isBackward, allowCurvedPath);
			
			// vérifie qu'il n'y a pas de blocage mécanique (n'importe quoi faisant que les moteurs tournent sans que les codeuses tournent)
			// TODO: il y a double emploi entre isMovementFinished et checkRobotNotBlocked, les deux vérifient de deux facons différentes que le robot n'est pas mécaniquement bloqué. Il faut centraliser la vérification.
			checkRobotNotBlocked();
			
			// met a jour ou nous sommes sur la table
			updatePositionAndOrientation();
			
			// vérifie qu'il n'y a rien la ou l'on se dirige qui pourrait obstruer le passage
			checkPathIsObstacleFree(!isBackward);

			// Vérifie si les hooks fournis doivent être déclenchés, et les déclenche si besoin
			haveToGiveOrderToMove = false;
			if(hooksToConsider != null)
				for(Hook hook : hooksToConsider)
				{
					// savegarde la consige de position, ca si un hook fait appel a cette classe, il écrase l'ancienne valeur de aim
					Vec2 oldAim = aim.clone();
					
					// vérifie si ce hook doit être déclenché, le déclenche si c'est le cas, et fera renvoyer au prochain tour de while l'ordre de déplacement si le hook a fait bouger le robot
					hook.evaluate();
					
					// restaure le aim de ce déplacement (au lieu ce celui d'un hook)
					aim = oldAim;
				}
			
			// on attends un peu pour ne pas saturer la série
			Sleep.sleep(minimumDelayBetweenMovementStatusCheck);
		}
	}

	/**
	 * Calcule l'angle duquel il faut tourner, de la distance de laquelle il faut avancer, et transmet les instructions au bas niveau.
	 * Non bloquant. Les calculs tiennent compte de la demande de symétrie et de marche arrière.
	 * @param allowCurvedPath true si l'on autorise le robot à se déplacer le long d'une trajectoire curviligne.  false pour une simple ligne brisée
	 * @param isBackward true si le déplacement doit se faire en marche arrière, false si le robot doit avancer en marche avant.
	 * @throws BlockedException en cas de blocage mécanique du robot: un obstacle non détecté par les capteurs a distance immobilise le robot
	 * @throws FinMatchException 
	 */
	private void moveInDirectionPlanner(boolean allowCurvedPath, boolean isBackward, boolean correction) throws BlockedException, FinMatchException
	{
		// Calcul du vecteur de déplacement: on doit se déplacer du vecteur égal a la position visée soustrait de la position actuelle
		// mise a jour de la position actuelle pour avoir un vecteur de déplacement a jour
		long t1 = System.currentTimeMillis();
		updatePositionAndOrientation();
		long t2 = System.currentTimeMillis();
		
		Vec2 displacement = aim.clone();
		if(symmetry)	// on oppose la composante X si l'on est de l'équipe jaune et non verte
			displacement.x = -displacement.x;
		// soustraction de la position actuelle a la position visée
		displacement.minus(position);
				
		// le robot doit avancer d'une distance égale a la longeur du vecteur délacement
		double distance = displacement.length();
		
		// TODO: trouver a quoi sert cette instruction conditionelle
		if(correction)
			distance -= (t2-t1);

		// calcul de l'angle duquel le robot doit tourner pour pointer dans la bonne direction avant d'avancer
		double angle =  Math.atan2(displacement.y, displacement.x);

		//gestion de la marche arrière du déplacement (peut aller à l'encontre de marche_arriere)
		// Si on demande faire ce mouvemnet en marche arrière, on doit tourner d'un demi-tour supplémentaire, puis avancer d'une distance négative.
		if(isBackward)
		{
			distance *= -1;
			angle += Math.PI;
		}        

		// transmet les instructions de mouvement aux cartes électroniques
		moveInDirection(angle, distance, allowCurvedPath);

	}

	/**
	 * Fait avancer le robot de la distance voulue dans la direction d�sir�e
	 * Cette classe est publique pour permettre aux utilisateurs d'avoir un contrôle parfois bas niveau sur le robot
	 * compatible avec les trajectoires courbes.
	 * Le d�placement n'est pas bloquant, mais le changement d'orientation pour que l'avant du robot pointe dans la bonne direction l'est.
	 * @param direction valeur relative en radian indiquant la direction dans laquelle on veut avancer
	 * @param distance valeur en mm indiquant de combien on veut avancer.
	 * @param allowCurvedPath si true, le robot essayera de tourner et avancer en m�me temps
	 * @throws BlockedException si blocage mécanique du robot en chemin (pas de gestion des capteurs ici)
	 * @throws FinMatchException 
	 */
	public void moveInDirection(double direction, double distance, boolean allowCurvedPath) throws BlockedException, FinMatchException
	{
		// On interdit la trajectoire courbe si on doit faire un virage trop grand (plus d'un quart de tour).
		if(Math.abs(direction - orientation) > Math.PI/2)
			allowCurvedPath = false;

		try
		{
			// demande aux moteurs de tourner le robot jusqu'a ce qu'il pointe dans la bonne direction
			mLocomotionCardWrapper.turn(direction);


			// attends que le robot soit dans la bonne direction si nous ne sommes pas autoris� � tourner en avancant
			if(!allowCurvedPath) 
			{

				// TODO: mettre la boucle d'attente dans une fonction part enti�re (la prise de oldInfo est moche ici)
				oldInfos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();
				float newOrientation = (float)oldInfos[2] + (float)direction*1000; // valeur absolue de l'orientation � atteindre
				while(!isTurnFinished(newOrientation)) 
					Sleep.sleep(minimumDelayBetweenMovementStatusCheck);
			}

			// demande aux moteurs d'avancer le robot de la distance demand�e
			mLocomotionCardWrapper.moveLengthwise(distance);
		} 
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}



	/**
	 * V�rifie si le robot a fini de tourner. (On suppose que l'on a pr�c�demment demand� au robot de tourner)
	 * @param finalOrientation on d�cr�te que le robot a fini de tourner lorsque son orientation �gale cette valeur (en radian, valeur absolue) 
	 * @return Faux si le robot tourne encore, vrai si arrivée au bon point, exception si blocage
	 * @throws BlockedException si blocage mécanique du robot survient durant la rotation (pas de gestion des capteurs ici)
	 * @throws FinMatchException 
	 */
	// TODO: pourquoi ne pas utiliser mLocomotionCardWrapper.isRobotMoving() ?
	private boolean isTurnFinished(float finalOrientation) throws BlockedException, FinMatchException
	{
		boolean out = false; 
		try
		{
			// demande ou l'on est et comment on est orienté a la carte d'asser
			double[] newInfos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();

			// Le robot tourne-t-il encore ?
			if(Math.abs(newInfos[2] - oldInfos[2]) > 20)
				out = false;

			// le robot est-t-il arrivé ?
			else if(Math.abs(newInfos[2]/1000 - finalOrientation) > 20)
				out = true;

			// si on ne bouge plus, et qu'on n'est pas arrivé, c'est que ca bloque
			else
				throw new BlockedException();

			oldInfos = newInfos;
		} 
		catch (SerialConnexionException e)
		{
			log.critical("Erreur de communication avec la carte d'asser", this);
			e.printStackTrace();
		}
		return out;
	}


	/**
	 * Vérifie si le robot a fini d'avancer. ( vérification d'un mouvement de translation uniquement)
	 * Renvois Faux si le robot bouge encore, vrai si arrivée au bon point, exception si blocage
	 * @returnFaux si le robot bouge encore, vrai si il est arrivée au bon point.
	 * @throws BlockedException en cas de bloquange mécanique du robot l'empéchant d'aller plus loin
	 * @throws FinMatchException 
	 */
	//TODO: le if... else if.... else.... est redondant avec la fonction checkRobotNotBlocked qui est elle aussi appellée dans moveInDirectionEventWatcher. 
	private boolean isMovementFinished() throws BlockedException, FinMatchException
	{
		// si on vérifie si l'ona fini de bouger pour la première fois depuis l'initialisation de tout le, il faut remplir oldInfos
		if(oldInfos == null)
		{
			try
			{
				oldInfos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();
			}
			catch (SerialConnexionException e)
			{
				e.printStackTrace();
			}
			return false;
		}
		
		boolean out = false;
		
		//distance parcourue par le robot entre deux rafraichissement de la position a partir de laquelle on considère que le robot est en mouvement
		// TODO : faire une détection paramétrable différamment en translation et en rotation, plus un calcul premant en compte le temps de rafraichissement de la position du robot
		// car on veut un seuil de vitesse (donc dépendant du temps dt de rafraichissement de l'asser) et non un seuil sur V*dt
		int motionThreshold = 0;
		
		// tolérance sur la position d'arrivée. L'exécution sera rendue a l'utilisateur de la classe Locomotion quand le robot sera plus proche de l'arrivée que cette distance
		int aimThreshold = 400;
		
		// demande ou l'on est et comment on est orienté a la carte d'asser
		double[] newInfos = null;
		try
		{
			newInfos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();

		}
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
			
		// Le robot bouge-t-il encore ?
		if(new Vec2((int)oldInfos[0], (int)oldInfos[1]).squaredDistance(new Vec2((int)newInfos[0], (int)newInfos[1])) > motionThreshold || Math.abs(newInfos[2] - oldInfos[2]) > motionThreshold)
			out = false;
		//TODO: si l'on veut savoir si le robot bouge encore, pourquoi ne pas utiliser mLocomotionCardWrapper.isRobotMoving() plutot ?

		// le robot est-t-il arrivé ?
		else if(new Vec2((int)newInfos[0], (int)newInfos[1]).squaredDistance(aim) < aimThreshold)
			out = true;

		// si on ne bouge plus, et qu'on n'est pas arrivé, c'est que ca bloque
		else
			throw new BlockedException();

		oldInfos = newInfos;
			
			
		return out;
	}

	/**
	 * Boucle d'acquittement générique. Retourne des valeurs spécifiques en cas d'arrêt anormal (blocage, capteur)
	 *  	false : si on roule
	 *  	true : si on est immobile 
	 *  	exception : si patinage
	 * @return true si le robot ne bouge plus parce que les moteurs ne tournent plus, false si le robot est encore en mouvement
	 * @throws BlockedException si blocage mécanique du robot survient durant le mouvement (a un moment, les moteurs tournaient mais pas les codeuses)
	 * @throws SerialConnexionException si la carte d'asservissement cesse de répondre
	 * @throws FinMatchException 
	 */
	//TODO: cette fonction est redondante avec le if... else if.... else.... de la fonction isMovementFinished qui est elle aussi appellée dans moveInDirectionEventWatcher. 
	private boolean checkRobotNotBlocked() throws BlockedException, SerialConnexionException, FinMatchException
	{
		// récupérations des informations d'acquittement
		// met a jour: 	l'écart entre la position actuelle et la position sur laquelle on est asservi
		//				la variation de l'écart a la position sur laquelle on est asservi
		//				la puissance demandée par les moteurs 	
		try 
		{
			mLocomotionCardWrapper.refreshFeedbackLoopStatistics();
		} 
		catch (SerialConnexionException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		// lève une exception de blocage si le robot patine (ie force sur ses moteurs sans bouger) 
		mLocomotionCardWrapper.raiseExceptionIfBlocked();

		// renvois true si le robot est immobile, false si encore en mouvement
		return !mLocomotionCardWrapper.isRobotMoving();
	}

	/**
	 * fonction vérifiant que l'on ne va pas bientot taper dans un obstacle si l'on continue a se déplacer dans le sens spécifié
	 * @param isForward : fait la détection derrière le robot si l'on avance à reculons 
	 * @throws UnexpectedObstacleOnPathException si obstacle sur le chemin
	 */
	private void checkPathIsObstacleFree(boolean isForward) throws UnexpectedObstacleOnPathException
	{
		// Le principe de cette fonction est de regarder dans le gestionnaire d'obstacle de la table s'il y a un obstacle qui est sur notre chemin.

		// la zone de détection d'obstacle est un disque comme suit:
		//    			          o  o
		//    			+----+ o        o		 Sens de déplacement du robot: ====>
		//   robot ->	|    |o          o
		//    			|    |o          o  <- Zone de vérification (ce disque est tangent au robot)
		//    			+----+ o        o 
		//    			          o  o
		


		// on vérifie la présence d'obstacle devant le robot si on est en marche avant, et derrière le robot si on est en marche arrière
		int sign = -1;
		if(isForward)
			sign = 1;

		// la distance entre le centre du disque a l'intérieur duquel on vérifie qu'il n'y a pas d'obstacle et le centre du robot est égale à
		// la demi longeur
		int distanceBetweenDiscCenterAndRobotCenter = robotLengh/2 + obstacleDetectionDiscRadius;
		
		// calcule la position du centre du cercle 
		Vec2 discCenter = new Vec2(	(int)(sign * distanceBetweenDiscCenterAndRobotCenter * Math.cos(orientation)),
									(int)(sign * distanceBetweenDiscCenterAndRobotCenter * Math.sin(orientation))	); // Ce calcul donne la position relative du centre du disque par rapport au centre du robot
		discCenter.plus(position);	// converti les coordonnées relative au centre du robot en coordonnées absolues sur la table
		
		// fais remonter un problème s'il y a un obstacle dans ce disque.
		if(obstaclemanager.is_obstacle_mobile_present(discCenter, obstacleDetectionDiscRadius))
		{
			log.warning("Obstacle sur notre chemin ! Nous somme en :" +position + ", et on détecte un obstacle dans un rayon de " + obstacleDetectionDiscRadius + "mm autour du point " + discCenter, this);
			throw new UnexpectedObstacleOnPathException();
		}

	}

	/**
	 * Demande la position et orientation du robot a la carte d'asservissement et stocke ces nouvelles inforamtions dans les champs position et orientation de Locomotion.
	 * @throws FinMatchException 
	 */
	private void updatePositionAndOrientation() throws FinMatchException
	{
		double[] infos = null;
		
		// demande a la carte d'asservissement de nouvelles informations sur la position et l'orientation du robot.
		try
		{
			infos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();
		}
		catch(SerialConnexionException e)
		{
			e.printStackTrace();
		}

		// Stoque les informations extraites de la carte d'asservissement dans les attributs de cette instance de Locomotion.
		position.x = (int)infos[0];
		position.y = (int)infos[1];
		orientation = infos[2]/1000; // cette division par 1000 converti les miliradiants renvoyés par le Wrapper de la carte d'asservissement en radiants pour cette classe
	}

	
	/**
	 * Met a jour la configuration de la classe via le fichier de configuration fourni par le sysème de container
	 */
	@Override
	public void updateConfig()
	{
		maxAllowedExceptionCount = config.getInt(ConfigInfo.NB_TENTATIVES);
		obstacleDetectionDiscRadius = config.getInt(ConfigInfo.DISTANCE_DETECTION);
		blockedExceptionRetraceDistance = config.getInt(ConfigInfo.DISTANCE_DEGAGEMENT_ROBOT);
		minimumDelayBetweenMovementStatusCheck = config.getInt(ConfigInfo.SLEEP_BOUCLE_ACQUITTEMENT);
		pullOutAngleInCaseOfBlockageWhileTurning = config.getDouble(ConfigInfo.ANGLE_DEGAGEMENT_ROBOT);
		//anticipation_trajectoire_courbe = Integer.parseInt(config.get("anticipation_trajectoire_courbe"));
		allowCurvedPath = config.getBoolean(ConfigInfo.TRAJECTOIRE_COURBE);
		symmetry = config.getColor().isSymmetry();
	}

	/**
	 * Arrête le robot sur la table.
	 * Le robot sera immobile après appel de cette méthode.
	 * @throws FinMatchException 
	 */
	public void immobilise() throws FinMatchException
	{

		try
		{
			mLocomotionCardWrapper.immobilise();
		}
		catch (SerialConnexionException e) 
		{
			e.printStackTrace();
		}           
	}

	/**
	 * Met à jour le point d'arrivée du mouvement.
	 * Le robot, dans son déplacement, ne s'arrètera que s'il atteint ce point.
	 * @param newAim nouvelle valeur du point d'arrivée
	 */
	public void setAim(Vec2 newAim)
	{
		newAim.copy(aim);
	}

	/**
	 * Change dans l'asservissement la position du robot sur la table .
	 * Après appel de cette méthode, le robot considèrera qu'il se trouve sur la table aux coordonnées fournies.
	 * Cette fonction n'est pas instantannée, un petit délai (de 300ms) pour que la communication série se fasse est nécéssaire.
	 * @param position
	 * @throws FinMatchException 
	 */
	public void setPosition(Vec2 position) throws FinMatchException
	{
		position.copy(this.position);
		try
		{
			mLocomotionCardWrapper.setX(position.x);
			mLocomotionCardWrapper.setY(position.y);
		}
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
		Sleep.sleep(300);
	}


	/**
	 * Change dans l'asservissement l'orientation du robot sur la table .
	 * Après appel de cette méthode, le robot considèrera qu'il se trouve sur la table avec l'orientation fournie.
	 * Cette fonction n'est pas instantannée, un petit délai (de 300ms) pour que la communication série se fasse est nécéssaire.
	 * @param orientation
	 * @throws FinMatchException 
	 */
	public void setOrientation(double orientation) throws FinMatchException
	{
		this.orientation = orientation;
		try
		{
			mLocomotionCardWrapper.setOrientation(orientation);
		}
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
		Sleep.sleep(300);
	}

	/**
	 * Donne la position du robot sur la table.
	 * Cette méthode est lente mais très précise: elle déclenche un appel a la série pour obtenir une position a jour.
	 * @return la position courante du robot sur la table
	 * @throws FinMatchException 
	 */
	public Vec2 getPosition() throws FinMatchException
	{
		updatePositionAndOrientation();
		return position.clone();
	}

	/**
	 * Donne la position du robot sur la table.
	 * Cette méthode est rapide mais peu précise: elle ne déclenche pas d'appel a la série pour obtenir une position a jour.
	 * La position revoyée est celle mémorisée lors de sa dernière mise a jour (la date de la dernière mise a jour est inconnue).
	 * @return la dernière position mémorisée du robot sur la table
	 */
	public Vec2 getPositionFast()
	{
		return position.clone();
	}

	/**
	 * Donne l'orientation du robot sur la table.
	 * Cette méthode est lente mais très précise: elle déclenche un appel a la série pour obtenir une orientation a jour.
	 * @return l'orientation en radiants courante du robot sur la table
	 * @throws FinMatchException 
	 */
	public double getOrientation() throws FinMatchException
	{
		updatePositionAndOrientation();
		return orientation;
	}
	
	/**
	 * Donne l'orientation du robot sur la table.
	 * Cette méthode est rapide mais peu précise: elle ne déclenche pas d'appel a la série pour obtenir une orientation a jour.
	 * L'orientation revoyée est celle mémorisée lors de sa dernière mise a jour (la date de la dernière mise a jour est inconnue).
	 * @return la dernière orientation mémorisée du robot sur la table
	 */
	public double getOrientationFast()
	{
		return orientation;
	}

	/**
	 *  désactive l'asservissement du robot sur la table. 
	 *  Après l'appel de cette méthode, le robot ne sera ni asservi dans son orientation, ni dans sa position.
	 * @throws FinMatchException 
	 */
	public void disableFeedbackLoop() throws FinMatchException
	{
		try
		{
			mLocomotionCardWrapper.disableRotationnalFeedbackLoop();
			mLocomotionCardWrapper.disableTranslationnalFeedbackLoop();
		}
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Change la vitesse a laquelle le robot tourne sur lui-même.
	 * @param pwm la vitesse de rotation désirée
	 * @throws FinMatchException 
	 */
	public void setRotationnalSpeed(int pwm) throws FinMatchException
	{
		try
		{
			mLocomotionCardWrapper.setRotationnalSpeed(pwm);
		}
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * Change la vitesse a laquelle le robot se translate sur la table.
	 * @param pwm la vitesse de translation désirée
	 * @throws FinMatchException 
	 */
	public void setTranslationnalSpeed(int pwm) throws FinMatchException
	{
		try
		{
			mLocomotionCardWrapper.setTranslationnalSpeed(pwm);
		}
		catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Renvois la classe de communication avec la carte d'asservissement
	 * @return the mLocomotion
	 */
	public LocomotionCardWrapper getLocomotionCardWrapper()
	{
		return mLocomotionCardWrapper;
	}


}


