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

import java.util.LinkedList;

import container.Service;
import container.dependances.HighPFClass;
import exceptions.PathfindingException;
import robot.CinematiqueObs;
import serie.Ticket;
import utils.Log;

/**
 * Faux chemin, sert à la prévision d'itinéraire
 * @author pf
 *
 */

public class FakeCheminPathfinding implements Service, CheminPathfindingInterface, HighPFClass
{
	private LinkedList<CinematiqueObs> path;
	protected Log log;
	
	public FakeCheminPathfinding(Log log)
	{
		this.log = log;
	}
	
	@Override
	public synchronized Ticket[] add(LinkedList<CinematiqueObs> points) throws PathfindingException
	{
		path = points;
		notify();
		return null;
	}

	@Override
	public void setUptodate(boolean uptodate)
	{}

	@Override
	public boolean needPartial()
	{
		return false;
	}
	
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
}
