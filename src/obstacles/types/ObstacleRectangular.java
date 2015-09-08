package obstacles.types;

import pathfinding.GameState;
import permissions.ReadOnly;
import permissions.ReadWrite;
import exceptions.FinMatchException;
import utils.Vec2;

/**
 * Rectangle ayant subi une rotation.
 * Ce rectangle peut être le robot, ou bien l'espace que parcourera le robot pendant un segment
 * @author pf
 *
 */

public class ObstacleRectangular extends ObstacleAvecAngle
{
	private double cos = Math.cos(angle);
	private double sin = Math.sin(angle);

	// taille selon l'axe X
	protected int sizeX;
	
	// taille selon l'axe Y
	protected int sizeY;
	
	// Longueur entre le centre et un des coins
	protected double demieDiagonale;
	
	// calcul des positions des coins
	public final Vec2<ReadOnly> coinBasGauche = new Vec2<ReadOnly>(-sizeX/2,-sizeY/2);
	public final Vec2<ReadOnly> coinHautGauche = new Vec2<ReadOnly>(-sizeX/2,sizeY/2);
	public final Vec2<ReadOnly> coinBasDroite = new Vec2<ReadOnly>(sizeX/2,-sizeY/2);
	public final Vec2<ReadOnly> coinHautDroite = new Vec2<ReadOnly>(sizeX/2,sizeY/2);

	protected final Vec2<ReadOnly> coinBasGaucheRotate = rotatePlusAngle(coinBasGauche).getReadOnly();
	protected final Vec2<ReadOnly> coinHautGaucheRotate = rotatePlusAngle(coinHautGauche).getReadOnly();
	protected final Vec2<ReadOnly> coinBasDroiteRotate = rotatePlusAngle(coinBasDroite).getReadOnly();
	protected final Vec2<ReadOnly> coinHautDroiteRotate = rotatePlusAngle(coinHautDroite).getReadOnly();
	
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

	/**
	 * Constructeur user-friendly pour le pathfinding
	 * @param depart
	 * @param arrivee
	 */
	public ObstacleRectangular(Vec2<ReadOnly> depart, Vec2<ReadOnly> arrivee)
	{
		this(depart.middleNewVector(arrivee).getReadOnly(), (int)depart.distance(arrivee)+longueurRobot+2*marge, largeurRobot+2*marge, Math.atan2(arrivee.y-depart.y, arrivee.x-depart.x));
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
	public ObstacleRectangular(Vec2<ReadOnly> position, int sizeX, int sizeY, double angle)
	{
		super(position,angle);
		this.sizeY = sizeY;
		this.sizeX = sizeX;
		this.angle = angle;
		demieDiagonale = Math.sqrt(sizeY*sizeY/4+sizeX*sizeX/4);
	}
	
	/**
	 * Crée l'obstacle du robot
	 * @param state
	 * @throws FinMatchException 
	 */
	public ObstacleRectangular(GameState<?,ReadOnly> state) throws FinMatchException
	{
		this(state.robot.getPosition(), longueurRobot, largeurRobot, state.robot.getOrientation());
	}
/*
	public void update(Vec2<ReadOnly> position, double angle)
	{
		int sizeX = o.sizeX, sizeY = o.sizeY;
		Vec2.copy(position.plusNewVector((new Vec2<ReadWrite>(-sizeX/2,-sizeY/2))).getReadOnly(), o.coinBasGauche);
		Vec2.copy(position.plusNewVector((new Vec2<ReadWrite>(-sizeX/2,sizeY/2))).getReadOnly(), o.coinHautGauche);
		Vec2.copy(position.plusNewVector((new Vec2<ReadWrite>(sizeX/2,-sizeY/2))).getReadOnly(), o.coinBasDroite);
		Vec2.copy(position.plusNewVector((new Vec2<ReadWrite>(sizeX/2,sizeY/2))).getReadOnly(), o.coinHautDroite);
		setAngle(o, angle);
		Vec2.copy(o.rotatePlusAngle(o.coinBasGauche.getReadOnly()).getReadOnly(), o.coinBasGaucheRotate);
		Vec2.copy(o.rotatePlusAngle(o.coinHautGauche.getReadOnly()).getReadOnly(), o.coinHautGaucheRotate);
		Vec2.copy(o.rotatePlusAngle(o.coinBasDroite.getReadOnly()).getReadOnly(), o.coinBasDroiteRotate);
		Vec2.copy(o.rotatePlusAngle(o.coinHautDroite.getReadOnly()).getReadOnly(), o.coinHautDroiteRotate);
	}
	*/
	/**
	 * Effectue la rotation d'un point, ce qui équivaut à la rotation de cet obstacle,
	 * ce qui équivaut à le faire devenir un ObstacleRectagularAligned
	 * On utilise ici -angle, ce qui explique que la formule n'est pas la
	 * formule de rotationt traditionnelle.
	 * @param point
	 * @return
	 */
	private Vec2<ReadWrite> rotateMoinsAngle(Vec2<ReadOnly> point)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>();
		out.x = (int)(cos*(point.x-position.x)+sin*(point.y-position.y))+position.x;
		out.y = (int)(-sin*(point.x-position.x)+cos*(point.y-position.y))+position.y;
		return out;
	}

