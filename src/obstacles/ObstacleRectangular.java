package obstacles;

import exceptions.FinMatchException;
import strategie.GameState;
import vec2.ReadOnly;
import vec2.ReadWrite;
import vec2.Vec2;

/**
 * Rectangle ayant subi une rotation.
 * Ce rectangle peut être le robot, ou bien l'espace que parcourera le robot pendant un segment
 * @author pf
 *
 */

public class ObstacleRectangular extends Obstacle implements ObstacleCollision
{
	private double cos;
	private double sin;

	// taille selon l'axe X
	protected int sizeX;
	
	// taille selon l'axe Y
	protected int sizeY;

	// Longueur entre le centre et un des coins
	protected double demieDiagonale;
	
	// calcul des positions des coins
	protected Vec2<ReadOnly> coinBasGauche;
	protected Vec2<ReadOnly> coinHautGauche;
	protected Vec2<ReadOnly> coinBasDroite;
	protected Vec2<ReadOnly> coinHautDroite;

	protected Vec2<ReadOnly> coinBasGaucheRotate;
	protected Vec2<ReadOnly> coinHautGaucheRotate;
	protected Vec2<ReadOnly> coinBasDroiteRotate;
	protected Vec2<ReadOnly> coinHautDroiteRotate;
	
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
		super(position);
		this.sizeY = sizeY;
		this.sizeX = sizeX;
		demieDiagonale = Math.sqrt(sizeY*sizeY/4+sizeX*sizeX/4);
		updateVariables(position, angle);
	}
	
	/**
	 * Crée l'obstacle du robot
	 * @param state
	 * @throws FinMatchException 
	 */
	public ObstacleRectangular(GameState<?> state) throws FinMatchException
	{
		this(state.robot.getPosition(), longueurRobot, largeurRobot, state.robot.getOrientation());
	}

	private void updateVariables(Vec2<ReadOnly> position, double angle)
	{
		coinBasGauche = position.plusNewVector((new Vec2<ReadWrite>(-sizeX/2,-sizeY/2))).getReadOnly();
		coinHautGauche = position.plusNewVector((new Vec2<ReadWrite>(-sizeX/2,sizeY/2))).getReadOnly();
		coinBasDroite = position.plusNewVector((new Vec2<ReadWrite>(sizeX/2,-sizeY/2))).getReadOnly();
		coinHautDroite = position.plusNewVector((new Vec2<ReadWrite>(sizeX/2,sizeY/2))).getReadOnly();
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		coinBasGaucheRotate = rotatePlusAngle(coinBasGauche).getReadOnly();
		coinHautGaucheRotate = rotatePlusAngle(coinHautGauche).getReadOnly();
		coinBasDroiteRotate = rotatePlusAngle(coinBasDroite).getReadOnly();
		coinHautDroiteRotate = rotatePlusAngle(coinHautDroite).getReadOnly();
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
	public boolean isColliding(ObstacleRectangular r)
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
	private boolean testeSeparation(int a, int b, int a2, int b2, int c2, int d2)
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
	
	@Override
	public boolean isColliding(Obstacle o)
	{
		if(o instanceof ObstacleRectangular)
			return isColliding((ObstacleRectangular)o);
		else if(o instanceof ObstacleCircular)
			return isColliding((ObstacleCircular)o);

		log.critical("Appel de isColliding avec un type d'obstacle inconnu!", this);
		return false;
	}

	public void update(Vec2<ReadOnly> position, double orientation)
	{
		updateVariables(position, orientation);	
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
	@Override
	public boolean isCollidingObstacleFixe()
	{
		for(ObstaclesFixes o: ObstaclesFixes.values())
			if(isColliding(o.getObstacle()))
				return true;
		return false;
	}

}
