/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.dstarlite.gridspace;

import java.io.Serializable;

/**
 * Une structure utilisée par le GridSpace
 * 
 * @author pf
 *
 */

public class PointDirige implements Serializable
{
	private static final long serialVersionUID = 7904466980326128967L;
	public final PointGridSpace point;
	public final Direction dir;

	PointDirige(PointGridSpace point, Direction dir)
	{
		this.point = point;
		this.dir = dir;
	}

	@Override
	public int hashCode()
	{
		return (point.hashcode << 3) + dir.ordinal();
	}

	@Override
	public boolean equals(Object d)
	{
		return d instanceof PointDirige && hashCode() == ((PointDirige) d).hashCode();
	}

	@Override
	public String toString()
	{
		return point + " " + dir;
	}

}
