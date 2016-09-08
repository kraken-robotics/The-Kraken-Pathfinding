package utils;

/**
 * Vecteur en lecture seule
 * @author pf
 *
 */

public class Vec2RO extends Vec2
{
	public Vec2RO()
	{
		super();
	}

	public Vec2RO(double longueur, double angle, boolean useless)
	{
		super(longueur, angle, useless);
	}

	public Vec2RO(double requestedX, double requestedY)
	{
		super(requestedX, requestedY);
	}

	private static final long serialVersionUID = 1L;
}
