package obstacles;

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

	protected Vec2 coinBasGaucheRotate;
	protected Vec2 coinHautGaucheRotate;
	protected Vec2 coinBasDroiteRotate;
	protected Vec2 coinHautDroiteRotate;
	
	/**
	 * Cas où l'angle est nul
	 * @param log
	 * @param config
	 * @param position
	 * @param sizeX
	 * @param sizeY
	 * @param angle
	 */
	public ObstacleRectangular(Vec2 position, int sizeX, int sizeY)
	{
		this(position, sizeX, sizeY, 0);
		cos = 1;
		sin = 0;
		coinBasGaucheRotate = coinBasGauche;
		coinHautGaucheRotate = coinHautGauche;
		coinBasDroiteRotate = coinBasDroite;
		coinHautDroiteRotate = coinHautDroite;
	}

	/**
	 * Constructeur user-friendly pour le pathfinding
	 * @param depart
	 * @param arrivee
	 */
	public ObstacleRectangular(Vec2 depart, Vec2 arrivee)
	{
		this(depart.middleNewVector(arrivee), (int)depart.distance(arrivee)+longueur_robot+2*marge, largeur_robot+2*marge, Math.atan2(arrivee.y-depart.y, arrivee.x-depart.x));
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
	public ObstacleRectangular(Vec2 position, int sizeX, int sizeY, double angle)
	{
		super(position, sizeX, sizeY);
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		coinBasGaucheRotate = rotatePlusAngle(coinBasGauche);
		coinHautGaucheRotate = rotatePlusAngle(coinHautGauche);
		coinBasDroiteRotate = rotatePlusAngle(coinBasDroite);
		coinHautDroiteRotate = rotatePlusAngle(coinHautDroite);
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
		out.x = (int)(cos*(point.x-position.x)+sin*(point.y-position.y))+position.x;
		out.y = (int)(-sin*(point.x-position.x)+cos*(point.y-position.y))+position.y;
		return out;
	}

	/**
	 * Rotation dans le sens +angle
	 * @param point
	 * @return
	 */
	private Vec2 rotatePlusAngle(Vec2 point)
	{
		Vec2 out = new Vec2();
		out.x = (int)(cos*(point.x-position.x)-sin*(point.y-position.y))+position.x;
		out.y = (int)(sin*(point.x-position.x)+cos*(point.y-position.y))+position.y;
		return out;
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getXRotatePlusAngle(Vec2 point)
	{
		return (int)(cos*(point.x-position.x)-sin*(point.y-position.y))+position.x;
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getYRotatePlusAngle(Vec2 point)
	{
		return (int)(sin*(point.x-position.x)+cos*(point.y-position.y))+position.y;
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getXRotateMoinsAngle(Vec2 point)
	{
		return (int)(cos*(point.x-position.x)+sin*(point.y-position.y))+position.x;
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getYRotateMoinsAngle(Vec2 point)
	{
		return (int)(-sin*(point.x-position.x)+cos*(point.y-position.y))+position.y;
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
/*		log.debug("coinBasGaucheRotate = "+coinBasGaucheRotate, this);
		log.debug("coinHautGaucheRotate = "+coinHautGaucheRotate, this);
		log.debug("coinHautDroiteRotate = "+coinHautDroiteRotate, this);
		log.debug("coinBasDroiteRotate = "+coinBasDroiteRotate, this);*/
		
		// Il faut tester les quatres axes
		return !testeSeparation(coinBasGauche.x, coinBasDroite.x, getXRotateMoinsAngle(r.coinBasGaucheRotate), getXRotateMoinsAngle(r.coinHautGaucheRotate), getXRotateMoinsAngle(r.coinBasDroiteRotate), getXRotateMoinsAngle(r.coinHautDroiteRotate))
				&& !testeSeparation(coinBasGauche.y, coinHautGauche.y, getYRotateMoinsAngle(r.coinBasGaucheRotate), getYRotateMoinsAngle(r.coinHautGaucheRotate), getYRotateMoinsAngle(r.coinBasDroiteRotate), getYRotateMoinsAngle(r.coinHautDroiteRotate))
				&& !testeSeparation(r.coinBasGauche.x, r.coinBasDroite.x, r.getXRotateMoinsAngle(coinBasGaucheRotate), r.getXRotateMoinsAngle(coinHautGaucheRotate), r.getXRotateMoinsAngle(coinBasDroiteRotate), r.getXRotateMoinsAngle(coinHautDroiteRotate))
				&& !testeSeparation(r.coinBasGauche.y, r.coinHautGauche.y, r.getYRotateMoinsAngle(coinBasGaucheRotate), r.getYRotateMoinsAngle(coinHautGaucheRotate), r.getYRotateMoinsAngle(coinBasDroiteRotate), r.getYRotateMoinsAngle(coinHautDroiteRotate));
	}
	
	/**
	 * Teste la séparation à partir des projections.
	 * Vérifie simplement si a et b sont bien séparés de a2, b2, c2 et d2,
	 * c'est-à-dire s'il existe x tel que a < x, b < x et
	 * a2 > x, b2 > x, c2 > x, d2 > x
	 * @param a
	 * @param b
	 * @param a2
	 * @param b2
	 * @param c2
	 * @param d2
	 * @return
	 */
	private boolean testeSeparation(int a, int b, int a2, int b2, int c2, int d2)
	{
		int min1 = Math.min(a,b);
		int max1 = Math.max(a,b);

		int min2 = Math.min(Math.min(a2, b2), Math.min(c2, d2));
		int max2 = Math.max(Math.max(a2, b2), Math.max(c2, d2));

/*		log.debug("a = "+a+", b = "+b+", a2 = "+a2+", b2 = "+b2+", c2 = "+c2+", d2 = "+d2, this);
		
		if(min1 > max2 || min2 > max1)
			log.debug("Séparation!", this);
		else
			log.debug("Pas séparation", this);*/
		
		return min1 > max2 || min2 > max1; // vrai s'il y a une séparation
	}
	
	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public int[] getXPositions()
	{
		int[] X = new int[4];
		X[0] = getXRotatePlusAngle(coinBasDroite);
		X[1] = getXRotatePlusAngle(coinHautDroite);
		X[2] = getXRotatePlusAngle(coinHautGauche);
		X[3] = getXRotatePlusAngle(coinBasGauche);
		return X;
	}

	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public int[] getYPositions()
	{
		int[] Y = new int[4];
		Y[0] = getYRotatePlusAngle(coinBasDroite);
		Y[1] = getYRotatePlusAngle(coinHautDroite);
		Y[2] = getYRotatePlusAngle(coinHautGauche);
		Y[3] = getYRotatePlusAngle(coinBasGauche);
		return Y;
	}

	public boolean isColliding(ObstacleCircular o)
	{
		return squaredDistance(o.position) < o.radius*o.radius;
	}
	
	public boolean isColliding(Obstacle o)
	{
		if(o instanceof ObstacleRectangular)
			return isColliding((ObstacleRectangular)o);
		else if(o instanceof ObstacleCircular)
			return isColliding((ObstacleCircular)o);

		log.critical("Appel de isColliding avec un type d'obstacle inconnu!", this);
		return false;
	}
	

}
