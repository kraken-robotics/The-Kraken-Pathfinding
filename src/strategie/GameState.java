package strategie;

import hook.types.HookFactory;
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
    
    // La hook factory est privée. Elle n'est pas copiée d'un gamestate à l'autre.
    private HookFactory hookfactory;
    
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
    public static GameState<RobotReal> constructRealGameState(Config config, Log log, Table table, GridSpace gridspace, RobotReal robot, HookFactory hookfactory)
    {
    	return new GameState<RobotReal>(config, log, table, gridspace, robot, hookfactory);
    }
    
    private GameState(Config config, Log log, Table table, GridSpace gridspace, R robot, HookFactory hookfactory)
    {
        this.config = config;
        this.log = log;
        this.table = table;
        this.gridspace = gridspace;
        this.robot = robot;
        this.hookfactory = hookfactory;
    }
    
    /**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si this est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
	public GameState<RobotChrono> cloneGameState() throws FinMatchException
	{
		// On instancie la table avant car il faut donner le même objet deux fois en paramètres
		Table new_table = table.clone();
		GameState<RobotChrono> cloned = new GameState<RobotChrono>(config, log, new_table, gridspace.clone(getTempsDepuisDebut(), new_table), robot.cloneIntoRobotChrono(), hookfactory);
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
        robot.copy(other.robot);
        try {
			other.robot.initHooksTable(other);
		} catch (Exception e) {
			// Ne devrait jamais arriver
			e.printStackTrace();
		}
    	// la copie de la table est faite dans gridspace
        // mise à jour des obstacles et du cache incluse dans la copie
        gridspace.copy(other.gridspace, robot.getTempsDepuisDebutMatch());
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
    	return robot.getTempsDepuisDebutMatch();
    }

    public long getTempsDepuisRacine()
    {
    	return robot.getTempsDepuisDebutMatch() + Config.getDateDebutMatch() - dateDebutRacine;
    }
    
    public void commenceRacine()
    {
    	dateDebutRacine = System.currentTimeMillis();
    }

}
