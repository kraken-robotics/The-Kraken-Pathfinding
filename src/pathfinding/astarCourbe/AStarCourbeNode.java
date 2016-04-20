package pathfinding.astarCourbe;

import pathfinding.ChronoGameState;

/**
 * Un nœud de l'A* courbe
 * @author pf
 *
 */

public class AStarCourbeNode {

	public ChronoGameState state;
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

	/**
	 * Cette copy n'est utilisée qu'à une seule occasion, quand on reconstruit partiellement le chemin
	 * @param modified
	 */
	public void copyReconstruct(AStarCourbeNode modified)
	{
		modified.g_score = g_score;
		modified.f_score = f_score;
		state.copyAStarCourbe(modified.state);
		modified.came_from = null;
	}
	
}
