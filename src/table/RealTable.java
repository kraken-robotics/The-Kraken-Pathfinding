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

package table;

import java.util.BitSet;

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import graphic.PrintBuffer;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import utils.Log;

/**
 * La "vraie" table
 * @author pf
 *
 */

public class RealTable extends Table implements Service, Configurable
{
	private PrintBuffer buffer;
	private boolean print;
	private long lastEtatTableDStarLite = 0;
	private BitSet[] newOldObstacles = new BitSet[2];
	private BitSet newObstacles = new BitSet(PointGridSpace.NB_POINTS * 8);
	private BitSet oldObstacles = new BitSet(PointGridSpace.NB_POINTS * 8);

	public RealTable(Log log, PrintBuffer buffer)
	{
		super(log);
		this.buffer = buffer;
		newOldObstacles[0] = oldObstacles;
		newOldObstacles[1] = newObstacles;
	}

	@Override
	public void useConfig(Config config)
	{
		print = config.getBoolean(ConfigInfo.GRAPHIC_GAME_ELEMENTS);
		if(print)
			for(GameElementNames g : GameElementNames.values())
				buffer.addSupprimable(g.obstacle);
	}
	
	/**
	 * Met à jour l'affichage en plus
	 */
	@Override
	public synchronized boolean setDone(GameElementNames id, Tribool done)
	{
		if(print && done.hash > Tribool.FALSE.hash)
			buffer.removeSupprimable(id.obstacle);
		return super.setDone(id, done);
	}
	
	/**
	 * Fournit les modifications des obstacles d'éléments de jeu au D* Lite
	 * @param shoot
	 * @return
	 */
	public BitSet[] getOldAndNewObstacles(boolean shoot)
	{
/*		oldObstacles.clear();
		newObstacles.clear();
		if(shoot)
		{
			for(GameElementNames id :GameElementNames.values())
				if(isDone(id, lastEtatTableDStarLite) != Tribool.FALSE)
				{
					List<PointDirige> = id.obstacle;
					oldObstacles.set();
				}
		}
		
		lastEtatTableDStarLite = etatTable;*/
		// TODO Auto-generated method stub
		return null;
	}

}
