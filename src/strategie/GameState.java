package strategie;

import pathfinding.GridSpace;
import container.Service;
import exceptions.FinMatchException;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotReal;
import table.Table;
import utils.Log;
import utils.Config;

/**
 * Le game state rassemble toutes les informations disponibles à un instant
 * - infos sur le robot (position, objet, ...) dans R
 * - infos sur les obstacles (robot ennemi, ...) dans GridSpace
 * - infos sur les éléments de jeux (pris ou non, ...) dans Table
 * @author pf
 *
 * @param <R>
 */

public class GameState<R extends Robot> implements Service
{    
    public final Table table;
    public R robot;
    public final GridSpace gridspace;

    private Log log;
    private Config config;
    private long dateDebutRacine;

    /**
     * De manière publique, on ne peut créer qu'un GameState<RobotReal>, et pas de GameState<RobotChrono>
     * @param config
     * @param log
     * @param table
     * @param obstaclemanager
     * @param robot
     * @return
     */
    public static GameState<RobotReal> constructRealGameState(Config config, Log log, Table table, GridSpace gridspace, RobotReal robot)
    {
    	return new GameState<RobotReal>(config, log, table, gridspace, robot);
    }
    
    private GameState(Config config, Log log, Table table, GridSpace gridspace, R robot)
    {
        this.config = config;
        this.log = log;
        this.table = table;
        this.gridspace = gridspace;
        this.robot = robot;
    }
    
    /**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si this est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
	public GameState<RobotChrono> cloneGameState() throws FinMatchException
	{
		GameState<RobotChrono> cloned = new GameState<RobotChrono>(config, log, table.clone(), gridspace.clone(getTempsDepuisDebut()), new RobotChrono(config, log));
		copy(cloned);
		return cloned;
	}

    /**
     * Copie this dans other. this reste inchangé.
     * Cette copie met à jour les obstacles et les attributs de temps.
     * @param other
     * @throws FinMatchException 
     */
    public void copy(GameState<RobotChrono> other) throws FinMatchException
    {
        table.copy(other.table);
        // réinitialisation de la durée incluse dans la copie
        robot.copy(other.robot);        
        // mise à jour des obstacles et du cache incluse dans la copie
        gridspace.copy(other.gridspace, robot.getDate() - Config.dateDebutMatch);
        other.dateDebutRacine = dateDebutRacine;
    }

    @Override
    public void updateConfig()
    {
        table.updateConfig();
        robot.updateConfig();
        gridspace.updateConfig();
    }
   
    public long getTempsDepuisDebut()
    {
    	return robot.getDate() - Config.dateDebutMatch;
    }

    public long getTempsDepuisRacine()
    {
    	return robot.getDate() - dateDebutRacine;
    }
    
    public void commenceRacine()
    {
    	dateDebutRacine = System.currentTimeMillis();
    }

}
