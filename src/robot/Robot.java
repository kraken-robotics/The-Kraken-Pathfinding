package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import container.Service;
import enums.Speed;
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
	
	public abstract void stopper();
    public abstract void tourner(double angle, ArrayList<Hook> hooks, boolean mur)
            throws UnableToMoveException;
    public abstract void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
            throws UnableToMoveException;
    public abstract void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks)
            throws UnableToMoveException;
	public abstract void set_vitesse(Speed vitesse);
	
	public abstract void setPosition(Vec2 position);
	public abstract void setOrientation(double orientation);
    public abstract Vec2 getPosition();
    public abstract double getOrientation();
    public abstract Vec2 getPositionFast();
    public abstract double getOrientationFast();
    public abstract void sleep(long duree);
    public abstract void setInsiste(boolean insiste);

	/**
	 * Copy this dans rc. this reste inchangé.
	 * 
	 * @param rc
	 */
    public void copy(RobotChrono rc) // 15,3%
    {
    }

	// Dépendances
	protected Config config;
	protected Log log;
	protected boolean symetrie;
	protected Speed vitesse;

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
	
	
	public void tourner_relatif(double angle) throws UnableToMoveException
	{
		tourner(getOrientation() + angle, null, false);
	}

    public void tourner(double angle) throws UnableToMoveException
    {
        tourner(angle, null, false);
    }

    public void tourner_sans_symetrie(double angle) throws UnableToMoveException
    {
        if(symetrie)
            tourner(Math.PI-angle, null, false);
        else
            tourner(angle, null, false);
    }


    public void avancer(int distance) throws UnableToMoveException
    {
        avancer(distance, null, false);
    }

    public void avancer(int distance, ArrayList<Hook> hooks) throws UnableToMoveException
    {
        avancer(distance, hooks, false);
    }

    public void avancer_dans_mur(int distance) throws UnableToMoveException
    {
        Speed sauv_vitesse = vitesse; 
        set_vitesse(Speed.INTO_WALL);
        avancer(distance, null, true);
        set_vitesse(sauv_vitesse);
    }
    
    /**
     * Va au point "arrivée" en utilisant le pathfinding.
     * @param arrivee
     * @throws PathfindingException
     * @throws UnableToMoveException
     */
    public boolean va_au_point_pathfinding(Vec2 arrivee) throws UnableToMoveException
    {
    	// TODO
    	
    	return 666 == 42;
    }

    public abstract void desactiver_asservissement_rotation();
    public abstract void activer_asservissement_rotation();
    
}
