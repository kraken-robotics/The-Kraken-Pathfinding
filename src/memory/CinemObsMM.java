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

package memory;

import config.Config;
import config.ConfigInfo;
import container.Container;
import container.HighPFClass;
import exceptions.ContainerException;
import pathfinding.astar.arcs.ArcCourbeDynamique;
import robot.CinematiqueObs;
import utils.Log;

/**
 * Classe qui fournit des objets CinematiqueObs
 * Ces CinematiqueObs ne sont utilisés QUE pas les arcs courbes cubiques, qui ont un nombre de CinematiqueObs pas connu à l'avance
 * Les arcs courbes de clothoïde contiennent des CinematiqueObs et sont gérés par le NodeMM
 * @author pf
 *
 */

public class CinemObsMM extends MemoryManager<CinematiqueObs> implements HighPFClass
{

	public CinemObsMM(Log log, Config config, Container container) throws ContainerException
	{
		super(CinematiqueObs.class, log, container, config.getInt(ConfigInfo.NB_INSTANCES_OBSTACLES));
	}

	// TODO : optimisable : la mémoire est contigue
	public void destroyNode(ArcCourbeDynamique arc)
	{
		for(int i = 0; i < arc.getNbPoints(); i++)
			destroyNode(arc.getPoint(i));
	}
	
}
