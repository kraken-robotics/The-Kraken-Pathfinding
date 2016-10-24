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

package obstacles.types;

import graphic.Fenetre;
import graphic.printable.Couleur;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import robot.RobotReal;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Obstacle rectangulaire de notre robot
 * @author pf
 *
 */

public class ObstacleRobot extends ObstacleRectangular
{
	public ObstacleRobot(RobotReal robot)
	{
		super(new Vec2RW());
		c = Couleur.ROBOT.couleur;
		centreGeometrique = new Vec2RW();
		coinBasGaucheRotate = new Vec2RW();
		coinHautGaucheRotate = new Vec2RW();
		coinBasDroiteRotate = new Vec2RW();
		coinHautDroiteRotate = new Vec2RW();
		int a = robot.getDemieLargeurDroite();
		int b = robot.getDemieLargeurGauche();
		int c = robot.getDemieLongueurAvant();
		int d = robot.getDemieLongueurArriere();
		coinBasGauche = new Vec2RW(-d, -a);
		coinHautGauche = new Vec2RW(-d, b);
		coinBasDroite = new Vec2RW(c, -a);
		coinHautDroite = new Vec2RW(c, b);
		demieDiagonale = Math.sqrt((a+b)*(a+b)/4+(c+d)*(c+d)/4);
		centreGeometrique = new Vec2RW();
	}	
	/**
	 * Mise à jour de l'obstacle
	 * @param position
	 * @param orientation
	 * @param robot
	 * @return
	 */
	public ObstacleRectangular update(Vec2RO position, double orientation)
	{
		position.copy(this.position);
		this.angle = orientation;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		this.angle = orientation;
		convertitVersRepereTable(coinBasGauche, coinBasGaucheRotate);
		convertitVersRepereTable(coinHautGauche, coinHautGaucheRotate);
		convertitVersRepereTable(coinBasDroite, coinBasDroiteRotate);
		convertitVersRepereTable(coinHautDroite, coinHautDroiteRotate);
		coinBasDroiteRotate.copy(centreGeometrique);
		centreGeometrique = centreGeometrique.plus(coinHautGaucheRotate).scalar(0.5);

		if(printAllObstacles)
			synchronized(buffer)
			{
				buffer.notify();
			}
	
		return this;
	}

	private double getAngleRoue(boolean roueDroite, double courbure)
	{
		if(Math.abs(courbure) < 0.01)
			return 0;
		double R = Math.abs(1000 / courbure); // le rayon de courbure
		if(roueDroite)
			return Math.signum(courbure) * Math.atan2(L, Math.abs(d+R));
		return Math.signum(courbure) * Math.atan2(L, Math.abs(R-d));
	}
	
	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		// TODO print image
		if(coinBasDroiteRotate == null)
			return;

		if(imageRobot != null && imageRobotRoueD != null && imageRobotRoueG != null)
		{
			Graphics2D g2d = (Graphics2D)g;
			Vec2RW centreRotationDroiteRotate = new Vec2RW();
			convertitVersRepereTable(centreRotationDroite, centreRotationDroiteRotate);			
			AffineTransform trans = new AffineTransform();
			trans.setTransform(new AffineTransform());
			trans.translate(f.XtoWindow((int)centreRotationDroiteRotate.getX()), f.YtoWindow((int)centreRotationDroiteRotate.getY()));
			trans.rotate(-angle-getAngleRoue(true, robot.getCinematique().courbureReelle));
			trans.translate(f.distanceXtoWindow(-(int)centreRotationDroite.getX()), f.distanceYtoWindow((int)centreRotationDroite.getY()));
			trans.translate(f.distanceXtoWindow((int)coinHautGauche.getX()), f.distanceYtoWindow(-(int)coinHautGauche.getY()));
			trans.scale(1. * f.distanceXtoWindow((int)coinHautGauche.distance(coinHautDroite)) / imageRobot.getWidth(null),
					1. * f.distanceXtoWindow((int)coinHautGauche.distance(coinBasGauche)) / imageRobot.getHeight(null));
			g2d.drawImage(imageRobotRoueD, trans, null);

			Vec2RW centreRotationGaucheRotate = new Vec2RW();
			convertitVersRepereTable(centreRotationGauche, centreRotationGaucheRotate);			
			trans = new AffineTransform();
			trans.setTransform(new AffineTransform());
			trans.translate(f.XtoWindow((int)centreRotationGaucheRotate.getX()), f.YtoWindow((int)centreRotationGaucheRotate.getY()));
			trans.rotate(-angle-getAngleRoue(false, robot.getCinematique().courbureReelle));
			trans.translate(f.distanceXtoWindow(-(int)centreRotationGauche.getX()), f.distanceYtoWindow((int)centreRotationGauche.getY()));
			trans.translate(f.distanceXtoWindow((int)coinHautGauche.getX()), f.distanceYtoWindow(-(int)coinHautGauche.getY()));
			trans.scale(1. * f.distanceXtoWindow((int)coinHautGauche.distance(coinHautDroite)) / imageRobot.getWidth(null),
					1. * f.distanceXtoWindow((int)coinHautGauche.distance(coinBasGauche)) / imageRobot.getHeight(null));
			g2d.drawImage(imageRobotRoueG, trans, null);
			
			trans = new AffineTransform();
			trans.setTransform(new AffineTransform());
			trans.translate(f.XtoWindow(position.getX()), f.YtoWindow(position.getY()));
			trans.rotate(-angle);
			trans.translate(f.distanceXtoWindow((int)coinHautGauche.getX()), f.distanceYtoWindow(-(int)coinHautGauche.getY()));
			trans.scale(1. * f.distanceXtoWindow((int)coinHautGauche.distance(coinHautDroite)) / imageRobot.getWidth(null),
					1. * f.distanceXtoWindow((int)coinHautGauche.distance(coinBasGauche)) / imageRobot.getHeight(null));
			g2d.drawImage(imageRobot, trans, null);
		}
		else
		{
			g.setColor(c);
			
			int[] X = new int[4];
			X[0] = (int) coinBasDroiteRotate.getX();
			X[1] = (int) coinHautDroiteRotate.getX();
			X[2] = (int) coinHautGaucheRotate.getX();
			X[3] = (int) coinBasGaucheRotate.getX();
	
			int[] Y = new int[4];
			Y[0] = (int) coinBasDroiteRotate.getY();
			Y[1] = (int) coinHautDroiteRotate.getY();
			Y[2] = (int) coinHautGaucheRotate.getY();
			Y[3] = (int) coinBasGaucheRotate.getY();
			
			for(int i = 0; i < 4; i++)
			{
				X[i] = f.XtoWindow(X[i]);
				Y[i] = f.YtoWindow(Y[i]);
			}
			g.fillPolygon(X, Y, 4);
		}
	}
	
	/**
	 * Met à jour cet obstacle
	 * @param obstacle
	 */
	public void copy(ObstacleRobot obstacle)
	{
		obstacle.update(position, angle);
	}

	
}
