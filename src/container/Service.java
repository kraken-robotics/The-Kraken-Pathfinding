package container;

import utils.Config;

/**
 * Interface commune à toutes les classes obtenables par container.getService
 * @author pf
 *
 */

public interface Service {

	/**
	 * Met à jour les variables de config qui doivent l'être. Peut-être appelé n'importe quand.
	 */
	public void updateConfig(Config config);

	/**
	 * Affecte les valeurs de config. N'est appelé qu'une fois, juste après le constructeur.
	 * @param config
	 */
	public void useConfig(Config config);
	
}
