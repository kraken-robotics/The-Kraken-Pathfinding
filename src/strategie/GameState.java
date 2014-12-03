package strategie;

import pathfinding.GridSpace;
import container.Service;
import exceptions.GameStateException;
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
    
    // time contient le temps écoulé depuis le début du match en ms
    // utilisé uniquement dans l'arbre des possibles
    public long temps_depuis_debut;
    public long temps_depuis_racine;  
    public int pointsObtenus;	// points marqués depus le debut du match

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
        pointsObtenus = 0;
    }
    
    /**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si this est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
    public GameState<RobotChrono> clone()
    {
    	// Si on fait le clone d'un GameState<RobotChrono>, on met à jour son temps auparavant
    	if(robot instanceof RobotChrono)
    	{
    		int temps_ecoule = ((RobotChrono)robot).get_compteur();
    		((RobotChrono)robot).initChrono();
    		temps_depuis_debut = temps_depuis_debut + temps_ecoule;
    		temps_depuis_racine = temps_depuis_racine + temps_ecoule;    		
    	}

    	Table new_table = table.clone();
        RobotChrono new_rc = new RobotChrono(config, log); 
        robot.copy(new_rc);;
        GridSpace new_gridspace = gridspace.clone(temps_depuis_debut);
        
        GameState<RobotChrono> out = new GameState<RobotChrono>(config, log, new_table, new_gridspace, new_rc);
        out.temps_depuis_debut = temps_depuis_debut;
        out.temps_depuis_racine = temps_depuis_racine;
        out.pointsObtenus = pointsObtenus;
        return out;
    }

    /**
     * Copie this dans other. this reste inchangé.
     * @param other
     */
    public void copy(GameState<RobotChrono> other)
    {
        table.copy(other.table);
        robot.copy(other.robot);        
        gridspace.copy(other.gridspace);

        other.temps_depuis_debut = temps_depuis_debut;
        other.temps_depuis_racine = temps_depuis_racine;
        other.pointsObtenus = pointsObtenus;
    }

    @Override
    public void updateConfig()
    {
        table.updateConfig();
        robot.updateConfig();
        gridspace.updateConfig();
    }
    
}
