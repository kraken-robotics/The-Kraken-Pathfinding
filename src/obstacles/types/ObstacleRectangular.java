/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package obstacles.types;

import java.awt.Color;
import java.awt.Graphics;

import graphic.Fenetre;
import graphic.printable.Layer;
import robot.RobotReal;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Rectangle ayant subi une rotation.
 * Ce rectangle peut être le robot, ou bien l'espace que parcourera le robot pendant un segment
 * @author pf
 *
 */

public class ObstacleRectangular extends Obstacle
{
	// Position est le centre de rotation
	
	// Longueur entre le centre et un des coins
	protected double demieDiagonale;
	protected Vec2RW centreGeometrique;
	
	// calcul des positions des coins
	// ces coins sont dans le repère de l'obstacle !
	public Vec2RW coinBasGauche;
	public Vec2RW coinHautGauche;
	public Vec2RW coinBasDroite;
	public Vec2RW coinHautDroite;

	// ces coins sont dans le repère de la table
	protected Vec2RW coinBasGaucheRotate;
	protected Vec2RW coinHautGaucheRotate;
	protected Vec2RW coinBasDroiteRotate;
	protected Vec2RW coinHautDroiteRotate;

	private Vec2RW in = new Vec2RW();

	protected double angle, cos, sin;

	/**
	 * Cas où l'angle est nul
	 * @param log
	 * @param config
	 * @param position
	 * @param sizeX
	 * @param sizeY
	 * @param angle
	 */
	public ObstacleRectangular(Vec2RO position, int sizeX, int sizeY)
	{
		this(position, sizeX, sizeY, 0);
	}

	public ObstacleRectangular(Vec2RO position, int sizeX, int sizeY, Layer l)
	{
		this(position, sizeX, sizeY, 0);
		this.l = l;
	}

	public ObstacleRectangular(Vec2RO position, int sizeX, int sizeY, Layer l, Color c)
	{
		this(position, sizeX, sizeY, l);
		this.c = c;
	}

	public ObstacleRectangular(Vec2RO position, int sizeX, int sizeY, double angle, Color c)
	{
		this(position, sizeX, sizeY, angle);
		this.c = c;
	}

	public ObstacleRectangular(Vec2RO position, int sizeX, int sizeY, Color c)
	{
		this(position, sizeX, sizeY);
		this.c = c;
	}

