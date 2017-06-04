/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 */

package kraken.robot;

/**
 * A point of the itinerary computed by Kraken
 * @author pf
 *
 */

public class ItineraryPoint
{
	public final double orientation;
	public final double x;
	public final double y;
	public final double courbure;

	public ItineraryPoint(Cinematique c)
	{
		x = c.getPosition().getX();
		y = c.getPosition().getY();
		if(c.enMarcheAvant)
		{
			orientation = c.orientationGeometrique;
			courbure = c.courbureGeometrique;
		}
		else
		{
			orientation = c.orientationGeometrique + Math.PI;
			courbure = -c.courbureGeometrique;
		}
	}
	
}
