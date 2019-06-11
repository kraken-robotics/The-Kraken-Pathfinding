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
 * Example with start position in obstacle
 * @author pf
 *
 */

public class Example8
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
		
		obs.add(new RectangularObstacle(new XY(0,500), 10, 250));
		obs.add(new CircularObstacle(new XY(500,600), 100));
		
		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110); 
		
		DebugTool debug = DebugTool.getDebugTool(new XY(0,1000), new XY(0, 1000), null, "kraken-examples.conf", "trajectory");
		Display display = debug.getDisplay();
		for(Obstacle o : obs)
			display.addPrintable(o, Color.BLACK, Layer.MIDDLE.layer);
		
		/**
		 * A Kraken instance that can't start in an obstacle
		 */
		KrakenParameters kp = new KrakenParameters(robot, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "trajectory", "detailed");
		kp.setFixedObstacles(obs);
		kp.setDisplay(display);
		Kraken kraken = new Kraken(kp);

		/**
		 * A Kraken instance that can start in an obstacle (notice the different config profiles)
		 */
		KrakenParameters kpStartInObstacle = new KrakenParameters(robot, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "trajectory", "detailed", "startInObstacle");
		kpStartInObstacle.setFixedObstacles(obs);
		kpStartInObstacle.setDisplay(display);
		Kraken krakenStartInObstacle = new Kraken(kpStartInObstacle);
		
		SearchParameters sp = new SearchParameters(new XYO(-200, 500, 0), new XY(1000, 1000));
		try
		{
			/**
			 * This search will fail
			 */
			kraken.initializeNewSearch(sp);
			kraken.search();
		}
		catch(PathfindingException e)
		{
			System.out.println("The first Kraken can't find a path: "+e);
		}

		try
		{
			/**
			 * This search won't fail
			 */
			krakenStartInObstacle.initializeNewSearch(sp);
			List<ItineraryPoint> path = krakenStartInObstacle.search();
			
			System.out.println("The second Kraken found a path!");			
			for(ItineraryPoint p : path)
				display.addPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
			display.refresh();
		}
		catch(PathfindingException e)
		{
			e.printStackTrace();
		}
	}
}
