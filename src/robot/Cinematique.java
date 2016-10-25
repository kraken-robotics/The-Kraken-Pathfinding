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

package robot;

import java.awt.Graphics;

import graphic.Fenetre;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Une structure qui regroupe des infos de cinématique
 * @author pf
 *
 */

public class Cinematique implements Printable
{
	protected final Vec2RW position = new Vec2RW();
	public volatile double orientationGeometrique; // il s'agit de l'orientation qui avance. donc l'arrière du robot s'il recule
	public volatile boolean enMarcheAvant;
	public volatile double courbureGeometrique;
	public volatile double vitesseMax;
	public volatile double orientationReelle;
	public volatile double courbureReelle;
	
	public Cinematique(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbure, double vitesseMax)
	{
		update(x,y,orientationGeometrique,enMarcheAvant, courbure, vitesseMax);
	}
	
	/**
	 * Constructeur par copie
	 * @param cinematique
	 */
	public Cinematique(Cinematique cinematique)
	{
		enMarcheAvant = cinematique.enMarcheAvant;
		courbureGeometrique = cinematique.courbureGeometrique;
		vitesseMax = cinematique.vitesseMax;
	}

	/**
	 * Cinématique vide
	 */
	public Cinematique()
	{}

	/**
	 * Copie this dans autre
	 * @param autre
	 */
	public void copy(Cinematique autre)
	{
		synchronized(autre)
		{
	    	position.copy(autre.position);
	    	autre.orientationGeometrique = orientationGeometrique;
	    	autre.orientationReelle = orientationReelle;
	    	autre.enMarcheAvant = enMarcheAvant;
	    	autre.courbureGeometrique = courbureGeometrique;
	    	autre.courbureReelle = courbureReelle;
	    	autre.vitesseMax = vitesseMax;
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
		return position+", "+orientationGeometrique+"(réelle : "+orientationReelle+"), "+(enMarcheAvant ? "marche avant" : "marche arrière")+", vitesse max : "+vitesseMax+" courbure : "+courbureGeometrique+"(réelle : "+courbureReelle+")";
	}
	
	@Override
	public int hashCode() // TODO
	{
		int codeSens = 0;
		if(enMarcheAvant)
			codeSens = 1;
		int codeOrientation;
//		System.out.println("codeCourbure : "+codeCourbure+", "+courbure);
		orientationGeometrique = orientationGeometrique % (2*Math.PI);
		if(orientationGeometrique < 0)
			orientationGeometrique += 2*Math.PI;
		
		codeOrientation = (int)(orientationGeometrique / (Math.PI / 6));
//		System.out.println("codeOrientation : "+codeOrientation+" "+orientation);
		
		return (((((int)(position.getX()) + 1500) / 30) * 200 + (int)(position.getY()) / 30) * 2 + codeSens) * 16 + codeOrientation;
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o.hashCode() == hashCode();
	}

	public void update(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbure, double vitesseMax)
	{
		if(enMarcheAvant)
		{
			orientationReelle = orientationGeometrique;
			courbureReelle = courbure;
		}
		else
		{
			orientationReelle = orientationGeometrique + Math.PI;
			courbureReelle = - courbure;
		}
		
		position.setX(x);
		position.setY(y);
		this.orientationGeometrique = orientationGeometrique;
		this.enMarcheAvant = enMarcheAvant;
		this.courbureGeometrique = courbure;
		this.vitesseMax = vitesseMax;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		g.setColor(Couleur.CINEMATIQUE.couleur);
		double n = PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS/2;
		Vec2RW point1 = new Vec2RW(n, 0), point2 = new Vec2RW(-n/2, n/2), point3 = new Vec2RW(-n/2, -n/2);
		point1.rotate(orientationGeometrique).plus(position);
		point2.rotate(orientationGeometrique).plus(position);
		point3.rotate(orientationGeometrique).plus(position);
		int[] X = {f.XtoWindow((int)point1.getX()), f.XtoWindow((int)point2.getX()), f.XtoWindow((int)point3.getX())};
		int[] Y = {f.YtoWindow((int)point1.getY()), f.YtoWindow((int)point2.getY()) ,f.YtoWindow((int)point3.getY())};
		
		g.drawPolygon(X, Y, 3);
	}
	
	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}
	
}
