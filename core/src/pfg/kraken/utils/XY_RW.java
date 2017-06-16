/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.utils;

/**
 * Vecteur en lecture/écriture
 * 
 * @author pf
 *
 */

public class XY_RW extends XY
{
	private static final long serialVersionUID = 1L;

	public XY_RW()
	{
		super(0, 0);
	}

	public XY_RW(double longueur, double angle, boolean useless)
	{
		super(longueur, angle, useless);
	}

	public XY_RW(double requestedX, double requestedY)
	{
		super(requestedX, requestedY);
	}

	public final XY_RW plus(XY other)
	{
		x += other.x;
		y += other.y;
		return this;
	}

	public final XY_RW minus(XY other)
	{
		x -= other.x;
		y -= other.y;
		return this;
	}

	public final XY_RW scalar(double d)
	{
		x = d * x;
		y = d * y;
		return this;
	}

	public final XY_RW Ysym(boolean symetrie)
	{
		if(symetrie)
			y = -y;
		return this;
	}

	public final XY_RW rotate(double angle)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double old_x = x;
		x = cos * x - sin * y;
		y = sin * old_x + cos * y;
		return this;
	}

	public final XY_RW rotate(double cos, double sin)
	{
		double old_x = x;
		x = cos * x - sin * y;
		y = sin * old_x + cos * y;
		return this;
	}

	public final void rotate(double angle, XY centreRotation)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double tmpx = cos * (x - centreRotation.x) - sin * (y - centreRotation.y) + centreRotation.x;
		y = sin * (x - centreRotation.x) + cos * (y - centreRotation.y) + centreRotation.y;
		x = tmpx;
	}

	public final void setX(double x)
	{
		this.x = x;
	}

	public final void setY(double y)
	{
		this.y = y;
	}

	public void set(double longueur, double angle)
	{
		x = Math.cos(angle) * longueur;
		y = Math.sin(angle) * longueur;
	}
}
