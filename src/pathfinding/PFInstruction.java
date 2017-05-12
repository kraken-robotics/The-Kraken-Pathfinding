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
	private volatile KeyPathCache k;
	private volatile PathfindingException e;
	private volatile boolean done; // la recherche est-elle finie ?
	private volatile boolean isSearching; // une recherche est-elle en cours ?
	
	public synchronized void set(KeyPathCache k)
	{
		done = false;
		this.k = k;
		notifyAll();
	}
	
	public synchronized void setDone()
	{
		this.done = true;
		this.isSearching = false;
		notifyAll();
	}
	
	public void setException(PathfindingException e)
	{
		this.e = e;
		setDone();
	}
	
	public void throwException() throws PathfindingException
	{
		if(e != null)
		{
			PathfindingException tmp = e;
			e = null;
			throw tmp;
		}
	}
	
	public KeyPathCache getKey()
	{
		KeyPathCache out = k;
		k = null;
		isSearching = true;
		return out;
	}
	
	public boolean isDone()
	{
		return done;
	}
	
	public boolean isSearching()
	{
		return isSearching;
	}
	
	public boolean isEmpty()
	{
		return k == null;
	}

}
