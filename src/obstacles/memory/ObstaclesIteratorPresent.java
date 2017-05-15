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
 * Itérator permettant de manipuler facilement les obstacles mobiles du présent
 * 
 * @author pf
 *
 */

public class ObstaclesIteratorPresent extends ObstaclesIterator
{
	private int save;

	public ObstaclesIteratorPresent(Log log, ObstaclesMemory memory)
	{
		super(log, memory);
	}

	/**
	 * Calcule l'entrée où commencent les obstacles maintenant
	 */
	public void reinit()
	{
		nbTmp = memory.getFirstNotDeadNow() - 1;
	}

	/**
	 * Pour parcourir tous ceux qui sont morts (utilisé par le GridSpace)
	 * 
	 * @return
	 */
	public boolean hasNextDead()
	{
		int firstNotDeadNow = memory.getFirstNotDeadNow();
		while(nbTmp + 1 < firstNotDeadNow && memory.getObstacle(nbTmp + 1) == null)
			nbTmp++;

		return nbTmp + 1 < firstNotDeadNow;
	}

	/**
	 * Sauvegarde la position actuelle pour pouvoir y revenir plus tard
	 */
	public void save()
	{
		save = nbTmp;
	}

	/**
	 * Recharge la position précédemment sauvegardée
	 */
	public void load()
	{
		nbTmp = save;
	}
}
