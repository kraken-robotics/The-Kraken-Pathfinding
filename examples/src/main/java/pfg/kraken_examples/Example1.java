/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */
package pfg.kraken_examples;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import pfg.graphic.DebugTool;
import pfg.kraken.Kraken;
import pfg.kraken.KrakenParameters;
import pfg.kraken.SearchParameters;
import pfg.kraken.display.Display;
import pfg.kraken.display.Layer;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.ItineraryPoint;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XYO;


/**
 * Minimalist example code
 * @author pf
 *
 */

public class Example1
{

	public static void main(String[] args)
	{
		/*
		 * Units information :
		 * the length are measured in mm
		 * the curvature is measured in m^-1
		 */
				
		/*
		 * The list of fixed, permanent obstacles
		 * Obstacles partially outside the search domain and colliding obstacles are OK.
		 * The obstacle are either rectangular or circular.
		 */
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new RectangularObstacle(new XY(800,200), 200, 200));
		obs.add(new RectangularObstacle(new XY(800,300), 200, 200));
		obs.add(new RectangularObstacle(new XY(-800,1200), 100, 200));
		obs.add(new RectangularObstacle(new XY(-1000,300), 500, 500));
		obs.add(new RectangularObstacle(new XY(200,1600), 800, 300));
		obs.add(new RectangularObstacle(new XY(1450,700), 300, 100));
		obs.add(new CircularObstacle(new XY(500,600), 100));
		
		/*
		 * The shape of the robot. It must fit into a rectangular obstacle (for collision check performance).
		 * The itinary points are given for the rotation center (which doesn't need to be the geometric center of the rectangle)
		 * 
		 * The parameters are :
		 * - the distance to the front
		 * - the distance to the back
		 * - the distance to the left side
		 * - the distance to the right side
		 */
		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110); 
		
		/*
		 * Create the graphical display
		 */
		DebugTool debug = DebugTool.getDebugTool(new XY(0,1000), new XY(0, 1000), null, "kraken-examples.conf", "trajectory");
		Display display = debug.getDisplay();
		for(Obstacle o : obs)
			display.addPrintable(o, Color.BLACK, Layer.MIDDLE.layer);
		
		/*
		 * Constructing Kraken
		 * We restrain the search domain to the rectangle -1500 < x < 1500, 0 < y < 2000
		 * You can uncomment the "detailed" profile to display the underneath pathfinder.
		 */
		KrakenParameters kp = new KrakenParameters(robot, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "trajectory"/*, "detailed"*/);
		kp.setFixedObstacles(obs);
		kp.setDisplay(display);
		Kraken kraken = new Kraken(kp);

		try
		{
			/*
			 * The pathfinding is split in two steps :
			 * - the initialization
			 * - the actual path searching
			 */
			
			/*
			 * We search a new path from the point (0,0) with orientation 0 to the point (1000, 1000).
			 * The orientation is the classical trigonometric orientation : 0 to the right, pi/2 up, etc.
			 */
			kraken.initializeNewSearch(new SearchParameters(new XYO(0, 200, 0), new XY(1000, 1000)));
			
			/*
			 * The pathfinder returns a list of ItineraryPoint, which contains all the cinematic information describing the path
			 */
			List<ItineraryPoint> path = kraken.search();
			
			/*
			 * The trajectory points are printed into the console
			 */
			System.out.println("Here is the trajectory :");
			for(ItineraryPoint p : path)
			{
				display.addPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}

			/*
			 * Print some statistics of the Kraken paths
			 */
			System.out.println(kraken.getTentaclesStatistics());
			
			/*
			 * Refresh the window frame.
			 */
			display.refresh();
		}
		catch(PathfindingException e)
		{
			/*
			 * This exception is thrown when no path is found
			 */
			e.printStackTrace();
		}
	}
}
