/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package pathfinding.astar;

import memory.Memorizable;
import pathfinding.ChronoGameState;
import pathfinding.astar.arcs.ArcCourbe;
import pathfinding.astar.arcs.ArcCourbeStatique;
import pathfinding.astar.arcs.ArcCourbeDynamique;
import robot.RobotReal;

/**
 * Un nœud de l'A* courbe
 * @author pf
 *
 */

public class AStarCourbeNode implements Memorizable
{
	public ChronoGameState state;
	public double g_score; // distance du point de départ à ce point
	public double f_score; // g_score + heuristique = meilleure distance qu'on peut espérer avec ce point
	public AStarCourbeNode parent;
	public final ArcCourbeStatique cameFromArc;
	public ArcCourbeDynamique cameFromArcCubique = null;
	private int indiceMemoryManager;
	
	public AStarCourbeNode(ChronoGameState state, RobotReal r)
	{
		cameFromArc = new ArcCourbeStatique(r);
		this.state = state;
	}
	
	public ArcCourbe getArc()
	{
		if(parent == null)
			return null;
		if(cameFromArcCubique != null)
			return cameFromArcCubique;
		return cameFromArc;
	}
	
	public void init()
	{
		g_score = Double.MAX_VALUE;
		f_score = Double.MAX_VALUE;
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o.hashCode() == hashCode();
	}
	
	@Override
	public int hashCode()
	{
		return state.robot.getCinematique().hashCode();
	}
	
	@Override
	public void setIndiceMemoryManager(int indice)
	{
		indiceMemoryManager = indice;
	}

	@Override
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
		modified.cameFromArcCubique = null;
		modified.g_score = g_score;
		modified.f_score = f_score;
		state.copyAStarCourbe(modified.state);
		modified.parent = null;
	}

}