package robot;

import obstacles.types.Obstacle;
import obstacles.types.ObstacleRectangular;
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
    
	public int getDemieLargeurGauche()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDemieLargeurDroite()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDemieLongueurAvant()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDemieLongueurArriere()
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	public double getDemieDiagonale()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
