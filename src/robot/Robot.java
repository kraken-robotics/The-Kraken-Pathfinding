package robot;

import obstacles.types.Obstacle;
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
    public abstract void avancer(int distance, boolean mur, Speed vitesse)
            throws UnableToMoveException;
	
    public abstract void sleep(long duree) throws FinMatchException;
    public abstract long getTempsDepuisDebutMatch();

    protected Cinematique cinematique;
    protected volatile boolean symetrie;
	protected volatile boolean matchDemarre = false;
	protected int pointsObtenus = 0;
    protected volatile long dateDebutMatch;
    protected boolean deploye = false;
    
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
		if(symetrie)
		{
			int x = config.getInt(ConfigInfo.X_DEPART);
			int y = config.getInt(ConfigInfo.Y_DEPART);
			double o = config.getDouble(ConfigInfo.O_DEPART);
			cinematique.getPosition().x = -x;
			cinematique.getPosition().y = y;
			cinematique.orientation = Math.PI - o;
		}
		else
		{
			int x = config.getInt(ConfigInfo.X_DEPART);
			int y = config.getInt(ConfigInfo.Y_DEPART);
			double o = config.getDouble(ConfigInfo.O_DEPART);
			cinematique.getPosition().x = x;
			cinematique.getPosition().y = y;
			cinematique.orientation = o;
		}
	}

	public void useConfig(Config config)
	{}

    /**
     * Avance dans un mur sans hook
     * @param distance
     * @throws UnableToMoveException
     * @throws FinMatchException
     * @throws ScriptHookException
     */
    public void avancerDansMur(int distance) throws UnableToMoveException
    {
    	avancer(distance, true, Speed.INTO_WALL);
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
    
    /**
     * Renvoie la forme actuelle du robot.
     * Utilisé par le pathfinding pour s'assurer qu'on ne se prend pas un obstacle
     * @return
     */
	public Obstacle getCurrentConvexHull()
	{
		return null;
	}    
}
