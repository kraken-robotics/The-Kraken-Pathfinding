package pathfinding.astarCourbe;

import pathfinding.VitesseCourbure;
import permissions.ReadWrite;
import utils.Vec2;

/**
 * Un arc de trajectoire courbe. Une clothoïde dont le paramètre "a" est vitesseCourbure
 * La destination est fourni au bas niveau afin de corriger un éventuel décalage avec la courbe
 * @author pf
 *
 */

public class ArcCourbe {

	public Vec2<ReadWrite> destination;
	public VitesseCourbure vitesseCourbure;
	
	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcCourbe
	 */
	public void copy(ArcCourbe arcCourbe)
	{
		arcCourbe.vitesseCourbure = vitesseCourbure;
	}


}
