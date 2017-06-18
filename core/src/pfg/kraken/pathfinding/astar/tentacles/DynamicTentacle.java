/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.pathfinding.astar.tentacles;

import java.util.List;

import pfg.kraken.pathfinding.astar.tentacles.types.TentacleType;
import pfg.kraken.robot.CinematiqueObs;

/**
 * Arc courbe de longueur inconnue à l'avance
 * 
 * @author pf
 *
 */

public class DynamicTentacle extends Tentacle
{
	private static final long serialVersionUID = 2188028584717495182L;
	public List<CinematiqueObs> arcs;
	public double longueur;

	public DynamicTentacle(List<CinematiqueObs> arcs, double longueur, TentacleType v)
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
