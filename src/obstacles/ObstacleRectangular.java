package obstacles;

import utils.Config;
import utils.Log;
import utils.Vec2;

/**
 * Pourquoi un obstacle rectangulaire serait un cas particulier d'un obstacle rectangulaire
 * aligné, alors qu'en fait la logique veut que ce soit le contraire? C'est simplement
 * une astuce; ici, un obstacle rectangulaire est seulement un obstacle rectangulaire aligné
 * qui a subi une rotation.
 * @author pf
 *
 */

public class ObstacleRectangular extends ObstacleRectangularAligned
{
	private double cos;
	private double sin;
	
	/**
	 * Cas où l'angle est nul
	 * @param log
	 * @param config
	 * @param position
	 * @param sizeX
	 * @param sizeY
	 * @param angle
	 */
	public ObstacleRectangular(Log log, Config config, Vec2 position, int sizeX, int sizeY)
	{
		super(log, config, position, sizeX, sizeY);
		cos = 1;
		sin = 0;
	}

	/**
	 * Cet angle est celui par lequel le rectangle a été tourné.
	 * C'est donc l'opposé de l'angle par lequel on va tourner les points afin de considérer
	 * le rectangle comme aligné
	 * @param log
	 * @param config
	 * @param position
	 * @param sizeX
	 * @param sizeY
	 * @param angle
	 */
	public ObstacleRectangular(Log log, Config config, Vec2 position, int sizeX, int sizeY, double angle)
	{
		super(log, config, position, sizeX, sizeY);
		cos = Math.cos(angle);
		sin = Math.sin(angle);
	}
	
	/**
	 * Effectue la rotation d'un point, ce qui équivaut à la rotation de cet obstacle,
	 * ce qui équivaut à le faire devenir un ObstacleRectagularAligned
	 * On utilise ici -angle, ce qui explique que la formule n'est pas la
	 * formule de rotationt traditionnelle.
	 * @param point
	 * @return
	 */
	private Vec2 rotateMoinsAngle(Vec2 point)
	{
		Vec2 out = new Vec2();
		out.x = (int)(cos*point.x+sin*point.y);
		out.y = (int)(-sin*point.x+cos*point.y);
		return out;
	}
	
	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getXRotatePlusAngle(Vec2 point)
	{
		return (int)(cos*point.x-sin*point.y);
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getYRotatePlusAngle(Vec2 point)
	{
		return (int)(sin*point.x+cos*point.y);
	}

	
	/**
	 * Ce point est-il dans l'obstacle?
	 */
	public boolean isInObstacle(Vec2 point)
	{
		return super.isInObstacle(rotateMoinsAngle(point));
	}

	/**
	 * Retourne la distance au carré du point à cet obstacle
	 */
	public float squaredDistance(Vec2 in)
	{
		return super.squaredDistance(rotateMoinsAngle(in));
	}
	
	/**
	 * Calcul s'il y a collision avec un ObstacleRectangularAligned.
	 * Attention! Ne pas utiliser un ObstacleRectangular au lieu de l'ObstacleRectangularAligned!
	 * Utilise le calcul d'axe de séparation
	 * @param r
	 * @return
	 */
	public boolean isColliding(ObstacleRectangular r)
	{
		// Il faut tester les quatres axes
		return !testeSeparation(coinBasGauche.x, coinHautGauche.x, coinBasDroite.x, coinHautDroite.x, getXRotatePlusAngle(r.coinBasGauche), getXRotatePlusAngle(r.coinHautGauche), getXRotatePlusAngle(r.coinBasDroite), getXRotatePlusAngle(r.coinHautDroite))
				&& !testeSeparation(coinBasGauche.y, coinHautGauche.y, coinBasDroite.y, coinHautDroite.y, getYRotatePlusAngle(r.coinBasGauche), getYRotatePlusAngle(r.coinHautGauche), getYRotatePlusAngle(r.coinBasDroite), getYRotatePlusAngle(r.coinHautDroite))
				&& !testeSeparation(r.coinBasGauche.x, r.coinHautGauche.x, r.coinBasDroite.x, r.coinHautDroite.x, r.getXRotatePlusAngle(coinBasGauche), r.getXRotatePlusAngle(coinHautGauche), r.getXRotatePlusAngle(coinBasDroite), r.getXRotatePlusAngle(coinHautDroite))
				&& !testeSeparation(r.coinBasGauche.y, r.coinHautGauche.y, r.coinBasDroite.y, r.coinHautDroite.y, r.getYRotatePlusAngle(coinBasGauche), r.getYRotatePlusAngle(coinHautGauche), r.getYRotatePlusAngle(coinBasDroite), r.getYRotatePlusAngle(coinHautDroite));
	}
	
	private boolean testeSeparation(int a, int b, int c, int d, int a2, int b2, int c2, int d2)
	{
		int min1 = Math.min(Math.min(a, b), Math.min(c, d));
		int max1 = Math.max(Math.max(a, b), Math.max(c, d));

		int min2 = Math.min(Math.min(a2, b2), Math.min(c2, d2));
		int max2 = Math.max(Math.max(a2, b2), Math.max(c2, d2));
		
		return min1 > max2 || min2 > max1; // vrai s'il y a une séparation
	}
	
}
