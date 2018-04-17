/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.io.Serializable;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;

/**
 * Structure for the search parameters
 * @author pf
 *
 */

public class SearchParameters implements Serializable
{
	private static final long serialVersionUID = 2287573842706477685L;
	
	public final Cinematique start, arrival;
	public final String mode;
	public DirectionStrategy directionstrategy = null;
	public Double maxSpeed = null;
	
	public SearchParameters(XYO start, XYO arrival)
	{
		this(start, arrival, "XYO");
	}
	
	public SearchParameters(XYO start, XY arrival)
	{
		this(start, arrival, "XY");
	}
	
	public SearchParameters(XYO start, XY arrival, String mode)
	{
		this.start = new Cinematique(start);
		this.arrival = new Cinematique(new XYO(arrival.clone(), 0));
		this.mode = mode;
	}
	
	public SearchParameters(XYO start, XYO arrival, String mode)
	{
		this.start = new Cinematique(start);
		this.arrival = new Cinematique(arrival);
		this.mode = mode;
	}
	
	public SearchParameters(Cinematique start, Cinematique arrival, String mode)
	{
		this.start = start;
		this.arrival = arrival;
		this.mode = mode;
	}
	
	public void setMaxSpeed(double maxSpeed)
	{
		this.maxSpeed = maxSpeed;
	}
	
	public void setDirectionStrategy(DirectionStrategy directionstrategy)
	{
		this.directionstrategy = directionstrategy;
	}
	
	@Override
	public String toString()
	{
		return "From "+start+" to "+arrival+", mode : "+mode+(maxSpeed == null ? "" : ", max speed : "+maxSpeed)+(directionstrategy == null ? "" : ", direction strategy : "+directionstrategy);
	}
}
