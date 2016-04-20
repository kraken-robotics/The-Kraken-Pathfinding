package pathfinding.astarCourbe;

import pathfinding.VitesseCourbure;
import robot.Cinematique;

/**
 * Un arc de trajectoire courbe.
 * @author pf
 *
 */

public class ArcCourbe {

	public Cinematique[] arcselems = new Cinematique[ClothoidesComputer.NB_POINTS];
	public double vitesse; // la vitesse à laquelle on souhaite parcourir l'arc.
	// si cette vitesse est nulle, ça veut dire qu'on souhaite s'arrêter à la fin de l'arc
	public VitesseCourbure vitesseCourbure; // la dérivée de la courbure
	public boolean marcheAvant;
	
	public ArcCourbe()
	{
		for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			arcselems[i] = new Cinematique();
	}
	
	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcCourbe
	 */
	public void copy(ArcCourbe arcCourbe)
	{
		arcCourbe.marcheAvant = marcheAvant;
		arcCourbe.vitesse = vitesse;
		arcCourbe.vitesseCourbure = vitesseCourbure;
		for(int i = 0; i < arcselems.length; i++)
			arcselems[i].copy(arcCourbe.arcselems[i]);
	}

}
