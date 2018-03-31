/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.HashMap;
import java.util.Random;
import pfg.config.Config;
import pfg.config.ConfigInfo;
import pfg.graphic.Chart;
import pfg.graphic.ConfigInfoGraphic;
import pfg.graphic.DebugTool;
import pfg.graphic.Vec2RO;
import pfg.graphic.printable.Plottable;
import pfg.kraken.utils.XY;

public class DisplayClient
{

	public static void main(String[] args) throws InterruptedException
	{
		if(args.length != 1)
		{
			System.out.println("Usage : DisplayClient hostname");
			System.out.println("hostname can be an IP address or a name");
		}
		else
		{
			XY topRightCorner = new XY(1500, 2000);
			XY bottomLeftCorner = new XY(-1500, 0);
			Config config = new Config(ConfigInfoKraken.values(), false, "kraken.conf", "default");
			HashMap<ConfigInfo, Object> overrideGraphic = new HashMap<ConfigInfo, Object>();
			for(ConfigInfoGraphic infoG : ConfigInfoGraphic.values())
				for(ConfigInfoKraken infoK : ConfigInfoKraken.values())
					if(infoG.toString().equals(infoK.toString()))
						overrideGraphic.put(infoG, config.getObject(infoK));
			overrideGraphic.put(ConfigInfoGraphic.SIZE_X_WITH_UNITARY_ZOOM, (int) (topRightCorner.getX() - bottomLeftCorner.getX()));
			overrideGraphic.put(ConfigInfoGraphic.SIZE_Y_WITH_UNITARY_ZOOM, (int) (topRightCorner.getY() - bottomLeftCorner.getY()));
			
			DebugTool debug = DebugTool.getDebugTool(overrideGraphic, new Vec2RO((topRightCorner.getX() + bottomLeftCorner.getX()) / 2, (topRightCorner.getY() + bottomLeftCorner.getY()) / 2), SeverityCategoryKraken.INFO, null);
			debug.startPrintClient(args[0]);
			while(true)
				Thread.sleep(1000);
		}
	}
	
	public static class RandomValue implements Plottable
	{
		private static final long serialVersionUID = 1L;
		private int value = 0;
		private Random r = new Random();
		private String nom;
		
		public RandomValue(String nom)
		{
			this.nom = nom;
		}

		@Override
		public void plot(Chart a)
		{
			HashMap<String, Double> values = new HashMap<String, Double>();
			values.put(nom, (double) value);
			a.addData(values);
			value += r.nextInt(5) - 2;
		}
	}

}
