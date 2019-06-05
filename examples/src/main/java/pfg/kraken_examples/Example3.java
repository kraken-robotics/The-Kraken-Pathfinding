/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */
package pfg.kraken_examples;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import pfg.graphic.DebugTool;
import pfg.graphic.printable.Layer;
import pfg.kraken.Kraken;
import pfg.kraken.KrakenParameters;
import pfg.kraken.SearchParameters;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.ItineraryPoint;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XYO;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.display.Display;


/**
 * Advanced options
 * @author pf
 *
 */

public class Example3
{

	public static void main(String[] args)
	{
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new RectangularObstacle(new XY(800,200), 200, 200));
		obs.add(new RectangularObstacle(new XY(800,300), 200, 200));
		obs.add(new RectangularObstacle(new XY(-800,1200), 100, 200));
		obs.add(new RectangularObstacle(new XY(-1000,300), 500, 500));
		obs.add(new RectangularObstacle(new XY(200,1600), 800, 300));
		obs.add(new RectangularObstacle(new XY(1450,700), 300, 100));
		obs.add(new CircularObstacle(new XY(500,600), 100));

		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110); 

		DebugTool debug = DebugTool.getDebugTool(new XY(0,1000), new XY(0, 1000), null, "kraken-examples.conf", "trajectory");
		Display display = debug.getDisplay();
		for(Obstacle o : obs)
			display.addPrintable(o, Color.BLACK, Layer.MIDDLE.layer);
		
		KrakenParameters kp = new KrakenParameters(robot, obs, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "trajectory"/*, "detailed"*/);
		kp.setDisplay(display);
		Kraken kraken = new Kraken(kp);
		/*
		 * You can perfectly use two instances of Kraken (for example if two robots have different size)
		 */
		RectangularObstacle secondRobot = new RectangularObstacle(20, 20, 20, 20); 
		KrakenParameters kp2 = new KrakenParameters(secondRobot, obs, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "trajectory"/*, "detailed"*/);
		kp2.setDisplay(display);
		Kraken krakenSecondRobot = new Kraken(kp2);

		try
		{
			/*
			 * You can force the moving direction. By default, the pathfinding can move the vehicle backward and forward.
			 * In example 1, the path makes the vehicle going backward. Let's force a forward motion.
			 */
			SearchParameters sp = new SearchParameters(new XYO(-100, 200, 0), new XY(1000, 1000));
			sp.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);
			sp.setMaxSpeed(1.);
			kraken.initializeNewSearch(sp);
			
			List<ItineraryPoint> path = kraken.search();
			
			System.out.println("\nBlack robot search :");
			for(ItineraryPoint p : path)
			{
				display.addPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			
			display.refresh();
			
			/*
			 * The second robot is smaller, so it can take a different path (blue trajectory)
			 * Furthermore, this path should be taken fast.
			 */
			sp.setMaxSpeed(3.);
			krakenSecondRobot.initializeNewSearch(sp);
			path = krakenSecondRobot.search();
			
			System.out.println("\nBlue robot search :");
			for(ItineraryPoint p : path)
			{
				display.addPrintable(p, Color.BLUE, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			
			display.refresh();

		}
		catch(PathfindingException e)
		{
			e.printStackTrace();
		}
	}
}
