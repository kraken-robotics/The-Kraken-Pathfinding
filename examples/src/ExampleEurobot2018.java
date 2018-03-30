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
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;


/**
 * An example with the obstacles from the Eurobot 2018 robotic competition
 * @author pf
 *
 */

public class ExampleEurobot2018
{

	public static void main(String[] args)
	{
		/*
		 * The obstacles of Eurobot 2018
		 */
		List<Obstacle> obs = new ArrayList<Obstacle>();
		
		obs.add(new RectangularObstacle(new XY(0, 250 / 2), 1200, 250));

		obs.add(new RectangularObstacle(new XY(1450, 2000-840), 100, 55));
		obs.add(new RectangularObstacle(new XY(-1450, 2000-840), 100, 55));
		
		obs.add(new RectangularObstacle(new XY(-890, 50), 55, 100));
		obs.add(new RectangularObstacle(new XY(890, 50), 55, 100));

		obs.add(new RectangularObstacle(new XY(1300, 1675), 400, 650));
		obs.add(new RectangularObstacle(new XY(-1300, 1675), 400, 650));

		obs.add(new RectangularObstacle(new XY(1100-560/2, 2000-180/2), 560, 180));
		obs.add(new RectangularObstacle(new XY(-1100+560/2, 2000-180/2), 560, 180));
		
		XY[] centers = new XY[]{new XY(-650,1460), new XY(650,1460), new XY(-400, 500), new XY(400, 500), new XY(-1200, 810), new XY(1200, 810)};
		
		for(XY c : centers)
		{
			obs.add(new RectangularObstacle(new XY(c.getX(), c.getY()), 58, 58));
			obs.add(new RectangularObstacle(new XY(c.getX()+58, c.getY()), 58, 58));
			obs.add(new RectangularObstacle(new XY(c.getX()-58, c.getY()), 58, 58));
			obs.add(new RectangularObstacle(new XY(c.getX(), c.getY()+58), 58, 58));
			obs.add(new RectangularObstacle(new XY(c.getX(), c.getY()-58), 58, 58));
		}

		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110);

		Kraken kraken = new Kraken(robot, obs, new XY(-1500,0), new XY(1500, 2000), "trajectory", "eurobot2018");

		GraphicDisplay display = kraken.getGraphicDisplay();
		
		try
		{
			kraken.initializeNewSearch(new XYO(-1100, 200, Math.PI/2), new XY(900, 1500));
			
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
	}
}
