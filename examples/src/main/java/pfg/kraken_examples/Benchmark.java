/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */
package pfg.kraken_examples;

import java.util.ArrayList;
import java.util.List;
import pfg.kraken.Kraken;
import pfg.kraken.KrakenParameters;
import pfg.kraken.SearchParameters;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XYO;


/**
 * A benchmark
 * @author pf
 *
 */

public class Benchmark
{

	public static void main(String[] args)
	{
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new CircularObstacle(new XY(-1000,1500), 400));
		obs.add(new RectangularObstacle(new XY(-300,500), 200, 1000));
		obs.add(new RectangularObstacle(new XY(700,1500), 200, 1000));
		obs.add(new CircularObstacle(new XY(600,700), 200));

		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110); 
		
		KrakenParameters kp = new KrakenParameters(robot, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "benchmark");
		kp.setFixedObstacles(obs);
		Kraken kraken = new Kraken(kp);

		try
		{
			Long avant = System.currentTimeMillis();
			int nbPF = 0;
			while(System.currentTimeMillis() - avant < 60000)
			{
				kraken.initializeNewSearch(new SearchParameters(new XYO(-900, 200, Math.PI), new XY(1200, 1400)));
				kraken.search();
				nbPF++;
			}
			System.out.println("Temps moyen: "+(System.currentTimeMillis()-avant)/nbPF);
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
