package utils;

import java.io.Serializable;

/**
 * Vecteur en lecture seule
 * @author pf
 *
 */

public class Vec2RO implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected volatile double x;
	protected volatile double y;
	
	public Vec2RO()
	{
		x = 0;
		y = 0;
	}

	public Vec2RO(double longueur, double angle, boolean useless)
	{
		x = Math.cos(angle)*longueur;
		y = Math.sin(angle)*longueur;
	}

	public Vec2RO(double requestedX, double requestedY)
	{
		x = requestedX;
		y = requestedY;
	}

	// Do not square a length, use squared length directly
	// to increase performances
	public final double squaredLength()
	{
		return x*x + y*y;
	}

	// Returns this vec2's magnitude
	public final double length()
	{
		return Math.hypot(x, y);
	}
	
	// dot product
	public final double dot(Vec2RO other)
	{
		return x*other.x + y*other.y;
	}

	// build a new Vec2RO by summing the calling Vec2RO and the one in args
	public final Vec2RW plusNewVector(Vec2RO other)
	{
		return new Vec2RW(x + other.x, y + other.y);
	}
	
	// build a new Vec2RO with the value obtained by decrementing the
	// calling Vec2RO by the provided Vec2RO in args
	public final Vec2RW minusNewVector(Vec2RO other)
	{
		return new Vec2RW(x - other.x, y - other.y);
	}

	@Override
	public final Vec2RW clone()
	{
		return new Vec2RW(this.x, this.y);
	}

	public final double squaredDistance(Vec2RO other)
	{
		double tmp_x = x-other.x, tmp_y = y-other.y;
		return tmp_x*tmp_x + tmp_y*tmp_y;
	}

	public final double distance(Vec2RO other)
	{
		return Math.hypot(x-other.x, y-other.y);
	}

	@Override
	public final String toString()
	{
		return "("+x+","+y+")";
	}
	
	public final boolean equals(Vec2RO other)
	{
		return x == other.x && y == other.y;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		else if (obj == null)
			return false;
		else if (!(obj instanceof Vec2RO))
			return false;

		Vec2RO other = (Vec2RO) obj;
		if (x != other.x || (y != other.y))
			return false;
		return true;
	}

	/**
	 * Copie this dans other.
	 * @param other
	 */
	public final void copy(Vec2RW other)
	{
	    other.x = x;
	    other.y = y;
	}

	public final Vec2RW middleNewVector(Vec2RO b)
	{
		return new Vec2RW((x+b.x)/2, (y+b.y)/2);
	}

	public final Vec2RW rotateNewVector(double angle, Vec2RO centreRotation)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vec2RW(cos*(x-centreRotation.x)-sin*(y-centreRotation.y)+centreRotation.x,
		sin*(x-centreRotation.x)+cos*(y-centreRotation.y)+centreRotation.y);
	}

	public final double getArgument()
	{
		return Math.atan2(y, x);
	}

	@Override
	public int hashCode() {
		return (int)((x+1500)*2000+y);
	}
	
	public final double getX()
	{
		return x;
	}

	public final double getY()
	{
		return y;
	}
}
