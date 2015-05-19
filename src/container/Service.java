package container;

import utils.Config;

/**
 * Interface commune à toutes les classes obtenables par container.getService
 * @author pf
 *
 */

public interface Service {

	/**
	 * Accès au fichier de configuration
	 */
	public void updateConfig(Config config);
	
}
