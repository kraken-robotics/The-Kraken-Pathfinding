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

package table;

import java.util.BitSet;
import java.util.List;
import obstacles.types.ObstacleMasque;
import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.CoreClass;
import graphic.PrintBufferInterface;
import pathfinding.dstarlite.gridspace.MasqueManager;
import pathfinding.dstarlite.gridspace.PointDirige;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import utils.Log;

/**
 * La "vraie" table
 * 
 * @author pf
 *
 */

public class RealTable extends Table implements Service, CoreClass
{
	private PrintBufferInterface buffer;
	private boolean print;
	private long lastEtatTableDStarLite = 0;
	private boolean lastShoot = true;
	private BitSet[] newOldObstacles = new BitSet[2];
	private BitSet newObstacles = new BitSet(PointGridSpace.NB_POINTS * 8);
	private BitSet oldObstacles = new BitSet(PointGridSpace.NB_POINTS * 8);

	public RealTable(Log log, PrintBufferInterface buffer, MasqueManager masquemanager, Config config)
	{
		super(log);
		this.buffer = buffer;
		newOldObstacles[0] = oldObstacles;
		newOldObstacles[1] = newObstacles;

		// On ajoute les masques aux cylindres
		for(GameElementNames g : GameElementNames.values())
			if(g.aUnMasque)
				((ObstacleMasque) g.obstacle).setMasque(masquemanager.getMasqueCylindre(g.obstacle.getPosition()));

		print = config.getBoolean(ConfigInfo.GRAPHIC_GAME_ELEMENTS);
		if(print)
			for(GameElementNames g : GameElementNames.values())
				buffer.addSupprimable(g.obstacle);
	}

	/**
	 * Met à jour l'affichage en plus
	 */
	@Override
	public synchronized boolean setDone(GameElementNames id, EtatElement done)
	{
		if(done.hash > isDone(id).hash)
			log.debug("Changement état de " + id + " : " + done);
		if(print && done.hash > EtatElement.INDEMNE.hash)
			buffer.removeSupprimable(id.obstacle);
		return super.setDone(id, done);
	}

	/**
	 * Fournit les modifications des obstacles d'éléments de jeu au D* Lite
	 * 
	 * @param shoot
	 * @return
	 */
	public BitSet[] getOldAndNewObstacles(boolean shoot)
	{
		oldObstacles.clear();
		newObstacles.clear();

		if(shoot) // si on shoot : on vire tout
		{
			// si on n'avait pas déjà shooté avant, il faut tout virer.
			if(!lastShoot)
				for(GameElementNames id : GameElementNames.values())
					if(id.aUnMasque && isDone(id, lastEtatTableDStarLite) == EtatElement.INDEMNE) // si
																									// l'élément
																									// de
																									// jeu
																									// n'est
																									// pas
																									// pris
					{
						List<PointDirige> points = ((ObstacleMasque) id.obstacle).getMasque().masque;
						for(PointDirige p : points)
							oldObstacles.set(p.hashCode());
					}
			// si on avait déjà shooté, il n'y a déjà plus rien…
		}
		else
		{
			if(lastShoot) // il faut tout ajouter
			{
				for(GameElementNames id : GameElementNames.values())
					if(id.aUnMasque && isDone(id, etatTable) == EtatElement.INDEMNE) // si
																						// l'élément
																						// de
																						// jeu
																						// n'est
																						// pas
																						// pris
					{
						List<PointDirige> points = ((ObstacleMasque) id.obstacle).getMasque().masque;
						for(PointDirige p : points)
							newObstacles.set(p.hashCode());
					}
			}
			else // des éléments de jeu ont pu disparaître
			{
				for(GameElementNames id : GameElementNames.values())
				{
					if(id.aUnMasque && isDone(id, lastEtatTableDStarLite) == EtatElement.INDEMNE && isDone(id, etatTable) != EtatElement.INDEMNE) // l'élément
																																					// de
																																					// jeu
																																					// été
																																					// indemne
																																					// et
																																					// ne
																																					// l'est
																																					// plus
					{
						List<PointDirige> points = ((ObstacleMasque) id.obstacle).getMasque().masque;
						for(PointDirige p : points)
							oldObstacles.set(p.hashCode());
					}
				}
			}
		}

		lastShoot = shoot;
		lastEtatTableDStarLite = etatTable;
		return newOldObstacles;
	}

}
