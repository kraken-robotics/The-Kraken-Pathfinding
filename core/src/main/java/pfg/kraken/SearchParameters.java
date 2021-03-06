/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.io.Serializable;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.struct.Kinematic;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XYO;

/**
 * Structure for the search parameters
 * @author pf
 *
 */

public class SearchParameters implements Serializable
{
	private static final long serialVersionUID = 2287573842706477685L;
	
	public final Kinematic start, arrival;
	public final String mode;
	public DirectionStrategy directionstrategy = null;
	public Double maxSpeed = null;
	public Integer timeout = null;
	
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
		this.start = new Kinematic(start);
		this.arrival = new Kinematic(new XYO(arrival.clone(), 0));
		this.mode = mode;
	}
	
	public SearchParameters(XYO start, XYO arrival, String mode)
	{
		this.start = new Kinematic(start);
		this.arrival = new Kinematic(arrival);
		this.mode = mode;
	}
	
	public SearchParameters(Kinematic start, Kinematic arrival, String mode)
	{
		this.start = start;
		this.arrival = arrival;
		this.mode = mode;
	}
	
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
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
