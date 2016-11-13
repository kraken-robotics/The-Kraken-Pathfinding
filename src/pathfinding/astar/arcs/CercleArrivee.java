/*
Copyright (C) 2016 Pierre-François Gimenez

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

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import graphic.Fenetre;
import graphic.PrintBuffer;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import obstacles.types.ObstacleCircular;
import pathfinding.SensFinal;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.RobotReal;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Cercle d'arrivée du pathfinding. Utilisé pour se coller aux cratères en marche arrière
 * @author pf
 *
 */

public class CercleArrivee implements Service, Configurable, Printable
{
	public Vec2RO position;
	public double rayon;
	public Vec2RO arriveeDStarLite;
	public SensFinal sens;
	
	private double distance;
	private boolean graphic;
	
	protected Log log;
	private PrintBuffer buffer;
	
	public CercleArrivee(Log log, PrintBuffer buffer)
	{
		this.log = log;
		this.buffer = buffer;
	}
	
	public void set(Vec2RO position, double orientationArriveeDStarLite, double rayon, SensFinal sens)
	{
		this.position = position;
		this.arriveeDStarLite = new Vec2RW(rayon, orientationArriveeDStarLite + Math.PI, false);
		((Vec2RW)arriveeDStarLite).plus(position);
		this.rayon = rayon;
		this.sens = sens;
		if(graphic)
			synchronized(buffer)
			{
				buffer.notify();
			}
	}
	
	public void set(ObstacleCircular element, double orientationArriveeDStarLite)
	{
		set(element.getPosition(), orientationArriveeDStarLite, element.radius + distance, SensFinal.MARCHE_ARRIERE);
	}

	private Vec2RW tmp = new Vec2RW();
	
	/**
	 * Fournit, pour cette cinématique, l'arrivée souhaitée
	 * @param cinematique
	 * @param arrivee
	 */
	public void updateArrivee(Cinematique robot, Cinematique arrivee)
	{
		Vec2RW out = arrivee.getPositionEcriture();
		position.copy(out);
		out.minus(robot.getPosition());
		double d = out.norm();
		out.scalar((d - rayon) / d);
		arrivee.orientationGeometrique = out.getArgument();
		out.plus(robot.getPosition());
	}

	/**
	 * Sommes-nous arrivés ?
	 * @param last
	 * @return
	 */
	public boolean isArrived(CinematiqueObs robot)
	{
		position.copy(tmp);
		tmp.minus(robot.getPosition());
		double o = tmp.getArgument();

		double diffo = (o - robot.orientationGeometrique) % (2*Math.PI);
		if(diffo > Math.PI)
			diffo -= 2*Math.PI;
		
		// on vérifie qu'on est proche du rayon avec la bonne orientation
		return (robot.getPosition().squaredDistance(position) - rayon * rayon) < 10 && diffo < 2 / 180 * Math.PI;
	}
	
	@Override
	public void useConfig(Config config)
	{
		distance = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE) + config.getInt(ConfigInfo.DISTANCE_AU_CRATERE);
		graphic = config.getBoolean(ConfigInfo.GRAPHIC_CERCLE_ARRIVEE);
		if(graphic)
			buffer.add(this);
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		g.setColor(Couleur.ROUGE.couleur);
		g.drawOval(f.XtoWindow(position.getX()-rayon), f.YtoWindow(position.getY()+rayon), f.distanceXtoWindow((int)(2*rayon)), f.distanceYtoWindow((int)(2*rayon)));
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

}
