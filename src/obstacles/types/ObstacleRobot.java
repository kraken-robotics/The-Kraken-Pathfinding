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

package obstacles.types;

import graphic.Fenetre;
import graphic.printable.Couleur;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import robot.RobotReal;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Obstacle rectangulaire de notre robot
 * 
 * @author pf
 *
 */

public class ObstacleRobot extends ObstacleRectangular implements Serializable
{
	private static final long serialVersionUID = -8994485842050904808L;

	protected static boolean withMarge = true;

	protected Vec2RW coinBasGaucheImg;
	protected Vec2RW coinHautGaucheImg;
	protected Vec2RW coinBasDroiteImg;
	protected Vec2RW coinHautDroiteImg;

	protected Vec2RW coinBasGaucheRotateImg;
	protected Vec2RW coinHautGaucheRotateImg;
	protected Vec2RW coinBasDroiteRotateImg;
	protected Vec2RW coinHautDroiteRotateImg;

	protected double demieDiagonaleImg;

	public ObstacleRobot(int demieLargeurNonDeploye, int demieLongueurArriere, int demieLongueurAvant, int marge)
	{
		super(new Vec2RW());
		c = Couleur.ROBOT.couleur;
		l = Couleur.ROBOT.l;
		centreGeometrique = new Vec2RW();
		coinBasGaucheRotate = new Vec2RW();
		coinHautGaucheRotate = new Vec2RW();
		coinBasDroiteRotate = new Vec2RW();
		coinHautDroiteRotate = new Vec2RW();
		coinBasGaucheRotateImg = new Vec2RW();
		coinHautGaucheRotateImg = new Vec2RW();
		coinBasDroiteRotateImg = new Vec2RW();
		coinHautDroiteRotateImg = new Vec2RW();

		int a = demieLargeurNonDeploye + marge;
		int b = demieLargeurNonDeploye + marge;
		int c = demieLongueurAvant;// + marge / 2;
		int d = demieLongueurArriere;// + marge / 2;

		int a2 = demieLargeurNonDeploye;
		int b2 = demieLargeurNonDeploye;
		int c2 = demieLongueurAvant;
		int d2 = demieLongueurArriere;

		coinBasGauche = new Vec2RW(-d, -a);
		coinHautGauche = new Vec2RW(-d, b);
		coinBasDroite = new Vec2RW(c, -a);
		coinHautDroite = new Vec2RW(c, b);

		coinBasGaucheImg = new Vec2RW(-d2, -a2);
		coinHautGaucheImg = new Vec2RW(-d2, b2);
		coinBasDroiteImg = new Vec2RW(c2, -a2);
		coinHautDroiteImg = new Vec2RW(c2, b2);

		demieDiagonale = Math.sqrt((a + b) * (a + b) / 4 + (c + d) * (c + d) / 4);
		demieDiagonaleImg = Math.sqrt((a2 + b2) * (a2 + b2) / 4 + (c2 + d2) * (c2 + d2) / 4);
		centreGeometrique = new Vec2RW();
	}

	public static void setMarge(boolean withMarge)
	{
		ObstacleRobot.withMarge = withMarge;
	}

	/**
	 * Mise à jour de l'obstacle
	 * 
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
		convertitVersRepereTable(coinBasGaucheImg, coinBasGaucheRotateImg);
		convertitVersRepereTable(coinHautGaucheImg, coinHautGaucheRotateImg);
		convertitVersRepereTable(coinBasDroiteImg, coinBasDroiteRotateImg);
		convertitVersRepereTable(coinHautDroiteImg, coinHautDroiteRotateImg);
		coinBasDroiteRotate.copy(centreGeometrique);
		centreGeometrique = centreGeometrique.plus(coinHautGaucheRotate).scalar(0.5);

		if(printAllObstacles)
			synchronized(buffer)
			{
				buffer.notify();
			}

		return this;
	}

/*	private double getAngleRoue(boolean roueDroite, double courbure)
	{
		if(Math.abs(courbure) < 0.01)
			return 0;
		double R = Math.abs(1000 / courbure); // le rayon de courbure
		if(roueDroite)
			return Math.signum(courbure) * Math.atan2(L, Math.abs(d + R));
		return Math.signum(courbure) * Math.atan2(L, Math.abs(R - d));
	}*/

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		if(!robot.isCinematiqueInitialised())
			return;

