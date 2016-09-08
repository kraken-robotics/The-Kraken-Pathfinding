package pathfinding.astarCourbe;

import memory.Memorizable;
import pathfinding.ChronoGameState;
import pathfinding.astarCourbe.arcs.ArcCourbe;
import pathfinding.astarCourbe.arcs.ArcCourbeClotho;
import utils.Config;

/**
 * Un nœud de l'A* courbe
 * @author pf
 *
 */

public class AStarCourbeNode implements Memorizable
{
	public ChronoGameState state;
	public double g_score;
	public double f_score;
	public AStarCourbeNode came_from;
	public ArcCourbe came_from_arc = new ArcCourbeClotho();
	private int indiceMemoryManager;
	
	public AStarCourbeNode(ChronoGameState state)
	{
		this.state = state;
	}
	
	public void init()
	{
		g_score = Double.MAX_VALUE;
		f_score = Double.MAX_VALUE;
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

	@Override
	public void useConfig(Config config)
	{
		state.updateConfig(config);
	}
	
}
