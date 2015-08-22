package strategie;

import obstacles.ObstaclesIterator;
import obstacles.ObstaclesMemory;
import buffer.DataForSerialOutput;
import permissions.Permission;
import permissions.ReadOnly;
import permissions.ReadWrite;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotReal;
import hook.HookFactory;
import container.Service;
import exceptions.FinMatchException;
import table.Table;
import utils.Log;
import utils.Config;

/**
 * Le game state rassemble toutes les informations disponibles à un instant
 * - infos sur le robot (position, objet, ...) dans Robot
 * - infos sur les obstacles mobiles dans ObstaclesMobilesIterator
 * - infos sur les éléments de jeux dans Table
 * @author pf
 *
 * @param <R>
 */

public class GameState<R extends Robot, T extends Permission> implements Service
{
    public final R robot;
    public final ObstaclesIterator iterator;
    public final Table table;
    
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
    public static GameState<RobotReal,ReadWrite> constructRealGameState(Log log, Table table, RobotReal robot, HookFactory hookfactory, DataForSerialOutput serie, ObstaclesMemory memory)
    {
		GameState<RobotReal,ReadWrite> out = new GameState<RobotReal,ReadWrite>(log, new ObstaclesIterator(log, memory), robot, table);
		serie.envoieHooks(hookfactory.getHooksPermanents(out));
		return out;
    }
    
    private GameState(Log log, ObstaclesIterator iterator, R robot, Table table)
    {
    	this.iterator = iterator;
        this.log = log;
        this.robot = robot;
        this.table = table;
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
		GameState<RobotChrono,ReadWrite> cloned = new GameState<RobotChrono,ReadWrite>(state.log, state.iterator.clone(state.robot.getTempsDepuisDebutMatch()), state.robot.cloneIntoRobotChrono(), state.table.clone());
		// la copie est déjà exacte
		//		GameState.copy(state, cloned);
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
        state.table.copy(modified.table);
        state.iterator.copy(modified.iterator, state.robot.getTempsDepuisDebutMatch());
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
		long hash = 0;
//		hash = state.gridspace.getHash(); // codé sur le reste
//		hash = (hash << 16) | state.robot.getHash(); // codé sur 16 bits (cf getHash() de RobotChrono)
		return hash;
	}
	
	public static final void printHash(GameState<RobotChrono,?> state)
	{
//		state.gridspace.printHash();
//		state.robot.printHash();
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
	 * Utilisé par le script d'attente
	 * @return
	 */
	public static final boolean canSleepUntilSomethingChange(GameState<?,ReadOnly> state)
	{
		// si on utilise le vrai robot, alors les valeurs des capteurs peuvent changer
		// (ce qui n'est pas anticipable par robotchrono)
		if(state.robot instanceof RobotReal)
			return true;
		return state.iterator.getDateSomethingChange() != Integer.MAX_VALUE;
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
		long date_fin = state.iterator.getDateSomethingChange() + 5;
/*		if(robot instanceof RobotReal)
		{
			while(robot.getTempsDepuisDebutMatch() < date_fin)
			{
				
			}
		}
		else*/
		state.robot.sleepUntil(date_fin);
	}


	@SuppressWarnings("unchecked")
	public final GameState<R, ReadOnly> getReadOnly() {
		return (GameState<R, ReadOnly>) this;
	}

}
