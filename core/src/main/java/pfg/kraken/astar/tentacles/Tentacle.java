/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.awt.Graphics;
import java.util.Iterator;

import pfg.kraken.display.Display;
import pfg.kraken.display.Printable;
import pfg.kraken.display.PrintablePoint;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.EmbodiedKinematic;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * 
 * @author pf
 *
 */

public abstract class Tentacle implements Printable, Iterable<RectangularObstacle>, Iterator<RectangularObstacle>
{
	public static final double PRECISION_TRACE = 0.02; // précision du tracé, en
														// m (distance entre
														// deux points
														// consécutifs). Plus le
														// tracé est précis,
														// plus on couvre de
														// point une même
														// distance
	public static final double PRECISION_TRACE_MM = PRECISION_TRACE * 1000; // précision
																			// du
																			// tracé,
																			// en
																			// mm
	public static final int NB_POINTS = 5; // nombre de points dans un arc
	public static final double DISTANCE_ARC_COURBE = PRECISION_TRACE_MM * NB_POINTS; // en
																						// mm
	public static final double DISTANCE_ARC_COURBE_M = PRECISION_TRACE * NB_POINTS; // en
																					// m
	
	private static final long serialVersionUID = 1268198325807123306L;

	public TentacleType vitesse; // utilisé pour le debug
	
	private int indexIter;
	
	private int remainingNb;
	
	public abstract int getNbPoints();

	public abstract EmbodiedKinematic getPoint(int indice);

	public abstract EmbodiedKinematic getLast();

	public final double getDuree(Tentacle tentacleParent, double translationalSpeed, int tempsArret, double curvaturePenalty)
	{
		boolean firstMove = tentacleParent == null; // le premier mouvement est particulier : on est déjà arrêté !
		int nb = getNbPoints();
		double out = 0;

		// l'arc courant est construit classiquement (ce cas est géré à la reconstruction de la trajectoire)
		for(int i = 0; i < nb; i++)
		{
			EmbodiedKinematic ek = getPoint(i);
			ek.maxSpeed = Math.min(ek.maxSpeed, translationalSpeed);
			out += PRECISION_TRACE_MM / ek.maxSpeed;
			out += Math.abs(ek.cinem.courbureReelle) * curvaturePenalty;
		}
		assert vitesse.getNbArrets(firstMove) >= 0;
		return out + vitesse.getNbArrets(firstMove) * tempsArret;
	}

	@Override
	public String toString()
	{
		String out = getClass().getSimpleName() + " (" + vitesse + ") with " + getNbPoints() + " points:\n";
		for(int i = 0; i < getNbPoints() - 1; i++)
			out += getPoint(i) + "\n";
		out += getLast();
		return out;
	}

	@Override
	public void print(Graphics g, Display f)
	{
		for(int i = 0; i < getNbPoints(); i++)
			new PrintablePoint(getPoint(i).cinem.getX(), getPoint(i).cinem.getY()).print(g, f);
	}



	@Override
	public Iterator<RectangularObstacle> iterator()
	{
		indexIter = 0;
		remainingNb = 0;
		for(int i = 0; i < getNbPoints(); i++)
			if(!getPoint(i).ignoreCollision)
				remainingNb++;
		return this;
	}

	@Override
	public boolean hasNext()
	{
		return remainingNb > 0;
	}

	@Override
	public RectangularObstacle next()
	{
		while(getPoint(indexIter).ignoreCollision)
			indexIter++;
		remainingNb--;
		return getPoint(indexIter++).obstacle;
	}
}
