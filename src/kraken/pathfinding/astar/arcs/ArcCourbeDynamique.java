/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.astar.arcs;

import java.util.List;
import kraken.pathfinding.astar.arcs.vitesses.VitesseCourbure;
import kraken.robot.CinematiqueObs;

/**
 * Arc courbe de longueur inconnue à l'avance
 * 
 * @author pf
 *
 */

public class ArcCourbeDynamique extends ArcCourbe
{
	public List<CinematiqueObs> arcs;
	public double longueur;

	public ArcCourbeDynamique(List<CinematiqueObs> arcs, double longueur, VitesseCourbure v)
	{
		vitesse = v;
		this.arcs = arcs;
		this.longueur = longueur;
	}

	@Override
	public int getNbPoints()
	{
		return arcs.size();
	}

	@Override
	public CinematiqueObs getPoint(int indice)
	{
		return arcs.get(indice);
	}

	@Override
	public CinematiqueObs getLast()
	{
		return arcs.get(arcs.size() - 1);
	}

	@Override
	public double getLongueur()
	{
		return longueur;
	}

}
