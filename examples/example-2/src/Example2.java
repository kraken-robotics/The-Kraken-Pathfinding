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
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DefaultDynamicObstacles;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.astar.TentacularAStar;
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
		
		/*
		 * The list of dynamic obstacles.
		 * "DefaultDynamicObstacles" is the default manager ; you can use a manager of your own if you want/need to
		 */
		DefaultDynamicObstacles obsDyn = new DefaultDynamicObstacles();

		Kraken kraken = Kraken.getKraken(obs, obsDyn, new XY(-1500,0), new XY(1500, 2000), "trajectory", "detailed");
		WindowFrame frame = kraken.getWindowFrame();
		PrintBuffer printBuffer = kraken.getPrintBuffer();
		
		/*
		 * The obstacles are printed
		 */
		for(Obstacle o : obs)
			printBuffer.add(o, Color.BLACK, Layer.MIDDLE.layer);
		frame.refresh();

		TentacularAStar astar = kraken.getAStar();
		try
		{
			astar.initializeNewSearch(new XYO(0, 200, 0), new XY(1000, 1000));
			LinkedList<ItineraryPoint> path = astar.search();
			
			/*
			 * We have the first trajectory
			 */
			for(ItineraryPoint p : path)
			{
				printBuffer.addSupprimable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			frame.refresh();
			
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
			
			printBuffer.clearSupprimables();

			Obstacle newObs = new CircularObstacle(new XY_RW(-200,600), 200);
			printBuffer.addSupprimable(newObs, Color.BLUE, Layer.MIDDLE.layer);
			obsDyn.add(newObs);
			
			astar.initializeNewSearch(new XYO(0, 200, 0), new XY(1000, 1000));
			path = astar.search();
			
			for(ItineraryPoint p : path)
			{
				printBuffer.addSupprimable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			frame.refresh();
			
			try
			{
				Thread.sleep(3000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			
			obsDyn.clear();
			printBuffer.clearSupprimables();
			astar.initializeNewSearch(new XYO(0, 200, 0), new XY(1000, 1000));
			path = astar.search();
			
			for(ItineraryPoint p : path)
			{
				printBuffer.addSupprimable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
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