	protected ObstacleRectangular(Vec2RW pos)
	{
		super(pos);
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
	public ObstacleRectangular(Vec2RO position, int sizeX, int sizeY, double angle)
	{
		super(position);
		this.angle = angle;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		coinBasGauche = new Vec2RW(-sizeX/2,-sizeY/2);
		coinHautGauche = new Vec2RW(-sizeX/2,sizeY/2);
		coinBasDroite = new Vec2RW(sizeX/2,-sizeY/2);
		coinHautDroite = new Vec2RW(sizeX/2,sizeY/2);
		coinBasGaucheRotate = new Vec2RW();
		coinHautGaucheRotate = new Vec2RW();
		coinBasDroiteRotate = new Vec2RW();
		coinHautDroiteRotate = new Vec2RW();
		convertitVersRepereTable(coinBasGauche, coinBasGaucheRotate);
		convertitVersRepereTable(coinHautGauche, coinHautGaucheRotate);
		convertitVersRepereTable(coinBasDroite, coinBasDroiteRotate);
		convertitVersRepereTable(coinHautDroite, coinHautDroiteRotate);
		centreGeometrique = position.clone();
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
	protected void convertitVersRepereObstacle(Vec2RO point, Vec2RW out)
	{
		out.setX(cos*(point.getX()-position.getX())+sin*(point.getY()-position.getY()));
		out.setY(-sin*(point.getX()-position.getX())+cos*(point.getY()-position.getY()));
	}

	/**
	 * Rotation dans le sens +angle
	 * Passe du repère de l'obstacle au repère de la table
	 * @param point
	 * @return
	 */
	protected void convertitVersRepereTable(Vec2RO point, Vec2RW out)
	{
		out.setX(cos*point.getX()-sin*point.getY()+position.getX());
		out.setY(sin*point.getX()+cos*point.getY()+position.getY());
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	protected double getXConvertiVersRepereObstacle(Vec2RO point)
	{
		return cos*(point.getX()-position.getX())+sin*(point.getY()-position.getY());
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * @param point
	 * @return
	 */
	protected double getYConvertiVersRepereObstacle(Vec2RO point)
	{
		return -sin*(point.getX()-position.getX())+cos*(point.getY()-position.getY());
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
		if(centreGeometrique.squaredDistance(r.centreGeometrique) >= (demieDiagonale+r.demieDiagonale)*(demieDiagonale+r.demieDiagonale))
			return false;
		// Il faut tester les quatres axes
		return !testeSeparation(coinBasGauche.getX(), coinBasDroite.getX(), getXConvertiVersRepereObstacle(r.coinBasGaucheRotate), getXConvertiVersRepereObstacle(r.coinHautGaucheRotate), getXConvertiVersRepereObstacle(r.coinBasDroiteRotate), getXConvertiVersRepereObstacle(r.coinHautDroiteRotate))
				&& !testeSeparation(coinBasGauche.getY(), coinHautGauche.getY(), getYConvertiVersRepereObstacle(r.coinBasGaucheRotate), getYConvertiVersRepereObstacle(r.coinHautGaucheRotate), getYConvertiVersRepereObstacle(r.coinBasDroiteRotate), getYConvertiVersRepereObstacle(r.coinHautDroiteRotate))
				&& !testeSeparation(r.coinBasGauche.getX(), r.coinBasDroite.getX(), r.getXConvertiVersRepereObstacle(coinBasGaucheRotate), r.getXConvertiVersRepereObstacle(coinHautGaucheRotate), r.getXConvertiVersRepereObstacle(coinBasDroiteRotate), r.getXConvertiVersRepereObstacle(coinHautDroiteRotate))
				&& !testeSeparation(r.coinBasGauche.getY(), r.coinHautGauche.getY(), r.getYConvertiVersRepereObstacle(coinBasGaucheRotate), r.getYConvertiVersRepereObstacle(coinHautGaucheRotate), r.getYConvertiVersRepereObstacle(coinBasDroiteRotate), r.getYConvertiVersRepereObstacle(coinHautDroiteRotate));
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
	private final boolean testeSeparation(double a, double b, double a2, double b2, double c2, double d2)
	{
		double min1 = Math.min(a,b);
		double max1 = Math.max(a,b);

		double min2 = Math.min(Math.min(a2, b2), Math.min(c2, d2));
		double max2 = Math.max(Math.max(a2, b2), Math.max(c2, d2));
		
		return min1 > max2 || min2 > max1; // vrai s'il y a une séparation
	}

	@Override
	public String toString()
	{
		return "ObstacleRectangulaire "+coinBasGaucheRotate+" "+coinBasDroiteRotate+" "+coinHautGaucheRotate+" "+coinHautDroiteRotate+" "+super.toString();
	}
		
	/**
	 * Fourni la plus petite distance au carré entre le point fourni et l'obstacle
	 * @param in
	 * @return la plus petite distance au carré entre le point fourni et l'obstacle
	 */
	@Override
	public double squaredDistance(Vec2RO v)
	{
		convertitVersRepereObstacle(v, in);

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
		if(in.getX() < coinBasGauche.getX() && in.getY() < coinBasGauche.getY())
			return in.squaredDistance(coinBasGauche);
		
		else if(in.getX() < coinHautGauche.getX() && in.getY() > coinHautGauche.getY())
			return in.squaredDistance(coinHautGauche);
		
		else if(in.getX() > coinBasDroite.getX() && in.getY() < coinBasDroite.getY())
			return in.squaredDistance(coinBasDroite);

		else if(in.getX() > coinHautDroite.getX() && in.getY() > coinHautDroite.getY())
			return in.squaredDistance(coinHautDroite);

		// Si le point fourni est dans les demi-bandes n°1,3,5,ou 7
		if(in.getX() > coinHautDroite.getX())
			return (in.getX() - coinHautDroite.getX())*(in.getX() - coinHautDroite.getX());
		
		else if(in.getX() < coinBasGauche.getX())
			return (in.getX() - coinBasGauche.getX())*(in.getX() - coinBasGauche.getX());

		else if(in.getY() > coinHautDroite.getY())
			return (in.getY() - coinHautDroite.getY())*(in.getY() - coinHautDroite.getY());
		
		else if(in.getY() < coinBasGauche.getY())
			return (in.getY() - coinBasGauche.getY())*(in.getY() - coinBasGauche.getY());

		// Sinon, on est dans l'obstacle
		return 0;
	}
	
	public double getDemieDiagonale()
	{
		return demieDiagonale;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		if(coinBasDroiteRotate == null)
			return;

		if(c != null)
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
	}
	
}
