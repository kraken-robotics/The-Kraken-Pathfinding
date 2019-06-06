/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.List;
import pfg.kraken.struct.EmbodiedKinematic;

/**
 * Arc courbe de longueur inconnue à l'avance
 * 
 * @author pf
 *
 */

public final class DynamicTentacle extends Tentacle
{
	private static final long serialVersionUID = 2188028584717495182L;
	public List<EmbodiedKinematic> arcs;

	public DynamicTentacle(List<EmbodiedKinematic> arcs, TentacleType v)
	{
		vitesse = v;
		this.arcs = arcs;
	}

	@Override
	public int getNbPoints()
	{
		return arcs.size();
	}

	@Override
	public EmbodiedKinematic getPoint(int indice)
	{
		return arcs.get(indice);
	}

	@Override
	public EmbodiedKinematic getLast()
	{
		return arcs.get(arcs.size() - 1);
	}
}
