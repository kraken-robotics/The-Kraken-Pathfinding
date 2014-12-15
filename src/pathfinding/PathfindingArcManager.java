package pathfinding;

import container.Service;
import robot.RobotChrono;
import strategie.GameState;
import utils.Log;
import utils.Config;
import enums.PathfindingNodes;
import exceptions.FinMatchException;

 public class PathfindingArcManager implements ArcManager, Service {

	private int iterator, id_node_iterator;
	private int date_precision_diminue;
	private Config config;
	private Log log;
	
	public PathfindingArcManager(Log log, Config config)
	{
		this.log = log;
		this.config = config;
		updateConfig();
	}
	
	@Override
	public double distanceTo(GameState<RobotChrono> state, Arc arc)
	{
		double out = state.robot.getPositionPathfinding().distanceTo((PathfindingNodes)arc);
		try {
			state.robot.va_au_point_pathfinding((PathfindingNodes)arc, null);
			return out;
		} catch (FinMatchException e) {
			return Double.MAX_VALUE;
		}
	}

	@Override
	public double heuristicCost(GameState<RobotChrono> state1, GameState<RobotChrono> state2)
	{
		return state1.robot.getPositionPathfinding().distanceTo(state2.robot.getPositionPathfinding());
	}

	@Override
	public double getHash(GameState<RobotChrono> state) {
		return state.robot.getPositionPathfinding().ordinal();
	}

    @Override
    public PathfindingNodes next()
    {
    	return PathfindingNodes.values()[iterator];
    }
    
    @Override
    public boolean hasNext(GameState<RobotChrono> state)
    {
    	// La précision diminue après la date.
    	boolean emergency = state.robot.getTempsDepuisDebutMatch() < date_precision_diminue;
    	if(!emergency)
    		log.debug("Précision diminuée", this);
    	do {
    		iterator++;
    		// Ce point n'est pas bon si:
    		// c'est le noeud appelant (un noeud n'est pas son propre voisin)
    		// c'est un noeud d'urgence et nous ne sommes pas en mode urgence
    		// le noeud appelant et ce noeud ne peuvent être joints par une ligne droite
    	} while(iterator < PathfindingNodes.values().length
    			&& (iterator == id_node_iterator
    			|| (!emergency && PathfindingNodes.values()[iterator].is_an_emergency_point())
    			|| !state.gridspace.isTraversable(PathfindingNodes.values()[id_node_iterator], PathfindingNodes.values()[iterator])));
    	return iterator != PathfindingNodes.values().length;
    }
    
    @Override
    public void reinitIterator(GameState<RobotChrono> gamestate)
    {
    	id_node_iterator = gamestate.robot.getPositionPathfinding().ordinal();
    	iterator = -1;
    }

	@Override
	public void updateConfig() {
		date_precision_diminue = 1000*Integer.parseInt(config.get("date_precision_diminue"));	
	}

}
