/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.astar.arcs;

import java.awt.Graphics;
import config.Config;
import graphic.Fenetre;
import graphic.printable.Layer;
import graphic.printable.Printable;
import kraken.ConfigInfoKraken;
import kraken.Couleur;
import kraken.obstacles.types.ObstacleCircular;
import kraken.pathfinding.astar.arcs.vitesses.VitesseCourbure;
import kraken.robot.CinematiqueObs;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * 
 * @author pf
 *
 */

public abstract class ArcCourbe implements Printable
{
	// public ObstacleArcCourbe obstacle = new ObstacleArcCourbe();
	protected static int tempsArret;
	public VitesseCourbure vitesse; // utilisé pour le debug

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
	public void print(Graphics g, Fenetre f)
	{
		for(int i = 0; i < getNbPoints(); i++)
			new ObstacleCircular(getPoint(i).getPosition(), 4, Couleur.TRAJECTOIRE).print(g, f);
	}

	@Override
	public Layer getLayer()
	{
		return Couleur.TRAJECTOIRE.l;
	}

}
