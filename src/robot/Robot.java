package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import container.Service;
import enums.PathfindingNodes;
import enums.Speed;
import exceptions.FinMatchException;
import exceptions.Locomotion.UnableToMoveException;
import utils.Log;
import utils.Config;

/**
 *  Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF, marsu
 */

public abstract class Robot implements Service 
{
	
	/*
	 * DÉPLACEMENT HAUT NIVEAU
	 */
	
	public abstract void stopper() throws FinMatchException;
    public abstract void tourner(double angle, ArrayList<Hook> hooks, boolean mur)
            throws UnableToMoveException, FinMatchException;
    public abstract void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
            throws UnableToMoveException, FinMatchException;
    public abstract void suit_chemin(ArrayList<PathfindingNodes> chemin, ArrayList<Hook> hooks)
            throws UnableToMoveException, FinMatchException;
	public abstract void set_vitesse(Speed vitesse) throws FinMatchException;
	
	public abstract void setPosition(Vec2 position) throws FinMatchException;
	public abstract void setOrientation(double orientation) throws FinMatchException;
    public abstract Vec2 getPosition() throws FinMatchException;
    public abstract double getOrientation() throws FinMatchException;
    public abstract void sleep(long duree);
    public abstract void setInsiste(boolean insiste);
    public abstract void desactiver_asservissement_rotation() throws FinMatchException;
    public abstract void activer_asservissement_rotation() throws FinMatchException;
    public abstract long getDate();
    
    /*
     * Actionneurs
     */
    
	public abstract void poserDeuxTapis() throws FinMatchException;
	public abstract void leverDeuxTapis() throws FinMatchException;

    
	/**
	 * Copy this dans rc. this reste inchangé.
	 * 
	 * @param rc
	 */
    public void copy(RobotChrono rc) throws FinMatchException
    {
    	// pas besoin de copier symétrie car elle ne change pas en cours de match
    	rc.vitesse = vitesse;
    	((Robot)rc).pointsObtenus = pointsObtenus;
    	rc.date = getDate();
    }

	// Dépendances
	protected Config config;
	protected Log log;
	protected boolean symetrie;
	protected Speed vitesse;
	private int pointsObtenus = 0;
	
	private boolean tapisPoses = false;
	
	public Robot(Config config, Log log)
	{
		this.config = config;
		this.log = log;
		updateConfig();
	}
		
	public void updateConfig()
	{
		symetrie = config.get("couleur").equals("rouge");
	}
	
	public Speed get_vitesse_() {
		return vitesse;
	}

	public void tourner_relatif(double angle) throws UnableToMoveException, FinMatchException
	{
		tourner(getOrientation() + angle, null, false);
	}

    public void tourner(double angle) throws UnableToMoveException, FinMatchException
    {
        tourner(angle, null, false);
    }

    /**
     * Utilisé lorsque le robot n'a pas de symétrie gauche/droite
     * @param angle
     * @throws UnableToMoveException
     */
    public void tourner_sans_symetrie(double angle) throws UnableToMoveException, FinMatchException
    {
        if(symetrie)
            tourner(Math.PI-angle, null, false);
        else
            tourner(angle, null, false);
    }

    public void avancer(int distance) throws UnableToMoveException, FinMatchException
    {
        avancer(distance, null, false);
    }

    public void avancer(int distance, ArrayList<Hook> hooks) throws UnableToMoveException, FinMatchException
    {
        avancer(distance, hooks, false);
    }

    public void avancer_dans_mur(int distance) throws UnableToMoveException, FinMatchException
    {
        Speed sauv_vitesse = vitesse; 
        set_vitesse(Speed.INTO_WALL);
        try {
        	avancer(distance, null, true);
        }
        finally
        {
        	// Dans tous les cas, il faut restaurer l'ancienne vitesse
        	set_vitesse(sauv_vitesse);
        }
    }
    
    // DEPENDS ON RULES

    // Point obtenus depuis le dernier clone! Et pas depuis le début du match.
    public int getPointsObtenus()
    {
    	return pointsObtenus;
    }
    
    protected void tapisRougePose(int nbTapis)
    {
    	if(!tapisPoses)
    		pointsObtenus = pointsObtenus + 12*nbTapis;    	
    	tapisPoses = true;
    }
    
    public boolean areTapisPoses()
    {
    	return tapisPoses;
    }
    
    /**
     * A appeler quand un clap est tombé
     */
    public void clapTombe()
    {
    	pointsObtenus = pointsObtenus + 5;
    }
    
}
