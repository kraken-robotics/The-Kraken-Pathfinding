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

package pathfinding;

import container.Service;
import container.dependances.HighPFClass;
import exceptions.PathfindingException;

/**
 * Classe qui permet de transférer les instructions du pathfinding au thread qui s'en occupe
 * Cette classe notifie à la fois le thread qui fait le pathfinding (et qui attend les instructions) et le thread
 * qui demande le pathfinding (et qui attend la notification de fin de recherche)
 * @author pf
 *
 */

public class PFInstruction implements Service, HighPFClass
{
	private volatile PathfindingException e = null;
	private volatile boolean done = true; // la recherche est-elle finie ?
	private volatile boolean isSearching = false; // une recherche est-elle en cours ?
	private volatile boolean request = false; // y a-t-il une requête ?
	
	public synchronized void searchRequest()
	{
		request = true;
		notifyAll();
	}
	
	public boolean hasRequest()
	{
		return request;
	}
	
	public synchronized void setDone()
	{
		done = true;
		isSearching = false;
		request = false;
		notifyAll();
	}
	
	public synchronized void setException(PathfindingException e)
	{
		this.e = e;
		setDone();
	}
	
	public synchronized void throwException() throws PathfindingException
	{
		done = false;
		if(e != null)
		{
			PathfindingException tmp = e;
			e = null;
			throw tmp;
		}
	}
	
	public synchronized void beginSearch()
	{
		isSearching = true;
		request = false;
	}
	
	public boolean isDone()
	{
		return done;
	}
	
	public boolean isSearching()
	{
		return isSearching;
	}

}
