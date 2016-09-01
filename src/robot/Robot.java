package robot;

import obstacles.types.Obstacle;
import container.Service;
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
	
	@Override
	public synchronized void updateConfig(Config config)
	{
		dateDebutMatch = config.getLong(ConfigInfo.DATE_DEBUT_MATCH);
		matchDemarre = config.getBoolean(ConfigInfo.MATCH_DEMARRE);
		symetrie = config.getSymmetry();
	}

	@Override
	public void useConfig(Config config)
	{}
    
    @Override
    public String toString()
    {
    	return cinematique.toString();
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
