/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.awt.Color;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pfg.graphic.GraphicDisplay;
import pfg.graphic.printable.Layer;
import pfg.graphic.printable.PrintablePoint;
import pfg.kraken.astar.tentacles.BezierComputer;
import pfg.kraken.astar.tentacles.DynamicTentacle;
import pfg.kraken.robot.Cinematique;

/**
 * Test unitaire de BezierComputer
 * @author pf
 *
 */

public class Test_BezierComputer extends JUnit_Test
{
	protected BezierComputer bezier;
	private GraphicDisplay buffer;
	
	@Before
	public void setUp() throws Exception
	{
		super.setUpStandard("default", "graphic");
		buffer = injector.getService(GraphicDisplay.class);
		bezier = injector.getService(BezierComputer.class);
	}

	@Test
	public void test_bezier_quad() throws Exception
	{
		Cinematique c = new Cinematique(0, 1000, Math.PI / 2, true, -1, false);
		Cinematique arrivee = new Cinematique(400, 1400, Math.PI / 2, false, 0, false);
		log.write("Initial : " + c, LogCategoryKraken.TEST);
		DynamicTentacle arc = bezier.quadraticInterpolationXYOC2XY(c, arrivee.getPosition(), 0);

		Assert.assertTrue(arc != null);
		
		for(int i = 0; i < arc.getNbPoints(); i++)
		{
			System.out.println(i + " " + arc.getPoint(i));
			buffer.addTemporaryPrintable(new PrintablePoint(arc.getPoint(i).getPosition()), Color.BLACK, Layer.FOREGROUND.layer);
		}
	}

}
