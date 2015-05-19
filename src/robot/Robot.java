package robot;

import java.util.ArrayList;

import permissions.ReadOnly;
import planification.LocomotionArc;
import hook.Hook;
import hook.types.HookDateFinMatch;
import container.Service;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.UnableToMoveException;
import utils.Log;
import utils.Config;
import utils.Vec2;

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
    public abstract void tourner(double angle)
            throws UnableToMoveException, FinMatchException;
    public abstract void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
            throws UnableToMoveException, FinMatchException;
    public abstract void suit_chemin(ArrayList<LocomotionArc> chemin, ArrayList<Hook> hooks)
            throws UnableToMoveException, FinMatchException, ScriptHookException;
	public abstract void set_vitesse(Speed vitesse) throws FinMatchException;
	
	public abstract void setPosition(Vec2<ReadOnly> position) throws FinMatchException;
	public abstract void setOrientation(double orientation) throws FinMatchException;
    public abstract Vec2<ReadOnly> getPosition() throws FinMatchException;
    public abstract double getOrientation() throws FinMatchException;
    public abstract void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException;
    public abstract void desactiver_asservissement_rotation() throws FinMatchException;
    public abstract void desactiver_asservissement_translation() throws FinMatchException;
    public abstract void activer_asservissement_rotation() throws FinMatchException;
    public abstract int getTempsDepuisDebutMatch();
    public abstract RobotChrono cloneIntoRobotChrono();

    protected HookDateFinMatch hookFinMatch;
    
    /*
     * Actionneurs
     */
    
    
	/**
	 * Copy this dans rc. this reste inchangé.
	 * 
	 * @param rc
	 */
    public void copy(RobotChrono rc)
    {
    	// pas besoin de copier symétrie car elle ne change pas en cours de match
    	rc.vitesse = vitesse;
    	rc.pointsObtenus = pointsObtenus;
    	rc.date = getTempsDepuisDebutMatch();
    }

	// Dépendances
	protected Log log;
	protected boolean symetrie;
	protected Speed vitesse;
	protected int pointsObtenus = 0;
	
	public Robot(Log log)
	{
		this.log = log;
		vitesse = Speed.BETWEEN_SCRIPTS;
	}
	
	/**
	 * Appelé une seule fois
	 * @param hookFinMatch
	 */
	public void setHookFinMatch(HookDateFinMatch hookFinMatch)
	{
		this.hookFinMatch = hookFinMatch;
	}
	
	/**
	 * Mise à jour permettant de modifier, pour RobotChrono, la date limite de la recherche stratégique
	 * @param dateLimite
	 */
	public void updateHookFinMatch(int dateLimite)
	{
		hookFinMatch.updateDate(dateLimite);
	}

	public void updateConfig(Config config)
	{
		symetrie = config.getSymmetry();
	}
	
	public Speed getVitesse() {
		return vitesse;
	}

	/**
	 * Tourne par rapport à l'angle actuel.
	 * @param angle
	 * @throws UnableToMoveException
	 * @throws FinMatchException
	 * @throws ScriptHookException
	 */
	public void tourner_relatif(double angle) throws UnableToMoveException, FinMatchException
	{
		tourner(getOrientation() + angle);
	}

    /**
     * Utilisé lorsque le robot n'a pas de symétrie gauche/droite
     * @param angle
     * @throws UnableToMoveException
     * @throws ScriptHookException 
     */
    public void tourner_sans_symetrie(double angle) throws UnableToMoveException, FinMatchException
    {
        if(symetrie)
            tourner(Math.PI-angle);
        else
            tourner(angle);
    }

    /**
     * Avancer sans hook, et pas dans un mur
     * @param distance
     * @throws UnableToMoveException
     * @throws FinMatchException
     * @throws ScriptHookException
     */
    public void avancer(int distance) throws UnableToMoveException, FinMatchException
    {
        avancer(distance, new ArrayList<Hook>(), false);
    }

    /**
     * Avancer, mais pas dans un mur
     * @param distance
     * @param hooks
     * @throws UnableToMoveException
     * @throws FinMatchException
     * @throws ScriptHookException
     */
    public void avancer(int distance, ArrayList<Hook> hooks) throws UnableToMoveException, FinMatchException
    {
        avancer(distance, hooks, false);
    }

    /**
     * Avance dans un mur sans hook
     * @param distance
     * @throws UnableToMoveException
     * @throws FinMatchException
     * @throws ScriptHookException
     */
    public void avancer_dans_mur(int distance) throws UnableToMoveException, FinMatchException
    {
        Speed sauv_vitesse = vitesse; 
        set_vitesse(Speed.INTO_WALL);
        try {
        	avancer(distance, new ArrayList<Hook>(), true);
        }
        finally
        {
        	// Dans tous les cas, il faut restaurer l'ancienne vitesse
        	set_vitesse(sauv_vitesse);
        }
    }

    /**
     * Dort jusqu'à une certaine date.
     * Utilisé le sleep du robot, donc réel ou simulé.
     * @param date
     * @throws FinMatchException
     */
    public void sleepUntil(int date) throws FinMatchException
    {
    	sleep(date - getTempsDepuisDebutMatch());
    }
    
    /**
     * Dort une certaine durée.
     * Utilisé le sleep du robot, donc réel ou simulé.
     * @param duree
     * @throws FinMatchException
     */
    public void sleep(long duree) throws FinMatchException
    {
    	sleep(duree, new ArrayList<Hook>());
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
