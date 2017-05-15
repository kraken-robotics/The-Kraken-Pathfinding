/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package utils;

/**
 * Vecteur en lecture/écriture
 * 
 * @author pf
 *
 */

public class Vec2RW extends Vec2RO
{
	private static final long serialVersionUID = 1L;

	public Vec2RW()
	{
		super(0, 0);
	}

	public Vec2RW(double longueur, double angle, boolean useless)
	{
		super(longueur, angle, useless);
	}

	public Vec2RW(double requestedX, double requestedY)
	{
		super(requestedX, requestedY);
	}

	public final Vec2RW plus(Vec2RO other)
	{
		x += other.x;
		y += other.y;
		return this;
	}

	public final Vec2RW minus(Vec2RO other)
	{
		x -= other.x;
		y -= other.y;
		return this;
	}

	public final Vec2RW scalar(double d)
	{
		x = d * x;
		y = d * y;
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
		x = cos * x - sin * y;
		y = sin * old_x + cos * y;
		return this;
	}

	public final Vec2RW rotate(double cos, double sin)
	{
		double old_x = x;
		x = cos * x - sin * y;
		y = sin * old_x + cos * y;
		return this;
	}

	public final void rotate(double angle, Vec2RO centreRotation)
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
