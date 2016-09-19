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

package pathfinding;

import java.awt.Graphics;

import obstacles.memory.ObstaclesIteratorPresent;
import graphic.Fenetre;
import graphic.printable.Layer;
import graphic.printable.Printable;
import robot.RobotReal;
import serie.BufferOutgoingOrder;
import utils.Config;
import utils.Log;
import container.Service;
import pathfinding.astarCourbe.arcs.ArcCourbe;

/**
 * S'occupe de la trajectoire actuelle.
 * Notifie dès qu'un chemin (partiel ou complet) est disponible
 * @author pf
 *
 */

public class CheminPathfinding implements Service, Printable
{
	protected Log log;
	private BufferOutgoingOrder out;
	private ObstaclesIteratorPresent iterator;
	
	private volatile ArcCourbe[] chemin = new ArcCourbe[256];
	private int indexFirst = 0;
	private int indexLast = 0;
	
	public CheminPathfinding(Log log, BufferOutgoingOrder out, ObstaclesIteratorPresent iterator)
	{
		this.log = log;
		this.out = out;
		this.iterator = iterator;
	}
	
	/**
	 * Y a-t-il une collision avec un obstacle de proximité ?
	 */
	public void checkColliding()
	{
		iterator.reinit();
		while(iterator.hasNext())
		{
			iterator.next();
			// TODO
		}
		notify();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	public synchronized boolean isEmpty()
	{
		return indexFirst == indexLast;
	}

	public synchronized void add(ArcCourbe arc)
	{
		arc.indexTrajectory = indexLast;
		chemin[indexLast++] = arc;
		
		out.envoieArcCourbe(arc);
		
		// si on revient au début, c'est qu'il y a un problème ou que le buffer est sous-dimensionné
		if(indexLast == indexFirst)
			log.critical("Buffer trop petit !");
	}

	public void clear()
	{
		indexLast = indexFirst;
	}

	public synchronized void setCurrentIndex(int indexTrajectory)
	{
		indexFirst = indexTrajectory;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		// TODO Auto-generated method stub		
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

}
