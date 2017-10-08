/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.awt.Graphics;
import pfg.graphic.GraphicPanel;
import pfg.graphic.printable.Printable;
import pfg.graphic.printable.PrintablePoint;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.robot.CinematiqueObs;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * 
 * @author pf
 *
 */

public abstract class Tentacle implements Printable
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
	// public ObstacleArcCourbe obstacle = new ObstacleArcCourbe();
	public TentacleType vitesse; // utilisé pour le debug

	public abstract int getNbPoints();

	public abstract CinematiqueObs getPoint(int indice);

	public abstract CinematiqueObs getLast();

	public final double getDuree(Tentacle tentacleParent, double translationalSpeed, int tempsArret, double maxAcceleration)
	{
		boolean firstMove = tentacleParent == null;
/*		int nb = getNbPoints();
		double out = 0;
		
		double firstMaxSpeed = Math.min(getPoint(0).maxSpeed, translationalSpeed);
		
		// premier mouvement : vitesse nulle (on est arrêté)
		double lastPossibleSpeed = 0;
		if(!firstMove)
			lastPossibleSpeed = tentacleParent.getPoint(tentacleParent.getNbPoints()-1).possibleSpeed;

		// prend en compte l'écart entre la vitesse max et la vitesse possible
		double deltaDuration = 0;
		
		if(lastPossibleSpeed >= firstMaxSpeed)
		{
			// le temps est perdu dans l'arc précédent car il doit anticiper la vitesse basse
			// on ne le met PAS à jour car peut-être que l'arc précédent sera au final utilisé avec un autre arc suivant
			if(lastPossibleSpeed > firstMaxSpeed)
			{
				
				firstMaxSpeed -= maxAcceleration;
			}
			
			// l'arc courant est construit classiquement
			for(int i = 0; i < nb; i++)
			{
				getPoint(i).maxSpeed = Math.min(getPoint(i).maxSpeed, translationalSpeed);
				assert getPoint(i).maxSpeed > 0;
				out += PRECISION_TRACE_MM / getPoint(i).maxSpeed;
			}

		}
		else if(lastPossibleSpeed < firstMaxSpeed)
		{
			// le temps est perdu dans l'arc courant qui doit accélérer
			// on MET à jour l'arc car son ancêtre ne pourra pas changer
			
		}
		assert vitesse.getNbArrets(firstMove) >= 0;
		return out + vitesse.getNbArrets(firstMove) * tempsArret + deltaDuration;*/
		
		int nb = getNbPoints();
		double out = 0;
		for(int i = 0; i < nb; i++)
		{
			getPoint(i).maxSpeed = Math.min(getPoint(i).maxSpeed, translationalSpeed);
			assert getPoint(i).maxSpeed > 0 : "Negative speed : "+getPoint(i).maxSpeed;
			out += PRECISION_TRACE_MM / getPoint(i).maxSpeed;
		}
		assert vitesse.getNbArrets(firstMove) >= 0;
		return out + vitesse.getNbArrets(firstMove) * tempsArret;
	}

	@Override
	public String toString()
	{
		String out = getClass().getSimpleName() + " :\n";
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

}
