/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 */

package kraken.robot;

/**
 * An example for the speed
 * @author pf
 *
 */

public enum DefaultSpeed implements Speed
{
	STANDARD(1.);

	private double maxSpeed;
	
	private DefaultSpeed(double maxSpeed)
	{
		this.maxSpeed = maxSpeed;
	}

	@Override
	public double getMaxForwardSpeed(double curvature)
	{
		return maxSpeed;
	}

	@Override
	public double getMaxBackwardSpeed(double curvature)
	{
		return maxSpeed;
	}
	
}
