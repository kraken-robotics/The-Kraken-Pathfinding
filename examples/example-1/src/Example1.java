/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import kraken.Kraken;
import kraken.exceptions.PathfindingException;
import kraken.pathfinding.astar.AStarCourbe;
import kraken.utils.XY;
import kraken.utils.XYO;


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
		try
		{
			astar.initializeNewSearch(new XYO(0, 0, 0), new XY(1000, 1000));
		}
		catch(PathfindingException e)
		{
			System.out.println(e);
			e.printStackTrace();
		}
	}
}
