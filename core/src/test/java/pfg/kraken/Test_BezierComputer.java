/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.awt.Color;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pfg.graphic.printable.Layer;
import pfg.kraken.astar.tentacles.BezierComputer;
import pfg.kraken.astar.tentacles.DynamicTentacle;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.utils.XYO;

/**
 * Test unitaire de BezierComputer
 * @author pf
 *
 */

public class Test_BezierComputer extends JUnit_Test
{
	protected BezierComputer bezier;
	
	@Before
	public void setUp() throws Exception
	{
		super.setUpWith(null, "default", "graphic", "empty");
		bezier = injector.getService(BezierComputer.class);
	}

	@Test
	public void test_xyoc_2_xy() throws Exception
	{
		Cinematique c = new Cinematique(0, 1000, Math.PI / 2, true, -1, false);
		Cinematique arrivee = new Cinematique(400, 1700, Math.PI / 2, false, 0, false);
		log.write("Initial : " + c, LogCategoryKraken.TEST);
		DynamicTentacle arc = bezier.quadraticInterpolationXYOC2XY(c, arrivee.getPosition(), 0);

		Assert.assertTrue(arc != null);
		
		display.addTemporaryPrintable(arc, Color.BLACK, Layer.FOREGROUND.layer);
		for(int i = 0; i < arc.getNbPoints(); i++)
			System.out.println(i + " " + arc.getPoint(i));
	}
	
	@Test
	public void test_xyo_2_xyo() throws Exception
	{
		Cinematique c = new Cinematique(0, 1000, Math.PI / 2, true, 0, false);
		Cinematique arrivee = new Cinematique(200, 1500, 0, false, 0, false);
		log.write("Initial : " + c, LogCategoryKraken.TEST);
		DynamicTentacle arc = bezier.quadraticInterpolationXYO2XYO(c, new XYO(arrivee.getPosition().clone(), arrivee.orientationGeometrique), 0);

		Assert.assertTrue(arc != null);
		
		display.addTemporaryPrintable(arc, Color.BLACK, Layer.FOREGROUND.layer);
		for(int i = 0; i < arc.getNbPoints(); i++)
			System.out.println(i + " " + arc.getPoint(i));
	}

	@Test
	public void test_xyoc_2_xyo() throws Exception
	{
		Cinematique c = new Cinematique(0, 1000, Math.PI / 2, true, -1, false);
		Cinematique arrivee = new Cinematique(900, 1500, 0, false, 0, false);
		log.write("Initial : " + c, LogCategoryKraken.TEST);
		DynamicTentacle arc = bezier.cubicInterpolationXYOC2XYO(c, arrivee, 0);

		Assert.assertTrue(arc != null);
		
		display.addTemporaryPrintable(arc, Color.BLACK, Layer.FOREGROUND.layer);
		for(int i = 0; i < arc.getNbPoints(); i++)
			System.out.println(i + " " + arc.getPoint(i));
	}
}
