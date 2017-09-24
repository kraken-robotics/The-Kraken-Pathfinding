/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import pfg.graphic.PrintBuffer;
import pfg.graphic.printable.Layer;
import pfg.kraken.Kraken;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DefaultDynamicObstacles;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;
import pfg.kraken.utils.XY_RW;


/**
 * Having fun with dynamic obstacles
 * @author pf
 *
 */

public class Example2
{

	public static void main(String[] args)
	{
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new RectangularObstacle(new XY_RW(800,200), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(800,300), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(-800,1200), 100, 200));
		obs.add(new RectangularObstacle(new XY_RW(-1000,300), 500, 500));
		obs.add(new RectangularObstacle(new XY_RW(200,1600), 800, 300));
		obs.add(new RectangularObstacle(new XY_RW(1450,700), 300, 100));
		obs.add(new CircularObstacle(new XY_RW(500,600), 100));
		
		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110); 

		/*
		 * The list of dynamic obstacles.
		 * "DefaultDynamicObstacles" is the default manager ; you can use a manager of your own if you want/need to
		 */
		DefaultDynamicObstacles obsDyn = new DefaultDynamicObstacles();

		Kraken kraken = new Kraken(robot, obs, obsDyn, new XY(-1500,0), new XY(1500, 2000), "trajectory"/*, "detailed"*/);
		PrintBuffer printBuffer = kraken.getPrintBuffer();
		
		/*
		 * The obstacles are printed
		 */
		for(Obstacle o : obs)
			printBuffer.addPrintable(o, Color.BLACK, Layer.MIDDLE.layer);
		printBuffer.refresh();

		try
		{
			kraken.initializeNewSearch(new XYO(0, 200, 0), new XY(1000, 1000));
			List<ItineraryPoint> path = kraken.search();
			
			/*
			 * We have the first trajectory
			 */
			for(ItineraryPoint p : path)
			{
				printBuffer.addTemporaryPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			printBuffer.refresh();
			
			/*
			 * Just a sleep to see clearly the different steps
			 */
			try
			{
				Thread.sleep(3000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			// Let's clear the previous trajectory
			printBuffer.clearTemporaryPrintables();

			// We add a dynamic obstacle
			Obstacle newObs1 = new CircularObstacle(new XY_RW(-200,600), 200);
			Obstacle newObs2 = new RectangularObstacle(new XY_RW(1200,1500), 100, 100);
			Obstacle newObs3 = new CircularObstacle(new XY_RW(0,1200), 100);
			Obstacle newObs4 = new CircularObstacle(new XY_RW(-900,600), 400);
			printBuffer.addTemporaryPrintable(newObs1, Color.BLUE, Layer.MIDDLE.layer);
			printBuffer.addTemporaryPrintable(newObs2, Color.BLUE, Layer.MIDDLE.layer);
			printBuffer.addTemporaryPrintable(newObs3, Color.BLUE, Layer.MIDDLE.layer);
			printBuffer.addTemporaryPrintable(newObs4, Color.BLUE, Layer.MIDDLE.layer);
			obsDyn.add(newObs1);
			obsDyn.add(newObs2);
			obsDyn.add(newObs3);
			obsDyn.add(newObs4);
			
			// Just as before
			kraken.initializeNewSearch(new XYO(0, 200, 0), new XY(1000, 1000));
			path = kraken.search();
			
			/*
			 * This time, the trajectory avoids the new obstacle
			 */
			for(ItineraryPoint p : path)
			{
				printBuffer.addTemporaryPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			printBuffer.refresh();
			
			try
			{
				Thread.sleep(3000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			
			// Let's remove the dynamic obstacle
			obsDyn.clear();
			printBuffer.clearTemporaryPrintables();
			kraken.initializeNewSearch(new XYO(0, 200, 0), new XY(1000, 1000));
			path = kraken.search();
			
			/*
			 * It finds the same trajectory as before, when there wasn't any dynamic obstacle
			 */
			for(ItineraryPoint p : path)
			{
				printBuffer.addTemporaryPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
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
