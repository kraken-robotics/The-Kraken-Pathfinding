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

package pathfinding.astar.arcs;

import java.util.LinkedList;

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import memory.CinemObsMM;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.Speed;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Classe qui s'occupe des calculs sur les courbes de Bézier
 * @author pf
 *
 */

public class BezierComputer implements Service, Configurable
{
	protected Log log;
	protected CinemObsMM memory;
	protected double courbureMax;
	
	public BezierComputer(Log log, CinemObsMM memory)
	{
		this.log = log;
		this.memory = memory;
	}
	
	private Vec2RW delta = new Vec2RW();
	private Vec2RW vecteurVitesse = new Vec2RW();
	
	/**
	 * Interpolation avec des courbes de Bézier quadratique. La solution est unique.
	 * Est assuré : la continuinité de la position, de l'orientation, de la courbure, et l'arrivée à la bonne position
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @param vitesseMax
	 * @return
	 */
	public ArcCourbeDynamique interpolationQuadratique(Cinematique cinematiqueInitiale, Cinematique arrivee, Speed vitesseMax)
	{
		arrivee.getPosition().copy(delta);
		delta.minus(cinematiqueInitiale.getPosition());
		vecteurVitesse.setX(Math.cos(cinematiqueInitiale.orientationGeometrique));
		vecteurVitesse.setY(Math.sin(cinematiqueInitiale.orientationGeometrique));
		vecteurVitesse.rotate(0, 1); // orthogonal à la vitesse

		double d = vecteurVitesse.dot(delta);
		
		// il faut absolument que la courbure ait déjà le bon signe
		// si la courbure est nulle, il faut aussi annuler
		if(Math.abs(cinematiqueInitiale.courbureGeometrique) < 0.1 || cinematiqueInitiale.courbureGeometrique >= 0 ^ d >= 0)
			return null;
		
		vecteurVitesse.rotate(0, -1);
		vecteurVitesse.scalar(Math.sqrt(d/(2*cinematiqueInitiale.courbureGeometrique/1000))); // c'est les maths qui le dise
		vecteurVitesse.plus(cinematiqueInitiale.getPosition());
		return constructBezierQuad(cinematiqueInitiale.getPosition(), vecteurVitesse, arrivee.getPosition(), cinematiqueInitiale.enMarcheAvant, vitesseMax);
	}

	private Vec2RW a = new Vec2RW(), b = new Vec2RW(), c = new Vec2RW(), acc = new Vec2RW();
	
	/**
	 * Construit la suite de points de la courbure de Bézier quadratique de points de contrôle A, B et C.
	 * On place la discontinuité au début
	 * @param position
	 * @param vecteurVitesse2
	 * @param position2
	 * @return
	 */
	private ArcCourbeDynamique constructBezierQuad(Vec2RO A, Vec2RO B, Vec2RO C, boolean enMarcheAvant, Speed vitesseMax)
	{
		double t = 1;
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
		Vec2RO lastPos = null;
		double longueur = 0;
		
		// l'accélération est constante pour une courbe quadratique
		A.copy(a);
		a.scalar(2);
		B.copy(b);
		b.scalar(-4);
		C.copy(c);
		c.scalar(2);
		a.copy(acc);
		acc.plus(b);
		acc.plus(c);
		
		while(t > 0)
		{
			CinematiqueObs obs = memory.getNewNode();
			
			// Évaluation de la position en t
			A.copy(a);
			a.scalar((1-t)*(1-t));
			B.copy(b);
			b.scalar(2*(1-t)*t);
			C.copy(c);
			c.scalar(t*t);
			
			a.copy(obs.getPositionEcriture());
			obs.getPositionEcriture().plus(b);
			obs.getPositionEcriture().plus(c);
			out.addFirst(obs);
			
			if(lastPos != null)
				longueur += lastPos.distanceFast(obs.getPosition());
			
			lastPos = obs.getPosition();
			
			// Évalution de la vitesse en t
			A.copy(a);
			a.scalar(-2*(1-t));
			B.copy(b);
			b.scalar(2*(1-2*t));
			C.copy(c);
			c.scalar(2*t);

			a.plus(b);
			a.plus(c);
			double vitesse = a.norm();
			double orientation = a.getArgument();
			a.rotate(0, 1);
			double accLongitudinale = a.dot(acc);
			
			obs.update(
					obs.getPosition().getX(), // x
					obs.getPosition().getY(), // y
					orientation,
					enMarcheAvant,
					accLongitudinale / (vitesse * vitesse), // Frenet
					vitesseMax.translationalSpeed); // TODO
			
			// on a dépassé la courbure maximale : on arrête tout
			if(Math.abs(obs.courbureGeometrique) > courbureMax)
			{
//				log.debug("Courbure max dépassée");
				for(CinematiqueObs c : out)
					memory.destroyNode(c);
				return null;
			}
			
			t -= ClothoidesComputer.PRECISION_TRACE_MM / vitesse;
		}
		
		return new ArcCourbeDynamique(out, longueur, false);
	}

	@Override
	public void useConfig(Config config)
	{
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);		
	}

}
