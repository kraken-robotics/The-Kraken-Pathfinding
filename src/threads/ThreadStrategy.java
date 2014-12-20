package threads;

import java.util.ArrayList;

import pathfinding.AStar;
import pathfinding.StrategyArcManager;
import container.Service;
import exceptions.FinMatchException;
import robot.RobotReal;
import scripts.Decision;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Sleep;

public class ThreadStrategy extends AbstractThread implements Service
{

	private Log log;
	private AStar<StrategyArcManager, Decision> strategie;
	private GameState<RobotReal> realstate;
	
	private ArrayList<Decision> decisions = null;
	private ArrayList<Decision> decisionsSecours = null;
	
	public ThreadStrategy(Log log, Config config, AStar<StrategyArcManager, Decision> strategie, GameState<RobotReal> realstate)
	{
		this.log = log;
		this.strategie = strategie;
		this.realstate = realstate;
	
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
		
		while(!finMatch)
		{
			try {
				decisions = strategie.computeStrategy(realstate, false);
				decisionsSecours = strategie.computeStrategy(realstate, true);
			} catch (FinMatchException e) {
				stopAllThread();
			}
			// Tant qu'on n'a pas besoin d'une nouvelle décision
			while(decisions != null)
				Sleep.sleep(50);			
		}
	}

	public ArrayList<Decision> getDecisions()
	{
		synchronized(decisions)
		{
			ArrayList<Decision> tmp = decisions;
			decisions = null;
			return tmp;
		}
	}

	@Override
	public void updateConfig() {
	}
}
