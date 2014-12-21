package threads;

import java.util.ArrayList;

import pathfinding.AStar;
import pathfinding.StrategyArcManager;
import container.Service;
import enums.ScriptNames;
import enums.PathfindingNodes;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Decision;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Sleep;

/**
 * Effectue les calculs de stratégie
 * @author pf
 *
 */

public class ThreadStrategy extends AbstractThread implements Service
{

	private Log log;
	private AStar<StrategyArcManager, Decision> strategie;
	private GameState<RobotReal> realstate;
	private GameState<RobotChrono> chronostate;
	
	private Decision decision = new Decision(new ArrayList<PathfindingNodes>(), ScriptNames.SortieZoneDepart, 0, false);
	private Decision decisionSecours = null;
	private Decision needNewBestAfterThis = null;
	
	public ThreadStrategy(Log log, Config config, AStar<StrategyArcManager, Decision> strategie, GameState<RobotReal> realstate)
	{
		this.log = log;
		this.strategie = strategie;
		this.realstate = realstate;
		try {
			chronostate = realstate.cloneGameState();
		} catch (FinMatchException e) {
			// Impossible
			e.printStackTrace();
		}
		
		Thread.currentThread().setPriority(4); // TODO
		updateConfig();
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de stratégie", this);
		while(!Config.matchDemarre)
		{
			if(stopThreads)
			{
				log.debug("Stoppage du thread de stratégie", this);
				return;
			}
			Sleep.sleep(50);
		}
		
		while(!finMatch && !stopThreads)
		{
			try {
				if(needNewBestAfterThis != null)
				{
					realstate.copy(chronostate);
					decision = strategie.computeStrategyAfter(chronostate, needNewBestAfterThis).get(0);
					needNewBestAfterThis = null; // en cas d'erreur, ce n'est pas mis à null
				}
				realstate.copy(chronostate);
				decisionSecours = strategie.computeStrategyEmergency(chronostate).get(0);
			} catch (FinMatchException e) {
				break;
			} catch (PathfindingException e) {
				log.critical("Départ dans un obstacle de proximité!", this);
				e.printStackTrace();
			}
		}
	}

	public void computeBestDecisionAfter(Decision d)
	{
		needNewBestAfterThis = d;
	}
	
	public Decision getBestDecision()
	{
		return decision;
	}

	public Decision getEmergencyDecision()
	{
		return decisionSecours;
	}

	
	@Override
	public void updateConfig()
	{}
}
