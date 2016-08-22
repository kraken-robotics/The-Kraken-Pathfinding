package robot;

import pathfinding.astarCourbe.arcs.ArcCourbe;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadWrite;
import exceptions.FinMatchException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 */

public class RobotChrono extends Robot
{
	protected int positionGridSpace;
   
	// Date en millisecondes depuis le début du match.
	protected long date;

	RobotChrono(Log log, Cinematique cinematique)
	{
		super(log);
		this.cinematique = new Cinematique(cinematique);
	}

	/**
	 * Mise à jour permettant de modifier, pour RobotChrono, la date limite de la recherche stratégique
	 * @param dateLimite
	 */
/*	public static void setTempsMax(int tempsMax)
	{
		RobotChrono.tempsMax = tempsMax;
	}*/
	
	@Override
    public void avancer(int distance, boolean mur, Speed vitesse)
	{
		date += Math.abs(distance)*vitesse.translationalSpeed;
	
        Vec2<ReadWrite> ecart = new Vec2<ReadWrite>((int)(distance*Math.cos(cinematique.orientation)), (int)(distance*Math.sin(cinematique.orientation)));

		Vec2.plus(cinematique.getPositionEcriture(), ecart);
//		isPositionPathfindingActive = false;
//		positionPathfindingAnterieure = null;
//		positionPathfinding = null;
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
	
	@Override
	public void sleep(long duree) throws FinMatchException 
	{
		this.date += duree;
	}

	public void setPositionGridSpace(int gridpoint)
	{
		positionGridSpace = gridpoint;
	}
	
	@Override
	public int getPositionGridSpace()
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

	/**
	 * UTILISE UNIQUEMENT PAR LES TESTS
	 */
	public void reinitDate()
	{
		date = 0;
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
