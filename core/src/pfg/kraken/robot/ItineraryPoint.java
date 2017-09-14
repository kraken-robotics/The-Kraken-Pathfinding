/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.robot;

/**
 * A point of the itinerary computed by Kraken
 * @author pf
 *
 */

public class ItineraryPoint
{
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
	 * The desired curvature
	 */
	public final double curvature;

	public ItineraryPoint(Cinematique c)
	{
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
		return "("+x+","+y+"), orientation = "+orientation+", curvature = "+curvature;
	}
	
}
