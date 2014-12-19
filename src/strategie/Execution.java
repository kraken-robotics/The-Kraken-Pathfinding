package strategie;

import hook.Hook;
import hook.types.HookFactory;

import java.util.ArrayList;

import container.Service;
import pathfinding.AStar;
import pathfinding.PathfindingArcManager;
import enums.PathfindingNodes;
import enums.Speed;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import exceptions.ScriptHookException;
import exceptions.UnknownScriptException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.strategie.ScriptException;
import robot.RobotReal;
import scripts.Decision;
import scripts.Script;
import scripts.ScriptManager;
import threads.ThreadStrategy;
import utils.Config;
import utils.Log;
import utils.Sleep;

/**
 * Exécute ce qu'a décidé la stratégie.
 * @author pf
 *
 */

public class Execution implements Service {

	private GameState<RobotReal> gamestate;
	private Log log;
//	private Config config;
	private ScriptManager scriptmanager;
	private AStar<PathfindingArcManager, PathfindingNodes> pathfinding;
//	private HookFactory hookfactory;
	private ThreadStrategy threadstrategy;
//	private RobotColor color;
	
	private ArrayList<Hook> hooks_entre_scripts;
	
	public Execution(Log log, Config config, AStar<PathfindingArcManager, PathfindingNodes> pathfinding, GameState<RobotReal> gamestate, ScriptManager scriptmanager, HookFactory hookfactory, ThreadStrategy threadstrategy)
	{
		this.log = log;
//		this.config = config;
		this.pathfinding = pathfinding;
		this.gamestate = gamestate;
		this.scriptmanager = scriptmanager;
//		this.hookfactory = hookfactory;
		this.threadstrategy = threadstrategy;

	    // DEPENDS_ON_RULES
		// TODO: peut-être d'autres hooks?
		hooks_entre_scripts = hookfactory.getHooksEntreScriptsReal(gamestate);
	}

	// Appelé par le lanceur
	public void boucleExecution()
	{
		while(!Config.matchDemarre)
			Sleep.sleep(20);
		
		// boucle exécution
		while(true)
		{
			try {
				ArrayList<Decision> decisions = threadstrategy.getDecisions();
				if(decisions == null)
					Sleep.sleep(500);
				else
					executerScript(decisions);
			} catch (UnknownScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FinMatchException e) {
				// la sortie se fait par l'exception FinMatchException
				break;
			}
		}
		
		// DEPENDS_ON_RULES
		// funny action (aucune cette année)
		
	}
	
	// TODO
	/*
	 * En gros, on peut jouer sur 2 paramètres dans le pathfinding
	 * D'abord, "more points". Ca ajoute des points dans la recherche de chemin: c'est plus long mais on a moins de chances d'être bloqué.
	 * Ensuite, "shoot game elements". Quand on est vraiment bloqué, on peut décider de shooter dans les éléments de jeux pour avancer.
	 */
	public void executerScript(ArrayList<Decision> decisions) throws UnknownScriptException, FinMatchException
	{
		for(int id_decision = 0; id_decision < 2; id_decision++)
		{
			Decision decision_actuelle = decisions.get(id_decision);
			Script s = scriptmanager.getScript(decision_actuelle.script_name);

			log.debug("On tente d'exécuter "+decision_actuelle.script_name, this);
			try {
				tryOnce(s, decision_actuelle.version, false);
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
					tryOnce(s, decision_actuelle.version, false);
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
	
	public void tryOnce(Script s, int id_version, boolean dont_avoid_game_element) throws PathfindingException, UnableToMoveException, ScriptException, PathfindingRobotInObstacleException, FinMatchException
	{
		ArrayList<PathfindingNodes> chemin;
		chemin = pathfinding.computePath(gamestate.cloneGameState(), s.point_entree(id_version), dont_avoid_game_element);
		gamestate.robot.set_vitesse(Speed.BETWEEN_SCRIPTS);
		try {
			gamestate.robot.suit_chemin(chemin, hooks_entre_scripts);
			s.agit(id_version, gamestate);	
		} catch (ScriptHookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateConfig() {
//		color = RobotColor.parse(config.get("couleur"));
	}
	
}
