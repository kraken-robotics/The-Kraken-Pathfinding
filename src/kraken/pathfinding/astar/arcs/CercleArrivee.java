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

package kraken.pathfinding.astar.arcs;

import java.awt.Graphics;
import config.Config;
import graphic.Fenetre;
import graphic.PrintBufferInterface;
import graphic.printable.Layer;
import graphic.printable.Printable;
import kraken.config.ConfigInfoKraken;
import kraken.graphic.Couleur;
import kraken.pathfinding.SensFinal;
import kraken.robot.Cinematique;
import kraken.utils.Log;
import kraken.utils.Vec2RO;
import kraken.utils.Vec2RW;
import kraken.utils.Log.Verbose;

/**
 * Cercle d'arrivée du pathfinding. Utilisé pour se coller aux cratères en
 * marche arrière
 * 
 * @author pf
 *
 */

public class CercleArrivee implements Printable
{
	public volatile Vec2RO position;
	public volatile double rayon;
	public volatile Vec2RO arriveeDStarLite;
	public volatile SensFinal sens;

	private boolean graphic;
	private volatile double distanceMax, distanceMin, angleMax, angleMin;
	public volatile Double[] anglesAttaquePossibles;
	
	protected Log log;
	private PrintBufferInterface buffer;

	public CercleArrivee(Log log, PrintBufferInterface buffer, Config config)
	{
		this.log = log;
		this.buffer = buffer;
		graphic = config.getBoolean(ConfigInfoKraken.GRAPHIC_CERCLE_ARRIVEE);

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

	private Vec2RW tmp = new Vec2RW();

	public boolean isArrivedAsser(Cinematique robot)
	{
		return isArrived(robot, angleMin, angleMax, rayon + distanceMin, rayon + distanceMax, true, false);
	}
	
	public boolean isArrivedPF(Cinematique robot)
	{
		return isArrived(robot, -1, 1, rayon - 5, rayon + 5, false, true);
	}
	
	/**
	 * Sommes-nous arrivés ?
	 * 
	 * @param last
	 * @return
	 */
	private boolean isArrived(Cinematique robot, double angleMin, double angleMax, double distanceMin, double distanceMax, boolean verbose, boolean checkAuthorizedAngle)
	{
		double deltaDist = robot.getPosition().distance(position);
		// on vérifie la distance au cratère
		if(deltaDist > distanceMax || deltaDist < distanceMin)
		{
			if(verbose)
				log.warning("Mauvaise distance au cratère : "+deltaDist, Verbose.SCRIPTS.masque);
			return false;
		}
		
		robot.orientationReelle %= 2 * Math.PI;
		if(robot.orientationReelle > Math.PI)
			robot.orientationReelle -= 2 * Math.PI;
		else if(robot.orientationReelle < -Math.PI)
			robot.orientationReelle += 2 * Math.PI;
		
		boolean accepted = anglesAttaquePossibles == null || !checkAuthorizedAngle;

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
				log.warning("L'orientation " + robot.orientationReelle + " n'est pas autorisée pour arriver sur le cratère !", Verbose.SCRIPTS.masque);
			return false;
		}
		
		position.copy(tmp);
		tmp.minus(robot.getPosition());
		double o = tmp.getArgument();

		double diffo = (o - robot.orientationReelle + Math.PI) % (2 * Math.PI);
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
	public void print(Graphics g, Fenetre f)
	{
		if(position != null)
		{
			g.setColor(Couleur.ROUGE.couleur);
			g.drawOval(f.XtoWindow(position.getX() - rayon), f.YtoWindow(position.getY() + rayon), f.distanceXtoWindow((int) (2 * rayon)), f.distanceYtoWindow((int) (2 * rayon)));
			int r = 20;
			g.fillOval(f.XtoWindow(arriveeDStarLite.getX() - r), f.YtoWindow(arriveeDStarLite.getY() + r), f.distanceXtoWindow((int) (2*r)), f.distanceYtoWindow((int) (2*r)));
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