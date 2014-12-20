package strategie;

import hook.types.HookFactory;
import pathfinding.GridSpace;
import container.Service;
import enums.GameElementNames;
import exceptions.FinMatchException;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotReal;
import utils.Log;
import utils.Config;

/**
 * Le game state rassemble toutes les informations disponibles à un instant
 * - infos sur le robot (position, objet, ...) dans R
 * - infos sur les obstacles et les éléments de jeux (robot ennemi, table, ...) dans GridSpace
 * @author pf
 *
 * @param <R>
 */

public class GameState<R extends Robot> implements Service
{    
    public final R robot;
    public final GridSpace gridspace;
    
    // La hook factory est privée. Elle n'est pas copiée d'un gamestate à l'autre.
    private HookFactory hookfactory;
    private int indice_memory_manager;
    
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
    public static GameState<RobotReal> constructRealGameState(Config config, Log log, GridSpace gridspace, RobotReal robot, HookFactory hookfactory)
    {
    	return new GameState<RobotReal>(config, log, gridspace, robot, hookfactory);
    }
    
    private GameState(Config config, Log log, GridSpace gridspace, R robot, HookFactory hookfactory)
    {
        this.config = config;
        this.log = log;
        this.gridspace = gridspace;
        this.robot = robot;
        this.hookfactory = hookfactory;        
        updateConfig();
    }
    
	public GameState<RobotChrono> cloneGameState() throws FinMatchException
	{
		return cloneGameState(-1);
	}

	/**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si this est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
	public GameState<RobotChrono> cloneGameState(int indice_memory_manager) throws FinMatchException
	{
		// On instancie la table avant car il faut donner le même objet deux fois en paramètres
		GameState<RobotChrono> cloned = new GameState<RobotChrono>(config, log, gridspace.clone(getTempsDepuisDebut()), robot.cloneIntoRobotChrono(), hookfactory);
		copy(cloned);
		cloned.indice_memory_manager = indice_memory_manager;
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
    	// la copie de la table est faite dans gridspace
        // mise à jour des obstacles et du cache incluse dans la copie
        gridspace.copy(other.gridspace, robot.getTempsDepuisDebutMatch());
        other.dateDebutRacine = dateDebutRacine;
    }

    @Override
    public void updateConfig()
    {
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
    
    public int getIndiceMemoryManager()
    {
    	return indice_memory_manager;
    }

	public long getHash()
	{
		// un long est codé sur 64 bits.
		// Du coup, on a de la marge.
		long hash = 0;
		try {
			hash = gridspace.nbObstaclesMobiles(); // codé sur autant de bits qu'il le faut puisqu'il est dans les bits de poids forts
			hash = (hash << 12) + robot.getPosition().x+1500; // codé sur 12 bits (0 à 3000)
			hash = (hash << 11) + robot.getPosition().y; // codé sur 11 bits (0 à 2000)
			hash = (hash << (2*GameElementNames.values().length)) + gridspace.getHashTable(); // codé sur 2 bits par élément de jeux (2 bit par Tribool)
			hash = (hash << 9) + robot.getPointsObtenus(); // d'ici provient le &511 de StrategyArcManager (511 = 2^9 - 1)
		} catch (FinMatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hash;
	}

	public void setIndiceMemoryManager(int indice)
	{
		indice_memory_manager = indice;
	}

}
