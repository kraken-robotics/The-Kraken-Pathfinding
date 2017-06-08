package container;

/**
 * Interface commune à toutes les classes obtenables par container.getService
 * @author pf
 *
 */

public interface Service {

	/**
	 * Cette méthode sert surtout aux tests, afin de rendre propre les modifications de config en plein match.
	 */
	public void updateConfig();
	
}
