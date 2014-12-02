package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import utils.Log;
import utils.Config;
import enums.Speed;
import exceptions.Locomotion.UnableToMoveException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf et marsu !
 */

public class RobotChrono extends Robot
{

	protected Vec2 position = new Vec2();
	protected double orientation;
	
	// Durée en millisecondes
	private int duree = 0;
	
	public RobotChrono(Config config, Log log)
	{
		super(config, log);
	}
	
	@Override
	public void setPosition(Vec2 position) {
		this.position = position;
	}
	
	@Override
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}

	// La plupart de ces méthodes resteront vides

	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
            throws UnableToMoveException
	{
		duree += Math.abs(distance)*vitesse.invertedTranslationnalSpeed;
		Vec2 ecart;
        ecart = new Vec2((int)(distance*Math.cos(orientation)), (int)(distance*Math.sin(orientation)));

		position.Plus(ecart);
	}
	
	@Override
	public void set_vitesse(Speed vitesse)
	{
	    this.vitesse = vitesse;
	}

	// Méthodes propres à RobotChrono
	public void initChrono()
	{
			duree = 0;
	}
	
	public int get_compteur()
	{
		return duree;
	}

	@Override
    public void tourner(double angle, ArrayList<Hook> hooks, boolean mur)
            throws UnableToMoveException
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
/*		if(delta != 0) 
		{
			try {
				dureePositive((long)(delta/vitesse_rpms));
			} catch (RobotChronoException e) {
				e.printStackTrace();
			}
		}*/
		duree += delta*vitesse.invertedRotationnalSpeed;
	}

	@Override
    public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks)
            throws UnableToMoveException
	{
		for(Vec2 point: chemin)
			va_au_point(point);
	}
	
	public void va_au_point(Vec2 point)
	{
		if(symetrie)
			point.x *= -1;
/*		try {
			dureePositive((long)(position.distance(point)/vitesse_mmpms));
		} catch (RobotChronoException e) {
			e.printStackTrace();
		}*/
		duree += position.distance(point)*vitesse.invertedTranslationnalSpeed;
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
	public void sleep(long duree) 
	{
		this.duree += duree;
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

    public void desactiver_asservissement_rotation()
    {}

    public void activer_asservissement_rotation()
    {}

	@Override
	public Vec2 getPositionFast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getOrientationFast() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInsiste(boolean insiste) {
		// TODO Auto-generated method stub
		
	}

}
