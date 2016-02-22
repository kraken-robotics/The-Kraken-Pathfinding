package pathfinding;

import obstacles.memory.ObstaclesIteratorFutur;
import robot.Robot;
import robot.RobotChrono;
import exceptions.FinMatchException;
import table.Table;
import utils.Config;
import utils.Log;

/**
 * Le game state rassemble toutes les informations disponibles à un instant
 * - infos sur le robot (position, objet, ...) dans Robot
 * - infos sur les obstacles mobiles dans ObstaclesIteratorFutur
 * - infos sur les éléments de jeux dans Table
 * @author pf
 *
 * @param <R>
 */

public class ChronoGameState
{
    public final RobotChrono robot;
    public final ObstaclesIteratorFutur iterator;
    public final Table table;    
    protected Log log;

    public ChronoGameState(Log log, RobotChrono robot, ObstaclesIteratorFutur iterator, Table table)
    {
    	this.log = log;
    	this.robot = robot;
    	this.iterator = iterator;
    	this.table = table;
    }
    
	/**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si l'original est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
	public final ChronoGameState cloneGameState()
	{
		ChronoGameState cloned = new ChronoGameState(log, robot.cloneIntoRobotChrono(), iterator.clone(), table.clone());
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
    public final void copyThetaStar(ChronoGameState modified)
    {
    	table.copy(modified.table);
        robot.copyThetaStar(modified.robot);
        iterator.copy(modified.iterator, robot.getTempsDepuisDebutMatch());
        // Table a été copié par gridspace
    }

    /**
     * Disponible uniquement pour GameState<RobotChrono>
     * @return
     */
	public final int getHashLPAStar()
	{
		/**
		 * Un long est codé sur 64 bits.
		 * T'es content Martial, y'a assez de commentaires?
		 * Je peux en rajouter si tu veux.
		 * La vitesse de pointe d'une autruche est de 70km/h (dans le référentiel de Piccadilly Circus)
		 * C'est plus rapide que RCVA. Du coup, on sait comment faire pour les battre.
		 */		
		int hash;
		hash = table.getHashLPAStar(); // codé sur le reste
		hash = (hash << 16) | robot.getHashLPAStar(); // codé sur 16 bits (cf getHash() de RobotChrono)
		return hash;
	}

	public Robot getRobot()
	{
		return robot;
	}

	public void updateConfig(Config config)
	{
		robot.updateConfig(config);
		table.updateConfig(config);
	}
	
	public void copyAStarCourbe(ChronoGameState state)
	{
	}
}
