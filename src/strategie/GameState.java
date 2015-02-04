package strategie;

import hook.HookFactory;
import hook.types.HookDemiPlan;
import container.Service;
import exceptions.FinMatchException;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotReal;
import table.GridSpace;
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
    }

    @Override
    public void updateConfig()
    {
        robot.updateConfig();
        gridspace.updateConfig();
    }
   
    /**
     * Petite surcouche
     * @return
     */
    public long getTempsDepuisDebut()
    {
    	return robot.getTempsDepuisDebutMatch();
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
	public void updateHookFinMatch(int dateLimite)
	{
		robot.updateHookFinMatch(dateLimite);
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
	public void sleepUntilSomethingChange() throws FinMatchException
	{
		// on ajoute quelques microsecondes afin d'être bien
		// sûr qu'après cette date l'obstacle soit parti
		int date_fin = gridspace.getDateSomethingChange() + 5;
/*		if(robot instanceof RobotReal)
		{
			while(robot.getTempsDepuisDebutMatch() < date_fin)
			{
				
			}
		}
		else*/
			robot.sleepUntil(date_fin);
	}

}
