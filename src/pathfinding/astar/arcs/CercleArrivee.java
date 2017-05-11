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

import config.Config;
import config.ConfigInfo;
import config.DynamicConfigurable;
import container.Service;
import container.dependances.HighPFClass;
import container.dependances.LowPFClass;
import graphic.Fenetre;
import graphic.PrintBufferInterface;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import pathfinding.SensFinal;
import robot.CinematiqueObs;
import robot.RobotReal;
import table.GameElementNames;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Cercle d'arrivée du pathfinding. Utilisé pour se coller aux cratères en marche arrière
 * @author pf
 *
 */

public class CercleArrivee implements Service, Printable, HighPFClass, LowPFClass, DynamicConfigurable
{
	public Vec2RO position;
	public double rayon;
	public Vec2RO arriveeDStarLite;
	public SensFinal sens;
	
	private boolean graphic;
	private boolean symetrie = false;
	
	protected Log log;
	private PrintBufferInterface buffer;
	
	public CercleArrivee(Log log, PrintBufferInterface buffer, Config config)
	{
		this.log = log;
		this.buffer = buffer;
		graphic = config.getBoolean(ConfigInfo.GRAPHIC_CERCLE_ARRIVEE);
		if(graphic)
			buffer.add(this);	}
	
	public void set(Vec2RO position, double orientationArriveeDStarLite, double rayon, SensFinal sens)
	{
		this.position = new Vec2RO(symetrie ? -position.getX() : position.getX(), position.getY());
		this.arriveeDStarLite = new Vec2RW(rayon, symetrie ? Math.PI - orientationArriveeDStarLite : orientationArriveeDStarLite, false);
		((Vec2RW)arriveeDStarLite).plus(position);
		this.rayon = rayon;
		this.sens = sens;
		if(graphic)
			synchronized(buffer)
			{
				buffer.notify();
			}
		log.debug("arriveeDStarLite : "+arriveeDStarLite);
	}
	
	public void set(GameElementNames element, double rayon)
	{
		set(element.obstacle.getPosition(), element.orientationArriveeDStarLite, rayon, SensFinal.MARCHE_ARRIERE);
	}

	private Vec2RW tmp = new Vec2RW();

	/**
	 * Sommes-nous arrivés ?
	 * @param last
	 * @return
	 */
	public boolean isArrived(CinematiqueObs robot)
	{
		position.copy(tmp);
		tmp.minus(robot.getPosition());
		double o = tmp.getFastArgument();

		double diffo = (o - robot.orientationGeometrique) % (2*Math.PI);
		if(diffo > Math.PI)
			diffo -= 2*Math.PI;
		
		// on vérifie qu'on est proche du rayon avec la bonne orientation
		return (robot.getPosition().distance(position) - rayon) < 2 && Math.abs(diffo) < 1 / 180. * Math.PI;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		if(position != null)
		{
			g.setColor(Couleur.ROUGE.couleur);
			g.drawOval(f.XtoWindow(position.getX()-rayon), f.YtoWindow(position.getY()+rayon), f.distanceXtoWindow((int)(2*rayon)), f.distanceYtoWindow((int)(2*rayon)));
		}
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

	public boolean isInCircle(Vec2RO position2)
	{
		return position2.squaredDistance(position) < rayon*rayon;
	}

	@Override
	public String toString()
	{
		return position+", rayon "+rayon+", arrivee "+arriveeDStarLite;
	}

	@Override
	public void updateConfig(Config config)
	{
		symetrie = config.getSymmetry();
	}
}
