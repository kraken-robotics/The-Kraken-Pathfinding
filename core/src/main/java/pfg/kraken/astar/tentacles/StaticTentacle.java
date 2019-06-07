/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import pfg.kraken.obstacles.RobotShape;
import pfg.kraken.struct.EmbodiedKinematic;

/**
 * Arc courbe de longueur fixe
 * 
 * @author pf
 *
 */

public final class StaticTentacle extends Tentacle
{
	private static final long serialVersionUID = -5599092863248049576L;
	public final EmbodiedKinematic[] arcselems = new EmbodiedKinematic[NB_POINTS];

	public StaticTentacle(RobotShape vehicleTemplate)
	{
		for(int i = 0; i < NB_POINTS; i++)
			arcselems[i] = new EmbodiedKinematic(vehicleTemplate);
	}

	/**
	 * Une copie afin d'éviter la création d'objet
	 * 
	 * @param arcCourbe
	 */
/*	public void copy(StaticTentacle arcCourbe)
	{
		for(int i = 0; i < arcselems.length; i++)
			arcselems[i].copy(arcCourbe.arcselems[i]);
	}*/

	@Override
	public int getNbPoints()
	{
		return NB_POINTS;
	}

	@Override
	public EmbodiedKinematic getPoint(int indice)
	{
		return arcselems[indice];
	}

	@Override
	public EmbodiedKinematic getLast()
	{
		return arcselems[NB_POINTS - 1];
	}

}
