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
import pfg.kraken.astar.autoreplanning.DynamicPath;
import pfg.kraken.display.Display;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DefaultDynamicObstacles;
import pfg.kraken.struct.ItineraryPoint;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XYO;
import pfg.kraken.exceptions.PathfindingException;


/**
 * Example for replaning with initial path
 * @author pf
 *
 */

public class Example7
{

	public static void main(String[] args) throws InterruptedException
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

		DefaultDynamicObstacles obsDyn = new DefaultDynamicObstacles();
		
		DebugTool debug = DebugTool.getDebugTool(new XY(0,1000), new XY(0, 1000), null, "kraken-examples.conf", "trajectory");
		Display display = debug.getDisplay();
		for(Obstacle o : obs)
			display.addPrintable(o, Color.BLACK, Layer.MIDDLE.layer);
		
		KrakenParameters kp = new KrakenParameters(robot, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "trajectory"/*, "detailed"*/);
		kp.setFixedObstacles(obs);
		kp.setDisplay(display);
		kp.setDynamicObstacle(obsDyn);
		Kraken kraken = new Kraken(kp);
		
		try
		{
			kraken.initializeNewSearch(new SearchParameters(new XYO(0, 200, 0), new XY(1000, 1000)));
			List<ItineraryPoint> initialPath = kraken.search();
			
			DynamicPath dpath = kraken.enableAutoReplanning();
			kraken.startContinuousSearchWithInitialPath(new SearchParameters(new XYO(0, 200, 0), new XY(1000, 1000)), initialPath);
			List<ItineraryPoint> path;
			
			/*
			 * The research part. We wait for dpath until a new path is available
			 */
			path = dpath.waitNewPath();
			
			for(ItineraryPoint p : path)
				display.addTemporaryPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);

			Thread.sleep(2000);
			
			/*
			 * The research is continuous : at the moment a new obstacle is added, Kraken tries to find a new path
			 */
			Obstacle newObs1 = new CircularObstacle(new XY(400,800), 100);
			display.addPrintable(newObs1, Color.BLUE, Layer.MIDDLE.layer);
			obsDyn.add(newObs1);
			
			/*
			 * We wait the new path
			 */
			path = dpath.waitNewPath();
			
			display.clearTemporaryPrintables();
			for(ItineraryPoint p : path)
				display.addTemporaryPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
			
			Thread.sleep(2000);
			
			/*
			 * We add a second obstacle
			 */
			Obstacle newObs2 = new CircularObstacle(new XY(100,1200), 100);
			display.addPrintable(newObs2, Color.BLUE, Layer.MIDDLE.layer);
			obsDyn.add(newObs2);
			
			path = dpath.waitNewPath();
			
			display.clearTemporaryPrintables();
			for(ItineraryPoint p : path)
				display.addTemporaryPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
			
			/*
			 * When the continuous search isn't needed anymore, we can stop it.
			 * You need to end the search to start a search with different parameters
			 */
			kraken.endContinuousSearch();
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
