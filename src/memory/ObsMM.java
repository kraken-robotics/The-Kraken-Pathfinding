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
