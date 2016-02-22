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
	public abstract int getPositionGridSpace();
    public abstract void tourner(double angle)
            throws UnableToMoveException, FinMatchException;
    public abstract void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
            throws UnableToMoveException, FinMatchException;
	public abstract void setVitesse(Speed vitesse);
	
    public abstract void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException;
    public abstract long getTempsDepuisDebutMatch();

    protected volatile Vec2<ReadWrite> position = new Vec2<ReadWrite>();
    protected volatile double orientation;
    protected volatile double courbure;
	protected volatile boolean symetrie;
    protected volatile boolean enMarcheAvant;
	protected volatile boolean matchDemarre = false;
	protected Speed vitesse;
	protected int pointsObtenus = 0;
    protected volatile long dateDebutMatch;
    
	// Dépendances
	protected Log log;
	
	public Robot(Log log)
	{
		this.log = log;
		vitesse = Speed.BETWEEN_SCRIPTS;
	}

	public RobotChrono cloneIntoRobotChrono()
	{
		RobotChrono cloned_robotchrono = new RobotChrono(log);
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
    	// pas besoin de copier symétrie car elle ne change pas en cours de match
    	Vec2.copy(position.getReadOnly(), rc.position);
    	rc.orientation = orientation;
    	rc.enMarcheAvant = enMarcheAvant;
    	rc.courbure = courbure;
    	rc.vitesse = vitesse;
    	rc.pointsObtenus = pointsObtenus;
    	rc.date = getTempsDepuisDebutMatch();
    	rc.positionGridSpace = getPositionGridSpace();
    }
	
	/**
	 * Copie this dans rc. this reste inchangé.
	 * Copie seulement ce qui importe pour le ThetaStar
	 * En particulier, la position sous forme de Vec2 n'est pas copiée (elle y est
	 * sous forme de gridpoint). Il n'y a pas non plus de copie du nombre de points obtenus.
	 * 
	 * @param rc
	 */
    public final void copyThetaStar(RobotChrono rc)
    {
    	rc.orientation = orientation;
    	rc.enMarcheAvant = enMarcheAvant;
    	rc.courbure = courbure;
    	rc.vitesse = vitesse;
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

/*	public Speed getVitesse() {
		return vitesse;
	}*/

	public double getCourbure()
	{
		return courbure;
	}
	
	/**
	 * Permet de savoir si on peut prendre des scripts de hook
	 * @return
	 */
	public boolean isEnMarcheAvant()
	{
		return enMarcheAvant;
	}
	
    public Vec2<ReadOnly> getPosition()
    {
        return position.getReadOnly();
    }

    public double getOrientation()
    {
        return orientation;
    }
    
	public boolean isArrete() {
		// TODO Auto-generated method stub
		return false;
	}



	/**
	 * Tourne par rapport à l'angle actuel.
	 * @param angle
	 * @throws UnableToMoveException
	 * @throws FinMatchException
	 */
	public void tournerRelatif(double angle) throws UnableToMoveException, FinMatchException
	{
		tourner(getOrientation() + angle);
	}

    /**
     * Utilisé lorsque le robot n'a pas de symétrie gauche/droite
     * @param angle
     * @throws UnableToMoveException
     */
    public void tournerSansSymetrie(double angle) throws UnableToMoveException, FinMatchException
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
    public void avancerDansMur(int distance) throws UnableToMoveException, FinMatchException
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
     * Utilise le sleep du robot, donc réel ou simulé.
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
