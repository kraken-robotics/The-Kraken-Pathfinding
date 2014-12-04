package strategie;

import hook.Hook;
import hook.types.HookFactory;

import java.util.ArrayList;

import container.Service;
import pathfinding.Pathfinding;
import enums.PathfindingNodes;
import enums.ScriptNames;
import enums.Speed;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import exceptions.UnknownScriptException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.strategie.ScriptException;
import robot.RobotReal;
import scripts.Decision;
import scripts.Script;
import scripts.ScriptManager;
import utils.Config;
import utils.Log;
import utils.Sleep;

public class Execution implements Service {

	private GameState<RobotReal> gamestate;
	private Log log;
//	private Config config;
	private ScriptManager scriptmanager;
	private Pathfinding pathfinding;
//	private HookFactory hookfactory;
	
	private ArrayList<Hook> hooks_entre_scripts;
	
	public Execution(Log log, Config config, Pathfinding pathfinding, GameState<RobotReal> gamestate, ScriptManager scriptmanager, HookFactory hookfactory)
	{
		this.log = log;
//		this.config = config;
		this.pathfinding = pathfinding;
		this.gamestate = gamestate;
		this.scriptmanager = scriptmanager;
//		this.hookfactory = hookfactory;
		
	    // DEPENDS_ON_RULES
		// TODO: hook qui renverse les plots en passant
		hooks_entre_scripts = new ArrayList<Hook>();
	}

	// Appelé par le lanceur
	public void boucleExecution()
	{
		while(!Config.matchDemarre)
			Sleep.sleep(100);
		
		// la sortie se fait par l'exception FinMatchException
		while(true)
		{
		}
		
		// boucle exécution
		
		// affichage stat
		
	}
	
	public void executerScript(ArrayList<Decision> decisions, ScriptNames scriptSecours, int id_version) throws UnknownScriptException, FinMatchException
	{
		for(int id_decision = 0; id_decision < decisions.size(); id_decision++)
		{
			Decision decision_actuelle = decisions.get(id_decision);
			Script s = scriptmanager.getScript(decision_actuelle.script_name);

			log.debug("On tente d'exécuter "+decision_actuelle.script_name, this);
			try {
				tryOnce(s, decision_actuelle.id_version);
			} catch (PathfindingException e) {
				log.critical("Abandon: pas de chemin pour "+decision_actuelle.script_name, this);
				// Problème de pathfinding: pas de chemin. On change de script.
				continue;
			} catch (ScriptException e) {
				log.critical("Abandon: erreur de script", this);
				// On a eu un problème au milieu du script. On change de script.
				// Note: le script lui-même possède des procédures de relance.
				continue;
			} catch (UnableToMoveException e) {
				// On a rencontré l'ennemi en chemin. On retente avec un autre chemin.
				try {
					log.debug("On réessaye d'exécuter "+decision_actuelle.script_name, this);
					tryOnce(s, decision_actuelle.id_version);
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
	
	public void tryOnce(Script s, int id_version) throws PathfindingException, UnableToMoveException, ScriptException, PathfindingRobotInObstacleException, FinMatchException
	{
		ArrayList<PathfindingNodes> chemin;
		chemin = pathfinding.computePath(gamestate.robot.getPosition(), s.point_entree(id_version), gamestate.gridspace);
		gamestate.robot.set_vitesse(Speed.BETWEEN_SCRIPTS);
		gamestate.robot.suit_chemin(chemin, hooks_entre_scripts);
		s.agit(id_version, gamestate, false);	
	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
	
}
