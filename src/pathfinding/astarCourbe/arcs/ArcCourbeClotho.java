package pathfinding.astarCourbe.arcs;

import robot.Cinematique;

/**
 * Arc de clothoïde, utilisé dans l'AStarCourbe
 * @author pf
 *
 */

public class ArcCourbeClotho extends ArcCourbe
{
	public Cinematique[] arcselems = new Cinematique[ClothoidesComputer.NB_POINTS];
	
	public ArcCourbeClotho()
	{
		for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			arcselems[i] = new Cinematique(0, 0, 0, true, 0, 0, 0);
	}
	
	/**
	 * Une copie afin d'éviter la création d'objet
	 * @param arcCourbe
	 */
	public void copy(ArcCourbeClotho arcCourbe)
	{
		for(int i = 0; i < arcselems.length; i++)
			arcselems[i].copy(arcCourbe.arcselems[i]);
	}
	
	@Override
	public int getNbPoints()
	{
		return ClothoidesComputer.NB_POINTS;
	}
	
	@Override
	public Cinematique getPoint(int indice)
	{
		return arcselems[indice];
	}
	
	@Override
	public Cinematique getLast()
	{
		return arcselems[ClothoidesComputer.NB_POINTS - 1];
	}
	
	@Override
	public double getDuree()
	{
		return ClothoidesComputer.DISTANCE_ARC_COURBE / arcselems[0].vitesseTranslation;
	}

}
