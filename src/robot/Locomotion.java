package robot;

import java.util.ArrayList;

import container.Service;
//import hook.Callback;
//import hook.Executable;
import hook.Hook;
import enums.Speed;
//import hook.methodes.ChangeConsigne;
//import hook.sortes.HookGenerator;
import exceptions.Locomotion.BlockedException;
import exceptions.Locomotion.UnexpectedObstacleOnPathException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import robot.cardsWrappers.LocomotionCardWrapper;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Config;
import utils.Sleep;

/**
 * Entre LocomtionCardWrapper (appels à la série) et RobotReal (déplacements haut niveau), Locomotion
 * s'occupe de la position, de la symétrie, des hooks, des trajectoires courbes et des blocages.
 * Structure, du bas au haut niveau: symétrie, hook, trajectoire courbe et blocage.
 * Les méthodes "non-bloquantes" se finissent alors que le robot roule encore.
 * (les méthodes non-bloquantes s'exécutent très rapidement)
 * Les méthodes "bloquantes" se finissent alors que le robot est arrêté.
 * @author pf, marsu
 *
 */

public class Locomotion implements Service
{

	//gestion des log
	private Log log;
	
	//endroit ou lire la configuration du robot
	private Config config;
	
	// La table sur laquelle le robot se déplace
	private Table table;
	
	// la ditance minimale entre le centre du robot et un obstacle
	private int largeur_robot;
	
	// TODO c'est quoi ?
	private int distance_detection;
	
	// Position courante du robot sur la table. La coordonnée X est multipliée par -1 si on est équipe jaune
	private Vec2 position = new Vec2(); //TODO: spécifier dans la doc a un endroit logique ou est défini le système d'axe (plus le système d'orientation)
	
	 // Position de la table que le robot cherche a ateindre. Elle peut être modifiée au sein d'un même mouvement.
	private Vec2 consigne = new Vec2();
	
	// Le système de trajectoire courbe ou de "tourner en avançant" fait rêver des génération d'INTechiens,
	// mais ça a toujours fais perdre du temps pour un truc qui ne marche pas
	private boolean trajectoire_courbe = false;

	// orientation actuelle du robot. L'orientation est multipliée par -1 si on est équipe jaune
	private double orientation;
	
	// interface de communication avec la carte d'asservissement
	private LocomotionCardWrapper mLocomotionCardWrapper;
	
	// la table est symétrisée si on est équipe jaune
	private boolean symetrie;
	
	// Lorsque le robot doit bouge, C'est le temps d'attente entre deux vérification de l'état de ce déplacement. (arrivée, blocage, etc.)
	private int sleep_boucle_acquittement = 10;
	
	// nombre maximum d'excpetions levés d'un certain type lors d'un déplacement
	private int maxAllowedExceptionCount = 5;
	
	// distance en mm sur laquelle on revient sur nos pas avant de réessayer d'atteindre le point d'arrivée lorsque le robot faire face a un obstacle qui immobilise mécaniquement le robot
	private int blockedExceptionRetraceDistance = 50;
	
	// temps en ms que l'on attends après avoir vu un ennemi avant de réessayer d'atteindre le point d'arrivée lorsque le robot détecte que sa route est obstruée
	private int unexpectedObstacleOnPathRetryDelay = 200; 
	
	private double angle_degagement_robot;
	private boolean insiste = false;
	
	
	// TODO: voir si on pet vraiment supprimer ces variables
	@SuppressWarnings("unused")
	private long debut_mouvement_fini;
	
	@SuppressWarnings("unused")
	private boolean fini = true;
	private double[] oldInfos;

	/**
	 * Instancie le service de d�placement haut niveau du robot.
	 * Appell� par le container
	 * @param log : la sortie de log à utiliser
	 * @param config : sur quel objet lire la configuration du match
	 * @param table : l'aire de jeu sur laquelle on se d�place
	 * @param mLocomotion : service de d�placement de bas niveau
	 */
	public Locomotion(Log log, Config config, Table table, LocomotionCardWrapper mLocomotion)
	{
		this.log = log;
		this.config = config;
		this.mLocomotionCardWrapper = mLocomotion;
		//        this.hookgenerator = hookgenerator;
		this.table = table;
		updateConfig();
	}

