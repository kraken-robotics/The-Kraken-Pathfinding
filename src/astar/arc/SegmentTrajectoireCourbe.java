package astar.arc;

import utils.Vec2;

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
	public final int differenceDistance;
	public final int distanceAnticipation;
	
	/**
	 * Le point où on commence à tourner
	 */
	public final Vec2 pointDepart;
	public final Vec2 directionHook;
	
	/**
	 * Constructeur pour trajectoire courbe
	 * @param n
	 * @param differenceDistance
	 * @param distanceAnticipation
	 * @param pointDepart
	 */
	public SegmentTrajectoireCourbe(PathfindingNodes objectifFinal, int differenceDistance, int distanceAnticipation, Vec2 pointDepart, Vec2 directionHook)
	{
		this.objectifFinal = objectifFinal;
		this.differenceDistance = differenceDistance;
		this.distanceAnticipation = distanceAnticipation;
		this.pointDepart = pointDepart;
		this.directionHook = directionHook;
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
	}
	
}
