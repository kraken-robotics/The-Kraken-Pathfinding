package pathfinding;

import java.util.ArrayList;
import java.util.Vector;

import hook.Hook;
import hook.types.HookFactory;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Decision;
import scripts.Script;
import scripts.ScriptManager;
import strategie.GameState;
import utils.Log;
import utils.Config;
import container.Service;
import enums.PathfindingNodes;
import enums.ScriptNames;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import exceptions.ScriptHookException;
import exceptions.UnknownScriptException;
import exceptions.strategie.ScriptException;

/**
 * Réalise les calculs sur les scripts et les gamestate pour l'AStar.
 * @author pf
 *
 */

public class StrategyArcManager implements Service, ArcManager {

//	private Log log;
	private ScriptManager scriptmanager;
	private AStar<PathfindingArcManager, PathfindingNodes> astar;
	private HookFactory hookfactory;
	
	private ArrayList<Decision> listeDecisions = new ArrayList<Decision>();
	private int iterator;
	private Vector<Long> hashes = new Vector<Long>();
	
	private int dateLimite = 90000;
	
	public StrategyArcManager(Log log, Config config, ScriptManager scriptmanager, GameState<RobotReal> real_gamestate, HookFactory hookfactory, AStar<PathfindingArcManager, PathfindingNodes> astar)
	{
//		this.log = log;
		this.scriptmanager = scriptmanager;
		this.hookfactory = hookfactory;
		this.astar = astar;
		try {
			initPointSortie(real_gamestate.cloneGameState());
		} catch (FinMatchException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reinitIterator(GameState<RobotChrono> gamestate)
	{
		listeDecisions.clear();
		for(ScriptNames s: ScriptNames.values())
		{
			if(s.canIDoIt())
			{
				Script script = scriptmanager.getScript(s);
				for(Integer v: script.meta_version(gamestate))
				{
					// On n'ajoute que les versions qui sont accessibles
					try {
						ArrayList<PathfindingNodes> chemin = astar.computePath(gamestate, script.point_entree(v), true);
						listeDecisions.add(new Decision(chemin, s, v, true));
						try {
							// On ne rajoute la version où on ne shoot pas seulement si le chemin proposé est différent
							ArrayList<PathfindingNodes> chemin2 = astar.computePath(gamestate, script.point_entree(v), false);
							if(!chemin2.equals(chemin))
								listeDecisions.add(new Decision(chemin2, s, v, true));
						} catch (PathfindingException
								| PathfindingRobotInObstacleException
								| FinMatchException e) {
						}
					} catch (PathfindingException
							| PathfindingRobotInObstacleException
							| FinMatchException e) {
					}
				}
			}
		}
		
		iterator = -1;
	}

	@Override
	public boolean hasNext(GameState<RobotChrono> state)
	{
		iterator++;
		return iterator < listeDecisions.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Decision next()
	{
//		log.debug("Prochain voisin: "+listeDecisions.get(iterator).script_name, this);
		return listeDecisions.get(iterator);
	}

	@Override
	public int distanceTo(GameState<RobotChrono> state, Arc arc) throws FinMatchException, UnknownScriptException, ScriptException
	{
		Decision d = (Decision)arc;
		Script s = scriptmanager.getScript(d.script_name);
		int old_temps = (int)state.robot.getTempsDepuisDebutMatch();
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScriptsChrono(state, dateLimite);
		state.robot.suit_chemin(d.chemin, hooks_table);
		try {
			s.agit(d.version, state);
			state.robot.setPositionPathfinding(s.point_sortie(d.version));
		} catch (ScriptHookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		state.robot.setPositionPathfinding(s.point_sortie(d.version));
		int new_temps = (int)state.robot.getTempsDepuisDebutMatch();
		return new_temps - old_temps;
	}

	@Override
	public int heuristicCost(GameState<RobotChrono> state)
	{
		return 0;
	}

	@Override
	public int getHash(GameState<RobotChrono> state)
	{
		long hash = state.getHash();
		int indice = hashes.indexOf(hash);
		if(indice == -1)
		{
			hashes.add(hash);
			return hashes.size()-1;
		}
		else
			return indice;
	}

	@Override
	public void updateConfig()
	{
	}

	public void reinitHashes()
	{
		hashes.clear();
	}
	
	@Override
	public boolean isArrive(int hash) {
		return false;
	}

	@Override
	public int getNoteReconstruct(int hash) {
		return (int)(hashes.get(hash)&511); // la composante "note" du hash (cf gamestate.getHash())
	}
	
	public void initPointSortie(GameState<RobotChrono> gamestate)
	{
		for(ScriptNames s: ScriptNames.values())
		{
			if(s.canIDoIt())
			{
				Script script = scriptmanager.getScript(s);
				for(Integer v: script.meta_version(gamestate))
				{
					gamestate.robot.setPositionPathfinding(script.point_entree(v));
					try {
						script.agit(v, gamestate);
					} catch (FinMatchException | ScriptException | ScriptHookException e) {
						e.printStackTrace();
					}
					script.setPointSortie(v, gamestate.robot.getPosition());
				}
			}
		}		
	}

	public void executeDecision(GameState<RobotChrono> state, Decision decision)
	{
		Script s;
		try {
			// Normalement, quand executeDecision est appelé, on est déjà au point d'entrée
			s = scriptmanager.getScript(decision.script_name);
			state.robot.setPositionPathfinding(s.point_entree(decision.version));
			s.agit(decision.version, state);
			state.robot.setPositionPathfinding(s.point_sortie(decision.version));
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (FinMatchException e) {
			e.printStackTrace();
		} catch (ScriptHookException e) {
			e.printStackTrace();
		}
	}
	
	public void setDateLimite(int dateLimite)
	{
		this.dateLimite = dateLimite;
	}
	
}
