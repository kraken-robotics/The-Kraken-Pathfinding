package scriptsHook;

import exceptions.FinMatchException;
import exceptions.strategie.ScriptException;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;

/**
 * Classe mère de tous les scripts de hook
 * @author pf
 *
 */

public abstract class ScriptHook {

	protected Log log;
	
	protected ScriptHook(Log log)
	{
		this.log = log;
	}
	
	protected abstract void execute(GameState<RobotReal> gamestate) throws ScriptException, FinMatchException;

	protected abstract void termine(GameState<RobotReal> gamestate) throws ScriptException, FinMatchException;
	
	public void agit(GameState<RobotReal> gamestate) throws FinMatchException
	{
		try {
			execute(gamestate);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				termine(gamestate);
			} catch (ScriptException e) {
				try {
					termine(gamestate);
				} catch (ScriptException e1) {
					log.critical("On n'a pas réussi à terminer un script...", this);
					e1.printStackTrace();
				}
			}
		}
		
	}
	
}
