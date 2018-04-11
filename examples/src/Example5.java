/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import pfg.graphic.GraphicDisplay;
import pfg.graphic.printable.Layer;
import pfg.kraken.Kraken;
import pfg.kraken.SearchParameters;
import pfg.kraken.astar.thread.DynamicPath;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DefaultDynamicObstacles;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;


/**
 * Example for replaning
 * @author pf
 *
 */

public class Example5
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

		/*
		 * The list of dynamic obstacles.
		 * "DefaultDynamicObstacles" is the default manager ; you can use a manager of your own if you want/need to
		 */
		DefaultDynamicObstacles obsDyn = new DefaultDynamicObstacles();

		Kraken kraken = new Kraken(robot, obs, obsDyn, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "trajectory"/*, "detailed"*/);
		GraphicDisplay display = kraken.getGraphicDisplay();

		try
		{
			kraken.initializeNewSearch(new SearchParameters(new XYO(0, 200, 0), new XY(1000, 1000)));
			DynamicPath dpath = kraken.enableAutoReplanning();
			dpath.startSearch();
			List<ItineraryPoint> path;
			synchronized(dpath)
			{
				System.out.println("Attente de la fin…");
				while(!dpath.isNewPathAvailable())
				{
					System.out.println("Pas de nouveau chemin : dodo");
					dpath.wait();
					System.out.println("Éveil !");
				}
				System.out.println("Chemin trouvé !");
				path = dpath.getPathItineraryPoint();
			}
			for(ItineraryPoint p : path)
			{
				display.addTemporaryPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			
			Thread.sleep(2000);
			display.clearTemporaryPrintables();
			
			Obstacle newObs1 = new CircularObstacle(new XY(400,800), 100);
			display.addTemporaryPrintable(newObs1, Color.BLUE, Layer.MIDDLE.layer);
			System.out.println("Ajout d'un obstacle !");
			obsDyn.add(newObs1);
			synchronized(dpath)
			{
				System.out.println("Attente de la fin…");
				while(!dpath.isNewPathAvailable())
				{
					System.out.println("Pas de nouveau chemin : dodo");
					dpath.wait();
					System.out.println("Éveil !");
				}
				System.out.println("Chemin trouvé !");
				path = dpath.getPathItineraryPoint();
				for(ItineraryPoint p : path)
				{
					display.addTemporaryPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
					System.out.println(p);
				}
			}
			dpath.endSearch();
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
