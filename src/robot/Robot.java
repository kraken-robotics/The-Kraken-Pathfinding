package robot;

import pathfinding.dstarlite.gridspace.PointGridSpace;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;

/**
 * Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF
 */

public abstract class Robot 
{
	
	/*
	 * DÉPLACEMENT HAUT NIVEAU
	 */
	
	public abstract PointGridSpace getPositionGridSpace();
    public abstract long getTempsDepuisDebutMatch();

    protected Cinematique cinematique;
    protected volatile boolean symetrie;
	protected volatile boolean matchDemarre = false;
    protected volatile long dateDebutMatch;
    protected boolean deploye = false;
    
	protected Log log;
	
	public Robot(Log log)
	{
		this.log = log;
		cinematique = new Cinematique();
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
	
	public synchronized void updateConfig(Config config)
	{
		dateDebutMatch = config.getLong(ConfigInfo.DATE_DEBUT_MATCH);
		matchDemarre = config.getBoolean(ConfigInfo.MATCH_DEMARRE);
		symetrie = config.getSymmetry();
	}

	public void useConfig(Config config)
	{}
    
    @Override
    public String toString()
    {
    	return cinematique.toString();
    }
    
	public int getDemieLargeurGauche()
	{
		return 100; // TODO
	}

	public int getDemieLargeurDroite()
	{
		return 100; // TODO
	}

	public int getDemieLongueurAvant()
	{
		return 200; // TODO
	}

	public int getDemieLongueurArriere()
	{
		return 200; // TODO
	}
	
	public double getDemieDiagonale()
	{
		// TODO optimiser en ne faisant le calcul qu'une fois
		return Math.max(Math.max(Math.hypot(getDemieLongueurArriere(), getDemieLargeurGauche()),Math.hypot(getDemieLongueurArriere(), getDemieLargeurDroite())),
				Math.max(Math.hypot(getDemieLongueurAvant(), getDemieLargeurGauche()), Math.hypot(getDemieLongueurAvant(), getDemieLargeurDroite())));
	}

}
