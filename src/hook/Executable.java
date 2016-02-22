package hook;

import java.util.ArrayList;

import pathfinding.ChronoGameState;

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
	 */
	public void execute();

	/**
	 * Cette mise à jour conserve surtout les Executables susceptibles d'être utilisés
	 * sur un GameState<RobotChrono>. Les hooks ne sont pas recréés pour chaque gamestate,
	 * mais recyclés, ce qui impose de pouvoir mettre à jour le gamestate afin de mettre
	 * à jour la référence du gamestate. Sinon, c'est un autre gamestate, celui d'avant
	 * le recyclage, qui est utilisé.
	 * @param state
	 */
	public void updateGameState(ChronoGameState state);
	
	/**
	 * Afin de l'envoyer par série
	 * @return
	 */
	public ArrayList<Byte> toSerial();

}
