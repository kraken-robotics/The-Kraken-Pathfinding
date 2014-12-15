package constSearch;

import pathfinding.AStar;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import container.Container;
import enums.PathfindingNodes;
import enums.ServiceNames;

// TODO: réparer ce code

public class PathfindingConst {

    public static void main(String[] args)
    {
    	try {
    		Container container = new Container();
			AStar pathfinding = (AStar)container.getService(ServiceNames.A_STAR);
			Log log = (Log)container.getService(ServiceNames.LOG);
			@SuppressWarnings("unchecked")
			GameState<RobotReal> state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
			GameState<RobotChrono> state_chrono = state.cloneGameState();

			for(int i = 1; i < 10; i++)
			{
				pathfinding.setHeuristiqueCoeff(i);
				int compteur = 0;
				for(PathfindingNodes m: PathfindingNodes.values())
					for(PathfindingNodes n: PathfindingNodes.values())
					{
						state_chrono.robot.setPositionPathfinding(m);
						pathfinding.computePath(state_chrono, n, true);
//						compteur += pathfinding.getCompteur();
					}
				log.appel_static(i+": "+compteur);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
