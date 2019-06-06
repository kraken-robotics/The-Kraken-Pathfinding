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
import pfg.kraken.display.Display;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.ItineraryPoint;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XYO;


/**
 * An example with the obstacles from the Eurobot 2019 robotic competition
 * @author pf
 *
 */

public class ExampleEurobot2019
{

	public static void main(String[] args)
	{
		/*
		 * The obstacles of Eurobot 2019
		 */
		List<Obstacle> obs = new ArrayList<Obstacle>();
		
		obs.add(new RectangularObstacle(new XY(0, 0), 3000, 5));
		obs.add(new RectangularObstacle(new XY(-1500, 1000), 5, 2000));
		obs.add(new RectangularObstacle(new XY(1500, 1000), 5, 2000));
		obs.add(new RectangularObstacle(new XY(0, 2000), 3000, 5));

		obs.add(new CircularObstacle(new XY(-500, 950), 150));
		obs.add(new CircularObstacle(new XY(500, 950), 150));

		obs.add(new RectangularObstacle(new XY(0, 1980), 2000, 40));
		
		obs.add(new RectangularObstacle(new XY(-661, 428), 780, 60));
		obs.add(new RectangularObstacle(new XY(661, 428), 780, 60));
		
		obs.add(new RectangularObstacle(new XY(0, 200), 504, 400));
		obs.add(new RectangularObstacle(new XY(0, 500), 40, 200));
		
		obs.add(new CircularObstacle(new XY(-1000, 950), 38));
		obs.add(new CircularObstacle(new XY(1000, 950), 38));

		obs.add(new CircularObstacle(new XY(-1000, 1250), 76/2));
		obs.add(new CircularObstacle(new XY(-1000, 1550), 76/2));

		obs.add(new CircularObstacle(new XY(1000, 1250), 76/2));
		obs.add(new CircularObstacle(new XY(1000, 1550), 76/2));
		
		RectangularObstacle robot = new RectangularObstacle(106, 278, 105, 105);

		DebugTool debug = DebugTool.getDebugTool(new XY(0,1000), new XY(0, 1000), null, "kraken-examples.conf", "trajectory", "eurobot2019");
		Display display = debug.getDisplay();
		for(Obstacle o : obs)
			display.addPrintable(o, Color.BLACK, Layer.MIDDLE.layer);
		
		KrakenParameters kp = new KrakenParameters(robot, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "eurobot2019", "trajectory"/*, "detailed"*/);
		kp.setFixedObstacles(obs);
		kp.setDisplay(display);
		Kraken kraken = new Kraken(kp);
		
		try
		{
			kraken.initializeNewSearch(new SearchParameters(new XYO(1210, 1400, Math.PI), new XYO(-125, 1690, Math.PI/2)));
			
			List<ItineraryPoint> path = kraken.search();
			
			for(ItineraryPoint p : path)
			{
				display.addPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			
			System.out.println(kraken.getTentaclesStatistics());

			display.refresh();
		}
		catch(PathfindingException e)
		{
			// Impossible
			e.printStackTrace();
		}
	}
}
