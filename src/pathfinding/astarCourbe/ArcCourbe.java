package pathfinding.astarCourbe;

import robot.Cinematique;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * @author pf
 *
 */

public class ArcCourbe {

	public Cinematique[] arcselems = new Cinematique[ClothoidesComputer.NB_POINTS];
	public boolean rebrousse; // cet arc commence par un rebroussement
	
	public ArcCourbe()
	{
		for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			arcselems[i] = new Cinematique(0, 0, 0, true, 0, 0, 0);
	}
	
	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcCourbe
	 */
	public void copy(ArcCourbe arcCourbe)
	{
		for(int i = 0; i < arcselems.length; i++)
			arcselems[i].copy(arcCourbe.arcselems[i]);
	}

}
