/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.robot;

import java.awt.Graphics;
import pfg.graphic.Chart;
import pfg.graphic.GraphicPanel;
import pfg.graphic.printable.Printable;

/**
 * A point of the itinerary computed by Kraken
 * @author pf
 *
 */

public class ItineraryPoint implements Printable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The desired orientation
	 */
	public final double orientation;
	
	/**
	 * The desired position (x)
	 */
	public final double x;
	
	/**
	 * The desired position (y)
	 */
	public final double y;
	
	/**
	 * If the robot need to go forward
	 */
	public final boolean goingForward;
	
	/**
	 * The desired curvature
	 */
	public final double curvature;

	public ItineraryPoint(Cinematique c)
	{
		goingForward = c.enMarcheAvant;
		x = c.getPosition().getX();
		y = c.getPosition().getY();
		if(c.enMarcheAvant)
		{
			orientation = c.orientationGeometrique;
			curvature = c.courbureGeometrique;
		}
		else
		{
			orientation = c.orientationGeometrique + Math.PI;
			curvature = -c.courbureGeometrique;
		}
	}
	
	@Override
	public String toString()
	{
		return "("+x+","+y+"), orientation = "+orientation+", curvature = "+curvature+" going "+(goingForward ? "forward" : "backward");
	}

	@Override
	public void print(Graphics g, GraphicPanel f, Chart a)
	{
		int taille = 5;
		g.fillOval(f.XtoWindow(x)-taille/2, f.YtoWindow(y)-taille/2, taille, taille);
		double directionLigne = orientation + Math.PI / 2;
		double longueur = curvature * 10;
		int deltaX = (int) (longueur * Math.cos(directionLigne));
		int deltaY = (int) (longueur * Math.sin(directionLigne));
		g.drawLine(f.XtoWindow(x), f.YtoWindow(y), f.XtoWindow(x)+deltaX, f.YtoWindow(y)-deltaY);
	}

}
