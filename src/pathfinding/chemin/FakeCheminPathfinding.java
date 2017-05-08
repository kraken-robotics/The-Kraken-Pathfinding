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

package pathfinding.chemin;

import java.awt.Graphics;
import java.util.LinkedList;

import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.HighPFClass;
import exceptions.PathfindingException;
import graphic.Fenetre;
import graphic.PrintBuffer;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import obstacles.types.ObstacleCircular;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.RobotReal;
import serie.Ticket;
import utils.Log;

/**
 * Faux chemin, sert à la prévision d'itinéraire
 * @author pf
 *
 */

public class FakeCheminPathfinding implements Service, CheminPathfindingInterface, HighPFClass, Printable
{
	private LinkedList<CinematiqueObs> path;
	protected Log log;
	private PrintBuffer buffer;
	private boolean print;
	private ObstacleCircular[] aff = new ObstacleCircular[256];
	
	public FakeCheminPathfinding(Log log, Config config, PrintBuffer buffer)
	{
		this.log = log;
		this.buffer = buffer;
		print = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY_FINAL);
		if(print)
			buffer.add(this);
	}
	
	@Override
	public synchronized Ticket[] addToEnd(LinkedList<CinematiqueObs> points) throws PathfindingException
	{
		path = points;
		if(print)
			synchronized(buffer)
			{
				buffer.notify();
			}
		notify();
		return null;
	}

	@Override
	public void setUptodate()
	{}

	public boolean isReady()
	{
		return path != null;
	}
	
	public LinkedList<CinematiqueObs> getPath()
	{
		LinkedList<CinematiqueObs> out = path;
		path = null;
		return out;
	}
	
	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		int i = 0;
		if(path != null)
			for(Cinematique c : path)
			{
				aff[i] = new ObstacleCircular(c.getPosition(), 8, Couleur.TRAJECTOIRE);
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
	public Cinematique needRestart()
	{
		return null;
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
}
