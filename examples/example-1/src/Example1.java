/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import java.util.LinkedList;
import pfg.kraken.Kraken;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;


/**
 * Minimalist example code
 * @author pf
 *
 */

public class Example1
{

	public static void main(String[] args)
	{
		Kraken kraken = Kraken.getKraken(null);
		TentacularAStar astar = kraken.getAStar();
		try
		{
			astar.initializeNewSearch(new XYO(0, 0, 0), new XY(1000, 1000));
			LinkedList<ItineraryPoint> path = astar.process();
			for(ItineraryPoint p : path)
				System.out.println(p);
		}
		catch(PathfindingException e)
		{
			System.out.println(e);
			e.printStackTrace();
		}
	}
}
