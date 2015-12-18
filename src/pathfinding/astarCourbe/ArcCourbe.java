package pathfinding.astarCourbe;

import pathfinding.VitesseCourbure;
import permissions.ReadWrite;
import utils.Vec2;

/**
 * Un arc de trajectoire courbe. Le bas niveau a besoin de connaître le point de départ de l'arc,
 * l'orientation et la courbure à ce point. Les autres informations sont interpolées.
 * La vitesse de courbure n'est pas envoyée à la STM mais est utilisée par le pathfinding.
 * @author pf
 *
 */

public class ArcCourbe {

	public Vec2<ReadWrite> pointDepart;
	public double courbure;
	public double theta;
	public VitesseCourbure vitesseCourbure; // ne sera pas envoyée à la STM
	
	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcCourbe
	 */
	public void copy(ArcCourbe arcCourbe)
	{
		Vec2.copy(pointDepart.getReadOnly(), arcCourbe.pointDepart);
		arcCourbe.courbure = courbure;
		arcCourbe.theta = theta;
		arcCourbe.vitesseCourbure = vitesseCourbure;
	}

}
