package obstacles;

import smartMath.Vec2;

/**
 * Obstacle rectangulaire sont les bords sont alignés avec les axes X et Y (pas de possibilité de faire un rectangle en biais)
 * @author pf, marsu
 */
public class ObstacleRectangular extends Obstacle
{

	// Convention: la "position" d'un ObstacleRectangulaire est celle de son centre (intersection des 2 diagonales)
	
	// taille selon l'axe X
	protected int sizeX;
	
	// taille selon l'axe Y
	protected int sizeY;
	
	public ObstacleRectangular(Vec2 position, int sizeX, int sizeY)
	{
		super(position);
		this.sizeY = sizeY;
		this.sizeX = sizeX;
	}

	public ObstacleRectangular clone()
	{
		return new ObstacleRectangular(position.clone(), sizeX, sizeY);
	}
	public String toString()
	{
		return "ObstacleRectangulaire";
	}
	
	/**
	 * Taille selon l'axe Y
	 * @return
	 */
	public int getSizeY()
	{
		return this.sizeY;
	}
	
	/**
	 * Taille selon l'axe X
	 * @return
	 */
	public int getSizeX()
	{
		return this.sizeX;
	}
	
	public float distance(Vec2 point)
	{
		return (float) Math.sqrt(squaredDistance(point));
	}
	
	public boolean isInObstacle(Vec2 point)
	{
		return (point.x < position.x + sizeX/2) &&
				(point.x > position.x - sizeX/2) &&
				(point.y < position.y + sizeY/2) &&
				(point.y > position.y - sizeY/2);
	}
	
	/**
	 * Fourni la plus petite distance au carré entre le point fourni et l'obstacle
	 * @param in
	 * @return la plus petite distance au carré entre le point fourni et l'obstacle
	 */
	public float squaredDistance(Vec2 in)
	{
		
		/*		
		 *  Shéma de la situation :
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
		
		// calcul des positions des coins
		Vec2 coinBasGauche = position.plusNewVector((new Vec2(-sizeX/2,-sizeY/2)));
		Vec2 coinHautGauche = position.plusNewVector((new Vec2(-sizeX/2,sizeY/2)));
		Vec2 coinBasDroite = position.plusNewVector((new Vec2(sizeX/2,-sizeY/2)));
		Vec2 coinHautDroite = position.plusNewVector((new Vec2(sizeX/2,sizeY/2)));
		
		// si le point fourni est dans lesquarts-de-plans n°2,4,6 ou 8
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
		return 0f;
	}

	public boolean isProcheObstacle(Vec2 point, int distance)
	{
		return squaredDistance(point) < (distance+0.01f) * (distance+0.01f); // vu qu'on a une précision limitée, mieux vaut prendre un peu de marge
	}

}
