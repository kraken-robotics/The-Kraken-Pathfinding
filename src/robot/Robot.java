package robot;

import java.util.ArrayList;

import permissions.ReadOnly;
import permissions.ReadWrite;
import hook.Hook;
import container.Service;
import exceptions.FinMatchException;
import exceptions.UnableToMoveException;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import utils.Vec2;

/**
 * Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF, marsu
 */

public abstract class Robot implements Service 
{
	
	/*
	 * DÉPLACEMENT HAUT NIVEAU
	 */
	
	public abstract void stopper();
    public abstract void tourner(double angle)
            throws UnableToMoveException, FinMatchException;
    public abstract void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
            throws UnableToMoveException, FinMatchException;
	public abstract void setVitesse(Speed vitesse);
	
	public abstract void setPositionOrientationSTM(Vec2<ReadOnly> position, double orientation);
    public abstract Vec2<ReadOnly> getPosition();
    public abstract double getOrientation();
    public abstract void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException;
//    public abstract void desactiveAsservissement() throws FinMatchException;
//    public abstract void activeAsservissement() throws FinMatchException;
    public abstract long getTempsDepuisDebutMatch();
    public abstract RobotChrono cloneIntoRobotChrono();

    protected volatile Vec2<ReadWrite> position = new Vec2<ReadWrite>();
    protected volatile double orientation;
	protected volatile boolean symetrie;
	protected Speed vitesse;
	protected int pointsObtenus = 0;
    protected volatile long dateDebutMatch;
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
	
	public Robot(Log log)
	{
		this.log = log;
		vitesse = Speed.BETWEEN_SCRIPTS;
	}

	public synchronized void updateConfig(Config config)
	{
		dateDebutMatch = config.getLong(ConfigInfo.DATE_DEBUT_MATCH);
		symetrie = config.getSymmetry();
	}

	public void useConfig(Config config)
	{}

	public Speed getVitesse() {
		return vitesse;
	}

	/**
	 * Tourne par rapport à l'angle actuel.
	 * @param angle
	 * @throws UnableToMoveException
	 * @throws FinMatchException
	 */
	public void tourner_relatif(double angle) throws UnableToMoveException, FinMatchException
	{
		tourner(getOrientation() + angle);
	}

    /**
     * Utilisé lorsque le robot n'a pas de symétrie gauche/droite
     * @param angle
     * @throws UnableToMoveException
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
        setVitesse(Speed.INTO_WALL);
        try {
        	avancer(distance, new ArrayList<Hook>(), true);
        }
        finally
        {
        	// Dans tous les cas, il faut restaurer l'ancienne vitesse
        	setVitesse(sauv_vitesse);
        }
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
