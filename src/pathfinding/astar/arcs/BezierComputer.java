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
import graphic.PrintBuffer;
import memory.CinemObsMM;
import pathfinding.astar.arcs.vitesses.VitesseBezier;
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
	protected PrintBuffer buffer;
	protected double courbureMax;
	
	public BezierComputer(Log log, CinemObsMM memory, PrintBuffer buffer)
	{
		this.log = log;
		this.memory = memory;
		this.buffer = buffer;
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
		vecteurVitesse.scalar(Math.sqrt(d/(2*cinematiqueInitiale.courbureGeometrique/1000))); // c'est les maths qui le disent
		vecteurVitesse.plus(cinematiqueInitiale.getPosition());
		return constructBezierQuad(cinematiqueInitiale.getPosition(), vecteurVitesse, arrivee.getPosition(), cinematiqueInitiale.enMarcheAvant, vitesseMax, cinematiqueInitiale);
	}

	private Vec2RW a_tmp = new Vec2RW(), b_tmp = new Vec2RW(), c_tmp = new Vec2RW(), d_tmp = new Vec2RW(), acc = new Vec2RW();
	
	/**
	 * Construit la suite de points de la courbure de Bézier quadratique de points de contrôle A, B et C.
	 * On place la discontinuité au début
	 * @param position
	 * @param vecteurVitesse2
	 * @param position2
	 * @return
	 */
	private ArcCourbeDynamique constructBezierQuad(Vec2RO A, Vec2RO B, Vec2RO C, boolean enMarcheAvant, Speed vitesseMax, Cinematique cinematiqueInitiale)
	{
/*		buffer.addSupprimable(new ObstacleCircular(A, 15));
		buffer.addSupprimable(new ObstacleCircular(B, 15));
		buffer.addSupprimable(new ObstacleCircular(C, 15));*/

		double t = 1;
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
		Vec2RO lastPos = null;
		double longueur = 0;

		// l'accélération est constante pour une courbe quadratique
		A.copy(a_tmp);
		a_tmp.scalar(2);
		B.copy(b_tmp);
		b_tmp.scalar(-4);
		C.copy(c_tmp);
		c_tmp.scalar(2);
		a_tmp.copy(acc);
		acc.plus(b_tmp);
		acc.plus(c_tmp);
		boolean first = true;
		double lastCourbure = 0;
		double lastOrientation = 0;
		while(t > 0)
		{
			CinematiqueObs obs = memory.getNewNode();
			
			// Évaluation de la position en t
			A.copy(a_tmp);
			a_tmp.scalar((1-t)*(1-t));
			B.copy(b_tmp);
			b_tmp.scalar(2*(1-t)*t);
			C.copy(c_tmp);
			c_tmp.scalar(t*t);
			
			a_tmp.copy(obs.getPositionEcriture());
			obs.getPositionEcriture().plus(b_tmp);
			obs.getPositionEcriture().plus(c_tmp);
			out.addFirst(obs);
			
			if(lastPos != null)
				longueur += lastPos.distanceFast(obs.getPosition());
			
			lastPos = obs.getPosition();
			
			// Évalution de la vitesse en t
			A.copy(a_tmp);
			a_tmp.scalar(-2*(1-t));
			B.copy(b_tmp);
			b_tmp.scalar(2*(1-2*t));
			C.copy(c_tmp);
			c_tmp.scalar(2*t);

			a_tmp.plus(b_tmp);
			a_tmp.plus(c_tmp);
			double vitesse = a_tmp.norm();
			double orientation = a_tmp.getArgument();
			a_tmp.rotate(0, 1);
			double accLongitudinale = a_tmp.dot(acc);
			
			obs.update(
					obs.getPosition().getX(), // x
					obs.getPosition().getY(), // y
					orientation,
					enMarcheAvant,
					accLongitudinale / (vitesse * vitesse), // Frenet
					vitesseMax.translationalSpeed); // TODO
			
			// on a dépassé la courbure maximale : on arrête tout
			if(Math.abs(obs.courbureGeometrique) > courbureMax || (!first && (Math.abs(obs.courbureGeometrique - lastCourbure) > 0.5) || Math.abs(obs.orientationGeometrique - lastOrientation) > 0.5))
			{
//				log.debug("Courbure max dépassée");
				for(CinematiqueObs c : out)
					memory.destroyNode(c);
				return null;
			}
			
			lastOrientation = obs.orientationGeometrique;
			lastCourbure = obs.courbureGeometrique;
			first = false;
			t -= ClothoidesComputer.PRECISION_TRACE_MM / vitesse;
		}
		
		if(!first && (Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure) > 0.5) || Math.abs(cinematiqueInitiale.orientationGeometrique - lastOrientation) > 0.5)
		{
//			log.debug("Courbure max dépassée");
			for(CinematiqueObs c : out)
				memory.destroyNode(c);
			return null;
		}
		if(out.getFirst().getPosition().distanceFast(cinematiqueInitiale.getPosition()) < ClothoidesComputer.PRECISION_TRACE_MM/2)
			out.removeFirst();
		
		return new ArcCourbeDynamique(out, longueur, VitesseBezier.BEZIER_QUAD);
	}

	private Vec2RW b = new Vec2RW(), c = new Vec2RW(), bp = new Vec2RW();
	
	/**
	 * Interpolation avec des courbes de Bézier cubique.
	 * On assure la continuité : la position, l'orientation, la courbure
	 * De plus, on vise : une position, une orientation
	 * Il reste un degré de liberté, donc on peut tester plusieurs valeurs
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @param vitesseMax
	 * @return
	 */
	public ArcCourbeDynamique interpolationCubique(Cinematique cinematiqueInitiale, Cinematique arrivee, Speed vitesseMax)
	{
		double[] abtab = {100, 50, 200, 500};
		for(double ab : abtab)
		{
			b.setX(Math.cos(cinematiqueInitiale.orientationGeometrique));
			b.setY(Math.sin(cinematiqueInitiale.orientationGeometrique));
			b.scalar(ab);
			b.plus(cinematiqueInitiale.getPosition()); // la position de b, avec un degré de liberté
	
			double d = cinematiqueInitiale.courbureGeometrique/1000*ab*ab*3/2.; // maths
			bp.setX(Math.cos(cinematiqueInitiale.orientationGeometrique));
			bp.setY(Math.sin(cinematiqueInitiale.orientationGeometrique));
			bp.rotate(0, 1);
			bp.scalar(d);
			bp.plus(b);
			
			c.setX(Math.cos(arrivee.orientationGeometrique));
			c.setY(Math.sin(arrivee.orientationGeometrique));
			c.scalar(-100);
			c.plus(arrivee.getPosition());
			
			double da = arrivee.getPosition().getX() - c.getX();
			double dd = arrivee.getPosition().getY() - c.getY();
			double db = b.getX() - cinematiqueInitiale.getPosition().getX();
			double de = b.getY() - cinematiqueInitiale.getPosition().getY();
			double dc = bp.getX() - arrivee.getPosition().getX();
			double df = bp.getY() - arrivee.getPosition().getY();
			
			double alpha, beta;
			
			if(da != 0)
			{
				alpha = (- df + dc*dd/da) / (de - dd * db / da);
				beta = (alpha*db + dc / da);
			}
			else
			{
				alpha = (- dc + df*da/dd) / (db - da * de / dd);
				beta = (alpha*de + df / dd);
			}
			
			b.copy(c);
			c.minus(cinematiqueInitiale.getPosition());
			c.scalar(alpha);
			c.plus(bp);
			
			if(beta >= 0)
				continue; // pas de solution (TODO beta négatif est un rebroussement en fait)

			return constructBezierCubique(cinematiqueInitiale.getPosition(), b, c, arrivee.getPosition(), cinematiqueInitiale.enMarcheAvant, vitesseMax);
		}
		return null;
	}
	
	private Vec2RW tmp_acc = new Vec2RW();
	
	private ArcCourbeDynamique constructBezierCubique(Vec2RO A, Vec2RO B, Vec2RO C, Vec2RO D, boolean enMarcheAvant, Speed vitesseMax)
	{
/*		buffer.addSupprimable(new ObstacleCircular(A, 15));
		buffer.addSupprimable(new ObstacleCircular(B, 15));
		buffer.addSupprimable(new ObstacleCircular(C, 15));
		buffer.addSupprimable(new ObstacleCircular(D, 15));*/
		double t = 1;
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
		Vec2RO lastPos = null;
		double longueur = 0;
		
		while(t > 0)
		{
			CinematiqueObs obs = memory.getNewNode();
			
			// Évaluation de la position en t
			A.copy(a_tmp);
			a_tmp.scalar((1-t)*(1-t)*(1-t));
			B.copy(b_tmp);
			b_tmp.scalar(3*(1-t)*(1-t)*t);
			C.copy(c_tmp);
			c_tmp.scalar(3*(1-t)*t*t);
			D.copy(d_tmp);
			d_tmp.scalar(t*t*t);
			
			a_tmp.copy(obs.getPositionEcriture());
			obs.getPositionEcriture().plus(b_tmp);
			obs.getPositionEcriture().plus(c_tmp);
			obs.getPositionEcriture().plus(d_tmp);
			out.addFirst(obs);
			
			if(lastPos != null)
				longueur += lastPos.distanceFast(obs.getPosition());
			
			lastPos = obs.getPosition();
			
			// Évalution de la vitesse en t
			A.copy(a_tmp);
			a_tmp.scalar(-3*(1-t)*(1-t));
			B.copy(b_tmp);
			b_tmp.scalar(3*(-2*(1-t)*t+(1-t)*(1-t)));
			C.copy(c_tmp);
			c_tmp.scalar(3*(-t*t+2*(1-t)*t));
			D.copy(d_tmp);
			d_tmp.scalar(3*t*t);

			a_tmp.copy(tmp_acc);
			tmp_acc.plus(b_tmp);
			tmp_acc.plus(c_tmp);
			tmp_acc.plus(d_tmp);
			double vitesse = tmp_acc.norm();
			double orientation = tmp_acc.getArgument();
			tmp_acc.rotate(0, 1);
			
			// Évalutation de l'accélération en t
			A.copy(a_tmp);
			a_tmp.scalar(6*(1-t));
			B.copy(b_tmp);
			b_tmp.scalar(3*(2*t-4*(1-t)));
			C.copy(c_tmp);
			c_tmp.scalar(3*(-4*t+2*(1-t)));
			D.copy(d_tmp);
			d_tmp.scalar(6*t);
			
			a_tmp.copy(acc);
			acc.plus(b_tmp);
			acc.plus(c_tmp);
			acc.plus(d_tmp);
			
			double accLongitudinale = tmp_acc.dot(acc);
			
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
		
		return new ArcCourbeDynamique(out, longueur, VitesseBezier.BEZIER_CUBIQUE);
	}
	
	@Override
	public void useConfig(Config config)
	{
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);		
	}

}
