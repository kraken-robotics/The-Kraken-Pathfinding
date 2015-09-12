package strategie.astar;

import pathfinding.GameState;
import permissions.ReadWrite;
import robot.RobotChrono;

/**
 * Noeud du AStar
 * @author pf
 *
 */

public class AStarNode {

	public GameState<RobotChrono,ReadWrite> state;
	public int g_score;
	public int f_score;
	public int hash;
	private int indiceMemoryManager;
	
	public void init()
	{
		g_score = Integer.MAX_VALUE;
		f_score = Integer.MAX_VALUE;
	}
	
	public void setIndiceMemoryManager(int indice)
	{
		indiceMemoryManager = indice;
	}

	public int getIndiceMemoryManager()
	{
		return indiceMemoryManager;
	}

	public void copy(AStarNode modified)
	{
		modified.g_score = g_score;
		modified.f_score = f_score;
		modified.hash = hash;
	}

}
