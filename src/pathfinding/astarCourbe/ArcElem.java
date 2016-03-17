package pathfinding.astarCourbe;

import pathfinding.VitesseCourbure;
import permissions.ReadWrite;
import utils.Vec2;
 
/**
 * Un tout petit bout de courbe. Trop petit pour être manipulé directement par le pathfinding.
 * On les regroupe donc dans un ArcCourbe
 * @author pf
 *
 */

public class ArcElem {

	public Vec2<ReadWrite> pointDepart; // la position au départ de l'arc
	public double thetaDepart; // l'angle au départ de l'arc

	public double courbureDepart; // la courbure au départ de l'arc
	
	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcelem
	 */
	public void copy(ArcElem arcelem)
	{
		Vec2.copy(pointDepart.getReadOnly(), arcelem.pointDepart);
		arcelem.courbureDepart = courbureDepart;
		arcelem.thetaDepart = thetaDepart;
	}

}
