package pathfinding.astar_courbe;

import permissions.ReadWrite;
import utils.Vec2;

/**
 * Un arc de trajectoire courbe. Une clothoïde
 * @author pf
 *
 */

public class ArcCourbe {

	public Vec2<ReadWrite> destination;
	public int vitesseCourbure;
	
	public void copy(ArcCourbe arcCourbe)
	{
		arcCourbe.vitesseCourbure = vitesseCourbure;
	}


}
