/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.astar.arcs;

import java.awt.Graphics;

import config.Config;
import graphic.Fenetre;
import graphic.AbstractPrintBuffer;
import graphic.printable.Layer;
import graphic.printable.Printable;
import kraken.ConfigInfoKraken;
import kraken.Couleur;
import kraken.robot.Cinematique;
import kraken.utils.Log;
import kraken.utils.XY;
import kraken.utils.XY_RW;

/**
 * Cercle d'arrivée du pathfinding. Utilisé pour se coller aux cratères en
 * marche arrière
 * 
 * @author pf
 *
 */

public class CercleArrivee implements Printable
{
	private static final long serialVersionUID = 990702816093466277L;
	public volatile XY position;
	public volatile double rayon;
	public volatile XY arriveeDStarLite;

	private boolean graphic;
	private volatile double distanceMax, distanceMin, angleMax, angleMin;
	public volatile Double[] anglesAttaquePossibles;
	
	protected Log log;
	private AbstractPrintBuffer buffer;

	public CercleArrivee(Log log, AbstractPrintBuffer buffer, Config config)
	{
		this.log = log;
		this.buffer = buffer;
		graphic = config.getBoolean(ConfigInfoKraken.GRAPHIC_CERCLE_ARRIVEE);

		if(graphic)
			buffer.add(this);
	}

	public void set(XY position, double orientationArriveeDStarLite, double rayon, Double[] anglesAttaquesPossibles, double distanceMax, double distanceMin, double angleMax, double angleMin)
	{		
		this.anglesAttaquePossibles = anglesAttaquesPossibles;
		this.distanceMax = distanceMax;
		this.distanceMin = distanceMin;
		this.angleMax = angleMax;
		this.angleMin = angleMin;

		this.position = new XY(position.getX(), position.getY());
		this.arriveeDStarLite = new XY_RW(rayon, orientationArriveeDStarLite, false);
		((XY_RW) arriveeDStarLite).plus(position);
		this.rayon = rayon;
		if(graphic)
			synchronized(buffer)
			{
				buffer.notify();
			}
		// log.debug("arriveeDStarLite : "+arriveeDStarLite);
	}

	private XY_RW tmp = new XY_RW();

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
	public int getLayer()
	{
		return Layer.FOREGROUND.ordinal();
	}

	public boolean isInCircle(XY position2)
	{
		return position2.squaredDistance(position) < rayon * rayon;
	}

	@Override
	public String toString()
	{
		return position + ", rayon " + rayon + ", arrivee " + arriveeDStarLite;
	}
}
