/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles;

import java.io.Serializable;

import pfg.kraken.Couleur;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XY_RW;

/**
 * Obstacle rectangulaire de notre robot
 * 
 * @author pf
 *
 */

public class ObstacleRobot extends RectangularObstacle implements Serializable
{
	private static final long serialVersionUID = -8994485842050904808L;

	public ObstacleRobot(int demieLargeurGauche, int demieLargeurDroite, int demieLongueurArriere, int demieLongueurAvant)
	{
		super(new XY_RW());
		c = Couleur.ROBOT.couleur;
		l = Couleur.ROBOT.l;
		centreGeometrique = new XY_RW();
		coinBasGaucheRotate = new XY_RW();
		coinHautGaucheRotate = new XY_RW();
		coinBasDroiteRotate = new XY_RW();
		coinHautDroiteRotate = new XY_RW();

		int a = demieLargeurDroite;
		int b = demieLargeurGauche;
		int c = demieLongueurAvant;
		int d = demieLongueurArriere;

		coinBasGauche = new XY_RW(-d, -a);
		coinHautGauche = new XY_RW(-d, b);
		coinBasDroite = new XY_RW(c, -a);
		coinHautDroite = new XY_RW(c, b);

		demieDiagonale = Math.sqrt((a + b) * (a + b) / 4 + (c + d) * (c + d) / 4);
		centreGeometrique = new XY_RW();
	}

	/**
	 * Mise à jour de l'obstacle
	 * 
	 * @param position
	 * @param orientation
	 * @param robot
	 * @return
	 */
	public RectangularObstacle update(XY position, double orientation)
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

	/**
	 * Met à jour cet obstacle
	 * 
	 * @param obstacle
	 */
	public void copy(ObstacleRobot obstacle)
	{
		obstacle.update(position, angle);
	}
}
