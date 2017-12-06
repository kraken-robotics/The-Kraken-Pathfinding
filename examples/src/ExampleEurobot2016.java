/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import pfg.graphic.GraphicDisplay;
import pfg.graphic.printable.Layer;
import pfg.kraken.Kraken;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;


/**
 * An example with the obstacles from the Eurobot 2016 robotic competition
 * @author pf
 *
 */

public class ExampleEurobot2016
{

	public static void main(String[] args)
	{
		/*
		 * The obstacles of Eurobot 2016
		 */
		List<Obstacle> obs = new ArrayList<Obstacle>();
		
		obs.add(new RectangularObstacle(new XY(0,1250), 1200, 22));
		obs.add(new RectangularObstacle(new XY(0,950), 48, 600));

		obs.add(new RectangularObstacle(new XY(-700,1900), 22, 200));
		obs.add(new RectangularObstacle(new XY(700,1900), 22, 200));

		obs.add(new CircularObstacle(new XY(-1500,0), 250));
		obs.add(new CircularObstacle(new XY(1500,0), 250));

		obs.add(new RectangularObstacle(new XY(-1200,1950), 100, 100));
		obs.add(new RectangularObstacle(new XY(-900,1950), 100, 100));
		obs.add(new RectangularObstacle(new XY(1200,1950), 100, 100));
		obs.add(new RectangularObstacle(new XY(900,1950), 100, 100));

		obs.add(new RectangularObstacle(new XY(-561,11), 22, 22));
		obs.add(new RectangularObstacle(new XY(561,11), 22, 22));

		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110);

		Kraken kraken = new Kraken(robot, obs, new XY(-1500,0), new XY(1500, 2000), "trajectory", "eurobot2016");
		
		GraphicDisplay display = kraken.getGraphicDisplay();
		
		try
		{
			kraken.initializeNewSearch(new XYO(0, 1500, 0), new XY(1000, 500));
			
			List<ItineraryPoint> path = kraken.search();
			
			for(ItineraryPoint p : path)
			{
				display.addPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
			}
			
			display.refresh();
		}
		catch(PathfindingException e)
		{
			// Impossible
			e.printStackTrace();
		}
		finally
		{
			kraken.stop();
		}
	}
}
