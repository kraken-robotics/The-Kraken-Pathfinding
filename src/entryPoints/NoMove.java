package entryPoints;

import utils.Sleep;
import container.Container;
import container.ServiceNames;
import exceptions.ContainerException;
import exceptions.PointSortieException;

/**
 * Debug l'asser en affichant les grandeurs
 * @author pf
 *
 */

public class NoMove
{
	public static void main(String[] args) throws ContainerException, InterruptedException, PointSortieException
	{
		Container container = new Container();
		container.getService(ServiceNames.ROBOT_REAL); // initialisation de l'odo

		System.out.println("Attente du début du match");

		while(true)
			Sleep.sleep(1000);
	}
}
