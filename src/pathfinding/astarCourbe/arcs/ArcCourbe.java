package pathfinding.astarCourbe.arcs;

import obstacles.types.ObstacleArcCourbe;
import robot.Cinematique;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * @author pf
 *
 */

public abstract class ArcCourbe {

	public boolean rebrousse; // cet arc commence par un rebroussement, c'est-à-dire que la marche avant change
	public boolean stop; // cet arc commence par un arrêt du robot
	public int indexTrajectory;
	public ObstacleArcCourbe obstacle;
	
	public ArcCourbe(boolean rebrousse, boolean stop)
	{
		this.rebrousse = rebrousse;
		this.stop = stop;
	}
	
	public abstract int getNbPoints();
	public abstract Cinematique getPoint(int indice);
	public abstract Cinematique getLast();
	public abstract double getDuree();
	public abstract double getVitesseTr();

}
