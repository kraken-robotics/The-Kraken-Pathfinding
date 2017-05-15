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

import utils.Log;

/**
 * Itérator permettant de manipuler facilement les obstacles mobiles dans le
 * GameState
 * 
 * @author pf
 *
 */

public class ObstaclesIteratorFutur extends ObstaclesIterator
{
	private int firstNotDead = 0;
	private long dateInit = -1;

	public ObstaclesIteratorFutur(Log log, ObstaclesMemory memory)
	{
		super(log, memory);
	}

	@Override
	public ObstaclesIteratorFutur clone()
	{
		ObstaclesIteratorFutur cloned_manager = new ObstaclesIteratorFutur(log, memory);
		copy(cloned_manager, dateInit);
		return cloned_manager;
	}

	/**
	 * Nécessaire au fonctionnement du memory manager
	 * 
	 * @param other
	 */
	public void copy(ObstaclesIteratorFutur other, long date)
	{
		other.init(date, firstNotDead);
	}

	/**
	 * Réinitialise l'itérateur avec la date donnée à la dernière initialisation
	 */
	public void reinit()
	{
		nbTmp = firstNotDead - 1;
	}

	/**
	 * Calcule l'entrée où commencent les obstacles à cette date.
	 * Se fait à la copie.
	 * 
	 * @param date
	 */
	public void init(long date, int firstNotDead)
	{
		this.firstNotDead = firstNotDead;
		while(this.firstNotDead < memory.size())
		{
			if(memory.isDestructionNecessary(this.firstNotDead, date))
				this.firstNotDead++;
			else
				break;
		}
		nbTmp = this.firstNotDead - 1;
		dateInit = date;
	}
}
