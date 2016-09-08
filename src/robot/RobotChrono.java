package robot;

import pathfinding.astarCourbe.arcs.ArcCourbe;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import utils.Log;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 */

public class RobotChrono extends Robot
{
	protected PointGridSpace positionGridSpace;
   
	// Date en millisecondes depuis le début du match.
	protected long date;

	/**
	 * Constructeur clone
	 * @param log
	 * @param robot
	 */
	public RobotChrono(Log log, RobotReal robot)
	{
		super(log);
		robot.copy(this);
	}

	@Override
	public long getTempsDepuisDebutMatch()
	{
		return date;
	}

	/**
	 * Donne l'angle (non orienté, en valeur absolue) entre
	 * l'orientation actuelle et l'angle donné en argument
	 * @param angle
	 * @return
	 */
	public double calculateDelta(double angle)
	{
		double delta = cinematique.orientation-angle % (2*Math.PI);
		delta = Math.abs(delta);
		if(delta > Math.PI)
			delta = 2*Math.PI - delta;
		return delta;
	}

	public void setPositionGridSpace(PointGridSpace gridpoint)
	{
		positionGridSpace = gridpoint;
	}
	
	@Override
	public PointGridSpace getPositionGridSpace()
	{
		return positionGridSpace;
	}
	
	/** Inverse le sens actuel de la marche.
	 * Utilisé lors d'un point de rebroussement
	 */
	public void inverseSensMarche()
	{
		cinematique.enMarcheAvant = !cinematique.enMarcheAvant;
	}

	public void suitArcCourbe(ArcCourbe came_from_arc)
	{
		came_from_arc.getLast().copy(cinematique);
	}

	
	public Cinematique getCinematique()
	{
		return cinematique;
	}
	
}
