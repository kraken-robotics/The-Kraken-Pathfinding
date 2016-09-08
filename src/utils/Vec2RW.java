package utils;

/**
 * Vecteur en lecture/écriture
 * @author pf
 *
 */

public class Vec2RW extends Vec2RO
{
	private static final long serialVersionUID = 1L;

	public Vec2RW()
	{
		super();
	}

	public Vec2RW(double longueur, double angle, boolean useless)
	{
		super(longueur, angle, useless);
	}

	public Vec2RW(double requestedX, double requestedY)
	{
		super(requestedX, requestedY);
	}

	public final Vec2RO getReadOnly()
	{
		return (Vec2RO) this;
	}

	public final Vec2RW plus(Vec2 other)
	{
		x += other.x;
		y += other.y;
		return this;
	}
	
	public final Vec2RW minus(Vec2 other)
	{
		x -= other.x;
		y -= other.y;
		return this;
	}

	
	public final Vec2RW scalar(double d)
	{
		x = d*x;
		y = d*y;
		return this;
	}

	public final Vec2RW Ysym(boolean symetrie)
	{
		if(symetrie)
			y = -y;
		return this;
	}

	public final Vec2RW rotate(double angle)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double old_x = x;
		x = cos*x-sin*y;
		y = sin*old_x+cos*y;
		return this;
	}

	public final Vec2RW rotate(double cos, double sin)
	{
		double old_x = x;
		x = cos*x-sin*y;
		y = sin*old_x+cos*y;
		return this;
	}

}
