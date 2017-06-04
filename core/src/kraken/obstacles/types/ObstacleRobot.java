/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.obstacles.types;

import java.io.Serializable;
import kraken.Couleur;
import kraken.utils.Vec2RO;
import kraken.utils.Vec2RW;

/**
 * Obstacle rectangulaire de notre robot
 * 
 * @author pf
 *
 */

public class ObstacleRobot extends ObstacleRectangular implements Serializable
{
	private static final long serialVersionUID = -8994485842050904808L;

	public ObstacleRobot(int demieLargeurNonDeploye, int demieLongueurArriere, int demieLongueurAvant)
	{
		super(new Vec2RW());
		c = Couleur.ROBOT.couleur;
		l = Couleur.ROBOT.l;
		centreGeometrique = new Vec2RW();
		coinBasGaucheRotate = new Vec2RW();
		coinHautGaucheRotate = new Vec2RW();
		coinBasDroiteRotate = new Vec2RW();
		coinHautDroiteRotate = new Vec2RW();

		int a = demieLargeurNonDeploye;
		int b = demieLargeurNonDeploye;
		int c = demieLongueurAvant;
		int d = demieLongueurArriere;

		coinBasGauche = new Vec2RW(-d, -a);
		coinHautGauche = new Vec2RW(-d, b);
		coinBasDroite = new Vec2RW(c, -a);
		coinHautDroite = new Vec2RW(c, b);

		demieDiagonale = Math.sqrt((a + b) * (a + b) / 4 + (c + d) * (c + d) / 4);
		centreGeometrique = new Vec2RW();
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
