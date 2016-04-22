package pathfinding.astarCourbe.arcs;

import pathfinding.VitesseCourbure;
import robot.Cinematique;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * @author pf
 *
 */

public abstract class ArcCourbe {

	public boolean rebrousse; // cet arc commence par un rebroussement, c'est-à-dire que la marche avant change
	public boolean stop; // cet arc commence par un arrêt du robot
	public VitesseCourbure v; // TODO virer
	
	public abstract int getNbPoints();
	public abstract Cinematique getPoint(int indice);
	public abstract Cinematique getLast();
	public abstract double getDuree();
}
