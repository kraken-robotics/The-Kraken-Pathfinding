/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import kraken.Kraken;
import kraken.pathfinding.astar.AStarCourbe;


/**
 * Minimalist example code
 * @author pf
 *
 */

public class Example1
{

	public static void main(String[] args)
	{
		Kraken kraken = new Kraken(null);
		AStarCourbe astar = kraken.getAStar();
	}
}
