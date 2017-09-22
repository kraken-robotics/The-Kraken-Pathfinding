/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pfg.graphic.PrintBuffer;
import pfg.graphic.WindowFrame;
import pfg.graphic.printable.Layer;
import pfg.kraken.Kraken;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;


/**
 * An example with the obstacles from the Eurobot 2017 robotic competition
 * @author pf
 *
 */

public class ExampleEurobot2017
{

	public static void main(String[] args)
	{
		/*
		 * The list of fixed, permanent obstacles
		 * Obstacles partially outside the search domain and colliding obstacles are OK 
		 */
		List<Obstacle> obs = new ArrayList<Obstacle>();
		
		obs.add(new RectangularObstacle(new XY(-(1140 - 350 / 2), 2000 - 360 / 2), 350, 360));
		obs.add(new RectangularObstacle(new XY(-(1500 - 360 / 2), 2000 - 360 / 2), 360, 360));
		obs.add(new RectangularObstacle(new XY(-(790 - 360 / 2), 2000 - 360 / 2), 360, 360));
		
		obs.add(new RectangularObstacle(new XY(1140 - 350 / 2, 2000 - 360 / 2), 350, 360));
		obs.add(new RectangularObstacle(new XY(1500 - 360 / 2, 2000 - 360 / 2), 360, 360	));
		obs.add(new RectangularObstacle(new XY(790 - 360 / 2, 2000 - 360 / 2), 360, 360));

		obs.add(new RectangularObstacle(new XY(1500 - 710 / 2, 2000 - 360 - 11), 710, 22));
		obs.add(new RectangularObstacle(new XY(-1500 + 710 / 2, 2000 - 360 - 11), 710, 22));

		obs.add(new RectangularObstacle(new XY(54 - 1500, 1075), 108, 494));
		obs.add(new RectangularObstacle(new XY(1500 - 54, 1075), 108, 494));

		obs.add(new CircularObstacle(new XY(0, 0), 200));

		obs.add(new RectangularObstacle(new XY(0, 500).rotateNewVector(-Math.PI / 4, new XY(0, 0)), 140, 600, -Math.PI / 4));
		obs.add(new RectangularObstacle(new XY(0, 500), 140, 600));
		obs.add(new RectangularObstacle(new XY(0, 500).rotateNewVector(Math.PI / 4, new XY(0, 0)), 140, 600, Math.PI / 4));

		obs.add(new CircularObstacle(new XY(-1500, 0), 540));
		obs.add(new CircularObstacle(new XY(1500, 0), 540));

		obs.add(new CircularObstacle(new XY(-350, 1960), 40));
		obs.add(new CircularObstacle(new XY(350, 1960), 40));

		obs.add(new CircularObstacle(new XY(-1460, 650), 40));
		obs.add(new CircularObstacle(new XY(1460, 650), 40));
		
		obs.add(new CircularObstacle(new XY(650 - 1500, 2000 - 555), 125));
		obs.add(new CircularObstacle(new XY(1500 - 650, 2000 - 555), 125));

		obs.add(new CircularObstacle(new XY(1070 - 1500, 2000 - 1870), 125));
		obs.add(new CircularObstacle(new XY(1500 - 1070, 2000 - 1870), 125));

		obs.add(new CircularObstacle(new XY(200 - 1500, 1400), 32));
		obs.add(new CircularObstacle(new XY(1500 - 200, 1400), 32));
		obs.add(new CircularObstacle(new XY(1000 - 1500, 1400), 32));
		obs.add(new CircularObstacle(new XY(1500 - 1000, 1400), 32));
		obs.add(new CircularObstacle(new XY(500 - 1500, 900), 32));
		obs.add(new CircularObstacle(new XY(1500 - 500, 900), 32));
		obs.add(new CircularObstacle(new XY(900 - 1500, 600), 32));
		obs.add(new CircularObstacle(new XY(1500 - 900, 600), 32));
		obs.add(new CircularObstacle(new XY(800 - 1500, 150), 32));
		obs.add(new CircularObstacle(new XY(1500 - 800, 150), 32));

		
		/*
		 * Getting Kraken (a singleton).
		 * We restrain the search domain to the rectangle -1500 < x < 1500, 0 < y < 2000
		 * You can add the "detailed" profile to display the underneath pathfinder.
		 */
		Kraken kraken = Kraken.getKraken(obs, new XY(-1500,0), new XY(1500, 2000), "trajectory", "detailed");
		
		/*
		 * The graphic display (optional)
		 */
		WindowFrame frame = kraken.getWindowFrame();
		PrintBuffer printBuffer = kraken.getPrintBuffer();
		
		/*
		 * The obstacles are printed
		 */
		for(Obstacle o : obs)
			printBuffer.add(o, Color.BLACK, Layer.MIDDLE.layer);
		frame.refresh();
		
		/*
		 * The pathfinder itself.
		 */
		TentacularAStar astar = kraken.getAStar();
		try
		{
			/*
			 * The pathfinding is split in two steps :
			 * - the initialization
			 * - the actual path searching
			 */
			
			/*
			 * We search a new path from the point (0,0) with orientation 0 to the point (1000, 1000).
			 */
			astar.initializeNewSearch(new XYO(-850, 400, Math.PI/2), new XY(850, 400));
			
			/*
			 * The pathfinder returns a list of ItineraryPoint, which contains all the cinematic information that described follow the path
			 */
			LinkedList<ItineraryPoint> path = astar.search();
			
			/*
			 * For this example, we just print the trajectory points
			 */
			for(ItineraryPoint p : path)
			{
				printBuffer.add(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			
			/*
			 * Refresh the window frame.
			 */
			frame.refresh();
		}
		catch(PathfindingException e)
		{
			/*
			 * This exception is thrown when no path is found
			 */
			e.printStackTrace();
		}
		finally 
		{
			/*
			 * You are expected to destroy Kraken properly
			 */
			kraken.destructor();
		}
	}
}
