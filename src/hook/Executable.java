package hook;

import robot.RobotChrono;
import strategie.GameState;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;

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
	 * @throws ScriptHookException 
	 * @throws WallCollisionDetectedException 
	 */
	public void execute() throws FinMatchException, ScriptHookException, WallCollisionDetectedException;

	/**
	 * Cette mise à jour conserve surtout les Executables susceptibles d'être utilisés
	 * sur un GameState<RobotChrono>. Les hooks ne sont pas recréés pour chaque gamestate,
	 * mais recyclés, ce qui impose de pouvoir mettre à jour le gamestate afin de mettre
	 * à jour la référence du gamestate. Sinon, c'est un autre gamestate, celui d'avant
	 * le recyclage, qui est utilisé.
	 * @param state
	 */
	public void updateGameState(GameState<RobotChrono> state);
	
}
