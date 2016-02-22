package pathfinding;

import obstacles.memory.ObstaclesIteratorFutur;
import obstacles.memory.ObstaclesMemory;
import pathfinding.dstarlite.GridSpace;
import permissions.Permission;
import permissions.ReadOnly;
import permissions.ReadWrite;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotReal;
import container.Service;
import exceptions.FinMatchException;
import table.Table;
import utils.Log;
import utils.Config;

/**
 * Le game state rassemble toutes les informations disponibles à un instant
 * - infos sur le robot (position, objet, ...) dans Robot
 * - infos sur les obstacles mobiles dans ObstaclesIteratorFutur
 * - infos sur les éléments de jeux dans Table
 * @author pf
 *
 * @param <R>
 */

public class GameState<R extends Robot, T extends Permission> implements Service
{
	// cet iterator et cette table sont ceux du gridspace. Modifier l'un modifie l'autre.
    public final R robot;
    public final ObstaclesIteratorFutur iterator;
    public final GridSpace gridspace;
    public final Table table;
    
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
    public static GameState<RobotReal,ReadWrite> constructRealGameState(Log log, RobotReal robot, GridSpace gridspace, ObstaclesMemory memory)
    {
		GameState<RobotReal,ReadWrite> out = new GameState<RobotReal,ReadWrite>(log, gridspace, robot, new ObstaclesIteratorFutur(log, memory));
		return out;
    }
    
    private GameState(Log log,GridSpace gridspace, R robot, ObstaclesIteratorFutur iterator)
    {
    	this.iterator = iterator.clone();
    	this.gridspace = gridspace;
        this.log = log;
        this.robot = robot;
        this.table = gridspace.getTable();
    }
    
	/**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si l'original est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
	public static final GameState<RobotChrono,ReadWrite> cloneGameState(GameState<? extends Robot,ReadOnly> state)
	{
		GameState<RobotChrono,ReadWrite> cloned = new GameState<RobotChrono,ReadWrite>(state.log, state.gridspace.clone(), state.robot.cloneIntoRobotChrono(), state.iterator.clone());
		// la copie est déjà exacte
		//		GameState.copy(state, cloned);
		return cloned;
	}

    /**
     * Copie this dans other. this reste inchangé.
     * Cette copie met à jour les obstacles et les attributs de temps.
     * @param other
     * @throws FinMatchException 
     */
    public static final void copyThetaStar(GameState<?,ReadOnly> state, GameState<RobotChrono,ReadWrite> modified)
    {
        state.robot.copyThetaStar(modified.robot);
        state.gridspace.copy(modified.gridspace);
        state.iterator.copy(modified.iterator, state.robot.getTempsDepuisDebutMatch());
        // Table a été copié par gridspace
    }

    @Override
    public void updateConfig(Config config)
    {
    	robot.updateConfig(config);
    }

    @Override
    public void useConfig(Config config)
    {}

    /**
     * Disponible uniquement pour GameState<RobotChrono>
     * @return
     */
	public static final int getHashLPAStar(GameState<RobotChrono,ReadOnly> state)
	{
		/**
		 * Un long est codé sur 64 bits.
		 * T'es content Martial, y'a assez de commentaires?
		 * Je peux en rajouter si tu veux.
		 * La vitesse de pointe d'une autruche est de 70km/h (dans le référentiel de Piccadilly Circus)
		 * C'est plus rapide que RCVA. Du coup, on sait comment faire pour les battre.
		 */		
		int hash;
		hash = state.table.getHashLPAStar(); // codé sur le reste
		hash = (hash << 16) | state.robot.getHashLPAStar(); // codé sur 16 bits (cf getHash() de RobotChrono)
		return hash;
	}
	
	@SuppressWarnings("unchecked")
	public final GameState<R, ReadOnly> getReadOnly() {
		return (GameState<R, ReadOnly>) this;
	}

	public static void copyAStarCourbe(GameState<?, ReadOnly> state,
			GameState<RobotChrono, ReadWrite> state2) {
		// TODO Auto-generated method stub
		
	}

}
