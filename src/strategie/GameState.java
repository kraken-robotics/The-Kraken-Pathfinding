package strategie;

import java.util.ArrayList;

import buffer.DataForSerialOutput;
import permissions.Permission;
import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.LocomotionArc;
import planification.astar.arc.PathfindingNodes;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotReal;
import robot.Speed;
import hook.Hook;
import hook.HookFactory;
import container.Service;
import enums.Tribool;
import exceptions.FinMatchException;
import exceptions.GridSpaceException;
import exceptions.ScriptHookException;
import exceptions.UnableToMoveException;
import table.GameElementNames;
import table.GridSpace;
import utils.Log;
import utils.Config;
import utils.Vec2;

/**
 * Le game state rassemble toutes les informations disponibles à un instant
 * - infos sur le robot (position, objet, ...) dans R
 * - infos sur les obstacles et les éléments de jeux (robot ennemi, table, ...) dans GridSpace
 * @author pf
 *
 * @param <R>
 */

public class GameState<R extends Robot, T extends Permission> implements Service
{
    private final R robot;
    private final GridSpace gridspace;
    
    private int indice_memory_manager;
    
    private Log log;

    /**
     * De manière publique, on ne peut créer qu'un GameState<RobotReal>, et pas de GameState<RobotChrono>
     * @param config
     * @param log
     * @param table
     * @param obstaclemanager
     * @param robot
     * @return
     */
    public static GameState<RobotReal,ReadWrite> constructRealGameState(Log log, GridSpace gridspace, RobotReal robot, HookFactory hookfactory, DataForSerialOutput serie)
    {
		GameState<RobotReal,ReadWrite> out = new GameState<RobotReal,ReadWrite>(log, gridspace, robot);
		serie.envoieHooks(hookfactory.getHooksPermanents(out));
		return out;
    }
    
    private GameState(Log log, GridSpace gridspace, R robot)
    {
        this.log = log;
        this.gridspace = gridspace;
        this.robot = robot;
    }
    
    /**
     * Clone en dehors du memory manager.
     * @return
     * @throws FinMatchException
     */
	public static final GameState<RobotChrono,ReadWrite> cloneGameState(GameState<? extends Robot,ReadOnly> state)
	{
		return GameState.cloneGameState(state, -1);
	}

	/**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si this est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
	public static final GameState<RobotChrono,ReadWrite> cloneGameState(GameState<? extends Robot,ReadOnly> state, int indice_memory_manager)
	{
		// On instancie la table avant car il faut donner le même objet deux fois en paramètres
		GameState<RobotChrono,ReadWrite> cloned = new GameState<RobotChrono,ReadWrite>(state.log, state.gridspace.clone(GameState.getTempsDepuisDebut(state)), state.robot.cloneIntoRobotChrono());
		GameState.copy(state, cloned);
		cloned.indice_memory_manager = indice_memory_manager;
		return cloned;
	}

    /**
     * Copie this dans other. this reste inchangé.
     * Cette copie met à jour les obstacles et les attributs de temps.
     * @param other
     * @throws FinMatchException 
     */
    public static final void copy(GameState<?,ReadOnly> state, GameState<RobotChrono,ReadWrite> modified)
    {
        state.robot.copy(modified.robot);
    	// la copie de la table est faite dans gridspace
        // mise à jour des obstacles et du cache incluse dans la copie
        state.gridspace.copy(modified.gridspace, state.robot.getTempsDepuisDebutMatch());
    }

    @Override
    public void updateConfig(Config config)
    {
    	robot.updateConfig(config);
    	gridspace.updateConfig(config);
    }

    @Override
    public void useConfig(Config config)
    {}

    /**
     * Petite surcouche
     * @return
     */
    public static final long getTempsDepuisDebut(GameState<?,ReadOnly> state)
    {
    	return state.robot.getTempsDepuisDebutMatch();
    }
    
    /**
     * Utilisé par le memory manager
     * @return
     */
    public static final int getIndiceMemoryManager(GameState<?,ReadOnly> state)
    {
    	return state.indice_memory_manager;
    }

    /**
     * Disponible uniquement pour GameState<RobotChrono>
     * @return
     */
	public static final long getHash(GameState<RobotChrono,ReadOnly> state)
	{
		/**
		 * Un long est codé sur 64 bits.
		 * T'es content Martial, y'a assez de commentaires?
		 * Je peux en rajouter si tu veux.
		 * La vitesse de pointe d'une autruche est de 70km/h (dans le référentiel de Piccadilly Circus)
		 * C'est plus rapide que RCVA. Du coup, on sait comment faire pour les battre.
		 */		
		long hash;
		hash = state.gridspace.getHash(); // codé sur le reste
		hash = (hash << 16) | state.robot.getHash(); // codé sur 16 bits (cf getHash() de RobotChrono)
		return hash;
	}
	
