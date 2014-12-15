package pathfinding;

import java.util.ArrayList;

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
import enums.ScriptNames;
import exceptions.FinMatchException;
import exceptions.UnknownScriptException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;

public class StrategyArcManager implements Service, ArcManager {

	private Log log;
	private ScriptManager scriptmanager;

	private ArrayList<Decision> listeDecisions = new ArrayList<Decision>();
	private int iterator;
	
	public StrategyArcManager(Log log, Config config, ScriptManager scriptmanager, GameState<RobotReal> real_gamestate, HookFactory hookfactory)
	{
		this.log = log;
		this.scriptmanager = scriptmanager;
	}

	@Override
	public void reinitIterator(GameState<RobotChrono> gamestate)
	{
		listeDecisions.clear();
		for(ScriptNames s: ScriptNames.values())
		{
			if(s.canIDoIt())
			{
				try {
					for(Integer v: scriptmanager.getScript(s).meta_version(gamestate))
					{
						listeDecisions.add(new Decision(s, v, true));
						listeDecisions.add(new Decision(s, v, false));
					}
				} catch (UnknownScriptException e) {
					log.warning("Script inconnu: "+s, this);
					// Ne devrait jamais arriver
					e.printStackTrace();
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

	@Override
	public Arc next()
	{
		return listeDecisions.get(iterator);
	}

	@Override
	public double distanceTo(GameState<RobotChrono> state, Arc arc) {
		Decision d = (Decision)arc;
		try {
			Script s = scriptmanager.getScript(d.script_name);
			try {
				int old_points = state.robot.getPointsObtenus();
				long old_temps = state.robot.getTempsDepuisDebutMatch();
				s.execute(d.meta_version, state);
				int new_points = state.robot.getPointsObtenus();
				long new_temps = state.robot.getTempsDepuisDebutMatch();
				return -((double)(new_points - old_points))/((double)(new_temps - old_temps));
				
				// On renvoie une valeur négative, car le A* minimise la distance.
				// En minimisant l'opposé du nombre de points qu'on fait,
				// on maximise le nombre de points qu'on fait.
			} catch (UnableToMoveException e) {
				e.printStackTrace();
			} catch (SerialConnexionException e) {
				e.printStackTrace();
			} catch (FinMatchException e) {
				// C'est normal et probable, donc pas de printStackTrace
			}
		} catch (UnknownScriptException e) {
			log.warning("Script inconnu: "+d.script_name, this);
			// Ne devrait jamais arriver
			e.printStackTrace();
		}
		return Double.MAX_VALUE;
	}

	@Override
	public double heuristicCost(GameState<RobotChrono> state1,
			GameState<RobotChrono> state2)
	{
		int points1 = state1.robot.getPointsObtenus();
		long temps1 = state1.robot.getTempsDepuisDebutMatch();
		int points2 = state2.robot.getPointsObtenus();
		long temps2 = state2.robot.getTempsDepuisDebutMatch();
		return -((double)(points2 - points1))/((double)(temps2 - temps1));
	}

	@Override
	public double getHash(GameState<RobotChrono> state)
	{
		return ((double)state.robot.getPointsObtenus())/((double)state.robot.getTempsDepuisDebutMatch());
	}

	@Override
	public void updateConfig()
	{
	}

}
