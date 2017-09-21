/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.awt.Graphics;
import pfg.config.Config;
import pfg.graphic.Chart;
import pfg.graphic.GraphicPanel;
import pfg.graphic.printable.Printable;
import pfg.graphic.printable.PrintablePoint;
import pfg.kraken.ConfigInfoKraken;
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
	private static final long serialVersionUID = 1268198325807123306L;
	// public ObstacleArcCourbe obstacle = new ObstacleArcCourbe();
	protected static int tempsArret;
	public TentacleType vitesse; // utilisé pour le debug

	public abstract int getNbPoints();

	public abstract CinematiqueObs getPoint(int indice);

	public abstract CinematiqueObs getLast();

	protected abstract double getLongueur();

	public final double getDuree(double translationalSpeed)
	{
		return getLongueur() / translationalSpeed + vitesse.getNbArrets() * tempsArret;
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

	public static void useConfig(Config config)
	{
		tempsArret = config.getInt(ConfigInfoKraken.TEMPS_ARRET);
	}

	@Override
	public void print(Graphics g, GraphicPanel f, Chart a)
	{
		for(int i = 0; i < getNbPoints(); i++)
			new PrintablePoint(getPoint(i).getPosition().getX(), getPoint(i).getPosition().getY()).print(g, f, a);
	}

}
