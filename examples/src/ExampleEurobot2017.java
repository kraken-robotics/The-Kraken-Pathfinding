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
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;


/**
 * An example with the obstacles from the Eurobot 2017 robotic competition
 * @author pf
 *
 */

public class ExampleEurobot2017
{

	public static void main(String[] args)
	{
		/*
		 * The obstacles of Eurobot 2017
		 */
		List<Obstacle> obs = new ArrayList<Obstacle>();
		
		obs.add(new RectangularObstacle(new XY(-(1140 - 350 / 2), 2000 - 360 / 2), 350, 360));
		obs.add(new RectangularObstacle(new XY(-(1500 - 360 / 2), 2000 - 360 / 2), 360, 360));
		obs.add(new RectangularObstacle(new XY(-(790 - 360 / 2), 2000 - 360 / 2), 360, 360));
		
		obs.add(new RectangularObstacle(new XY(1140 - 350 / 2, 2000 - 360 / 2), 350, 360));
		obs.add(new RectangularObstacle(new XY(1500 - 360 / 2, 2000 - 360 / 2), 360, 360	));
		obs.add(new RectangularObstacle(new XY(790 - 360 / 2, 2000 - 360 / 2), 360, 360));

		obs.add(new RectangularObstacle(new XY(1500 - 710 / 2, 2000 - 360 - 11), 710, 22));
		obs.add(new RectangularObstacle(new XY(-1500 + 710 / 2, 2000 - 360 - 11), 710, 22));

		obs.add(new RectangularObstacle(new XY(54 - 1500, 1075), 108, 494));
		obs.add(new RectangularObstacle(new XY(1500 - 54, 1075), 108, 494));

		obs.add(new CircularObstacle(new XY(0, 0), 200));

		obs.add(new RectangularObstacle(new XY(0, 500).rotateNewVector(-Math.PI / 4, new XY(0, 0)), 140, 600, -Math.PI / 4));
		obs.add(new RectangularObstacle(new XY(0, 500), 140, 600));
		obs.add(new RectangularObstacle(new XY(0, 500).rotateNewVector(Math.PI / 4, new XY(0, 0)), 140, 600, Math.PI / 4));

		obs.add(new CircularObstacle(new XY(-1500, 0), 540));
		obs.add(new CircularObstacle(new XY(1500, 0), 540));

		obs.add(new CircularObstacle(new XY(-350, 1960), 40));
		obs.add(new CircularObstacle(new XY(350, 1960), 40));

		obs.add(new CircularObstacle(new XY(-1460, 650), 40));
		obs.add(new CircularObstacle(new XY(1460, 650), 40));
		
		obs.add(new CircularObstacle(new XY(650 - 1500, 2000 - 555), 125));
		obs.add(new CircularObstacle(new XY(1500 - 650, 2000 - 555), 125));

		obs.add(new CircularObstacle(new XY(1070 - 1500, 2000 - 1870), 125));
		obs.add(new CircularObstacle(new XY(1500 - 1070, 2000 - 1870), 125));

		obs.add(new CircularObstacle(new XY(200 - 1500, 1400), 32));
		obs.add(new CircularObstacle(new XY(1500 - 200, 1400), 32));
		obs.add(new CircularObstacle(new XY(1000 - 1500, 1400), 32));
		obs.add(new CircularObstacle(new XY(1500 - 1000, 1400), 32));
		obs.add(new CircularObstacle(new XY(500 - 1500, 900), 32));
		obs.add(new CircularObstacle(new XY(1500 - 500, 900), 32));
		obs.add(new CircularObstacle(new XY(900 - 1500, 600), 32));
		obs.add(new CircularObstacle(new XY(1500 - 900, 600), 32));
		obs.add(new CircularObstacle(new XY(800 - 1500, 150), 32));
		obs.add(new CircularObstacle(new XY(1500 - 800, 150), 32));

		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110);

		Kraken kraken = new Kraken(robot, obs, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "trajectory", "eurobot2017");

		GraphicDisplay display = kraken.getGraphicDisplay();
		
		try
		{
			kraken.initializeNewSearch(new XYO(-850, 400, Math.PI/2), new XYO(850, 400, -Math.PI/2));
			
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
