/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package obstacles;

import java.awt.Color;
import java.awt.Graphics;

import graphic.Fenetre;
import graphic.Printable;
import robot.RobotReal;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Un capteur de proximité du robot
 * @author pf
 *
 */

public class Capteur implements Printable
{
	public final Vec2RO positionRelative;
	public final double orientationRelative;
	public final int angleCone; // angle du cône en DEGRÉS
	public final int portee;
	
	public Capteur(Vec2RO positionRelative, double orientationRelative, int angleCone, int portee)
	{
		this.positionRelative = positionRelative;
		this.orientationRelative = orientationRelative;
		this.angleCone = angleCone;
		this.portee = portee;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		Vec2RW p1 = positionRelative.plusNewVector(robot.getPosition());
		Vec2RW p2 = p1.plusNewVector(new Vec2RO(portee, angleCone + orientationRelative + robot.getOrientation(), true));
		Vec2RW p3 = p1.plusNewVector(new Vec2RO(portee, - angleCone + orientationRelative + robot.getOrientation(), true));
		int[] x = new int[3];
		x[0] = f.XtoWindow(p1.getX());
		x[1] = f.XtoWindow(p2.getX());
		x[2] = f.XtoWindow(p3.getX());
		int[] y = new int[3];
		y[0] = f.YtoWindow(p1.getY());
		y[1] = f.YtoWindow(p2.getY());
		y[2] = f.YtoWindow(p3.getY());
		g.setColor(new Color(0, 130, 0, 50));
		g.fillArc(f.XtoWindow(p1.getX()-portee), f.YtoWindow(p1.getY()+portee), f.distanceXtoWindow(2*portee), f.distanceYtoWindow(2*portee), (int)(robot.getOrientation()+orientationRelative * 180/Math.PI - angleCone), angleCone * 2);
//		g.fillPolygon(x, y, 3);
		g.setColor(new Color(0, 130, 0, 255));
		g.drawPolygon(x, y, 3);	}

}
