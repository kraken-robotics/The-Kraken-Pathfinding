package obstacles.types;

import robot.RobotChrono;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

/**
 * Rectangle ayant subi une rotation.
 * Ce rectangle peut être le robot, ou bien l'espace que parcourera le robot pendant un segment
 * @author pf
 *
 */

public class ObstacleRectangular extends Obstacle
{
	// Position est le centre de rotation, c'est-à-dire le croisement des deux diagonales
	
	// Longueur entre le centre et un des coins
	protected double demieDiagonale;
	
	// calcul des positions des coins
	// ces coins sont dans le repère de l'obstacle !
	public Vec2<ReadOnly> coinBasGauche;
	public Vec2<ReadOnly> coinHautGauche;
	public Vec2<ReadOnly> coinBasDroite;
	public Vec2<ReadOnly> coinHautDroite;

	// ces coins sont dans le repère de la table
	protected Vec2<ReadOnly> coinBasGaucheRotate;
	protected Vec2<ReadOnly> coinHautGaucheRotate;
	protected Vec2<ReadOnly> coinBasDroiteRotate;
	protected Vec2<ReadOnly> coinHautDroiteRotate;
	
	private int indiceMemory;
	
	protected double angle, cos, sin;
/*
	protected void setAngle(double angle)
	{
		this.angle = angle;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
	}
	*/
	/**
	 * Cas où l'angle est nul
	 * @param log
	 * @param config
	 * @param position
	 * @param sizeX
	 * @param sizeY
	 * @param angle
	 */
	public ObstacleRectangular(Vec2<ReadOnly> position, int sizeX, int sizeY)
	{
		this(position, sizeX, sizeY, 0);
	}
	
	public ObstacleRectangular()
	{
		this(new Vec2<ReadOnly>(), 0, 0, 0);
	}
	
	/**
	 * Cet angle est celui par lequel le rectangle a été tourné.
	 * C'est donc l'opposé de l'angle par lequel on va tourner les points afin de considérer
	 * le rectangle comme aligné
	 * Le rectangle est centré sur la position
	 * @param log
	 * @param config
	 * @param position
	 * @param sizeX
	 * @param sizeY
	 * @param angle
	 */
	public ObstacleRectangular(Vec2<ReadOnly> position, int sizeX, int sizeY, double angle)
	{
		super(position);
		this.angle = angle;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		coinBasGauche = new Vec2<ReadOnly>(-sizeX/2,-sizeY/2);
		coinHautGauche = new Vec2<ReadOnly>(-sizeX/2,sizeY/2);
		coinBasDroite = new Vec2<ReadOnly>(sizeX/2,-sizeY/2);
		coinHautDroite = new Vec2<ReadOnly>(sizeX/2,sizeY/2);
		coinBasGaucheRotate = convertitVersRepereTable(coinBasGauche).getReadOnly();
		coinHautGaucheRotate = convertitVersRepereTable(coinHautGauche).getReadOnly();
		coinBasDroiteRotate = convertitVersRepereTable(coinBasDroite).getReadOnly();
		coinHautDroiteRotate = convertitVersRepereTable(coinHautDroite).getReadOnly();
		demieDiagonale = Math.sqrt(sizeY*sizeY/4+sizeX*sizeX/4);
	}
	
