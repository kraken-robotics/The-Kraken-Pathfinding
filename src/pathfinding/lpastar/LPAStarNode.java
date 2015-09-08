package pathfinding.lpastar;

import java.util.ArrayList;

import pathfinding.GameState;
import permissions.ReadWrite;
import robot.RobotChrono;

/**
 * Noeud de la recherche par LPA*
 * @author pf
 *
 */

public class LPAStarNode
{
	
	private ArrayList<LPAStarNode> predecesseurs;
	private ArrayList<LPAStarNode> successeurs;
	private GameState<RobotChrono,ReadWrite> state;
	
	@Override
	public int hashCode()
	{
		return GameState.getHashLPAStar(state.getReadOnly());
	}
	
}
