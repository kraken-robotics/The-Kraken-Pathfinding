package utils;

/**
 * Classe de calcul de vecteurs de dimension 2
 * @author martial
 * @author pf
 *
 */

public class Vec2
{

	public int x;
	public int y;
	
	public Vec2()
	{
		x = 0;
		y = 0;
	}
	
	public Vec2(double angle)
	{
		x = (int) (Math.cos(angle)*1000);
		y = (int) (Math.sin(angle)*1000);
	}

	public Vec2(Vec2 model)
	{
		x = model.x;
		y = model.y;
	}

	public Vec2(int requestedX, int requestedY)
	{
		x = requestedX;
		y = requestedY;
	}

	// Do not square a length, use squared length directly
	// to increase performances
	public int squaredLength()
	{
		return x*x + y*y;
	}

	// Returns this vec2's magnitude
	public double length()
	{
		return Math.hypot(x, y);
	}
	
	// dot product
	public int dot(Vec2 other)
	{
		return x*other.x + y*other.y;
	}
	

	// build a new Vec2 by summing the calling Vec2 and the one in args
	public Vec2 plusNewVector(Vec2 other)
	{
		return new Vec2(x + other.x, y + other.y);
	}
	
	// build a new Vec2 with the value obtained by decrementing the
	// calling Vec2 by the provided Vec2 in args
	public Vec2 minusNewVector(Vec2 other)
	{
		return new Vec2(x - other.x, y - other.y);
	}

	public void plus(Vec2 other)
	{
		x += other.x;
		y += other.y;
	}
	
	public void minus(Vec2 other)
	{
		x -= other.x;
		y -= other.y;
	}

	public Vec2 clone()
	{
		return new Vec2(this.x, this.y);
	}
	
	public int squaredDistance(Vec2 other)
	{
		int tmp_x = x-other.x, tmp_y = y-other.y;
		return tmp_x*tmp_x + tmp_y*tmp_y;
	}

	public double distance(Vec2 other)
	{
		return Math.sqrt(squaredDistance(other));
	}
	
	public String toString()
	{
		return "("+x+","+y+")";
	}
	
	public boolean equals(Vec2 other)
	{
		return x == other.x && y == other.y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		else if (obj == null)
			return false;
		else if (!(obj instanceof Vec2))
			return false;
		Vec2 other = (Vec2) obj;
		if (x != other.x || (y != other.y))
			return false;
		return true;
	}

	/**
	 * Copie this dans other.
	 * @param other
	 */
	public void copy(Vec2 other)
	{
	    other.x = x;
	    other.y = y;
	}

	public Vec2 middleNewVector(Vec2 b)
	{
		return new Vec2((x+b.x)/2, (y+b.y)/2);
	}

	public void scalar(double d)
	{
		x = (int)(d*x);
		y = (int)(d*y);
	}
	
	public Vec2 scalarNewVector(double d)
	{
		Vec2 out = new Vec2(this);
		out.scalar(d);
		return out;
	}
	
	public Vec2 rotateNewVector(double angle, Vec2 centreRotation)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vec2((int)(cos*(x-centreRotation.x)-sin*(y-centreRotation.y))+centreRotation.x,
		(int)(sin*(x-centreRotation.x)+cos*(y-centreRotation.y))+centreRotation.y);
	}

	/**
	 * Rotation avec pour centre de rotation (0,0)
	 * @param d
	 * @return
	 */
	public Vec2 rotateNewVector(double angle)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vec2((int)(cos*x-sin*y),	(int)(sin*x+cos*y));
	}

	/**
	 * Normalise le vecteur
	 */
	public void normalize()
	{
		double longueur = length();
		if(longueur != 0)
		{
			x /= longueur;
			y /= longueur;
		}
	}

	/**
	 * Renvoie le vecteur normalisé
	 * @return
	 */
	public Vec2 normalizeNewVector()
	{
		Vec2 out = new Vec2(this);
		out.normalize();
		return out;
	}
	
	public double getArgument()
	{
		return Math.atan2(y, x);
	}
	
	/**
	 * Fait le projeté de this sur other, puis renvoie le résultat dans un nouveau Vec2
	 * @param other
	 * @return
	 */
	public Vec2 projectOnNewVector(Vec2 other)
	{
		return scalarNewVector(dot(other));
	}

}

