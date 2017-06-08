package hook;

/**
 * Interface utilisée pour passer des méthodes à Callback.
 * Il faut créer une classe implémentant cette interface par méthode d'intérêt.
 * Il y a alors, dans l'implémentation, des attributs pour les arguments qui sont initialisés par le constructeur,
 * de manière à ce que execute reste sans argument.
 * @author pf
 */

public interface Executable
{

	/**
	 * La méthode qui sera exécutée par le hook
     * @return un booléen qui renseignera sur le fait que la méthode fait ou non bouger le robot
	 */
	public boolean execute();
	
}
