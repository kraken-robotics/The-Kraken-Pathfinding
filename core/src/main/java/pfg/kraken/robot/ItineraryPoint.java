/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.robot;

import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
	 * The robot should stop at this point
	 */
	public final boolean stop;
	
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
	
	/**
	 * The maximal speed
	 */
	public final double maxSpeed;
	
	/**
	 * The recommended speed
	 */
	public final double possibleSpeed;

	private final static NumberFormat formatter = new DecimalFormat("#0.00");

	public ItineraryPoint(double x, double y, double orientation, double curvature, boolean goingForward, double maxSpeed, double possibleSpeed, boolean stop)
	{
		this.x = x;
		this.y = y;
		this.orientation = orientation;
		this.curvature = curvature;
		this.goingForward = goingForward;
		this.maxSpeed = maxSpeed;
		this.possibleSpeed = possibleSpeed;
		this.stop = stop;
	}
	
	public ItineraryPoint(CinematiqueObs c)
	{
		goingForward = c.enMarcheAvant;
		maxSpeed = c.maxSpeed;
		possibleSpeed = c.possibleSpeed;
		x = c.getPosition().getX();
		y = c.getPosition().getY();
		stop = c.stop;

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
		return "(" + formatter.format(x) + "," + formatter.format(y) + ")"
				+ ", o: " + formatter.format(orientation)
				+ ", going "+(goingForward ? "forward" : "backward")
				+ ", c: " + formatter.format(curvature)
				+ ", max speed: "+formatter.format(maxSpeed)
				+ ", possible speed: "+formatter.format(possibleSpeed)
				+ (stop ? ", ending with a stop":"");
	}

	@Override
	public void print(Graphics g, GraphicPanel f)
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
