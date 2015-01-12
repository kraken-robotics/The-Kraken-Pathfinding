package strategie;

import hook.Hook;
import hook.HookFactory;

import java.util.ArrayList;

import astar.arc.Decision;
import container.Service;
import exceptions.FinMatchException;
import exceptions.ScriptException;
import exceptions.ScriptHookException;
import exceptions.UnableToMoveException;
import robot.RobotReal;
import robot.Speed;
import scripts.ScriptManager;
import threads.ThreadStrategy;
import utils.Config;
import utils.Log;

/**
 * Exécute ce qu'a décidé la stratégie.
 * Gère en particulier les réponses du système aux différentes erreurs.
 * @author pf
 *
 */

public class Execution implements Service {

	private GameState<RobotReal> gamestate;
	private Log log;
	protected Config config;
	private ScriptManager scriptmanager;
	private ThreadStrategy threadstrategy;
	
	private ArrayList<Hook> hooks_entre_scripts;
	
	public Execution(Log log, Config config, GameState<RobotReal> gamestate, ScriptManager scriptmanager, HookFactory hookfactory, ThreadStrategy threadstrategy)
	{
		updateConfig();
		this.log = log;
		this.config = config;
		this.gamestate = gamestate;
		this.scriptmanager = scriptmanager;
		this.threadstrategy = threadstrategy;

		hooks_entre_scripts = hookfactory.getHooksEntreScriptsReal(gamestate);
	}

	/**
	 * Appelé par le lanceur. C'est la boucle exécutée par le robot pendant tout le match
	 */
	public void boucleExecution()
	{		
		/**
		 * Boucle d'exécution
		 */
		while(true)
		{
			Decision bestDecision = threadstrategy.getBestDecision();
			try {
				executerScript(bestDecision);
			} catch (FinMatchException e) {
				break;
			}
		}
		log.debug("Match terminé!", this);
	}
	
	/**
	 * Exécute un script et passe sur la stratégie de secours si besoin est
	 * @param decision_actuelle
	 * @throws FinMatchException
	 */
	public void executerScript(Decision decision_actuelle) throws FinMatchException
	{
		log.debug("On tente d'exécuter "+decision_actuelle.script_name, this);
		try {
			tryOnce(decision_actuelle);
		} catch (UnableToMoveException | ScriptException e) {
			// On a rencontré l'ennemi en chemin. On applique la stratégie d'urgence.
			boolean recommence = true;
			do {
				if(gamestate.robot.isEnemyHere())
				{
					log.debug("Stratégie d'urgence avec ennemi: "+decision_actuelle.script_name, this);
					decision_actuelle = threadstrategy.getEmergencyDecision();
				}
				else
				{
					log.debug("Stratégie d'urgence sans ennemi: "+decision_actuelle.script_name, this);
					decision_actuelle = threadstrategy.getNormalDecision();
				}
				try {
					tryOnce(decision_actuelle);
					recommence = false;
				} catch (UnableToMoveException | ScriptException e1) {}
			} while(recommence);
		}
	}
	
	/**
	 * Exécute un script. Si celui-ci demande à exécuter un script de hook, l'exécuter
	 * @param d
	 * @throws UnableToMoveException
	 * @throws ScriptException
	 * @throws FinMatchException
	 */
	private void tryOnce(Decision d) throws UnableToMoveException, ScriptException, FinMatchException
	{
		try {
			gamestate.robot.set_vitesse(Speed.BETWEEN_SCRIPTS);
			gamestate.robot.suit_chemin(d.chemin, hooks_entre_scripts);
			threadstrategy.computeBestDecisionAfter(d);
			scriptmanager.getScript(d.script_name).agit(d.version, gamestate);
		} catch (ScriptHookException e) {
			try {
				scriptmanager.getScript(e.getNomScript()).agit(0, gamestate);
			} catch (ScriptHookException e1) {
				// Impossible...?
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void updateConfig()
	{}
	
}
