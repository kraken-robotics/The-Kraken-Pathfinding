/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package robot;

import java.awt.Graphics;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import graphic.Fenetre;
import graphic.printable.Layer;
import graphic.printable.Printable;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Une structure qui regroupe des infos de cinématique
 * 
 * @author pf
 *
 */

public class Cinematique implements Printable, Serializable
{
	private static final long serialVersionUID = 1548985891767047059L;
	protected final Vec2RW position = new Vec2RW();
	public volatile double orientationGeometrique; // il s'agit de l'orientation
													// qui avance. donc
													// l'arrière du robot s'il
													// recule
	public volatile boolean enMarcheAvant;
	public volatile double courbureGeometrique;
	public volatile double orientationReelle;
	public volatile double courbureReelle;
	private static NumberFormat formatter = new DecimalFormat("#0.000");

	public Cinematique(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbure)
	{
		update(x, y, orientationGeometrique, enMarcheAvant, courbure);
	}

	/**
	 * Constructeur par copie
	 * 
	 * @param cinematique
	 */
	public Cinematique(Cinematique cinematique)
	{
		enMarcheAvant = cinematique.enMarcheAvant;
		courbureGeometrique = cinematique.courbureGeometrique;
	}

	/**
	 * Cinématique vide
	 */
	public Cinematique()
	{}

	/**
	 * Copie this dans autre
	 * 
	 * @param autre
	 */
	public synchronized void copy(Cinematique autre)
	{
		synchronized(autre)
		{
			position.copy(autre.position);
			autre.orientationGeometrique = orientationGeometrique;
			autre.orientationReelle = orientationReelle;
			autre.enMarcheAvant = enMarcheAvant;
			autre.courbureGeometrique = courbureGeometrique;
			autre.courbureReelle = courbureReelle;
		}
	}

	public final Vec2RO getPosition()
	{
		return position;
	}

	public final Vec2RW getPositionEcriture()
	{
		return position;
	}

	@Override
	public String toString()
	{
		return position + ", " + formatter.format(orientationReelle) + ", " + (enMarcheAvant ? "marche avant" : "marche arrière") + ", courbure : " + formatter.format(courbureReelle);
	}

	/**
	 * Renvoie un code pour le cache de pathfinding
	 * 
	 * @return
	 */
	public int codeForPFCache()
	{
		int codeOrientation;

		// System.out.println("codeCourbure : "+codeCourbure+", "+courbure);
		orientationReelle = orientationReelle % (2 * Math.PI);
		if(orientationReelle < 0)
			orientationReelle += 2 * Math.PI;
		else if(orientationReelle > 2 * Math.PI)
			orientationReelle -= 2 * Math.PI;

		codeOrientation = (int) ((orientationReelle + Math.PI / 80) / (Math.PI / 40)); // le
																						// +(pi/10)/2
																						// est
																						// utilisé
																						// afin
																						// d'avoir
																						// une
																						// tolérance
																						// sur
																						// l'orientation
																						// du
																						// robot
																						// si
																						// son
																						// orientation
																						// est
																						// "ronde"
																						// (0,
																						// pi/2,
																						// pi,
																						// -pi/2)
		// System.out.println("codeOrientation : "+codeOrientation+"
		// "+orientation);

		return ((((int) (position.getX()) + 1500) / 10) * 200 + (int) (position.getY()) / 10) * 40 + codeOrientation;
	}

	@Override
	public int hashCode()
	{

		// Il faut fusionner les points trop proches pour pas que le PF ne
		// s'entête dans des coins impossibles
		// Par contre, il ne faut pas trop fusionner sinon on ne verra pas les
		// chemins simples et ne restera que les compliqués

		int codeSens = 0;
		if(enMarcheAvant)
			codeSens = 1;
		int codeCourbure, codeOrientation;
		if(courbureReelle < -3)
			codeCourbure = 0;
		// else if(courbureReelle < -2)
		// codeCourbure = 1;
		else if(courbureReelle < 0)
			codeCourbure = 2;
		// else if(courbureReelle < 2)
		// codeCourbure = 3;
		else if(courbureReelle < 3)
			codeCourbure = 4;
		else
			codeCourbure = 5;

		// System.out.println("codeCourbure : "+codeCourbure+", "+courbure);
		orientationReelle = orientationReelle % (2 * Math.PI);
		if(orientationReelle < 0)
			orientationReelle += 2 * Math.PI;
		else if(orientationReelle > 2 * Math.PI)
			orientationReelle -= 2 * Math.PI;

		codeOrientation = (int) (orientationReelle / (Math.PI / 6));
		// System.out.println("codeOrientation : "+codeOrientation+"
		// "+orientation);

		// return ((((((int)(position.getX()) + 1500) / 100) * 2000 +
		// (int)(position.getY()) / 100) * 2 + codeSens) * 16 + codeOrientation)
		// * 6 + codeCourbure;

		return ((((((int) (position.getX()) + 1500) / 30) * 200 + (int) (position.getY()) / 30) * 2 + codeSens) * 16 + codeOrientation) * 6 + codeCourbure;
	}

	@Override
	public boolean equals(Object o)
	{
		return o.hashCode() == hashCode();
	}

	/**
	 * Met à jour la cinématique à partir d'info réelle
	 * 
	 * @param x
	 * @param y
	 * @param orientationGeometrique
	 * @param enMarcheAvant
	 * @param courbure
	 */
	public void updateReel(double x, double y, double orientationReelle, boolean enMarcheAvant, double courbureReelle)
	{
		if(enMarcheAvant)
		{
			orientationGeometrique = orientationReelle;
			courbureGeometrique = courbureReelle;
		}
		else
		{
			orientationGeometrique = orientationReelle + Math.PI;
			courbureGeometrique = -courbureReelle;
		}

		position.setX(x);
		position.setY(y);
		this.orientationReelle = orientationReelle;
		this.enMarcheAvant = enMarcheAvant;
		this.courbureReelle = courbureReelle;
	}

	public void update(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbureGeometrique)
	{
		if(enMarcheAvant)
		{
			orientationReelle = orientationGeometrique;
			courbureReelle = courbureGeometrique;
		}
		else
		{
			orientationReelle = orientationGeometrique + Math.PI;
			courbureReelle = -courbureGeometrique;
		}

		position.setX(x);
		position.setY(y);
		this.orientationGeometrique = orientationGeometrique;
		this.enMarcheAvant = enMarcheAvant;
		this.courbureGeometrique = courbureGeometrique;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		double n = PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS / 2;
		Vec2RW point1 = new Vec2RW(n, 0), point2 = new Vec2RW(-n / 2, n / 2), point3 = new Vec2RW(-n / 2, -n / 2);
		point1.rotate(orientationGeometrique).plus(position);
		point2.rotate(orientationGeometrique).plus(position);
		point3.rotate(orientationGeometrique).plus(position);
		int[] X = { f.XtoWindow((int) point1.getX()), f.XtoWindow((int) point2.getX()), f.XtoWindow((int) point3.getX()) };
		int[] Y = { f.YtoWindow((int) point1.getY()), f.YtoWindow((int) point2.getY()), f.YtoWindow((int) point3.getY()) };

		g.drawPolygon(X, Y, 3);
	}

	/**
	 * Doit être évité à tout prix
	 */
	@Override
	public Cinematique clone()
	{
		Cinematique out = new Cinematique();
		copy(out);
		return out;
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

}