		if(coinBasDroiteRotate == null)
			return;

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

		if(imageRobot != null && imageRobotRoueD != null && imageRobotRoueG != null)
		{
			Graphics2D g2d = (Graphics2D) g;
			Vec2RW centreRotationDroiteRotate = new Vec2RW();
			convertitVersRepereTable(centreRotationDroite, centreRotationDroiteRotate);
			AffineTransform trans = new AffineTransform();
			trans.setTransform(new AffineTransform());
			trans.translate(f.XtoWindow((int) centreRotationDroiteRotate.getX()), f.YtoWindow((int) centreRotationDroiteRotate.getY()));
			trans.rotate(-angle - robot.getAngleRoueDroite());
			trans.translate(f.distanceXtoWindow(-(int) centreRotationDroite.getX()), f.distanceYtoWindow((int) centreRotationDroite.getY()));
			trans.translate(f.distanceXtoWindow((int) coinHautGaucheImg.getX()), f.distanceYtoWindow(-(int) coinHautGaucheImg.getY()));
			trans.scale(1. * f.distanceXtoWindow((int) coinHautGaucheImg.distance(coinHautDroiteImg)) / imageRobot.getWidth(null), 1. * f.distanceXtoWindow((int) coinHautGaucheImg.distance(coinBasGaucheImg)) / imageRobot.getHeight(null));
			g2d.drawImage(imageRobotRoueD, trans, null);

			Vec2RW centreRotationGaucheRotate = new Vec2RW();
			convertitVersRepereTable(centreRotationGauche, centreRotationGaucheRotate);
			trans = new AffineTransform();
			trans.setTransform(new AffineTransform());
			trans.translate(f.XtoWindow((int) centreRotationGaucheRotate.getX()), f.YtoWindow((int) centreRotationGaucheRotate.getY()));
			trans.rotate(-angle - robot.getAngleRoueGauche());
			trans.translate(f.distanceXtoWindow(-(int) centreRotationGauche.getX()), f.distanceYtoWindow((int) centreRotationGauche.getY()));
			trans.translate(f.distanceXtoWindow((int) coinHautGaucheImg.getX()), f.distanceYtoWindow(-(int) coinHautGaucheImg.getY()));
			trans.scale(1. * f.distanceXtoWindow((int) coinHautGaucheImg.distance(coinHautDroiteImg)) / imageRobot.getWidth(null), 1. * f.distanceXtoWindow((int) coinHautGaucheImg.distance(coinBasGaucheImg)) / imageRobot.getHeight(null));
			g2d.drawImage(imageRobotRoueG, trans, null);

			trans = new AffineTransform();
			trans.setTransform(new AffineTransform());
			trans.translate(f.XtoWindow(position.getX()), f.YtoWindow(position.getY()));
			trans.rotate(-angle);
			trans.translate(f.distanceXtoWindow((int) coinHautGaucheImg.getX()), f.distanceYtoWindow(-(int) coinHautGaucheImg.getY()));
			trans.scale(1. * f.distanceXtoWindow((int) coinHautGaucheImg.distance(coinHautDroiteImg)) / imageRobot.getWidth(null), 1. * f.distanceXtoWindow((int) coinHautGaucheImg.distance(coinBasGaucheImg)) / imageRobot.getHeight(null));
			g2d.drawImage(imageRobot, trans, null);
		}
	}

	@Override
	public double getDemieDiagonale()
	{
		if(withMarge)
			return super.getDemieDiagonale();
		return demieDiagonaleImg;
	}

	@Override
	public double squaredDistance(Vec2RO v)
	{
		if(withMarge)
			return super.squaredDistance(v);

		convertitVersRepereObstacle(v, in);

		// log.debug("in = : "+in);
		/*
		 * Schéma de la situation :
		 * y
		 * 4 | 3 | 2 ^
		 * ____________________________________ |
		 * | |
		 * 5 | obstacle | 1
		 * ____________________________________
		 * 6 | 7 | 8
		 */

		// si le point fourni est dans les quarts de plan n°2,4,6 ou 8
		if(in.getX() < coinBasGaucheImg.getX() && in.getY() < coinBasGaucheImg.getY())
			return in.squaredDistance(coinBasGaucheImg);

		else if(in.getX() < coinHautGaucheImg.getX() && in.getY() > coinHautGaucheImg.getY())
			return in.squaredDistance(coinHautGaucheImg);

		else if(in.getX() > coinBasDroiteImg.getX() && in.getY() < coinBasDroiteImg.getY())
			return in.squaredDistance(coinBasDroiteImg);

		else if(in.getX() > coinHautDroiteImg.getX() && in.getY() > coinHautDroiteImg.getY())
			return in.squaredDistance(coinHautDroiteImg);

		// Si le point fourni est dans les demi-bandes n°1,3,5,ou 7
		if(in.getX() > coinHautDroiteImg.getX())
			return (in.getX() - coinHautDroiteImg.getX()) * (in.getX() - coinHautDroiteImg.getX());

		else if(in.getX() < coinBasGaucheImg.getX())
			return (in.getX() - coinBasGaucheImg.getX()) * (in.getX() - coinBasGaucheImg.getX());

		else if(in.getY() > coinHautDroiteImg.getY())
			return (in.getY() - coinHautDroiteImg.getY()) * (in.getY() - coinHautDroiteImg.getY());

		else if(in.getY() < coinBasGaucheImg.getY())
			return (in.getY() - coinBasGaucheImg.getY()) * (in.getY() - coinBasGaucheImg.getY());

		// Sinon, on est dans l'obstacle
		return 0;
	}

	@Override
	public final boolean isColliding(ObstacleRectangular r)
	{
		if(withMarge)
			return super.isColliding(r);

		// Calcul simple permettant de vérifier les cas absurdes où les
		// obstacles sont loin l'un de l'autre
		if(centreGeometrique.squaredDistance(r.centreGeometrique) >= (demieDiagonale + r.demieDiagonale) * (demieDiagonale + r.demieDiagonale))
			return false;
		// Il faut tester les quatres axes
		return !testeSeparation(coinBasGaucheImg.getX(), coinBasDroiteImg.getX(), getXConvertiVersRepereObstacle(r.coinBasGaucheRotate), getXConvertiVersRepereObstacle(r.coinHautGaucheRotate), getXConvertiVersRepereObstacle(r.coinBasDroiteRotate), getXConvertiVersRepereObstacle(r.coinHautDroiteRotate)) && !testeSeparation(coinBasGaucheImg.getY(), coinHautGaucheImg.getY(), getYConvertiVersRepereObstacle(r.coinBasGaucheRotate), getYConvertiVersRepereObstacle(r.coinHautGaucheRotate),
				getYConvertiVersRepereObstacle(r.coinBasDroiteRotate), getYConvertiVersRepereObstacle(r.coinHautDroiteRotate)) && !testeSeparation(r.coinBasGauche.getX(), r.coinBasDroite.getX(), r.getXConvertiVersRepereObstacle(coinBasGaucheRotateImg), r.getXConvertiVersRepereObstacle(coinHautGaucheRotateImg), r.getXConvertiVersRepereObstacle(coinBasDroiteRotateImg), r.getXConvertiVersRepereObstacle(coinHautDroiteRotateImg)) && !testeSeparation(r.coinBasGauche.getY(), r.coinHautGauche.getY(), r
						.getYConvertiVersRepereObstacle(coinBasGaucheRotateImg), r.getYConvertiVersRepereObstacle(coinHautGaucheRotateImg), r.getYConvertiVersRepereObstacle(coinBasDroiteRotateImg), r.getYConvertiVersRepereObstacle(coinHautDroiteRotateImg));
	}

	/**
	 * Met à jour cet obstacle
	 * 
	 * @param obstacle
	 */
	public void copy(ObstacleRobot obstacle)
	{
		obstacle.update(position, angle);
	}

	public static boolean getMarge()
	{
		return withMarge;
	}

}