	public static final void printHash(GameState<RobotChrono,?> state)
	{
		state.gridspace.printHash();
		state.robot.printHash();
	}

	/**
	 * Utilisé par le memory manager
	 * @param indice
	 */
	public static final void setIndiceMemoryManager(GameState<?,ReadWrite> state, int indice)
	{
		state.indice_memory_manager = indice;
	}
	
	/**
	 * Permet de décider de la durée de l'anticipation
	 * @param dateLimite
	 */
	public static final void updateHookFinMatch(GameState<RobotChrono,ReadWrite> state, int dateLimite)
	{
		RobotChrono.setTempsMax(dateLimite);
	}

	/**
	 * Utilisé par le script d'attente
	 * @return
	 */
	public static final boolean canSleepUntilSomethingChange(GameState<?,ReadOnly> state)
	{
		// si on utilise le vrai robot, alors les valeurs des capteurs peuvent changer
		// (ce qui n'est pas anticipable par robotchrono)
		if(state.robot instanceof RobotReal)
			return true;
		return state.gridspace.getDateSomethingChange() != Integer.MAX_VALUE;
	}
	
	// FIXME: vérifier aussi les capteurs du robot vrai
	/**
	 * Utilisé par le script d'attente
	 * @throws FinMatchException
	 */
	public static final void sleepUntilSomethingChange(GameState<?,ReadWrite> state) throws FinMatchException
	{
		// on ajoute quelques microsecondes afin d'être bien
		// sûr qu'après cette date l'obstacle soit parti
		long date_fin = state.gridspace.getDateSomethingChange() + 5;
/*		if(robot instanceof RobotReal)
		{
			while(robot.getTempsDepuisDebutMatch() < date_fin)
			{
				
			}
		}
		else*/
		sleepUntil(state, date_fin);
	}

	public static final void stopper(GameState<? extends Robot, ReadWrite> state) throws FinMatchException
	{
		state.robot.stopper();
	}
	
    public static final void tourner(GameState<? extends Robot, ReadWrite> state, double angle) throws UnableToMoveException, FinMatchException
    {
    	state.robot.tourner(angle);
    }
    
