package entryPoints;
import container.Container;
import container.ServiceNames;
import exceptions.ContainerException;
import exceptions.FinMatchException;
import exceptions.PointSortieException;
import exceptions.SerialConnexionException;
import exceptions.ThreadException;
import utils.Config;
import utils.ConfigInfo;

/**
 * Affiche les points de sorties après modification des scripts
 * @author pf
 *
 */

// TODO: virer si inutile

public class PrintPointsSortie {

	public static void main(String[] args)
	{
		Container container;
		try {
			container = new Container();
		} catch (ContainerException e1) {
			e1.printStackTrace();
			return;
		}
		try {
			Config config = (Config) container.getService(ServiceNames.CONFIG);
			config.set(ConfigInfo.CHECK_POINTS_SORTIE, "true");
			container.getService(ServiceNames.PATHFINDING);
		} catch (ContainerException | ThreadException
				| SerialConnexionException | FinMatchException
				| PointSortieException e) {
			e.printStackTrace();
		}
		container.destructor();
	}

}
