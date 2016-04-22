package pathfinding.astarCourbe.arcs;

import java.util.ArrayList;

import robot.Cinematique;

/**
 * Arc de trajectoire courbe issu d'une interpolation cubique
 * @author pf
 *
 */

public class ArcCourbeCubique extends ArcCourbe
{
	public ArrayList<Cinematique> arcs;
	public double longueur;
	
	public ArcCourbeCubique(ArrayList<Cinematique> arcs, double longueur)
	{
		this.arcs = arcs;
		this.longueur = longueur;
	}
	
	@Override
	public int getNbPoints()
	{
		return arcs.size();
	}
	
	@Override
	public Cinematique getPoint(int indice)
	{
		return arcs.get(indice);
	}
	
	@Override
	public Cinematique getLast()
	{
		return arcs.get(arcs.size()-1);
	}
}
