package strategie;

import java.util.ArrayList;

import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import hook.Hook;
import hook.HookFactory;
import hook.types.HookDemiPlan;
import container.Service;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.UnableToMoveException;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotReal;
import robot.Speed;
import table.GridSpace;
import utils.Log;
import utils.Config;
import vec2.Permission;
import vec2.ReadOnly;
import vec2.ReadWrite;
import vec2.Vec2;

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
    
    // La hook factory est privée. Elle n'est pas copiée d'un gamestate à l'autre.
    private HookFactory hookfactory;
    
    private int indice_memory_manager;
    
    private Log log;
    private Config config;

    /**
     * De manière publique, on ne peut créer qu'un GameState<RobotReal>, et pas de GameState<RobotChrono>
     * @param config
     * @param log
     * @param table
     * @param obstaclemanager
     * @param robot
     * @return
     */
    public static GameState<RobotReal,ReadWrite> constructRealGameState(Config config, Log log, GridSpace gridspace, RobotReal robot, HookFactory hookfactory)
    {
    	return new GameState<RobotReal,ReadWrite>(config, log, gridspace, robot, hookfactory);
    }
    
    private GameState(Config config, Log log, GridSpace gridspace, R robot, HookFactory hookfactory)
    {
        this.config = config;
        this.log = log;
        this.gridspace = gridspace;
        this.robot = robot;
        this.hookfactory = hookfactory;
        if(robot instanceof RobotReal)
        {
        	robot.setHookFinMatch(hookfactory.getHooksFinMatchReal(this));
        	((RobotReal)robot).setHookTrajectoireCourbe(new HookDemiPlan(config, log, this));
        }
        else
            robot.setHookFinMatch(hookfactory.getHooksFinMatchChrono(this));
        updateConfig();
    }
    
    /**
     * Clone en dehors du memory manager.
     * @return
     * @throws FinMatchException
     */
	public static GameState<RobotChrono,ReadWrite> cloneGameState(GameState<? extends Robot,ReadOnly> state) throws FinMatchException
	{
		return GameState.cloneGameState(state, -1);
	}

	/**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si this est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
	public static GameState<RobotChrono,ReadWrite> cloneGameState(GameState<? extends Robot,ReadOnly> state, int indice_memory_manager) throws FinMatchException
	{
		// On instancie la table avant car il faut donner le même objet deux fois en paramètres
		GameState<RobotChrono,ReadWrite> cloned = new GameState<RobotChrono,ReadWrite>(state.config, state.log, state.gridspace.clone(GameState.getTempsDepuisDebut(state)), state.robot.cloneIntoRobotChrono(), state.hookfactory);
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
    public static void copy(GameState<?,ReadOnly> state, GameState<RobotChrono,ReadWrite> modified) throws FinMatchException
    {
        state.robot.copy(other.robot);
    	// la copie de la table est faite dans gridspace
        // mise à jour des obstacles et du cache incluse dans la copie
        state.gridspace.copy(other.gridspace, state.robot.getTempsDepuisDebutMatch());
    }

    @Override
    public void updateConfig()
    {
    	log.updateConfig();
        robot.updateConfig();
        gridspace.updateConfig();
        hookfactory.updateConfig();
    }
   
    /**
     * Petite surcouche
     * @return
     */
    public static long getTempsDepuisDebut(GameState<RobotChrono,ReadOnly> state)
    {
    	return state.robot.getTempsDepuisDebutMatch();
    }
    
    /**
     * Utilisé par le memory manager
     * @return
     */
    public int getIndiceMemoryManager()
    {
    	return indice_memory_manager;
    }

    /**
     * Disponible uniquement pour GameState<RobotChrono>
     * @return
     */
	public long getHash()
	{
		/**
		 * Un long est codé sur 64 bits.
		 * T'es content Martial, y'a assez de commentaires?
		 * Je peux en rajouter si tu veux.
		 * La vitesse de pointe d'une autruche est de 70km/h (dans le référentiel de Piccadilly Circus)
		 * C'est plus rapide que RCVA. Du coup, on sait comment faire pour les battre.
		 */		
		long hash;
		hash = gridspace.getHash(); // codé sur le reste
		hash = (hash << 16) | ((RobotChrono) robot).getHash(); // codé sur 16 bits (cf getHash() de RobotChrono)
		return hash;
	}
	
	/**
	 * Debug
	 */
	public void printHash()
	{
		gridspace.printHash();
		((RobotChrono)robot).printHash();
	}

	/**
	 * Utilisé par le memory manager
	 * @param indice
	 */
	public void setIndiceMemoryManager(int indice)
	{
		indice_memory_manager = indice;
	}
	
	/**
	 * Permet de décider de la durée de l'anticipation
	 * @param dateLimite
	 */
	public static void updateHookFinMatch(GameState<?,ReadWrite> state, int dateLimite)
	{
		state.robot.updateHookFinMatch(dateLimite);
	}

	/**
	 * Utilisé par le script d'attente
	 * @return
	 */
	public boolean canSleepUntilSomethingChange()
	{
		// si on utilise le vrai robot, alors les valeurs des capteurs peuvent changer
		// (ce qui n'est pas anticipable par robotchrono)
		if(robot instanceof RobotReal)
			return true;
		return gridspace.getDateSomethingChange() != Integer.MAX_VALUE;
	}
	
	// FIXME: vérifier aussi les capteurs du robot vrai
	/**
	 * Utilisé par le script d'attente
	 * @throws FinMatchException
	 */
	public static void sleepUntilSomethingChange(GameState<?,ReadWrite> state) throws FinMatchException
	{
		// on ajoute quelques microsecondes afin d'être bien
		// sûr qu'après cette date l'obstacle soit parti
		int date_fin = state.gridspace.getDateSomethingChange() + 5;
/*		if(robot instanceof RobotReal)
		{
			while(robot.getTempsDepuisDebutMatch() < date_fin)
			{
				
			}
		}
		else*/
		state.robot.sleepUntil(date_fin);
	}

	public static void stopper(GameState<? extends Robot, ReadWrite> state) throws FinMatchException
	{
		state.robot.stopper();
	}
	
    public static void tourner(GameState<? extends Robot, ReadWrite> state, double angle) throws UnableToMoveException, FinMatchException
    {
    	state.robot.tourner(angle);
    }
    
    public static void avancer(GameState<? extends Robot, ReadWrite> state, int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException, FinMatchException
    {
    	state.robot.avancer(distance, hooks, mur);
    }
    
    public static void suit_chemin(GameState<? extends Robot, ReadWrite> state, ArrayList<SegmentTrajectoireCourbe> chemin, ArrayList<Hook> hooks) throws UnableToMoveException, FinMatchException, ScriptHookException
    {
    	state.robot.suit_chemin(chemin, hooks);
    }
    
	public static void set_vitesse(GameState<? extends Robot, ReadWrite> state, Speed vitesse) throws FinMatchException
	{
		state.robot.set_vitesse(vitesse);
	}
	
	public static void setPosition(GameState<? extends Robot, ReadWrite> state, Vec2<ReadOnly> position) throws FinMatchException
	{
		state.robot.setPosition(position);
	}
	
	public static void setOrientation(GameState<? extends Robot, ReadWrite> state, double orientation) throws FinMatchException
	{
		state.robot.setOrientation(orientation);
	}
	
    public static Vec2<ReadOnly> getPosition(GameState<? extends Robot, ReadOnly> state) throws FinMatchException
    {
    	return state.robot.getPosition();
    }
    
    public static double getOrientation(GameState<? extends Robot, ReadOnly> state) throws FinMatchException
    {
    	return state.robot.getOrientation();
    }
    
    public static void sleep(GameState<? extends Robot, ReadWrite> state, long duree, ArrayList<Hook> hooks) throws FinMatchException
    {
    	state.robot.sleep(duree, hooks);
    }

    public static void sleep(GameState<? extends Robot, ReadWrite> state, long duree) throws FinMatchException
    {
    	state.robot.sleep(duree);
    }

    public static void desactiver_asservissement_rotation(GameState<? extends Robot, ReadWrite> state) throws FinMatchException
    {
    	state.robot.desactiver_asservissement_rotation();
    }
    
    public static void desactiver_asservissement_translation(GameState<? extends Robot, ReadWrite> state) throws FinMatchException
    {
    	state.robot.desactiver_asservissement_translation();
    }
    
    public static void activer_asservissement_rotation(GameState<? extends Robot, ReadWrite> state) throws FinMatchException
    {
    	state.robot.activer_asservissement_rotation();
    }
    
    public static int getTempsDepuisDebutMatch(GameState<? extends Robot, ReadOnly> state)
    {
    	return state.robot.getTempsDepuisDebutMatch();
    }
    
    public static RobotChrono cloneIntoRobotChrono(GameState<? extends Robot, ReadOnly> state) throws FinMatchException
    {
    	return state.robot.cloneIntoRobotChrono();
    }

	public static void va_au_point_pathfinding_no_hook(GameState<RobotChrono, ReadWrite> state, SegmentTrajectoireCourbe segment) throws FinMatchException
	{
		state.robot.va_au_point_pathfinding_no_hook(segment);
	}
	
	public static void va_au_point_pathfinding(GameState<RobotChrono, ReadWrite> state, PathfindingNodes n, int differenceDistance, ArrayList<Hook> hooks) throws FinMatchException
	{
		state.robot.va_au_point_pathfinding(n, differenceDistance, hooks);
	}

	public static double calculateDelta(GameState<RobotChrono, ReadOnly> state, double angle)
	{
		return state.robot.calculateDelta(angle);
	}
	
	public static void setPositionPathfinding(GameState<RobotChrono, ReadWrite> state, PathfindingNodes n)
	{
		state.robot.setPositionPathfinding(n);
	}

	public static PathfindingNodes getPositionPathfinding(GameState<RobotChrono, ReadOnly> state)
	{
		return state.robot.getPositionPathfinding();
	}
    
    public static void createHypotheticalEnnemy(GameState<RobotChrono, ReadWrite> state, Vec2<ReadOnly> position, int date_actuelle)
    {
    	state.gridspace.createHypotheticalEnnemy(position, date_actuelle);
    }

	@SuppressWarnings("unchecked")
	public final GameState<R, ReadOnly> getReadOnly() {
		return (GameState<R, ReadOnly>) this;
	}

	public static boolean isAtPathfindingNodes(GameState<RobotChrono, ReadOnly> state) {
		return state.robot.isAtPathfindingNodes();
	}
	
}
