package strategie;

import hook.Hook;
import hook.HookFactory;

import java.util.ArrayList;

import astar.arc.Decision;
import container.Service;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import exceptions.ScriptException;
import exceptions.ScriptHookException;
import exceptions.UnableToMoveException;
import robot.RobotReal;
import robot.Speed;
import scripts.ScriptManager;
import threads.ThreadStrategy;
import utils.Config;
import utils.Log;
import utils.Sleep;

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
		 * Attente du début du match
		 */
		while(!Config.matchDemarre)
			Sleep.sleep(20);
		
		/**
		 * Boucle d'exécution
		 */
		while(true)
		{
			try {
				Decision bestDecision = threadstrategy.getBestDecision();
				if(bestDecision == null)
					Sleep.sleep(50);
				else
					executerScript(bestDecision);
			} catch (FinMatchException e) {
				// la sortie se fait par l'exception FinMatchException
				break;
			}
		}
		
		// DEPENDS_ON_RULES
		// funny action (aucune cette année)
		
	}
	
	public void executerScript(Decision decision_actuelle) throws FinMatchException
	{
		for(int essai = 0; essai < 2; essai++)
		{
			if(essai == 1)
			do {
				// Normalement, cette décision n'est jamais vide (sauf au tout tout début du match)
				decision_actuelle = threadstrategy.getEmergencyDecision();
				if(decision_actuelle == null)
					Sleep.sleep(10);
			} while(decision_actuelle == null);
			log.debug("On tente d'exécuter "+decision_actuelle.script_name, this);
			try {
				tryOnce(decision_actuelle);
			} catch (PathfindingException e) {
// TODO				tryOnce(s, decision_actuelle.id_version, true, false);
				// Problème de pathfinding: pas de chemin. On rajoute des points
			} catch (ScriptException e) {
				log.critical("Abandon: erreur de script", this);
				// On a eu un problème au milieu du script. On change de script.
				// Note: le script lui-même possède des procédures de relance.
				continue;
			} catch (UnableToMoveException e) {
				// On a rencontré l'ennemi en chemin. On retente avec un autre chemin.
				try {
					log.debug("On réessaye d'exécuter "+decision_actuelle.script_name, this);
					tryOnce(decision_actuelle);
				} catch (Exception e1) {
					log.critical("Abandon: erreur pendant l'itinéraire de secours.", this);
					continue;
				}
			} catch (PathfindingRobotInObstacleException e) {
				log.critical("Abandon: on est dans un obstacle. Attente.", this);
				// On est dans un obstacle. Le mieux est encore d'attendre un peu.
				Sleep.sleep(1000);
				break; // on annule l'exécution.
			}
			// Tout s'est bien passé? On s'arrête là.
			break;
			
		}
	}
	
	private void tryOnce(Decision d) throws PathfindingException, UnableToMoveException, ScriptException, PathfindingRobotInObstacleException, FinMatchException
	{
		gamestate.robot.set_vitesse(Speed.BETWEEN_SCRIPTS);
		try {
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
