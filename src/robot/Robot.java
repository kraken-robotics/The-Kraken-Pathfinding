package robot;

import java.util.ArrayList;

import hook.Hook;
import hook.types.HookFactory;
import smartMath.Vec2;
import strategie.GameState;
import container.Service;
import enums.ActuatorOrder;
import enums.HauteurBrasClap;
import enums.PathfindingNodes;
import enums.RobotColor;
import enums.Side;
import enums.Speed;
import exceptions.FinMatchException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
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
    public abstract void tourner(double angle, boolean mur)
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
    public abstract void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException;
    public abstract void setInsiste(boolean insiste);
    public abstract void desactiver_asservissement_rotation() throws FinMatchException;
    public abstract void activer_asservissement_rotation() throws FinMatchException;
    public abstract long getTempsDepuisDebutMatch();
    public abstract RobotChrono cloneIntoRobotChrono() throws FinMatchException;

    /*
     * Actionneurs
     */
    
    
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
    	rc.date = getTempsDepuisDebutMatch();
    }

	// Dépendances
	protected Config config;
	protected Log log;
	protected HookFactory hookfactory;
	private boolean symetrie; // normalement, RobotReal et RobotChrono n'ont pas à connaître notre couleur.
	protected Speed vitesse;
	private int pointsObtenus = 0;
	
	private boolean tapisPoses = false;
	protected ArrayList<Hook> hooksTable = new ArrayList<Hook>();
	
	public Robot(Config config, Log log, HookFactory hookfactory)
	{
		this.config = config;
		this.log = log;
		this.hookfactory = hookfactory;
		vitesse = Speed.BETWEEN_SCRIPTS;		
		updateConfig();
	}

	public void initHooksTable(GameState<?> state) throws Exception
	{
		if(state.robot == this)
			hooksTable = hookfactory.getHooksEntreScripts(state);
		else
			throw new Exception();
	}

	
	public void updateConfig()
	{
		symetrie = (RobotColor.parse(config.get("couleur")) == RobotColor.YELLOW);
	}
	
	public Speed get_vitesse_() {
		return vitesse;
	}

	public void tourner_relatif(double angle) throws UnableToMoveException, FinMatchException
	{
		tourner(getOrientation() + angle, false);
	}

    public void tourner(double angle) throws UnableToMoveException, FinMatchException
    {
        tourner(angle, false);
    }

    /**
     * Utilisé lorsque le robot n'a pas de symétrie gauche/droite
     * @param angle
     * @throws UnableToMoveException
     */
    public void tourner_sans_symetrie(double angle) throws UnableToMoveException, FinMatchException
    {
        if(symetrie)
            tourner(Math.PI-angle, false);
        else
            tourner(angle, false);
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

    public void sleep(long duree) throws FinMatchException
    {
    	sleep(duree, null);
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
    
	public ActuatorOrder bougeBrasClapOrder(Side cote, HauteurBrasClap hauteur) throws SerialConnexionException, FinMatchException
	{
		if(cote == Side.LEFT && hauteur == HauteurBrasClap.TOUT_EN_HAUT)
			return ActuatorOrder.LEVE_CLAP_GAUCHE;
		else if(cote == Side.LEFT && hauteur == HauteurBrasClap.FRAPPE_CLAP)
			return ActuatorOrder.POSITION_TAPE_CLAP_GAUCHE;
		else if(cote == Side.LEFT && hauteur == HauteurBrasClap.RENTRE)
			return ActuatorOrder.BAISSE_CLAP_GAUCHE;
		else if(cote == Side.RIGHT && hauteur == HauteurBrasClap.TOUT_EN_HAUT)
			return ActuatorOrder.LEVE_CLAP_DROIT;
		else if(cote == Side.RIGHT && hauteur == HauteurBrasClap.FRAPPE_CLAP)
			return ActuatorOrder.POSITION_TAPE_CLAP_DROIT;
		else if(cote == Side.RIGHT && hauteur == HauteurBrasClap.RENTRE)
			return ActuatorOrder.BAISSE_CLAP_DROIT;
		return null;
	}

	public void bougeBrasClap(Side cote, HauteurBrasClap hauteur) throws SerialConnexionException, FinMatchException
	{
		ActuatorOrder order = bougeBrasClapOrder(cote, hauteur);
		sleep(order.getSleepValue());
	}

	public void poserDeuxTapis() throws FinMatchException {
		sleep(ActuatorOrder.BAISSE_TAPIS_DROIT.getSleepValue());
		sleep(ActuatorOrder.BAISSE_TAPIS_GAUCHE.getSleepValue());
	}

	public void leverDeuxTapis() throws FinMatchException {
		sleep(ActuatorOrder.LEVE_TAPIS_DROIT.getSleepValue());
		sleep(ActuatorOrder.LEVE_TAPIS_GAUCHE.getSleepValue());
	}

	
}
