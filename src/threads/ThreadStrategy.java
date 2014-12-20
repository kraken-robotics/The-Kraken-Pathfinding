package threads;

import java.util.ArrayList;

import pathfinding.AStar;
import pathfinding.StrategyArcManager;
import container.Service;
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
	
	private ArrayList<Decision> decisions = null;
	private ArrayList<Decision> decisionsSecours = null;
	
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
				realstate.copy(chronostate);
				decisionsSecours = strategie.computeStrategyEmergency(chronostate);
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
		try {
			realstate.copy(chronostate);
			decisions = strategie.computeStrategyAfter(chronostate, d);
		} catch (FinMatchException e) {
			e.printStackTrace();
		} catch (PathfindingException e) {
			log.critical("Départ dans un obstacle de proximité!", this);
			e.printStackTrace();
		}
	}
	
	public Decision getBestDecision()
	{
		Decision out = decisions.get(0);
		return out;
	}

	public Decision getEmergencyDecision()
	{
		// Il y a un problème; il faut renouveler également bestDecision
		Decision out = decisionsSecours.get(0);
		return out;
	}

	
	@Override
	public void updateConfig() {
	}
}
