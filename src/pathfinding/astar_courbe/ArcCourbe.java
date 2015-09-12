package pathfinding.astar_courbe;

import java.util.Collection;

import pathfinding.dstarlite.GridSpace;

/**
 * Un arc de trajectoire courbe. Une clothoïde
 * @author pf
 *
 */

public class ArcCourbe {

	public int vitesseCourbure;
	
	public void copy(ArcCourbe arcCourbe)
	{
		arcCourbe.vitesseCourbure = vitesseCourbure;
	}

	public Collection<? extends String> toSerialFirst(GridSpace gridspace) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<? extends String> toSerial(GridSpace gridspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
