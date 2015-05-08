package threads;

import java.util.ArrayList;

import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.astar.AStar;
import planification.astar.arc.Decision;
import planification.astar.arc.PathfindingNodes;
import planification.astar.arc.SegmentTrajectoireCourbe;
import planification.astar.arcmanager.StrategyArcManager;
import container.Service;
import exceptions.FinMatchException;
import exceptions.MemoryManagerException;
import exceptions.PathfindingException;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.ScriptAnticipableNames;
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
	private final GameState<RobotReal,ReadOnly> realstate;
	private final GameState<RobotChrono,ReadWrite> chronostate;
	
	private Decision decision = new Decision(new ArrayList<SegmentTrajectoireCourbe>(), ScriptAnticipableNames.SORTIE_ZONE_DEPART, PathfindingNodes.POINT_DEPART);
	private Decision decisionSecours = null;
	private Decision decisionNormale = null;
	private Decision needNewBestAfterThis = null;
	
	public ThreadStrategy(Log log, Config config, AStar<StrategyArcManager, Decision> strategie, GameState<RobotReal,ReadOnly> realstate)
	{
		this.log = log;
		this.strategie = strategie;
		this.realstate = realstate;
		chronostate = GameState.cloneGameState(realstate);
		
		Thread.currentThread().setPriority(4); // TODO priorité thread
		updateConfig();
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de stratégie");
		while(!Config.matchDemarre)
		{
			if(stopThreads)
			{
				log.debug("Stoppage du thread de stratégie");
				return;
			}
			Sleep.sleep(50);
		}
		
		while(!finMatch && !stopThreads)
		{
			try {
				GameState.copy(realstate, chronostate);
				if(needNewBestAfterThis != null)
				{
					ArrayList<Decision> decisions = strategie.computeStrategyAfter(chronostate.getReadOnly(), needNewBestAfterThis, 10000);
					printCurrentStrategy(decisions);
					decision = decisions.get(0);
					needNewBestAfterThis = null; // en cas d'erreur, ce n'est pas mis à null
				}
				decisionSecours = strategie.computeStrategyEmergency(chronostate.getReadOnly(), 10000).get(0);
				decisionNormale = strategie.computeStrategy(chronostate, 10000).get(0);
			} catch (FinMatchException e) {
				break;
			} catch (PathfindingException e) {
				log.critical("Départ dans un obstacle de proximité!");
				e.printStackTrace();
			} catch (MemoryManagerException e) {
				// Imposible
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
		do {
			Sleep.sleep(5);
		} while(decision == null);
		return decision;
	}

	public Decision getEmergencyDecision()
	{
		do {
			Sleep.sleep(5);
		} while(decisionSecours == null);
		log.warning("Stratégie de secours demandée! "+decisionSecours);
		return decisionSecours;
	}

	public Decision getNormalDecision()
	{
		do {
			Sleep.sleep(5);
		} while(decisionNormale  == null);
		log.warning("Stratégie demandée! "+decisionNormale);
		return decisionNormale;
	}

	private void printCurrentStrategy(ArrayList<Decision> decisions)
	{
		String s = new String();
		for(Decision d: decisions)
			s += d.toString()+" ";
		log.debug(s);
	}
	
	@Override
	public void updateConfig()
	{
		log.updateConfig();
		strategie.updateConfig();
		realstate.updateConfig();
	}
}
