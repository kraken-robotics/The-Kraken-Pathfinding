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

package obstacles.memory;

import java.util.Iterator;
import obstacles.types.ObstacleProximity;
import utils.Log;

/**
 * Itérator permettant de manipuler facilement les obstacles mobiles
 * 
 * @author pf
 *
 */

public abstract class ObstaclesIterator implements Iterator<ObstacleProximity>
{
	protected Log log;
	protected ObstaclesMemory memory;

	protected volatile int nbTmp;

	public ObstaclesIterator(Log log, ObstaclesMemory memory)
	{
		this.log = log;
		this.memory = memory;
	}

	@Override
	public boolean hasNext()
	{
		while(nbTmp + 1 < memory.size() && memory.getObstacle(nbTmp + 1) == null)
			nbTmp++;

		return nbTmp + 1 < memory.size();
	}

	@Override
	public ObstacleProximity next()
	{
		return memory.getObstacle(++nbTmp);
	}

	@Override
	public void remove()
	{
		memory.remove(nbTmp);
	}

}
