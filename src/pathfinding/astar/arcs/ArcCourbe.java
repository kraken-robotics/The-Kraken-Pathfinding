/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package pathfinding.astar.arcs;

import java.awt.Graphics;

import obstacles.types.ObstacleCircular;
import pathfinding.astar.arcs.vitesses.VitesseCourbure;
import graphic.Fenetre;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import config.Config;
import config.ConfigInfo;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.RobotReal;

/**
 * Un arc de trajectoire courbe. Juste une succession de points.
 * @author pf
 *
 */

public abstract class ArcCourbe implements Printable
{

//	public ObstacleArcCourbe obstacle = new ObstacleArcCourbe();
	protected static int tempsArret;
	public VitesseCourbure vitesse; // utilisé pour le debug
	
	public abstract int getNbPoints();
	public abstract CinematiqueObs getPoint(int indice);
	public abstract CinematiqueObs getLast();
	public abstract double getVitesseTr();
	protected abstract double getLongueur();

	public final double getDuree()
	{
		return getLongueur() / getVitesseTr() + vitesse.getNbArrets() * tempsArret;
	}
	
	@Override
	public String toString()
	{
		String out = getClass().getSimpleName()+" :\n";
		for(int i = 0; i < getNbPoints()-1; i++)
			out += getPoint(i)+"\n";
		out += getLast();
		return out;
	}
	
	public static void useConfig(Config config)
	{
		tempsArret = config.getInt(ConfigInfo.TEMPS_ARRET);
	}


	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		for(int i = 0; i < getNbPoints(); i++)
			new ObstacleCircular(getPoint(i).getPosition(), 4, Couleur.TRAJECTOIRE).print(g, f, robot);
	}

	@Override	
	public Layer getLayer()
	{
		return Couleur.TRAJECTOIRE.l;
	}

}
