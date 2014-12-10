package robot;

import java.util.ArrayList;

import hook.Hook;
import hook.types.HookFactory;
import smartMath.Vec2;
import utils.Log;
import utils.Config;
import enums.PathfindingNodes;
import enums.Speed;
import exceptions.FinMatchException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf et marsu !
 */

public class RobotChrono extends Robot
{
	private HookFactory hookfactory;
	
	protected Vec2 position = new Vec2();
	protected double orientation;
	
	// Durée en millisecondes
	protected long date;
	
	public RobotChrono(Config config, Log log, HookFactory hookfactory)
	{
		super(config, log, hookfactory);
		this.hookfactory = hookfactory;
	}
	
	@Override
	public void setPosition(Vec2 position) {
		this.position = position;
	}
	
	@Override
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}

	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws FinMatchException
	{
		date += Math.abs(distance)*vitesse.invertedTranslationnalSpeed;
		Vec2 ecart;
        ecart = new Vec2((int)(distance*Math.cos(orientation)), (int)(distance*Math.sin(orientation)));

		checkHooks(position, position.plusNewVector(ecart), hooks);
		position.plus(ecart);
	}
	
	@Override
	public void set_vitesse(Speed vitesse)
	{
	    this.vitesse = vitesse;
	}
	
	@Override
	public long getTempsDepuisDebutMatch()
	{
		return date;
	}

	public RobotChrono cloneRobot() throws FinMatchException
	{
		RobotChrono cloned_robotchrono = new RobotChrono(config, log, hookfactory);
		copy(cloned_robotchrono);
		return cloned_robotchrono;
	}

	@Override
    public void tourner(double angle, boolean mur)
	{
        if(symetrie)
            angle = Math.PI-angle;
	    
		double delta = angle-orientation;
		if(delta < 0)
			delta *= -1;
		while(delta > 2*Math.PI)
			delta -= 2*Math.PI;
		if(delta > Math.PI)
			delta = 2*(float)Math.PI - delta;
		orientation = angle;
		date += delta*vitesse.invertedRotationnalSpeed;
	}

	@Override
    public void suit_chemin(ArrayList<PathfindingNodes> chemin, ArrayList<Hook> hooks) throws FinMatchException
	{
		for(PathfindingNodes point: chemin)
			va_au_point(point.getCoordonnees(), hooks);
	}
	
	public void va_au_point(Vec2 point, ArrayList<Hook> hooks) throws FinMatchException
	{
		checkHooks(position, point, hooks);
		if(symetrie)
			point.x *= -1;
		date += position.distance(point)*vitesse.invertedTranslationnalSpeed;
		position = point.clone();
	}

	/**
	 * Utilisé par les tests
	 * @param other
	 * @return
	 */
	// TODO à compléter au fur et à mesure
	public boolean equals(RobotChrono other)
	{
		return 	position.equals(other.position)
				&& orientation == other.orientation;
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
    }

    public void desactiver_asservissement_rotation()
    {}

    public void activer_asservissement_rotation()
    {}

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
					hook.trigger();
	}
	
	
}
