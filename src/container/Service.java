package container;

import utils.Config;

/**
 * Interface commune à toutes les classes obtenables par container.getService
 * @author pf
 *
 */

public interface Service {

	/**
	 * Met à jour les variables de config qui doivent l'être
	 */
	public void updateConfig(Config config);

	/**
	 * Affecte les valeurs de config
	 * @param config
	 */
	public void useConfig(Config config);
	
}