	/**
	 * Rotation dans le sens +angle
	 * @param point
	 * @return
	 */
	private Vec2<ReadWrite> rotatePlusAngle(Vec2<ReadOnly> point)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>();
		out.x = (int)(cos*(point.x-position.x)-sin*(point.y-position.y))+position.x;
		out.y = (int)(sin*(point.x-position.x)+cos*(point.y-position.y))+position.y;
		return out;
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getXRotatePlusAngle(Vec2<ReadOnly> point)
	{
		return (int)(cos*(point.x-position.x)-sin*(point.y-position.y))+position.x;
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getYRotatePlusAngle(Vec2<ReadOnly> point)
	{
		return (int)(sin*(point.x-position.x)+cos*(point.y-position.y))+position.y;
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getXRotateMoinsAngle(Vec2<ReadOnly> point)
	{
		return (int)(cos*(point.x-position.x)+sin*(point.y-position.y))+position.x;
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	private int getYRotateMoinsAngle(Vec2<ReadOnly> point)
	{
		return (int)(-sin*(point.x-position.x)+cos*(point.y-position.y))+position.y;
	}

	/**
	 * Calcul s'il y a collision avec un ObstacleRectangularAligned.
	 * Attention! Ne pas utiliser un ObstacleRectangular au lieu de l'ObstacleRectangularAligned!
	 * Utilise le calcul d'axe de séparation
	 * @param r
	 * @return
	 */
	public final boolean isCollidingRectangular(ObstacleRectangular r)
	{
		// Calcul simple permettant de vérifier les cas absurdes où les obstacles sont loin l'un de l'autre
		if(position.squaredDistance(r.position) >= (demieDiagonale+r.demieDiagonale)*(demieDiagonale+r.demieDiagonale))
			return false;
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
	private static final boolean testeSeparation(int a, int b, int a2, int b2, int c2, int d2)
	{
		int min1 = Math.min(a,b);
		int max1 = Math.max(a,b);

		int min2 = Math.min(Math.min(a2, b2), Math.min(c2, d2));
		int max2 = Math.max(Math.max(a2, b2), Math.max(c2, d2));
		
		return min1 > max2 || min2 > max1; // vrai s'il y a une séparation
	}
	
	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public static int[] getXPositions(ObstacleRectangular t)
	{
		int[] X = new int[4];
		X[0] = t.getXRotatePlusAngle(t.coinBasDroite);
		X[1] = t.getXRotatePlusAngle(t.coinHautDroite);
		X[2] = t.getXRotatePlusAngle(t.coinHautGauche);
		X[3] = t.getXRotatePlusAngle(t.coinBasGauche);
		return X;
	}

	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public static int[] getYPositions(ObstacleRectangular t)
	{
		int[] Y = new int[4];
		Y[0] = t.getYRotatePlusAngle(t.coinBasDroite);
		Y[1] = t.getYRotatePlusAngle(t.coinHautDroite);
		Y[2] = t.getYRotatePlusAngle(t.coinHautGauche);
		Y[3] = t.getYRotatePlusAngle(t.coinBasGauche);
		return Y;
	}

	public boolean isCollidingCircular(ObstacleCircular o)
	{
		return squaredDistance(o.position) < o.radius*o.radius;
	}
	
	public boolean isColliding(Obstacle o)
	{
		if(o instanceof ObstacleRectangular)
			return isCollidingRectangular((ObstacleRectangular)o);
		else if(o instanceof ObstacleCircular)
			return isCollidingCircular((ObstacleCircular)o);

		log.critical("Appel de isColliding avec un type d'obstacle inconnu!");
		return false;
	}

	/**
	 * Retourne le coin le plus proche de la position donnée.
	 * @param pos
	 * @return
	 */
	public Vec2<ReadOnly> getPlusProcheCoinVisible(Vec2<ReadOnly> pos, boolean coinBasDroiteVisible, boolean coinBasGaucheVisible, boolean coinHautDroiteVisible, boolean coinHautGaucheVisible)
	{
		Vec2<ReadOnly> out = null;
		double min = Integer.MAX_VALUE, tmp;
		if(coinBasGaucheVisible)
		{
			out = coinBasGauche;
			min = pos.squaredDistance(coinBasGauche);
		}
		if(coinHautGaucheVisible)
		{
			tmp = pos.squaredDistance(coinHautGauche);
			if(tmp < min)
			{
				out = coinHautGauche;
				min = tmp;
			}
		}
		if(coinBasDroiteVisible)
		{
			tmp = pos.squaredDistance(coinBasDroite);
			if(tmp < min)
			{
				out = coinBasDroite;
				min = tmp;
			}
		}
		if(coinHautDroiteVisible)
		{
			tmp = pos.squaredDistance(coinHautDroite);
			if(tmp < min)
			{
				out = coinHautDroite;
				min = tmp;
			}
		}
		return out;
	}
	
	/**
	 * Utilisé pour l'affichage uniquement
	 * @return
	 */
	public int getSizeX()
	{
		return sizeX;
	}

	/**
	 * Utilisé pour l'affichage uniquement
	 * @return
	 */
	public int getSizeY()
	{
		return sizeY;
	}

	public String toString()
	{
		return "ObstacleRectangulaire";
	}
	
	public double distance(Vec2<ReadOnly> point)
	{
		return Math.sqrt(squaredDistance(point));
	}
	
	public boolean isInObstacle(Vec2<ReadOnly> point)
	{
		Vec2<ReadWrite> pointWrite = rotateMoinsAngle(point);
		return (pointWrite.x < position.x + sizeX/2) &&
				(pointWrite.x > position.x - sizeX/2) &&
				(pointWrite.y < position.y + sizeY/2) &&
				(pointWrite.y > position.y - sizeY/2);
	}
	
	/**
	 * Fourni la plus petite distance au carré entre le point fourni et l'obstacle
	 * @param in
	 * @return la plus petite distance au carré entre le point fourni et l'obstacle
	 */
	public double squaredDistance(Vec2<ReadOnly> v)
	{
		Vec2<ReadWrite> in = rotateMoinsAngle(v);
		/*		
		 *  Schéma de la situation :
		 *
		 * 		 												  y
		 * 			4	|		3		|		2					    ^
		 * 				|				|								|
		 * 		____________________________________				    |
		 * 				|				|								-----> x
		 * 				|				|
		 * 			5	|	obstacle	|		1
		 * 		
		 * 		____________________________________
		 * 		
		 * 			6	|		7		|		8
		 * 				|				|
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

	public boolean isProcheObstacle(Vec2<ReadOnly> point, int distance)
	{
		// Attention! squaredDistance effectue déjà la rotation du point
		return squaredDistance(point) < (distance+0.01f) * (distance+0.01f); // vu qu'on a une précision limitée, mieux vaut prendre un peu de marge
	}

	/**
	 * Y a-t-il collision avec un obstacle fixe?
	 * @return
	 */
	public boolean isCollidingObstacleFixe()
	{
		for(ObstaclesFixes o: ObstaclesFixes.values)
			if(isColliding(o.getObstacle()))
				return true;
		return false;
	}
}
