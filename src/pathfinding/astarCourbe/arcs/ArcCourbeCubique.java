package pathfinding.astarCourbe.arcs;

import java.util.ArrayList;

import obstacles.types.ObstacleArcCourbe;
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
	
	public ArcCourbeCubique(ObstacleArcCourbe obstacle, ArrayList<Cinematique> arcs, double longueur, boolean rebrousse, boolean stop)
	{
		super(rebrousse, stop);
		this.arcs = arcs;
		this.longueur = longueur;
		this.obstacle = obstacle;
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

	@Override
	public double getDuree()
	{
		return longueur / getVitesseTr();
	}
	
	@Override
	public double getVitesseTr()
	{
		double v = 0;
		for(Cinematique c : arcs)
			v += c.vitesseTranslation;
		return v / arcs.size();
	}

}
