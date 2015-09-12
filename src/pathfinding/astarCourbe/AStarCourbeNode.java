package pathfinding.astarCourbe;

import pathfinding.GameState;
import permissions.ReadWrite;
import robot.RobotChrono;

public class AStarCourbeNode {

	public GameState<RobotChrono,ReadWrite> state;
	public int g_score;
	public int f_score;
	public AStarCourbeNode came_from;
	public ArcCourbe came_from_arc = new ArcCourbe();
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

	public void copy(AStarCourbeNode modified)
	{
		modified.g_score = g_score;
		modified.f_score = f_score;
	}
	
}
