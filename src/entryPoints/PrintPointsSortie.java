package entryPoints;
import container.Container;
import container.ServiceNames;
import exceptions.ContainerException;
import exceptions.PointSortieException;
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
		Config config;
		try {
			config = (Config) container.getService(ServiceNames.CONFIG);
			config.set(ConfigInfo.CHECK_POINTS_SORTIE, "true");
			container.getService(ServiceNames.D_STAR_LITE);
			container.destructor();
		} catch (ContainerException e) {
			e.printStackTrace();
		} catch (PointSortieException e) {
			e.printStackTrace();
		}
	}

}
