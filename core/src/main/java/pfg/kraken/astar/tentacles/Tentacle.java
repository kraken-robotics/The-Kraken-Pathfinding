/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.awt.Graphics;
import java.util.Iterator;

import pfg.graphic.GraphicPanel;
import pfg.graphic.printable.Printable;
import pfg.graphic.printable.PrintablePoint;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.CinematiqueObs;

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
	
	public abstract int getNbPoints();

	public abstract CinematiqueObs getPoint(int indice);

	public abstract CinematiqueObs getLast();

	public final double getDuree(Tentacle tentacleParent, double translationalSpeed, int tempsArret, double maxAcceleration, double deltaSpeedFromStop)
	{
		boolean firstMove = tentacleParent == null; // le premier mouvement est particulier : on est déjà arrêté !
		boolean beginWithStop = getPoint(0).stop;
		int nb = getNbPoints();
		double out = 0;
		
		/*
		 * The maximum speed at the first point
		 */
		double firstMaxSpeed = Math.min(getPoint(0).maxSpeed, translationalSpeed);
		
		// premier mouvement : vitesse nulle (on est arrêté)
		double lastPossibleSpeed = 0;
		if(!firstMove && !beginWithStop)
			lastPossibleSpeed = tentacleParent.getLast().possibleSpeed;

		// prend en compte l'écart entre la vitesse max et la vitesse possible
		double deltaDuration = 0;
		
		/*
		 * On suppose l'accélération constante.
		 * Ce qui signifie que la vitesse s'écrit : v = v_0 + a * t
		 * On va chercher en combien de temps le robot parcourt 20mm
		 * deltaPos = \int_{0}^{t} (a*t + v_0) dt
		 *          = a * t^2 / 2 + v_0 * t
		 * C'est un trinôme du second degré
		 * Au final, le delta temps : (-v_0 + \sqrt{v_0^2 - 2*deltaPos*a}) / a
		 * 
		 * Avec un développement limité, le delta vitesse vaut : 2*a*deltaPos/v_0
		 * 
		 */
		
		if(lastPossibleSpeed >= firstMaxSpeed)
		{
			// l'arc courant est construit classiquement (ce cas est géré à la reconstruction de la trajectoire)
			for(int i = 0; i < nb; i++)
			{
				getPoint(i).maxSpeed = Math.min(getPoint(i).maxSpeed, translationalSpeed);
				getPoint(i).possibleSpeed = getPoint(i).maxSpeed;
				out += PRECISION_TRACE_MM / getPoint(i).maxSpeed;
			}
		}
		else if(lastPossibleSpeed < firstMaxSpeed)
		{
			// le temps est perdu dans l'arc courant qui doit accélérer
			// on MET à jour l'arc car son ancêtre ne pourra pas changer
			double currentSpeed = lastPossibleSpeed;
			for(int i = 0; i < nb; i++)
			{
				double maxSpeed = Math.min(getPoint(i).maxSpeed, translationalSpeed);
				if(currentSpeed != maxSpeed)
				{
					double deltaVitesse;
					if(currentSpeed < 0.1)
						deltaVitesse = deltaSpeedFromStop;
					else
						deltaVitesse = 2 * maxAcceleration * PRECISION_TRACE / currentSpeed;

					currentSpeed += deltaVitesse;
					currentSpeed = Math.min(currentSpeed, maxSpeed);
					deltaDuration += PRECISION_TRACE_MM * (deltaVitesse / 2 + (maxSpeed - currentSpeed));
				}
				getPoint(i).possibleSpeed = currentSpeed;
				getPoint(i).maxSpeed = maxSpeed;
				out += PRECISION_TRACE_MM / getPoint(i).maxSpeed;
			}

		}
		assert deltaDuration >= 0 : deltaDuration;
		assert vitesse.getNbArrets(firstMove) >= 0;
		return out + vitesse.getNbArrets(firstMove) * tempsArret + deltaDuration;
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
	public void print(Graphics g, GraphicPanel f)
	{
		for(int i = 0; i < getNbPoints(); i++)
			new PrintablePoint(getPoint(i).getPosition().getX(), getPoint(i).getPosition().getY()).print(g, f);
	}



	@Override
	public Iterator<RectangularObstacle> iterator()
	{
		indexIter = 0;
		return this;
	}

	@Override
	public boolean hasNext()
	{
		return indexIter < getNbPoints();
	}

	@Override
	public RectangularObstacle next()
	{
		return getPoint(indexIter++).obstacle;
	}
}