	/**
	 * Recale le robot sur la table pour qu'il sache ou il est sur la table et dans quel sens il est.
	 * c'est obligatoire avant un match,
	 */
	public void readjust()
	{
		try
		{
			// Retrouve l'abscisse du robot en foncant dans un mur d'abscisse connue
			log.debug("recale X",this);

			moveForward(-200, null, true);
			mLocomotionCardWrapper.setTranslationnalSpeed(200);
			mLocomotionCardWrapper.disableRotationnalFeedbackLoop();
			Sleep.sleep(1000);
			moveForward(-200, null, true);
			mLocomotionCardWrapper.enableRotationnalFeedbackLoop();
			mLocomotionCardWrapper.setTranslationnalSpeed(Speed.READJUSTMENT.PWMTranslation);

			position.x = 1500 - 165;
			if(symetrie)
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
			moveForward(40, null, true);
			turn(-Math.PI/2, null, false);


			log.debug("recale Y",this);
			moveForward(-600, null, true);
			mLocomotionCardWrapper.setTranslationnalSpeed(200);
			mLocomotionCardWrapper.disableRotationnalFeedbackLoop();
			Sleep.sleep(1000);
			moveForward(-200, null, true);
			mLocomotionCardWrapper.enableRotationnalFeedbackLoop();
			mLocomotionCardWrapper.setTranslationnalSpeed(Speed.READJUSTMENT.PWMTranslation);
			position.y = 2000 - 165;
			mLocomotionCardWrapper.setY(2000 - 165);


			log.debug("Done !",this);
			Sleep.sleep(500);
			moveForward(100, null, false);
			orientation = -Math.PI/2;
			setOrientation(-Math.PI/2);
			//Normalement on se trouve à (1500 - 165 - 100 = 1225 ; 2000 - 165 - 100 = 1725)
			mLocomotionCardWrapper.enableRotationnalFeedbackLoop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Fait tourner le robot (méthode bloquante)
	 * @throws UnableToMoveException 
	 */
	public void turn(double angle, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException
	{

		if(symetrie)
			angle = Math.PI-angle;

		// Tourne-t-on dans le sens trigonométrique?
		// C'est important de savoir pour se dégager.
		boolean trigo = angle > orientation;

		try {
			oldInfos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();
			mLocomotionCardWrapper.turn(angle);
			while(!isMovementFinished()) // on attend la fin du mouvement
			{
				Sleep.sleep(sleep_boucle_acquittement);
				//   log.debug("abwa?", this);
			}
		} catch(BlockedException e)
		{
			try
			{
				update_x_y_orientation();
				if(!mur)
				{
					if(trigo ^ symetrie)
						mLocomotionCardWrapper.turn(orientation+angle_degagement_robot);
					else
						mLocomotionCardWrapper.turn(orientation-angle_degagement_robot);
				}
			} catch (SerialConnexionException e1)
			{
				e1.printStackTrace();
			}
			throw new UnableToMoveException();
		} catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Fait avancer le robot de la distance spécifiée.
	 * C'est la méthode que les utilisateurs (externes au développement du système de locomotion) vont utiliser
	 * @param distance en mm que le robot doit franchir
	 * @param hooks	// TODO : trouver ce que sont ces hooks a vérifier
	 * @param insiste // TODO : trouver ce que insiste fait
	 * @throws UnableToMoveException si le robot rencontre un problème dans son déplacement
	 */
	public void moveForward(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException
	{
		
		update_x_y_orientation();

		
		// calcule la position a atteindre en fin de mouvement
		consigne.x = (int) (position.x + distance*Math.cos(orientation));
		consigne.y = (int) (position.y + distance*Math.sin(orientation));
		if(symetrie)
			consigne.x = -consigne.x;
		  

		moveForwardInDirectionExeptionHandler(hooks, false, distance < 0, mur);

		update_x_y_orientation();
	}

	/**
	 * Suit un chemin. Crée les hooks de trajectoire courbe si besoin est.
	 * @param chemin
	 * @param hooks
	 * @param insiste
	 * @throws UnableToMoveException
	 */
	public void followPath(ArrayList<Vec2> chemin, ArrayList<Hook> hooks) throws UnableToMoveException
	{
		// en cas de coup de folie a INTech, on active la trajectoire courbe.
		if(trajectoire_courbe)
		{
			log.critical("Désactive la trajectoire courbe, pauvre fou!", this);
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
			for(Vec2 point: chemin)
			{
				consigne = point.clone();
				moveBackwardInDirection(hooks, false, false);
			}
	}

	/**
	 * Bloquant. Gère la marche arrière automatique.
	 * @param hooks
	 * @param insiste
	 * @throws UnableToMoveException
	 */
	private void moveBackwardInDirection(ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean mur) throws UnableToMoveException
	{
		// choisit de manière intelligente la marche arrière ou non
		// mais cette année, on ne peut aller, de manière automatique, que tout droit.
		// Donc on force marche_arriere à false.
		// Dans les rares cas où on veut la marche arrière, c'est en utilisant avancer.
		// Or, avancer parle directement à va_au_point_gestion_exception

		/*
		 * Ce qui suit est une méthode qui permet de choisir si la marche arrière
		 * est plus rapide que la marche avant. Non utilisé, mais bon à savoir.
		 */
		/*
        Vec2 delta = consigne.clone();
        if(symetrie)
            delta.x *= -1;
        delta.Minus(position);
        // Le coeff 1000 vient du fait que Vec2 est constitué d'entiers
        Vec2 orientationVec = new Vec2((int)(1000*Math.cos(orientation)), (int)(1000*Math.sin(orientation)));

        // On regarde le produit scalaire; si c'est positif, alors on est dans le bon sens, et inversement
        boolean marche_arriere = delta.dot(orientationVec) > 0;
		 */

		moveForwardInDirectionExeptionHandler(hooks, trajectoire_courbe, false, mur);
	}

	/**
	 * Intercepte les exceptions des capteurs (on va rentrer dans un ennemi) et les exceptions de l'asservissement (le robot est mécaniquement bloqué)
	 * Déclenche différentes réactions sur ces évènements, et si les réactions mises en places ici sont insuffisantes (on n'arrive pas a se dégager)
	 * fais remonter l'exeption a l'utilisateur de la classe
	 * @param hooks a considérer lors de ce déplacement. Le hook n'est déclenché que s'il est dans cette liste et que sa condition d'activation est remplie
	 * @param allowCurvedPath true si l'on autorise le robot à se déplacer le long d'une trajectoire curviligne.  false pour une simple ligne brisée
	 * @param isBackward true si le déplacement doit se faire en marche arrière, false si le robot doit avancer en marche avant.
	 * @param expectsWallImpact true si le robot doit s'attendre a percuter un mur au cours du déplacement. false si la route est sensée être dégagée.
	 * @throws UnableToMoveException losrque quelque chose sur le chemin cloche et que le robot ne peut s'en défaire simplement: bloquage mécanique immobilisant le robot ou obstacle percu par les capteurs
	 */
	public void moveForwardInDirectionExeptionHandler(ArrayList<Hook> hooks, boolean allowCurvedPath, boolean isBackward, boolean expectsWallImpact) throws UnableToMoveException
	{
		// nombre d'exception (et donc de nouvels essais) que l'on va lever avant de prévenir
		// l'utilisateur en cas de bloquage mécanique du robot l'empéchant d'avancer plus loin
		int blockedExceptionStillAllowed = 2;
		if (blockedExceptionStillAllowed > maxAllowedExceptionCount)
			blockedExceptionStillAllowed = maxAllowedExceptionCount;
		
		// nombre d'exception (et donc de nouvels essais) que l'on va lever avant de prévenir
		// l'utilisateur en cas de découverte d'un obstacle imprévu (robot adverse ou autre) sur la route
		int unexpectedObstacleOnPathExceptionStillAllowed;
		if(insiste)
			unexpectedObstacleOnPathExceptionStillAllowed = maxAllowedExceptionCount;
		else
			unexpectedObstacleOnPathExceptionStillAllowed = 2;
		
		
		
		// drapeau indiquant s'il faut retenter d'atteindre le point d'arrivée. Initialisé à vrai pour l'essai initial.
		boolean tryAgain = true;
		// on essaye (de nouveau) d'aller jusqu'au point d'arrivée du mouvement
		// cette boucle prend fin soit quand on est arrivée, soit lors d'une exception de type UnableToMoveException
		while(tryAgain)
		{
		
			tryAgain = false;
			try
			{
				va_au_point_hook_correction_detection(hooks, allowCurvedPath, isBackward);
			}
			
			// Si on remarque que le robot a percuté un obstacle l'empéchant d'avancer plus loin
			catch (BlockedException e)
			{
				// Réaction générique aux exceptions de déplacement du robot
				generalLocomotionExeptionReaction(blockedExceptionStillAllowed);
				
				//On tolère une exception de moins
				blockedExceptionStillAllowed--;
				
				// On réagit spécifiquement à la présence d'un bloquage mécanique du robot
				tryAgain = BlockedExceptionReaction(e, isBackward, expectsWallImpact);
			}
			
			// Si on a vu un obstacle inattendu sur notre chemin (robot ennemi ou autre)
			catch (UnexpectedObstacleOnPathException e)
			{
				// Réaction générique aux exceptions de déplacement du robot
				generalLocomotionExeptionReaction(unexpectedObstacleOnPathExceptionStillAllowed);
				
				//On tolère une exception de moins
				unexpectedObstacleOnPathExceptionStillAllowed--;
				
				// Réagit spécifiquement à la présence d'un obstacle
				tryAgain = unexpectedObstacleOnPathExceptionReaction(e);
			}
		} // while

		// si on arrive ici, c'est que l'on est au point d'arrivée
	}
	
	/**
	 * Réaction générique face a un souci dans le déplacment du robot. (arreter les moteurs de propultion par exemple)
	 * @param exeptionCountStillAllowed nombre d'exception pour ce déplacement que l'on tolère encore avant de remonter le problème a l'utilisateur de la classe
	 * @throws UnableToMoveException exception lancée quand on ne tolère plus de soucis interne au déplacement. Elle indique soit un obstacle détecté sur la route, soit que le robot a un blocage mécanique pour continuer a avancer
	 */
	private void generalLocomotionExeptionReaction(int exeptionCountStillAllowed) throws UnableToMoveException
	{
		log.warning("Exeption de déplacement lancée, Immobilisation du robot.", this);
		/* TODO: si on veut pouvoir enchaîner avec un autre chemin, il ne faut pas arrêter le robot.
		 * ATTENTION! ceci peut être dangereux, car si aucun autre chemin ne lui est donné, le robot va continuer sa course et percuter l'obstacle ! */
		immobilise();
		
		// Si cette exception fait dépasser le quota autorisé, on la remonte a l'utilisateur de la classe
		if(exeptionCountStillAllowed <= 0)
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
	 */
	private boolean BlockedExceptionReaction(BlockedException e, boolean isBackward, boolean expectsWallImpact)
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
					mLocomotionCardWrapper.moveForward(blockedExceptionRetraceDistance);
				else
					mLocomotionCardWrapper.moveForward(-blockedExceptionRetraceDistance);
				
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
	 * Bloquant. Gère les hooks, la correction de trajectoire et la détection.
	 * @param point
	 * @param hooks
	 * @param trajectoire_courbe
	 * @throws BlockedException 
	 * @throws UnexpectedObstacleOnPathException 
	 */
	public void va_au_point_hook_correction_detection(ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean marche_arriere) throws BlockedException, UnexpectedObstacleOnPathException
	{
		boolean relancer;
		va_au_point_symetrie(trajectoire_courbe, marche_arriere, false);
		try
		{
			oldInfos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();
		} catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
		do
		{
			relancer = false;
			detecter_collision(!marche_arriere);

			if(hooks != null)
				for(Hook hook : hooks)
				{
					Vec2 sauv_consigne = consigne.clone();
					relancer |= hook.evaluate();
					consigne = sauv_consigne;
				}

			// Correction de la trajectoire ou reprise du mouvement
			// Si on ne fait que relancer et qu'on a interdit la trajectoire courbe, on attend à la rotation.
			if(relancer)
			{
				log.debug("On relance", this);
				va_au_point_symetrie(false, marche_arriere, trajectoire_courbe);
			}
			else
				update_x_y_orientation();

		} while(!isMovementFinished());

	}

	/**
	 * Non bloquant. Gère la symétrie et la marche arrière.
	 * @param point
	 * @param sans_lever_exception
	 * @param trajectoire_courbe
	 * @param marche_arriere
	 * @throws BlockedException 
	 */
	public void va_au_point_symetrie(boolean trajectoire_courbe, boolean marche_arriere, boolean correction) throws BlockedException
	{
		Vec2 delta = consigne.clone();
		if(symetrie)
			delta.x = -delta.x;

		long t1 = System.currentTimeMillis();
		update_x_y_orientation();
		long t2 = System.currentTimeMillis();

		delta.Minus(position);
		double distance = delta.Length();
		if(correction)
			distance -= (t2-t1);

		//gestion de la marche arrière du déplacement (peut aller à l'encontre de marche_arriere)
		double angle =  Math.atan2(delta.y, delta.x);
		if(marche_arriere)
		{
			distance *= -1;
			angle += Math.PI;
		}        

		moveForwardInDirection(angle, distance, trajectoire_courbe);

	}

	/**
	 * Fait avancer le robot de la distance voulue dans la direction d�sir�e
	 * compatible avec les trajectoires courbes.
	 * Le d�placement n'est pas bloquant, mais le changement d'orientation pour que l'avant du robot pointe dans la bonne direction l'est.
	 * @param direction valeur relative en radian indiquant la direction dans laquelle on veut avancer
	 * @param distance valeur en mm indiquant de combien on veut avancer.
	 * @param allowCurvedPath si true, le robot essayera de tourner et avancer en m�me temps
	 * @throws BlockedException si blocage mécanique du robot en chemin (pas de gestion des capteurs ici)
	 */
	public void moveForwardInDirection(double direction, double distance, boolean allowCurvedPath) throws BlockedException
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
					Sleep.sleep(sleep_boucle_acquittement);
			}

			// demande aux moteurs d'avancer le robot de la distance demand�e
			mLocomotionCardWrapper.moveForward(distance);
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
	 * @throws BlockedException si un obstacle est rencontr� durant la rotation
	 */
	private boolean isTurnFinished(float finalOrientation) throws BlockedException
	{
		boolean out = false; 
		try
		{
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
		} catch (SerialConnexionException e)
		{
			log.critical("Erreur de communication avec la carte d'asser", this);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}


	/**
	 * Faux si le robot bouge encore, vrai si arrivée au bon point, exception si blocage
	 * @return
	 * @throws BlockedException
	 */
	private boolean isMovementFinished() throws BlockedException
	{
		boolean out = false;
		
		//distance parcourue par le robot entre deux rafraichissement de la position a partir de laquelle on cosidère que le robot est en mouvement
		// (distance en trasnlation ou en rotation)
		// TODO : faire une détection paramétrable différamment en translation et en rotation, plus un calcul premant en compte le temps de rafraichissement de la position du robot
		// car on veut un seuil de vitesse (donc dépendant du temps dt de rafraichissement de l'asser) et non un seuil sur V*dt
		int motionThreshold = 10;
		
		// tolérance sur la position d'arrivée. L'exécution sera rendue a l'utilisateur de la classe Locomotion quand le robot sera plus proche de l'arrivée que cette distance
		int aimThreshold = 10;
		
		try
		{
			double[] new_infos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();
			
			// Le robot bouge-t-il encore ?
			if(new Vec2((int)oldInfos[0], (int)oldInfos[1]).SquaredDistance(new Vec2((int)new_infos[0], (int)new_infos[1])) > motionThreshold || Math.abs(new_infos[2] - oldInfos[2]) > motionThreshold)
				out = false;

			// le robot est-t-il arrivé ?
			else if(new Vec2((int)new_infos[0], (int)new_infos[1]).SquaredDistance(consigne) < aimThreshold)
				out = true;

			// si on ne bouge plus, et qu'on n'est pas arrivé, c'est que ca bloque
			  else
			     throw new BlockedException();

			oldInfos = new_infos;
		} catch (SerialConnexionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}

	/**
	 * Surcouche de mouvement_fini afin de ne pas freezer
	 * @return
	 * @throws BlocageException
	 */
	/*    private boolean mouvement_fini() throws BlocageException
    {
        if(nouveau_mouvement)
            debut_mouvement_fini = System.currentTimeMillis();
        nouveau_mouvement = false;
        fini = mouvement_fini_routine();
        if(!fini && ((System.currentTimeMillis() - debut_mouvement_fini) > 2000))
        {
            log.critical("Erreur d'acquittement. On arrête l'attente du robot.", this);
            fini = true;
        }
        return fini;
    }*/

	/**
	 * Boucle d'acquittement générique. Retourne des valeurs spécifiques en cas d'arrêt anormal (blocage, capteur)
	 *  	
	 *  	false : si on roule
	 *  	true : si arrivé a destination
	 *  	exeption : si patinage
	 * 
	 * 
	 * @param detection_collision
	 * @param sans_lever_exception
	 * @return true si le robot est arrivé à destination, false si encore en mouvement
	 * @throws BlockedException
	 * @throws UnexpectedObstacleOnPathException
	 */
	@SuppressWarnings("unused") // TODO: Voir ce qu'il se passe: pourquoi elle n'est jamais appllée ?
	private boolean mouvement_fini_routine() throws BlockedException
	{
		// récupérations des informations d'acquittement
		try {

			// met a jour: 	l'écart entre la position actuelle et la position sur laquelle on est asservi
			//				la variation de l'écart a la position sur laquelle on est asservi
			//				la puissance demandée par les moteurs 	
			mLocomotionCardWrapper.refreshFeedbackLoopStatistics();

			// lève une exeption de blocage si le robot patine (ie force sur ses moteurs sans bouger) 
			mLocomotionCardWrapper.raiseExeptionIfBlocked();

			// robot arrivé?
			//            System.out.println("deplacements.update_enMouvement() : " + deplacements.isRobotMoving());
			return !mLocomotionCardWrapper.isRobotMoving();

		} 
		catch (SerialConnexionException e) 
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * fonction vérifiant que l'on ne va pas taper dans le robot adverse. 
	 * @param devant: fait la détection derrière le robot si l'on avance à reculons 
	 * @throws UnexpectedObstacleOnPathException si obstacle sur le chemin
	 */
	private void detecter_collision(boolean devant) throws UnexpectedObstacleOnPathException
	{
		int signe = -1;
		if(devant)
			signe = 1;

		int rayon_detection = largeur_robot/2 + distance_detection;
		Vec2 centre_detection = new Vec2((int)(signe * rayon_detection * Math.cos(orientation)), (int)(signe * rayon_detection * Math.sin(orientation)));
		centre_detection.Plus(position);
		if(table.gestionobstacles.obstaclePresent(centre_detection, distance_detection))
		{
			log.warning("Ennemi détecté en : " + centre_detection, this);
			throw new UnexpectedObstacleOnPathException();
		}

	}

	/**
	 * Met à jour position et orientation via la carte d'asservissement.
	 * @throws SerialConnexionException
	 */
	private void update_x_y_orientation()
	{
		try {
			double[] infos = mLocomotionCardWrapper.getCurrentPositionAndOrientation();
			position.x = (int)infos[0];
			position.y = (int)infos[1];
			orientation = infos[2]/1000; // car get_infos renvoie des milliradians
		}
		catch(SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void updateConfig()
	{
		maxAllowedExceptionCount = Integer.parseInt(config.get("nb_tentatives"));
		distance_detection = Integer.parseInt(config.get("distance_detection"));
		blockedExceptionRetraceDistance = Integer.parseInt(config.get("distance_degagement_robot"));
		sleep_boucle_acquittement = Integer.parseInt(config.get("sleep_boucle_acquittement"));
		angle_degagement_robot = Double.parseDouble(config.get("angle_degagement_robot"));
		//        anticipation_trajectoire_courbe = Integer.parseInt(config.get("anticipation_trajectoire_courbe"));
		trajectoire_courbe = Boolean.parseBoolean(config.get("trajectoire_courbe"));
		symetrie = config.get("couleur").equals("rouge");
	}

	/**
	 * Arrête le robot.
	 */
	public void immobilise()
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
	 * Met à jour la consigne (utilisé par les hooks)
	 * @param point
	 */
	public void setConsigne(Vec2 point)
	{
		log.debug("Nouvelle consigne: "+point, this);
		consigne = point.clone();
	}

	/**
	 * Met à jour la position. A ne faire qu'en début de match.
	 * @param position
	 */
	public void setPosition(Vec2 position)
	{
		this.position = position.clone();
		try {
			mLocomotionCardWrapper.setX(position.x);
			mLocomotionCardWrapper.setY(position.y);
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		}
		Sleep.sleep(300);
	}

	/**
	 * Met à jour l'orientation. A ne faire qu'en début de match.
	 * @param orientation
	 */
	public void setOrientation(double orientation)
	{
		this.orientation = orientation;
		try {
			mLocomotionCardWrapper.setOrientation(orientation);
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		}
	}

	public Vec2 getPosition()
	{
		update_x_y_orientation();
		return position.clone();
	}

	public Vec2 getPositionFast()
	{
		return position.clone();
	}

	public double getOrientation()
	{
		update_x_y_orientation();
		return orientation;
	}

	public double getOrientationFast()
	{
		return orientation;
	}

	public void disableFeedbackLoop()
	{
		try
		{
			mLocomotionCardWrapper.disableRotationnalFeedbackLoop();
			mLocomotionCardWrapper.disableTranslationnalFeedbackLoop();
		} catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}

	public void setRotationnalSpeed(int pwm_max)
	{
		try
		{
			mLocomotionCardWrapper.setRotationnalSpeed(pwm_max);
		} catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}

	public void setTranslationnalSpeed(int pwm_max)
	{
		try
		{
			mLocomotionCardWrapper.setTranslationnalSpeed(pwm_max);
		} catch (SerialConnexionException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return the mLocomotion
	 */
	public LocomotionCardWrapper getLocomotionCardWrapper()
	{
		return mLocomotionCardWrapper;
	}


}


