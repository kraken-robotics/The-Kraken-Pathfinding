package astar.arc;

import robot.Speed;
import vec2.Vec2;

/**
 * Un arc du pathfinding.
 * Contient un noeud et différentes informations sur le mouvement
 * @author pf
 *
 */

public class SegmentTrajectoireCourbe implements Arc
{
	/**
	 * La fin du segment
	 */
	public PathfindingNodes objectifFinal;

	/**
	 * Différence de distance entre la trajectoire en ligne brisée et la trajectoire courbe
	 * Cette distance est positive car la trajectoire courbe réduit la distance parcourue
 	 */
	public int differenceDistance;
	public int distanceAnticipation;
	
	/**
	 * Le point où on commence à tourner
	 */
	public final Vec2 pointDepart;
	public final Vec2 directionHook;
	
	public final Speed vitesse;
	
	/**
	 * Constructeur pour trajectoire courbe
	 * @param n
	 * @param differenceDistance
	 * @param distanceAnticipation
	 * @param pointDepart
	 */
	public SegmentTrajectoireCourbe(PathfindingNodes objectifFinal, int differenceDistance, int distanceAnticipation, Vec2 pointDepart, Vec2 directionHook, Speed vitesse)
	{
		this.objectifFinal = objectifFinal;
		this.differenceDistance = differenceDistance;
		this.distanceAnticipation = distanceAnticipation;
		this.pointDepart = pointDepart;
		this.directionHook = directionHook;
		this.vitesse = vitesse;
	}
	
	/**
	 * Constructeur sans trajectoire courbe
	 * @param n
	 */
	public SegmentTrajectoireCourbe(PathfindingNodes objectifFinal)
	{
		this.objectifFinal = objectifFinal;
		this.differenceDistance = 0;
		this.distanceAnticipation = 0;
		this.pointDepart = null;
		this.directionHook = null;
		this.vitesse = Speed.BETWEEN_SCRIPTS;
	}
	
	/**
	 * Affichage user-friendly
	 */
	public String toString()
	{
		if(differenceDistance == 0)
			return objectifFinal.toString()+" sans trajectoire courbe";
		else
			return objectifFinal.toString()+" avec trajectoire courbe";
	}
	
}
