package robot;

import java.util.ArrayList;

import hook.Hook;
import container.Service;
import exceptions.FinMatchException;
import exceptions.UnableToMoveException;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;

/**
 * Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF
 */

public abstract class Robot implements Service 
{
	
	/*
	 * DÉPLACEMENT HAUT NIVEAU
	 */
	
	public abstract int getPositionGridSpace();
    public abstract void tourner(double angle, Speed vitesse)
            throws UnableToMoveException, FinMatchException;
    public abstract void avancer(int distance, ArrayList<Hook> hooks, boolean mur, Speed vitesse)
            throws UnableToMoveException, FinMatchException;
	
    public abstract void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException;
    public abstract long getTempsDepuisDebutMatch();

    protected Cinematique cinematique;
    protected volatile boolean symetrie;
	protected volatile boolean matchDemarre = false;
	protected int pointsObtenus = 0;
    protected volatile long dateDebutMatch;
    
	// Dépendances
	protected Log log;
	
	public Robot(Log log)
	{
		this.log = log;
	}

	public RobotChrono cloneIntoRobotChrono()
	{
		RobotChrono cloned_robotchrono = new RobotChrono(log, cinematique);
		copy(cloned_robotchrono);
		return cloned_robotchrono;
	}

	/**
	 * Copy this dans rc. this reste inchangé.
	 * 
	 * @param rc
	 */
    public final void copy(RobotChrono rc)
    {
    	cinematique.copy(rc.cinematique);
    	// pas besoin de copier symétrie car elle ne change pas en cours de match
    	rc.pointsObtenus = pointsObtenus;
    	rc.date = getTempsDepuisDebutMatch();
    	rc.positionGridSpace = getPositionGridSpace();
    }
	
	public synchronized void updateConfig(Config config)
	{
		dateDebutMatch = config.getLong(ConfigInfo.DATE_DEBUT_MATCH);
		matchDemarre = config.getBoolean(ConfigInfo.MATCH_DEMARRE);
		symetrie = config.getSymmetry();
	}

	public void useConfig(Config config)
	{}
	
	/**
	 * Tourne par rapport à l'angle actuel.
	 * @param angle
	 * @throws UnableToMoveException
	 * @throws FinMatchException
	 */
	public void tournerRelatif(double angle, Speed vitesse) throws UnableToMoveException, FinMatchException
	{
		tourner(cinematique.orientation + angle, vitesse);
	}

    /**
     * Utilisé lorsque le robot n'a pas de symétrie gauche/droite
     * @param angle
     * @throws UnableToMoveException
     */
    public void tournerSansSymetrie(double angle, Speed vitesse) throws UnableToMoveException, FinMatchException
    {
        if(symetrie)
            tourner(Math.PI-angle, vitesse);
        else
            tourner(angle, vitesse);
    }

    /**
     * Avancer sans hook, et pas dans un mur
     * @param distance
     * @throws UnableToMoveException
     * @throws FinMatchException
     * @throws ScriptHookException
     */
    public void avancer(int distance, Speed vitesse) throws UnableToMoveException, FinMatchException
    {
        avancer(distance, new ArrayList<Hook>(), false, vitesse);
    }

    /**
     * Avancer, mais pas dans un mur
     * @param distance
     * @param hooks
     * @throws UnableToMoveException
     * @throws FinMatchException
     * @throws ScriptHookException
     */
    public void avancer(int distance, ArrayList<Hook> hooks, Speed vitesse) throws UnableToMoveException, FinMatchException
    {
        avancer(distance, hooks, false, vitesse);
    }

    /**
     * Avance dans un mur sans hook
     * @param distance
     * @throws UnableToMoveException
     * @throws FinMatchException
     * @throws ScriptHookException
     */
    public void avancerDansMur(int distance) throws UnableToMoveException, FinMatchException
    {
    	avancer(distance, new ArrayList<Hook>(), true, Speed.INTO_WALL);
    }

    /**
     * Dort jusqu'à une certaine date.
     * Utilisé le sleep du robot, donc réel ou simulé.
     * @param date
     * @throws FinMatchException
     */
    public void sleepUntil(long date) throws FinMatchException
    {
    	sleep(date - getTempsDepuisDebutMatch());
    }
    
    /**
     * Dort une certaine durée.
     * Utilise le sleep du robot, donc réel ou simulé.
     * @param duree
     * @throws FinMatchException
     */
    public void sleep(long duree) throws FinMatchException
    {
    	sleep(duree, new ArrayList<Hook>());
    }
    
    @Override
    public String toString()
    {
    	return cinematique.toString();
    }

    
    // DEPENDS_ON_RULES

    /**
     * Points obtenus depuis le début du match
     * @return
     */
    public int getPointsObtenus()
    {
    	return pointsObtenus;
    }    
}
