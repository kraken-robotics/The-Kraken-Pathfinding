package pathfinding.astarCourbe;

import pathfinding.VitesseCourbure;

/**
 * Un arc de trajectoire courbe.
 * @author pf
 *
 */

public class ArcCourbe {

	ArcElem[] arcselems;
	public double vitesse; // la vitesse à laquelle on souhaite parcourir l'arc
	public VitesseCourbure vitesseCourbure; // la dérivée de la courbure

	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcCourbe
	 */
	public void copy(ArcCourbe arcCourbe)
	{
		arcCourbe.vitesse = vitesse;
		arcCourbe.vitesseCourbure = vitesseCourbure;
		for(int i = 0; i < arcselems.length; i++)
			arcselems[i].copy(arcCourbe.arcselems[i]);
	}

}
