/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package pathfinding.astar.arcs;

import java.awt.Graphics;

import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.HighPFClass;
import container.dependances.LowPFClass;
import graphic.Fenetre;
import graphic.PrintBufferInterface;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import pathfinding.SensFinal;
import robot.Cinematique;
import robot.RobotReal;
import table.GameElementNames;
import utils.Log;
import utils.Log.Verbose;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Cercle d'arrivée du pathfinding. Utilisé pour se coller aux cratères en
 * marche arrière
 * 
 * @author pf
 *
 */

public class CercleArrivee implements Service, Printable, HighPFClass, LowPFClass
{
	public Vec2RO position;
	public double rayon;
	public Vec2RO arriveeDStarLite;
	public SensFinal sens;

	private boolean graphic;
	private double distanceMax, distanceMin, angleMax, angleMin;
	public Double[] anglesAttaquePossibles;
	
	protected Log log;
	private PrintBufferInterface buffer;

	public CercleArrivee(Log log, PrintBufferInterface buffer, Config config)
	{
		this.log = log;
		this.buffer = buffer;
		graphic = config.getBoolean(ConfigInfo.GRAPHIC_CERCLE_ARRIVEE);

		if(graphic)
			buffer.add(this);
	}

	public void set(Vec2RO position, double orientationArriveeDStarLite, double rayon, SensFinal sens, Double[] anglesAttaquesPossibles, double distanceMax, double distanceMin, double angleMax, double angleMin)
	{		
		this.anglesAttaquePossibles = anglesAttaquesPossibles;
		this.distanceMax = distanceMax;
		this.distanceMin = distanceMin;
		this.angleMax = angleMax;
		this.angleMin = angleMin;

		this.position = new Vec2RO(position.getX(), position.getY());
		this.arriveeDStarLite = new Vec2RW(rayon, orientationArriveeDStarLite, false);
		((Vec2RW) arriveeDStarLite).plus(position);
		this.rayon = rayon;
		this.sens = sens;
		if(graphic)
			synchronized(buffer)
			{
				buffer.notify();
			}
		// log.debug("arriveeDStarLite : "+arriveeDStarLite);
	}

	public void set(GameElementNames element, double rayon, double distanceMax, double distanceMin, double angleMax, double angleMin)
	{
		set(element.obstacle.getPosition(), element.orientationArriveeDStarLite, rayon, SensFinal.MARCHE_ARRIERE, null, distanceMax, distanceMin, angleMax, angleMin);
	}

	private Vec2RW tmp = new Vec2RW();

	public boolean isArrivedAsser(Cinematique robot)
	{
		return isArrived(robot, angleMin, angleMax, rayon+distanceMin, rayon+distanceMax, true);
	}
	
	public boolean isArrivedPF(Cinematique robot)
	{
		return isArrived(robot, -1, 1, rayon - 5, rayon + 5, false);
	}
	
	/**
	 * Sommes-nous arrivés ?
	 * 
	 * @param last
	 * @return
	 */
	private boolean isArrived(Cinematique robot, double angleMin, double angleMax, double distanceMin, double distanceMax, boolean verbose)
	{
		double deltaDist = robot.getPosition().distance(position);
		// on vérifie la distance au cratère
		if(deltaDist > distanceMax || deltaDist < distanceMin)
		{
			if(verbose)
				log.debug("Mauvaise distance au cratère : "+deltaDist);
			return false;
		}
		
		robot.orientationReelle %= 2 * Math.PI;
		if(robot.orientationReelle > Math.PI)
			robot.orientationReelle -= 2 * Math.PI;
		else if(robot.orientationReelle < -Math.PI)
			robot.orientationReelle += 2 * Math.PI;
		
		boolean accepted = anglesAttaquePossibles == null;

		if(!accepted)
			for(int i = 0; i < anglesAttaquePossibles.length / 2; i++)
				if(robot.orientationReelle >= anglesAttaquePossibles[2 * i] && robot.orientationReelle <= anglesAttaquePossibles[2 * i + 1])
				{
					accepted = true;
					break;
				}
		
		if(!accepted)
		{
			if(verbose)
				log.debug("L'orientation " + robot.orientationReelle + " n'est pas autorisée pour arriver sur le cratère !");
			return false;
		}
		
		position.copy(tmp);
		tmp.minus(robot.getPosition());
		double o = tmp.getArgument();

		double diffo = (o - robot.orientationGeometrique) % (2 * Math.PI);
		if(diffo > Math.PI)
			diffo -= 2 * Math.PI;
		else if(diffo < -Math.PI)
			diffo += 2 * Math.PI;
		
		diffo *= 180. / Math.PI;
		
		// on vérifie qu'on a la bonne orientation
		boolean out = diffo <= angleMax && diffo >= angleMin;
		
		if(verbose)
			log.debug("Arrivée sur cercle ? " + out + ". Delta orientation : " + diffo + ", delta distance : " + deltaDist, Verbose.SCRIPTS.masque);
		
		return out;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		if(position != null)
		{
			g.setColor(Couleur.ROUGE.couleur);
			g.drawOval(f.XtoWindow(position.getX() - rayon), f.YtoWindow(position.getY() + rayon), f.distanceXtoWindow((int) (2 * rayon)), f.distanceYtoWindow((int) (2 * rayon)));
		}
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

	public boolean isInCircle(Vec2RO position2)
	{
		return position2.squaredDistance(position) < rayon * rayon;
	}

	@Override
	public String toString()
	{
		return position + ", rayon " + rayon + ", arrivee " + arriveeDStarLite;
	}
}
