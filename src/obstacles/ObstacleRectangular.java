package obstacles;

import permissions.Permission;
import permissions.ReadOnly;
import permissions.ReadWrite;
import permissions.TestOnly;
import exceptions.FinMatchException;
import strategie.GameState;
import utils.Vec2;

/**
 * Rectangle ayant subi une rotation.
 * Ce rectangle peut être le robot, ou bien l'espace que parcourera le robot pendant un segment
 * @author pf
 *
 */

public class ObstacleRectangular<T extends Permission> extends ObstacleAvecAngle<T>
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
	protected final Vec2<T> coinBasGauche = Vec2.getT(position.plusNewVector((new Vec2<ReadWrite>(-sizeX/2,-sizeY/2))), position);
	protected final Vec2<T> coinHautGauche = Vec2.getT(position.plusNewVector((new Vec2<ReadWrite>(-sizeX/2,sizeY/2))), position);
	protected final Vec2<T> coinBasDroite = Vec2.getT(position.plusNewVector((new Vec2<ReadWrite>(sizeX/2,-sizeY/2))), position);
	protected final Vec2<T> coinHautDroite = Vec2.getT(position.plusNewVector((new Vec2<ReadWrite>(sizeX/2,sizeY/2))), position);

	protected final Vec2<T> coinBasGaucheRotate = Vec2.getT(rotatePlusAngle(coinBasGauche.getReadOnly()), position);
	protected final Vec2<T> coinHautGaucheRotate = Vec2.getT(rotatePlusAngle(coinHautGauche.getReadOnly()), position);
	protected final Vec2<T> coinBasDroiteRotate = Vec2.getT(rotatePlusAngle(coinBasDroite.getReadOnly()), position);
	protected final Vec2<T> coinHautDroiteRotate = Vec2.getT(rotatePlusAngle(coinHautDroite.getReadOnly()), position);
	
	/**
	 * Cas où l'angle est nul
	 * @param log
	 * @param config
	 * @param position
	 * @param sizeX
	 * @param sizeY
	 * @param angle
	 */
	public ObstacleRectangular(Vec2<T> position, int sizeX, int sizeY)
	{
		this(position, sizeX, sizeY, 0);
	}

	/**
	 * Constructeur user-friendly pour le pathfinding
	 * @param depart
	 * @param arrivee
	 */
	public ObstacleRectangular(Vec2<T> depart, Vec2<T> arrivee)
	{
		this(Vec2.getT(depart.middleNewVector(arrivee), depart), (int)depart.distance(arrivee)+longueurRobot+2*marge, largeurRobot+2*marge, Math.atan2(arrivee.y-depart.y, arrivee.x-depart.x));
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
	public ObstacleRectangular(Vec2<T> position, int sizeX, int sizeY, double angle)
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
	public ObstacleRectangular(GameState<?,ReadOnly> state, Vec2<T> useless) throws FinMatchException
	{
		this(Vec2.getT(GameState.getPosition(state).clone(), useless), longueurRobot, largeurRobot, GameState.getOrientation(state));
	}

	public static void update(ObstacleRectangular<ReadWrite> o, Vec2<ReadOnly> position, double angle)
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
	public static final boolean isCollidingRectangular(ObstacleRectangular<ReadOnly> t, ObstacleRectangular<ReadOnly> r)
	{
		// Calcul simple permettant de vérifier les cas absurdes où les obstacles sont loin l'un de l'autre
		if(t.position.squaredDistance(r.position) >= (t.demieDiagonale+r.demieDiagonale)*(t.demieDiagonale+r.demieDiagonale))
			return false;
		// Il faut tester les quatres axes
		return !testeSeparation(t.coinBasGauche.x, t.coinBasDroite.x, t.getXRotateMoinsAngle(r.coinBasGaucheRotate), t.getXRotateMoinsAngle(r.coinHautGaucheRotate), t.getXRotateMoinsAngle(r.coinBasDroiteRotate), t.getXRotateMoinsAngle(r.coinHautDroiteRotate))
				&& !testeSeparation(t.coinBasGauche.y, t.coinHautGauche.y, t.getYRotateMoinsAngle(r.coinBasGaucheRotate), t.getYRotateMoinsAngle(r.coinHautGaucheRotate), t.getYRotateMoinsAngle(r.coinBasDroiteRotate), t.getYRotateMoinsAngle(r.coinHautDroiteRotate))
				&& !testeSeparation(r.coinBasGauche.x, r.coinBasDroite.x, r.getXRotateMoinsAngle(t.coinBasGaucheRotate), r.getXRotateMoinsAngle(t.coinHautGaucheRotate), r.getXRotateMoinsAngle(t.coinBasDroiteRotate), r.getXRotateMoinsAngle(t.coinHautDroiteRotate))
				&& !testeSeparation(r.coinBasGauche.y, r.coinHautGauche.y, r.getYRotateMoinsAngle(t.coinBasGaucheRotate), r.getYRotateMoinsAngle(t.coinHautGaucheRotate), r.getYRotateMoinsAngle(t.coinBasDroiteRotate), r.getYRotateMoinsAngle(t.coinHautDroiteRotate));
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
	public static int[] getXPositions(ObstacleRectangular<TestOnly> t)
	{
		int[] X = new int[4];
		X[0] = t.getXRotatePlusAngle(t.coinBasDroite.getReadOnly());
		X[1] = t.getXRotatePlusAngle(t.coinHautDroite.getReadOnly());
		X[2] = t.getXRotatePlusAngle(t.coinHautGauche.getReadOnly());
		X[3] = t.getXRotatePlusAngle(t.coinBasGauche.getReadOnly());
		return X;
	}

	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public static int[] getYPositions(ObstacleRectangular<TestOnly> t)
	{
		int[] Y = new int[4];
		Y[0] = t.getYRotatePlusAngle(t.coinBasDroite.getReadOnly());
		Y[1] = t.getYRotatePlusAngle(t.coinHautDroite.getReadOnly());
		Y[2] = t.getYRotatePlusAngle(t.coinHautGauche.getReadOnly());
		Y[3] = t.getYRotatePlusAngle(t.coinBasGauche.getReadOnly());
		return Y;
	}

	public boolean isCollidingCircular(ObstacleCircular<ReadOnly> o)
	{
		return squaredDistance(o.position) < o.radius*o.radius;
	}
	
	public boolean isColliding(Obstacle<ReadOnly> o)
	{
		if(o instanceof ObstacleRectangular)
			return isCollidingRectangular(getReadOnly(), (ObstacleRectangular<ReadOnly>)o);
		else if(o instanceof ObstacleCircular)
			return isCollidingCircular((ObstacleCircular<ReadOnly>)o);

		log.critical("Appel de isColliding avec un type d'obstacle inconnu!");
		return false;
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

	@SuppressWarnings("unchecked")
	public final ObstacleRectangular<TestOnly> getTestOnly()
	{
		return (ObstacleRectangular<TestOnly>) this;
	}

	@SuppressWarnings("unchecked")
	public final ObstacleRectangular<ReadOnly> getReadOnly()
	{
		return (ObstacleRectangular<ReadOnly>) this;
	}
}
