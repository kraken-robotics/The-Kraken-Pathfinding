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

package pathfinding.chemin;

import java.awt.Graphics;

import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.types.ObstacleArcCourbe;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleProximity;
import graphic.Fenetre;
import graphic.PrintBuffer;
import graphic.printable.Layer;
import graphic.printable.Printable;
import robot.RobotReal;
import serie.BufferOutgoingOrder;
import utils.Config;
import utils.ConfigInfo;
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
	private IteratorCheminPathfinding iterChemin;	
	private PrintBuffer buffer;
	
	private volatile ArcCourbe[] chemin = new ArcCourbe[256];
	protected int indexFirst = 0;
	protected int indexLast = 0;
	private int lastValidIndex; // l'indice du dernier index (-1 si aucun ne l'est, Integer.MAX_VALUE si tous le sont)
	
	public CheminPathfinding(Log log, BufferOutgoingOrder out, ObstaclesIteratorPresent iterator, PrintBuffer buffer)
	{
		this.log = log;
		this.out = out;
		this.iterator = iterator;
		iterChemin = new IteratorCheminPathfinding(this);
		this.buffer = buffer;
	}
	
	/**
	 * Y a-t-il une collision avec un obstacle de proximité ?
	 */
	public void checkColliding()
	{
		if(isColliding())
			notify();
	}
	
	/**
	 * Vérifie des collisions et met à jour lastIndex
	 * @return
	 */
	private boolean isColliding()
	{
		iterator.save(); // on sauvegarde sa position actuelle, c'est-à-dire la première position des nouveaux obstacles
		iterChemin.reinit();
		lastValidIndex = -1;
		
		while(iterChemin.hasNext())
		{
			ObstacleArcCourbe a = iterChemin.next().obstacle;
			iterator.load();
			while(iterator.hasNext())
			{
				if(iterator.next().isColliding(a))
				{
					while(iterator.hasNext()) // on s'assure que l'iterateur se termine à la fin des obstacles connus
						iterator.next();
					return true;
				}
			}
			lastValidIndex = iterChemin.getIndex();
		}
		return false;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		if(config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY_FINAL))
			buffer.add(this);
	}
	
	public synchronized boolean isEmpty()
	{
		return indexFirst == indexLast;
	}

	public synchronized void add(ArcCourbe arc)
	{
		arc.indexTrajectory = indexLast;
		chemin[indexLast++] = arc;
		indexLast &= 0xFF;
		out.envoieArcCourbe(arc);
		
		// si on revient au début, c'est qu'il y a un problème ou que le buffer est sous-dimensionné
		if(indexLast == indexFirst)
			log.critical("Buffer trop petit !");
	}
	
	protected synchronized ArcCourbe get(int index)
	{
		if((indexFirst <= index && index < indexLast)
		|| (indexFirst > indexLast && indexFirst <= index && index < indexLast + 256)
		|| (indexFirst > indexLast && indexFirst <= index + 256 && index < indexLast))
			return chemin[index];
		return null;
	}

	public void clear()
	{
		indexLast = indexFirst;
	}

	public synchronized void setCurrentIndex(int indexTrajectory)
	{
		indexFirst = indexTrajectory;
	}
	
	public int getLastValidIndex()
	{
		return lastValidIndex;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		iterChemin.reinit();
		while(iterChemin.hasNext())
		{
			ArcCourbe a = iterChemin.next();
			for(int i = 0; i < a.getNbPoints(); i++)
				buffer.addSupprimable(new ObstacleCircular(a.getPoint(i).getPosition(), 8));
		}
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

}
