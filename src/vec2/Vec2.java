package vec2;


/**
 * Classe de calcul de vecteurs de dimension 2
 * @author pf
 *
 */

public class Vec2<T extends ReadOnly>
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

	public Vec2(Vec2<? extends ReadOnly> model)
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
	public int dot(Vec2<? extends ReadOnly> other)
	{
		return x*other.x + y*other.y;
	}

	// build a new Vec2 by summing the calling Vec2 and the one in args
	public Vec2<ReadWrite> plusNewVector(Vec2<? extends ReadOnly> other)
	{
		return new Vec2<ReadWrite>(x + other.x, y + other.y);
	}
	
	// build a new Vec2 with the value obtained by decrementing the
	// calling Vec2 by the provided Vec2 in args
	public Vec2<ReadWrite> minusNewVector(Vec2<? extends ReadOnly> other)
	{
		return new Vec2<ReadWrite>(x - other.x, y - other.y);
	}

	public static Vec2<ReadWrite> plus(Vec2<ReadWrite> modified, Vec2<? extends ReadOnly> other)
	{
		modified.x += other.x;
		modified.y += other.y;
		return modified;
	}
	
	public static Vec2<ReadWrite> minus(Vec2<ReadWrite> modified, Vec2<? extends ReadOnly> other)
	{
		modified.x -= other.x;
		modified.y -= other.y;
		return modified;
	}

	public Vec2<ReadWrite> clone()
	{
		return new Vec2<ReadWrite>(this.x, this.y);
	}

	@SuppressWarnings("unchecked")
	public Vec2<ReadOnly> getReadOnly()
	{
		return (Vec2<ReadOnly>) this;
	}

	public int squaredDistance(Vec2<? extends ReadOnly> other)
	{
		int tmp_x = x-other.x, tmp_y = y-other.y;
		return tmp_x*tmp_x + tmp_y*tmp_y;
	}

	public double distance(Vec2<? extends ReadOnly> other)
	{
		return Math.sqrt(squaredDistance(other));
	}
	
	public String toString()
	{
		return "("+x+","+y+")";
	}
	
	public boolean equals(Vec2<? extends ReadOnly> other)
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
		@SuppressWarnings("unchecked")
		Vec2<ReadOnly> other = (Vec2<ReadOnly>) obj;
		if (x != other.x || (y != other.y))
			return false;
		return true;
	}

	/**
	 * Copie this dans other.
	 * @param other
	 */
	public void copy(Vec2<ReadWrite> other)
	{
	    other.x = x;
	    other.y = y;
	}

	public Vec2<ReadWrite> middleNewVector(Vec2<? extends ReadOnly> b)
	{
		return new Vec2<ReadWrite>((x+b.x)/2, (y+b.y)/2);
	}

	public static Vec2<ReadWrite> scalar(Vec2<ReadWrite> v, double d)
	{
		v.x = (int)(d*v.x);
		v.y = (int)(d*v.y);
		return v;
	}
	
	public Vec2<ReadWrite> scalarNewVector(double d)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>(this);
		scalar(out, d);
		return out;
	}
	
/*	@SuppressWarnings("unchecked")
	public static Vec2<ReadOnly> lectureSeule(Vec2<? extends ReadOnly> v)
	{
		return (Vec2<ReadOnly>) v;
	}*/
	
	public Vec2<ReadWrite> rotateNewVector(double angle, Vec2<? extends ReadOnly> centreRotation)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vec2<ReadWrite>((int)(cos*(x-centreRotation.x)-sin*(y-centreRotation.y))+centreRotation.x,
		(int)(sin*(x-centreRotation.x)+cos*(y-centreRotation.y))+centreRotation.y);
	}

	/**
	 * Rotation avec pour centre de rotation (0,0)
	 * @param d
	 * @return
	 */
	public Vec2<ReadWrite> rotateNewVector(double angle)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vec2<ReadWrite>((int)(cos*x-sin*y), (int)(sin*x+cos*y));
	}

	/**
	 * Normalise le vecteur
	 */
	public static Vec2<ReadWrite> normalize(Vec2<ReadWrite> v)
	{
		double longueur = v.length();
		if(longueur != 0)
		{
			v.x /= longueur;
			v.y /= longueur;
		}
		return v;
	}

	/**
	 * Renvoie le vecteur normalisé
	 * @return
	 */
	public Vec2<ReadWrite> normalizeNewVector()
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>(this);
		normalize(out);
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
	public Vec2<ReadWrite> projectOnNewVector(Vec2<? extends ReadOnly> other)
	{
		return scalarNewVector(dot(other));
	}

}

