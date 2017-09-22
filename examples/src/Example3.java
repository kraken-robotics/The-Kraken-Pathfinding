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
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;
import pfg.kraken.utils.XY_RW;


/**
 * Advanced options
 * @author pf
 *
 */

public class Example3
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

		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110); 

		Kraken kraken = new Kraken(robot, obs, new XY(-1500,0), new XY(1500, 2000), "trajectory", "detailed");
		PrintBuffer printBuffer = kraken.getPrintBuffer();
		for(Obstacle o : obs)
			printBuffer.add(o, Color.BLACK, Layer.MIDDLE.layer);
		printBuffer.refresh();

		RectangularObstacle secondRobot = new RectangularObstacle(20, 20, 20, 20); 
		Kraken krakenSecondRobot = new Kraken(secondRobot, obs, new XY(-1500,0), new XY(1500, 2000), "trajectory", "detailed");

		try
		{
			/*
			 * You can force the direction. By default, the pathfinding can move the vehicle backward and forward.
			 * In example 1, the path makes the vehicle going backward. Let's force a forward motion.
			 */
			kraken.initializeNewSearch(new XYO(0, 200, 0), new XY(1000, 1000), DirectionStrategy.FORCE_FORWARD_MOTION);
			
			LinkedList<ItineraryPoint> path = kraken.search();
			
			for(ItineraryPoint p : path)
			{
				printBuffer.add(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			
			printBuffer.refresh();
			
			krakenSecondRobot.initializeNewSearch(new XYO(0, 800, 0), new XY(1000, 1000), DirectionStrategy.FORCE_FORWARD_MOTION);
			path = krakenSecondRobot.search();
			
			for(ItineraryPoint p : path)
			{
				printBuffer.add(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			
			printBuffer.refresh();

		}
		catch(PathfindingException e)
		{
			e.printStackTrace();
		}
		finally 
		{
			kraken.destructor();
		}
	}
}