	/**
	 * Effectue la rotation d'un point, ce qui équivaut à la rotation de cet obstacle,
	 * ce qui équivaut à le faire devenir un ObstacleRectagularAligned
	 * On utilise ici -angle, ce qui explique que la formule n'est pas la
	 * formule de rotation traditionnelle.
	 * @param point
	 * @return
	 */
	private Vec2<ReadWrite> convertitVersRepereObstacle(Vec2<ReadOnly> point)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>();
		out.x = (int)(cos*(point.x-position.x)+sin*(point.y-position.y));
		out.y = (int)(-sin*(point.x-position.x)+cos*(point.y-position.y));
		return out;
	}

	/**
	 * Rotation dans le sens +angle
	 * Passe du repère de l'obstacle au repère de la table
	 * @param point
	 * @return
	 */
	private Vec2<ReadWrite> convertitVersRepereTable(Vec2<ReadOnly> point)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>();
		out.x = (int)(cos*point.x-sin*point.y)+position.x;
		out.y = (int)(sin*point.x+cos*point.y)+position.y;
		return out;
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getXConvertiVersRepereObstacle(Vec2<ReadOnly> point)
	{
		return (int)(cos*(point.x-position.x)+sin*(point.y-position.y));
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getYConvertiVersRepereObstacle(Vec2<ReadOnly> point)
	{
		return (int)(-sin*(point.x-position.x)+cos*(point.y-position.y));
	}

	/**
	 * Calcul s'il y a collision avec un ObstacleRectangularAligned.
	 * Attention! Ne pas utiliser un ObstacleRectangular au lieu de l'ObstacleRectangularAligned!
	 * Utilise le calcul d'axe de séparation
	 * @param r
	 * @return
	 */
	@Override
	public final boolean isColliding(ObstacleRectangular r)
	{
		// Calcul simple permettant de vérifier les cas absurdes où les obstacles sont loin l'un de l'autre
		if(position.squaredDistance(r.position) >= (demieDiagonale+r.demieDiagonale)*(demieDiagonale+r.demieDiagonale))
			return false;
		// Il faut tester les quatres axes
		return !testeSeparation(coinBasGauche.x, coinBasDroite.x, getXConvertiVersRepereObstacle(r.coinBasGaucheRotate.getReadOnly()), getXConvertiVersRepereObstacle(r.coinHautGaucheRotate.getReadOnly()), getXConvertiVersRepereObstacle(r.coinBasDroiteRotate.getReadOnly()), getXConvertiVersRepereObstacle(r.coinHautDroiteRotate.getReadOnly()))
				&& !testeSeparation(coinBasGauche.y, coinHautGauche.y, getYConvertiVersRepereObstacle(r.coinBasGaucheRotate.getReadOnly()), getYConvertiVersRepereObstacle(r.coinHautGaucheRotate.getReadOnly()), getYConvertiVersRepereObstacle(r.coinBasDroiteRotate.getReadOnly()), getYConvertiVersRepereObstacle(r.coinHautDroiteRotate.getReadOnly()))
				&& !testeSeparation(r.coinBasGauche.x, r.coinBasDroite.x, r.getXConvertiVersRepereObstacle(coinBasGaucheRotate.getReadOnly()), r.getXConvertiVersRepereObstacle(coinHautGaucheRotate.getReadOnly()), r.getXConvertiVersRepereObstacle(coinBasDroiteRotate.getReadOnly()), r.getXConvertiVersRepereObstacle(coinHautDroiteRotate.getReadOnly()))
				&& !testeSeparation(r.coinBasGauche.y, r.coinHautGauche.y, r.getYConvertiVersRepereObstacle(coinBasGaucheRotate.getReadOnly()), r.getYConvertiVersRepereObstacle(coinHautGaucheRotate.getReadOnly()), r.getYConvertiVersRepereObstacle(coinBasDroiteRotate.getReadOnly()), r.getYConvertiVersRepereObstacle(coinHautDroiteRotate.getReadOnly()));
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
	private static final boolean testeSeparation(double a, double b, double a2, double b2, double c2, double d2)
	{
		double min1 = Math.min(a,b);
		double max1 = Math.max(a,b);

		double min2 = Math.min(Math.min(a2, b2), Math.min(c2, d2));
		double max2 = Math.max(Math.max(a2, b2), Math.max(c2, d2));
		
		return min1 > max2 || min2 > max1; // vrai s'il y a une séparation
	}
	
	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public static int[] getXPositions(ObstacleRectangular t)
	{
		int[] X = new int[4];
		X[0] = (int)t.coinBasDroiteRotate.x;
		X[1] = (int)t.coinHautDroiteRotate.x;
		X[2] = (int)t.coinHautGaucheRotate.x;
		X[3] = (int)t.coinBasGaucheRotate.x;
		return X;
	}

	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public static int[] getYPositions(ObstacleRectangular t)
	{
		int[] Y = new int[4];
		Y[0] = (int)t.coinBasDroiteRotate.y;
		Y[1] = (int)t.coinHautDroiteRotate.y;
		Y[2] = (int)t.coinHautGaucheRotate.y;
		Y[3] = (int)t.coinBasGaucheRotate.y;
		return Y;
	}

	@Override
	public String toString()
	{
		return "ObstacleRectangulaire";
	}
	
	/**
	 * Fourni la plus petite distance au carré entre le point fourni et l'obstacle
	 * @param in
	 * @return la plus petite distance au carré entre le point fourni et l'obstacle
	 */
	@Override
	public double squaredDistance(Vec2<ReadOnly> v)
	{
		Vec2<ReadWrite> in = convertitVersRepereObstacle(v);
//		log.debug("in = : "+in);
		/*		
		 *  Schéma de la situation :
		 *
		 * 		 												  y
		 * 			4	|		3		|		2					    ^
		 * 		____________________________________				    |
		 * 				|				|
		 * 			5	|	obstacle	|		1
		 * 		____________________________________
		 * 		
		 * 			6	|		7		|		8
		 */		
		
		// si le point fourni est dans les quarts de plan n°2,4,6 ou 8
		if(in.x < coinBasGauche.x && in.y < coinBasGauche.y)
			return in.squaredDistance(coinBasGauche);
		
		else if(in.x < coinHautGauche.x && in.y > coinHautGauche.y)
			return in.squaredDistance(coinHautGauche);
		
		else if(in.x > coinBasDroite.x && in.y < coinBasDroite.y)
			return in.squaredDistance(coinBasDroite);

		else if(in.x > coinHautDroite.x && in.y > coinHautDroite.y)
			return in.squaredDistance(coinHautDroite);

		// Si le point fourni est dans les demi-bandes n°1,3,5,ou 7
		if(in.x > coinHautDroite.x)
			return (in.x - coinHautDroite.x)*(in.x - coinHautDroite.x);
		
		else if(in.x < coinBasGauche.x)
			return (in.x - coinBasGauche.x)*(in.x - coinBasGauche.x);

		else if(in.y > coinHautDroite.y)
			return (in.y - coinHautDroite.y)*(in.y - coinHautDroite.y);
		
		else if(in.y < coinBasGauche.y)
			return (in.y - coinBasGauche.y)*(in.y - coinBasGauche.y);

		// Sinon, on est dans l'obstacle
		return 0;
	}

	public void setIndiceMemoryManager(int indice)
	{
		indiceMemory = indice;
	}

	public int getIndiceMemoryManager()
	{
		return indiceMemory;
	}

	/**
	 * Mise à jour de l'obstacle
	 * @param position
	 * @param orientation
	 * @param robot
	 * @return
	 */
	public ObstacleRectangular update(Vec2<ReadOnly> position, double orientation, RobotChrono robot)
	{
		Vec2.copy(position, this.position);
		this.angle = orientation;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		int a = robot.getDemieLargeurDroite();
		int b = robot.getDemieLargeurGauche();
		int c = robot.getDemieLongueurAvant();
		int d = robot.getDemieLongueurArriere();
		this.angle = orientation;
		coinBasGauche.x = -d;
		coinBasGauche.y = -a;
		coinHautGauche.x = -d;
		coinHautGauche.y = b;		
		coinBasDroite.x = c;
		coinBasDroite.y = -a;
		coinHautDroite.x = c;
		coinHautDroite.y = b;
		coinBasGaucheRotate = convertitVersRepereTable(coinBasGauche).getReadOnly();
		coinHautGaucheRotate = convertitVersRepereTable(coinHautGauche).getReadOnly();
		coinBasDroiteRotate = convertitVersRepereTable(coinBasDroite).getReadOnly();
		coinHautDroiteRotate = convertitVersRepereTable(coinHautDroite).getReadOnly();
		demieDiagonale = robot.getDemieDiagonale();
		return this;
	}
	
}
