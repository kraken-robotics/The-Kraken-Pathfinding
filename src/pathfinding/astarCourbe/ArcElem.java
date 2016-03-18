package pathfinding.astarCourbe;

import permissions.ReadWrite;
import utils.Vec2;
 
/**
 * Un tout petit bout de courbe. Trop petit pour être manipulé directement par le pathfinding.
 * On les regroupe donc dans un ArcCourbe
 * @author pf
 *
 */

public class ArcElem {

	public Vec2<ReadWrite> point = new Vec2<ReadWrite>(); // la position
	public double theta; // l'angle
	public double courbure; // la courbure
	
	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcelem
	 */
	public void copy(ArcElem arcelem)
	{
		Vec2.copy(point.getReadOnly(), arcelem.point);
		arcelem.courbure = courbure;
		arcelem.theta = theta;
	}

}
