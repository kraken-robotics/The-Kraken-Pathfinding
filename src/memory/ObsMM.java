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

package memory;

import container.Container;
import exceptions.ContainerException;
import obstacles.types.ObstacleArcCourbe;
import obstacles.types.ObstacleRectangular;
import utils.Log;

/**
 * Classe qui fournit des objets ObstaclesRectangular
 * Le moteur a besoin de beaucoup de ces obstacles de robot, et l'instanciation d'un objet est long.
 * Du coup on réutilise les mêmes objets sans devoir en créer tout le temps de nouveaux.
 * @author pf
 *
 */

public class ObsMM extends MemoryManager<ObstacleRectangular>
{

	public ObsMM(Log log, Container container) throws ContainerException
	{
		super(ObstacleRectangular.class, log, container, 100);
	}

	public void destroyNode(ObstacleArcCourbe obstacle)
	{
		for(ObstacleRectangular o : obstacle.ombresRobot)
			destroyNode(o);
	}
	
}
