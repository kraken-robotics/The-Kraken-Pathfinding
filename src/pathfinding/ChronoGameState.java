package pathfinding;

import obstacles.memory.ObstaclesIteratorFutur;
import robot.RobotChrono;
import table.Table;
import utils.Config;
import utils.Log;

/**
 * Le game state rassemble toutes les informations disponibles à un instant
 * - infos sur le robot (position, objet, ...) dans Robot
 * - infos sur les obstacles mobiles dans ObstaclesIteratorFutur
 * - infos sur les éléments de jeux dans Table
 * Utilisé dans l'arbre des possibles
 * @author pf
 */

public class ChronoGameState extends GameState
{
    public final ObstaclesIteratorFutur iterator;
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
		return new ChronoGameState(log, robot.cloneIntoRobotChrono(), iterator.clone(), table.clone());
	}

    /**
     * Copie this dans other. this reste inchangé.
     * Cette copie met à jour les obstacles et les attributs de temps.
     * @param other
     * @throws FinMatchException 
     */
    @Override
	public final void copyAStarCourbe(ChronoGameState modified)
    {
    	table.copy(modified.table);
        robot.copy((RobotChrono) modified.robot);
        iterator.copy(modified.iterator, robot.getTempsDepuisDebutMatch());
        // Table a été copié par gridspace
    }

	public void updateConfig(Config config)
	{
		robot.updateConfig(config);
		table.updateConfig(config);
	}
}
