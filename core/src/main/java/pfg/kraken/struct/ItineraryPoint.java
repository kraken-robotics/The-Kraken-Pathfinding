/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.struct;

import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import pfg.kraken.display.Display;
import pfg.kraken.display.Printable;

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

	private final static NumberFormat formatter = new DecimalFormat("#0.00");

	public ItineraryPoint(double x, double y, double orientation, double curvature, boolean goingForward, double maxSpeed, boolean stop)
	{
		this.x = x;
		this.y = y;
		
		orientation %= 2 * Math.PI;
		if(orientation > Math.PI)
			orientation -= 2 * Math.PI;
		else if(orientation <= -Math.PI)
			orientation += 2 * Math.PI;

		this.orientation = orientation;
		this.curvature = curvature;
		this.goingForward = goingForward;
		this.maxSpeed = maxSpeed;
		this.stop = stop;
	}
	
	public ItineraryPoint(EmbodiedKinematic c)
	{
		this(c.cinem.getX(), c.cinem.getY(), c.cinem.orientationReelle, c.cinem.courbureReelle, c.cinem.enMarcheAvant, c.maxSpeed, c.cinem.stop);
	}
	
	@Override
	public String toString()
	{
		return "(" + formatter.format(x) + "," + formatter.format(y) + ")"
				+ ", o: " + formatter.format(orientation)
				+ ", going "+(goingForward ? "forward" : "backward")
				+ ", c: " + formatter.format(curvature)
				+ ", max speed: "+formatter.format(maxSpeed)
				+ (stop ? ", ending with a stop":"");
	}

	@Override
	public void print(Graphics g, Display f)
	{
		int taille = 5;
		g.fillOval(f.XtoWindow(x)-taille/2, f.YtoWindow(y)-taille/2, taille, taille);
		double directionLigne = orientation + Math.PI / 2;
		double longueur = curvature * 10;
		int deltaX = (int) (longueur * Math.cos(directionLigne));
		int deltaY = (int) (longueur * Math.sin(directionLigne));
		g.drawLine(f.XtoWindow(x), f.YtoWindow(y), f.XtoWindow(x)+deltaX, f.YtoWindow(y)-deltaY);
	}
	
	@Override
	public int hashCode()
	{
		return (int) Math.round(x * y);
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof ItineraryPoint))
			return false;
		ItineraryPoint ip = (ItineraryPoint) o;
		return ip.goingForward == goingForward
				&& ip.stop == stop
				&& ip.x == x
				&& ip.y == y
				&& ip.curvature == curvature
				&& ip.orientation == orientation
				&& ip.maxSpeed == maxSpeed;
	}
}
