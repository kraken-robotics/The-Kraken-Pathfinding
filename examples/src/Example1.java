/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pfg.graphic.PrintBuffer;
import pfg.graphic.printable.Layer;
import pfg.kraken.Kraken;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;
import pfg.kraken.utils.XY_RW;


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
		 * The list of fixed, permanent obstacles
		 * Obstacles partially outside the search domain and colliding obstacles are OK 
		 */
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new RectangularObstacle(new XY_RW(800,200), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(800,300), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(-800,1200), 100, 200));
		obs.add(new RectangularObstacle(new XY_RW(-1000,300), 500, 500));
		obs.add(new RectangularObstacle(new XY_RW(200,1600), 800, 300));
		obs.add(new RectangularObstacle(new XY_RW(1450,700), 300, 100));
		obs.add(new CircularObstacle(new XY_RW(500,600), 100));
		
		/*
		 * The shape of the robot. It must fit into a rectangular obstacle (for collision check performance).
		 * All the measure are done from the rotation center (which doesn't need to be the geometric center of the rectangle)
		 * 
		 * The parameters are :
		 * - the distance to the front
		 * - the distance to the back
		 * - the distance to the left side
		 * - the distance to the right side
		 * - an tilt angle
		 */
		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110); 
		
		/*
		 * Constructing Kraken
		 * We restrain the search domain to the rectangle -1500 < x < 1500, 0 < y < 2000
		 * You can add the "detailed" profile to display the underneath pathfinder.
		 */
		Kraken kraken = new Kraken(robot, obs, new XY(-1500,0), new XY(1500, 2000), "trajectory", "detailed");
		
		/*
		 * The graphic display (optional)
		 */
		PrintBuffer printBuffer = kraken.getPrintBuffer();
		
		/*
		 * The obstacles are printed
		 */
		for(Obstacle o : obs)
			printBuffer.add(o, Color.BLACK, Layer.MIDDLE.layer);
		printBuffer.refresh();
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
			kraken.initializeNewSearch(new XYO(0, 200, 0), new XY(1000, 1000));
			
			/*
			 * The pathfinder returns a list of ItineraryPoint, which contains all the cinematic information that described follow the path
			 */
			LinkedList<ItineraryPoint> path = kraken.search();
			
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
			printBuffer.refresh();
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
