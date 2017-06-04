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

package kraken.pathfinding.chemin;

import java.awt.Graphics;
import java.util.LinkedList;
import config.Config;
import graphic.Fenetre;
import graphic.PrintBuffer;
import graphic.printable.Layer;
import graphic.printable.Printable;
import kraken.config.ConfigInfoKraken;
import kraken.graphic.Couleur;
import kraken.obstacles.types.ObstacleCircular;
import kraken.robot.Cinematique;
import kraken.robot.ItineraryPoint;
import kraken.utils.Log;
import kraken.utils.Vec2RO;

/**
 * Faux chemin, sert à la prévision d'itinéraire
 * 
 * @author pf
 *
 */

public class DefaultCheminPathfinding implements CheminPathfindingInterface, Printable
{
	private LinkedList<ItineraryPoint> path;
	protected Log log;
	private PrintBuffer buffer;
	private boolean print;
	private ObstacleCircular[] aff = new ObstacleCircular[256];

	public DefaultCheminPathfinding(Log log, Config config, PrintBuffer buffer)
	{
		this.log = log;
		this.buffer = buffer;
		print = config.getBoolean(ConfigInfoKraken.GRAPHIC_TRAJECTORY_FINAL);
		if(print)
			buffer.add(this);
	}

	@Override
	public synchronized void addToEnd(LinkedList<ItineraryPoint> points)
	{
		path = points;
		if(print)
			synchronized(buffer)
			{
				buffer.notify();
			}
		notify();
	}

	@Override
	public void setUptodate()
	{}

	public LinkedList<ItineraryPoint> getPath()
	{
		LinkedList<ItineraryPoint> out = path;
		path = null;
		return out;
	}

	@Override
	public void print(Graphics g, Fenetre f)
	{
		int i = 0;
		if(path != null)
			for(ItineraryPoint c : path)
			{
				aff[i] = new ObstacleCircular(x, y, 8, Couleur.TRAJECTOIRE);
				buffer.addSupprimable(aff[i]);
				i++;
			}
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

	@Override
	public boolean aAssezDeMarge()
	{
		return true;
	}

	@Override
	public boolean needStop()
	{
		return false;
	}

	@Override
	public Cinematique getLastValidCinematique()
	{
		return null;
	}

	public void clear()
	{
		path = null;
	}
}