    public static final void avancer(GameState<? extends Robot, ReadWrite> state, int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException, FinMatchException
    {
    	state.robot.avancer(distance, hooks, mur);
    }
    
    public static final void suit_chemin(GameState<RobotChrono, ReadWrite> state, ArrayList<LocomotionArc> chemin, ArrayList<Hook> hooks) throws UnableToMoveException, FinMatchException, ScriptHookException
    {
    	state.robot.suit_chemin(chemin, hooks);
    }
    
	public static final void set_vitesse(GameState<? extends Robot, ReadWrite> state, Speed vitesse) throws FinMatchException
	{
		state.robot.setVitesse(vitesse);
	}
	
	public static final void setPositionOrientationSTM(GameState<? extends Robot, ReadWrite> state, Vec2<ReadOnly> position, double orientation) throws FinMatchException
	{
		state.robot.setPositionOrientationSTM(position, orientation);
	}
	
    public static final Vec2<ReadOnly> getPosition(GameState<? extends Robot, ReadOnly> state) throws FinMatchException
    {
    	return state.robot.getPosition();
    }
    
    public static final double getOrientation(GameState<? extends Robot, ReadOnly> state) throws FinMatchException
    {
    	return state.robot.getOrientation();
    }
    
    public static final void sleep(GameState<? extends Robot, ReadWrite> state, long duree, ArrayList<Hook> hooks) throws FinMatchException
    {
    	state.robot.sleep(duree, hooks);
    }

    public static final void sleep(GameState<? extends Robot, ReadWrite> state, long duree) throws FinMatchException
    {
    	state.robot.sleep(duree, new ArrayList<Hook>());
    }
    
/*    public static final void desactiveAsservissement(GameState<? extends Robot, ReadWrite> state) throws FinMatchException
    {
    	state.robot.desactiveAsservissement();
    }
    
    public static final void activeAsservissement(GameState<? extends Robot, ReadWrite> state) throws FinMatchException
    {
    	state.robot.activeAsservissement();
    }*/
    
    public static final long getTempsDepuisDebutMatch(GameState<? extends Robot, ReadOnly> state)
    {
    	return state.robot.getTempsDepuisDebutMatch();
    }
    
    public static final RobotChrono cloneIntoRobotChrono(GameState<? extends Robot, ReadOnly> state) throws FinMatchException
    {
    	return state.robot.cloneIntoRobotChrono();
    }
    
	public static final void va_au_point_pathfinding_no_hook(GameState<RobotChrono, ReadWrite> state, LocomotionArc segment) throws FinMatchException
	{
		state.robot.va_au_point_pathfinding_no_hook(segment);
	}
	
	public static final void va_au_point_pathfinding(GameState<RobotChrono, ReadWrite> state, PathfindingNodes n, int differenceDistance, ArrayList<Hook> hooks) throws FinMatchException
	{
		state.robot.va_au_point_pathfinding(n, differenceDistance, hooks);
	}

	public static final double calculateDelta(GameState<RobotChrono, ReadOnly> state, double angle)
	{
		return state.robot.calculateDelta(angle);
	}
	
	public static final void setPositionPathfinding(GameState<RobotChrono, ReadWrite> state, PathfindingNodes n)
	{
		state.robot.setPositionPathfinding(n);
	}

	public static final PathfindingNodes getPositionPathfinding(GameState<RobotChrono, ReadOnly> state)
	{
		return state.robot.getPositionPathfinding();
	}
    
/*    public static final void createHypotheticalEnnemy(GameState<RobotChrono, ReadWrite> state, Vec2<ReadOnly> position, int date_actuelle)
    {
    	state.gridspace.createHypotheticalEnnemy(position, date_actuelle);
    }*/
    
    public static final void setAvoidGameElement(GameState<RobotChrono, ReadWrite> state, boolean avoidGameElement)
    {
    	state.gridspace.setAvoidGameElement(avoidGameElement);
    }

	public static final PathfindingNodes nearestReachableNode(GameState<?, ReadOnly> state, Vec2<ReadOnly> point, int date) throws GridSpaceException
	{
		return state.gridspace.nearestReachableNode(point, date);
	}
	
    public static final boolean isTraversable(GameState<?, ReadOnly> state, Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, int date)
    {
    	return state.gridspace.isTraversable(pointA, pointB, date);
    }

	public static final boolean isTraversable(GameState<?, ReadOnly> state, PathfindingNodes i, PathfindingNodes j, int date)
    {
    	return state.gridspace.isTraversable(i, j, date);
    }

	public static final boolean isTraversableCourbe(GameState<?, ReadOnly> state, PathfindingNodes objectifFinal, PathfindingNodes intersection, Vec2<ReadOnly> directionAvant, int tempsDepuisDebutMatch)
	{
		return state.gridspace.isTraversableCourbe(objectifFinal, intersection, directionAvant, tempsDepuisDebutMatch);
	}

	public static final void setDone(GameState<?, ReadWrite> state, GameElementNames element, Tribool done)
	{
		state.gridspace.setDone(element, done);
	}

	public static final Tribool isDone(GameState<?, ReadOnly> state, GameElementNames element)
	{
		return state.gridspace.isDone(element);
	}

	public static final void tourner_relatif(GameState<?, ReadWrite> state, double angle) throws UnableToMoveException, FinMatchException
	{
		state.robot.tourner(GameState.getOrientation(state.getReadOnly()) + angle);
	}

    public static final void tourner_sans_symetrie(GameState<?, ReadWrite> state, double angle) throws UnableToMoveException, FinMatchException
    {
    	state.robot.tourner_sans_symetrie(angle);
    }

    public static final void avancer(GameState<?, ReadWrite> state, int distance) throws UnableToMoveException, FinMatchException
    {
        state.robot.avancer(distance, new ArrayList<Hook>(), false);
    }

    public static final void avancer(GameState<?, ReadWrite> state, int distance, ArrayList<Hook> hooks) throws UnableToMoveException, FinMatchException
    {    	
        GameState.avancer(state, distance, hooks, false);
    }

    public static final void avancer_dans_mur(GameState<?, ReadWrite> state, int distance) throws UnableToMoveException, FinMatchException
    {
    	state.robot.avancer_dans_mur(distance);
    }

    public static final void sleepUntil(GameState<?, ReadWrite> state, long date) throws FinMatchException
    {
    	state.robot.sleepUntil(date);
    }
    
/*    public static final void creer_obstacle(GameState<RobotReal, ReadWrite> state, Vec2<ReadOnly> position, int date)
    {
    	state.gridspace.creer_obstacle(position, date);
    }*/

	public static final void reinitConnections(GameState<RobotReal, ?> state)
	{
		state.gridspace.reinitConnections();
	}
	
	public static final void reinitDate(GameState<RobotChrono, ?> state)
	{
		state.robot.reinitDate();
	}
	
    /**
     * Créer un obstacle maintenant.
     * Utilisé par le thread de capteurs.
     * @param position
     */
/*    public static final void creer_obstacle(GameState<RobotReal, ReadWrite> state, Vec2<ReadOnly> position)
    {
    	creer_obstacle(state, position, (int)(System.currentTimeMillis() - Config.getDateDebutMatch()));
    }*/

	@SuppressWarnings("unchecked")
	public final GameState<R, ReadOnly> getReadOnly() {
		return (GameState<R, ReadOnly>) this;
	}

	public static final boolean isAtPathfindingNodes(GameState<RobotChrono, ReadOnly> state) {
		return state.robot.isAtPathfindingNodes();
	}
	
}
