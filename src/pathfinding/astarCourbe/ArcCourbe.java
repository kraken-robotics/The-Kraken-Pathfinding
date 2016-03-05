package pathfinding.astarCourbe;

import pathfinding.VitesseCourbure;
import permissions.ReadWrite;
import utils.Vec2;

/**
 * Un arc de trajectoire courbe.
 * @author pf
 *
 */

public class ArcCourbe {

	public Vec2<ReadWrite> pointDepart; // la position au départ de l'arc
	public double thetaDepart; // l'angle au départ de l'arc

	public double courbureDepart; // la courbure au départ de l'arc
	public double vitesse; // la vitesse à laquelle on souhaite parcourir l'arc
	public VitesseCourbure vitesseCourbure; // la dérivée de la courbure
	
	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcCourbe
	 */
	public void copy(ArcCourbe arcCourbe)
	{
		Vec2.copy(pointDepart.getReadOnly(), arcCourbe.pointDepart);
		arcCourbe.vitesse = vitesse;
		arcCourbe.courbureDepart = courbureDepart;
		arcCourbe.thetaDepart = thetaDepart;
		arcCourbe.vitesseCourbure = vitesseCourbure;
	}

}
