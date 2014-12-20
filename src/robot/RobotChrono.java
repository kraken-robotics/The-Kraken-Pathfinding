package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import utils.Log;
import utils.Config;
import enums.HauteurBrasClap;
import enums.PathfindingNodes;
import enums.Side;
import enums.Speed;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf et marsu !
 */

public class RobotChrono extends Robot
{
	protected Vec2 position = new Vec2();
	protected PathfindingNodes positionPathfinding;
	protected boolean isPositionPathfindingActive = false;
	protected double orientation;
	
	// Date en millisecondes depuis le début du match.
	protected int date;
	
	/** valeur approchée du temps (en milisecondes) nécéssaire pour qu'une information que l'on envois a la série soit aquité */
	private final static int approximateSerialLatency = 50;

	public RobotChrono(Config config, Log log)
	{
		super(config, log);
	}
	
	@Override
	public void setPosition(Vec2 position) {
		position.copy(this.position);
		isPositionPathfindingActive = false;
		this.date += approximateSerialLatency;
	}
	
	@Override
	public void setOrientation(double orientation) {
		this.orientation = orientation;
		this.date += approximateSerialLatency;
	}

	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws FinMatchException
	{
		date += Math.abs(distance)*vitesse.invertedTranslationnalSpeed;
		Vec2 ecart;
        ecart = new Vec2((int)(distance*Math.cos(orientation)), (int)(distance*Math.sin(orientation)));

		checkHooks(position, position.plusNewVector(ecart), hooks);
		position.plus(ecart);
		isPositionPathfindingActive = false;
		this.date += approximateSerialLatency;
		this.date += Speed.translationStopDuration;
	}
	
	@Override
	public void set_vitesse(Speed vitesse)
	{
	    this.vitesse = vitesse;
		this.date += approximateSerialLatency;
	}
	
	@Override
	public int getTempsDepuisDebutMatch()
	{
		return date;
	}

	@Override
	public RobotChrono cloneIntoRobotChrono() throws FinMatchException
	{
		RobotChrono cloned_robotchrono = new RobotChrono(config, log);
		copy(cloned_robotchrono);
		return cloned_robotchrono;
	}

	@Override
    public void tourner(double angle)
    {
        tourner(angle, false);
    }
	
	/**
	 * Donne l'angle entre l'orientation actuelle et l'angle donné en argument
	 * @param angle
	 * @return
	 */
	public double calculateDelta(double angle)
	{
		double delta = orientation-angle;
		if(delta < 0)
			delta *= -1;
		while(delta > 2*Math.PI)
			delta -= 2*Math.PI;
		if(delta > Math.PI)
			delta = 2*(float)Math.PI - delta;
		return delta;
	}
	
	@Override
    public void tourner(double angle, boolean mur)
	{
		// TODO: avec les trajectoires courbes, les durées changent
		// et la marche arrière automatique?
		double delta = calculateDelta(angle);
		orientation = angle;
		date += delta*vitesse.invertedRotationnalSpeed;
		isPositionPathfindingActive = false;
		date += approximateSerialLatency;
		date += Speed.rotationStopDuration;
	}

	@Override
    public void suit_chemin(ArrayList<PathfindingNodes> chemin, ArrayList<Hook> hooks) throws FinMatchException
	{
		for(PathfindingNodes point: chemin)
			va_au_point(point.getCoordonnees(), hooks);
	}
	
	public void va_au_point(Vec2 point, ArrayList<Hook> hooks) throws FinMatchException
	{
		double orientation_finale = Math.atan2(point.y - position.y, point.x - position.x);
		tourner(orientation_finale);
		checkHooks(position, point, hooks);
		date += position.distance(point)*vitesse.invertedTranslationnalSpeed;
		point.copy(position);
		isPositionPathfindingActive = false;
		date += approximateSerialLatency;
		date += Speed.translationStopDuration;
	}

	public void va_au_point_pathfinding(PathfindingNodes n, ArrayList<Hook> hooks) throws FinMatchException
	{
		if(!isPositionPathfindingActive)
			va_au_point(n.getCoordonnees(), hooks);
		else
		{
			tourner(positionPathfinding.getOrientationFinale(n));
			checkHooks(position, n.getCoordonnees(), hooks);
			date += positionPathfinding.distanceTo(n)*vitesse.invertedTranslationnalSpeed;
			date += approximateSerialLatency;
			date += Speed.translationStopDuration;			
		}
		setPositionPathfinding(n);
	}

	@Override
	public void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException 
	{
		this.date += duree;
		checkHooks(position, position, hooks);
	}
	
	@Override
    public void stopper()
    {
		this.date += approximateSerialLatency;
		this.date += Speed.translationStopDuration;
    }

    @Override
    public Vec2 getPosition()
    {
        return position.clone();
    }

    @Override
    public double getOrientation()
    {
        return orientation;
    }
    
    @Override
    public void copy(RobotChrono rc) throws FinMatchException
    {
        super.copy(rc);
        position.copy(rc.position);
        rc.orientation = orientation;
    	rc.positionPathfinding = positionPathfinding;
    	rc.isPositionPathfindingActive = isPositionPathfindingActive;
    }

    public void desactiver_asservissement_rotation()
    {
		this.date += approximateSerialLatency;
    }

    public void activer_asservissement_rotation()
    {
    	this.date += approximateSerialLatency;
    }

	@Override
	public void setInsiste(boolean insiste) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * On déclenche tous les hooks entre le point A et le point B.
	 * @param pointA
	 * @param pointB
	 * @param hooks
	 * @throws FinMatchException 
	 */
	private void checkHooks(Vec2 pointA, Vec2 pointB, ArrayList<Hook> hooks) throws FinMatchException
	{
		if(hooks != null)
			for(Hook hook: hooks)
				if(hook.simulated_evaluate(pointA, pointB, date))
					try {
						hook.trigger();
					} catch (ScriptHookException e) {
						// Impossible qu'en simulation on ait des script de hooks
						e.printStackTrace();
					}
	}

	public void setPositionPathfinding(PathfindingNodes n)
	{
		n.getCoordonnees().copy(position);
		positionPathfinding = n;
		isPositionPathfindingActive = true;
	}
	
	public PathfindingNodes getPositionPathfinding()
	{
		if(isPositionPathfindingActive)
			return positionPathfinding;
		return null;
	}

	/**
	 * UTILISE UNIQUEMENT PAR LES TESTS
	 */
	public void reinitDate()
	{
		date = 0;
	}

	@Override
	public void bougeBrasClap(Side cote, HauteurBrasClap hauteur,
			boolean needToSleep) throws FinMatchException
	{
		if(needToSleep)
			bougeBrasClapSleep(bougeBrasClapOrder(cote, hauteur));
	}

	@Override
	public void poserDeuxTapis(boolean needToSleep) throws FinMatchException
	{
    	tapisPoses = true;
    	// TODO points
		pointsObtenus = pointsObtenus + 24;
		if(needToSleep)
			poserDeuxTapisSleep();
	}

	@Override
	public void leverDeuxTapis(boolean needToSleep) throws FinMatchException
	{
		if(needToSleep)
			leverDeuxTapisSleep();
	}

	// Permet de commander le lissage en vérifiant si on part d'un point qui n'est pas un node
	public boolean isAtPathfindingNodes()
	{
		return isPositionPathfindingActive;
	}

	@Override
	public void clapTombe()
	{
		// TODO points
		pointsObtenus = pointsObtenus + 5;				
	}
	
}
