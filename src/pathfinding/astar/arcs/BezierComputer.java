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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import graphic.PrintBuffer;
import memory.CinemObsMM;
import pathfinding.astar.arcs.vitesses.VitesseBezier;
import pathfinding.astar.arcs.vitesses.VitesseClotho;
import pathfinding.astar.arcs.vitesses.VitesseRameneVolant;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.RobotReal;
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
	protected CercleArrivee cercle;
	protected double courbureMax;
	private ClothoidesComputer clothocomputer;
	
	public BezierComputer(Log log, CinemObsMM memory, PrintBuffer buffer, ClothoidesComputer clothocomputer, RobotReal robot, CercleArrivee cercle)
	{
		this.log = log;
		this.memory = memory;
		this.buffer = buffer;
		this.clothocomputer = clothocomputer;
		this.cercle = cercle;
		tmp = new ArcCourbeStatique(robot);
	}
	
	private Vec2RW delta = new Vec2RW(), vecteurVitesse = new Vec2RW();
//	private Vec2RW pointA = new Vec2RW(), pointB = new Vec2RW(), pointC = new Vec2RW();
	private Cinematique debut;
	private ArcCourbeStatique tmp;

	/**
	 * Interpolation avec des courbes de Bézier quadratique. La solution est unique.
	 * Est assuré : la continuinité de la position, de l'orientation, de la courbure, et l'arrivée à la bonne position
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @param vitesseMax
	 * @return
	 */
	public ArcCourbeDynamique interpolationQuadratique(Cinematique cinematiqueInitiale, Vec2RO arrivee, Speed vitesseMax)
	{
		debut = cinematiqueInitiale;
		arrivee.copy(delta);
		delta.minus(debut.getPosition());
		vecteurVitesse.setX(Math.cos(debut.orientationGeometrique));
		vecteurVitesse.setY(Math.sin(debut.orientationGeometrique));
		vecteurVitesse.rotate(0, 1); // orthogonal à la vitesse

		double d = vecteurVitesse.dot(delta);
		
		ArcCourbeDynamique prefixe = null;
		
		// il faut absolument que la courbure ait déjà le bon signe
		// si la courbure est nulle, il faut aussi annuler
		if(Math.abs(debut.courbureGeometrique) < 0.1 || debut.courbureGeometrique >= 0 ^ d >= 0)
		{
			if(Math.abs(debut.courbureGeometrique) > 0.1)
			{
//				log.debug("Préfixe nécessaire !");
				prefixe = clothocomputer.getTrajectoireRamene(debut, VitesseRameneVolant.RAMENE_VOLANT, vitesseMax);
			}
			if(prefixe == null)
				prefixe = new ArcCourbeDynamique(new ArrayList<CinematiqueObs>(), 0, VitesseBezier.BEZIER_QUAD);
				
			clothocomputer.getTrajectoire(prefixe.getNbPoints() > 0 ? prefixe.getLast() : cinematiqueInitiale, d > 0 ? VitesseClotho.GAUCHE_1 : VitesseClotho.DROITE_1 , vitesseMax, tmp);
			for(CinematiqueObs c : tmp.arcselems)
			{
				CinematiqueObs o = memory.getNewNode();
				c.copy(o);
				prefixe.arcs.add(o);
			}

			prefixe.longueur += tmp.getLongueur();
			debut = prefixe.getLast();

			arrivee.copy(delta);
			delta.minus(debut.getPosition());
			vecteurVitesse.setX(Math.cos(debut.orientationGeometrique));
			vecteurVitesse.setY(Math.sin(debut.orientationGeometrique));
			vecteurVitesse.rotate(0, 1); // orthogonal à la vitesse
			d = vecteurVitesse.dot(delta);
		}
		
		if(Math.abs(debut.courbureGeometrique) < 0.1 || debut.courbureGeometrique >= 0 ^ d >= 0)
		{
			memory.destroyNode(prefixe);
			return null;
		}
		
		vecteurVitesse.rotate(0, -1);
		vecteurVitesse.scalar(Math.sqrt(d/(2*debut.courbureGeometrique/1000))); // c'est les maths qui le disent
		vecteurVitesse.plus(debut.getPosition());
		
		ArcCourbeDynamique arc = constructBezierQuad(debut.getPosition(), vecteurVitesse, arrivee, debut.enMarcheAvant, vitesseMax, debut);
		
		if(arc == null)
		{
			if(prefixe != null)
				memory.destroyNode(prefixe);
			return null;
		}
		
		// on lui colle son préfixe si besoin est
		if(prefixe != null)
		{
			prefixe.arcs.addAll(arc.arcs);
			prefixe.longueur += arc.longueur;
			prefixe.vitesse = VitesseBezier.BEZIER_QUAD;
			return prefixe;
		}
		return arc;
	}
	
	/**
	 * Essai d'arrêt sur cercle. Fait une interpolation quadratique classique et retire les points du cercle.
	 * Aucune assurance sur l'orientation d'arrivée.
	 * @param cinematiqueInitiale
	 * @param vitesseMax
	 * @return
	 */
	public ArcCourbeDynamique interpolationQuadratiqueCercle(Cinematique cinematiqueInitiale, Speed vitesseMax)
	{
		ArcCourbeDynamique out = interpolationQuadratique(cinematiqueInitiale, cercle.position, vitesseMax);
		if(out == null)
			return null;

		boolean del = false;
		Iterator<CinematiqueObs> it = out.arcs.iterator();
		while(it.hasNext())
		{
			CinematiqueObs o = it.next();
			if(del || cercle.isInCircle(o.getPosition()))
			{
				del = true;
				memory.destroyNode(o);
				it.remove();
			}
		}
		
		if(out.getNbPoints() == 0)
			return null;
		
		return out;
	}
	
	/**
	 * Une interpolation quadratique qui arrive sur un cercle
	 * @param cinematique
	 * @param vitesseMax
	 * @return
	 */
/*	public ArcCourbeDynamique interpolationQuadratiqueCercle(Cinematique cinematiqueInitiale, Speed vitesseMax)
	{
		ArcCourbeDynamique prefixe = initCourbe(cinematiqueInitiale, cercle.position, vitesseMax);
		cercle.position.copy(a_tmp);
		a_tmp.minus(vecteurVitesse); // le point B
		vecteurVitesse.copy(b_tmp);
		b_tmp.minus(cinematiqueInitiale.getPosition());
		double alpha = a_tmp.getArgument() - b_tmp.getArgument(); // une estimation seulement du résultat final
		double sin = Math.sin(alpha);
		double c = cinematiqueInitiale.courbureGeometrique;
		b_tmp.scalar(1/b.norm()); // normalisation de b
		cercle.position.copy(a_tmp);
		a_tmp.minus(cinematiqueInitiale.getPosition());
		double L = a_tmp.dot(b);
		b.rotate(0,1);
		double l = a_tmp.dot(b);
		double r = cercle.rayon;
		
		return null;
	}
*/
	
	private Vec2RW a_tmp = new Vec2RW(), b_tmp = new Vec2RW(), c_tmp = new Vec2RW(), acc = new Vec2RW();
	
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
			double orientation = a_tmp.getFastArgument();
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
			if(Math.abs(obs.courbureGeometrique) > courbureMax || (!first && (Math.abs(obs.courbureGeometrique - lastCourbure) > 0.5 || Math.abs(obs.orientationGeometrique - lastOrientation) > 0.5)))
			{
//				log.debug("Courbure max dépassée : "+obs.courbureGeometrique+" "+Math.abs(obs.courbureGeometrique - lastCourbure)+" "+obs.orientationGeometrique+" "+orientation+" "+lastOrientation);
				for(CinematiqueObs c : out)
					memory.destroyNode(c);
				return null;
			}
	
			lastOrientation = obs.orientationGeometrique;
			lastCourbure = obs.courbureGeometrique;
			first = false;
			t -= ClothoidesComputer.PRECISION_TRACE_MM / vitesse;
		}
		
		double diffOrientation = (Math.abs(cinematiqueInitiale.orientationGeometrique - lastOrientation)) % (2*Math.PI);
		if(diffOrientation > Math.PI)
			diffOrientation -= 2*Math.PI;
		if(!first && (Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure) > 0.5) || Math.abs(diffOrientation) > 0.5)
		{
//			log.debug("Erreur raccordement : "+cinematiqueInitiale.courbureGeometrique+" "+Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure)+" "+cinematiqueInitiale.orientationGeometrique+" "+lastOrientation);
			for(CinematiqueObs c : out)
				memory.destroyNode(c);
			return null;
		}
		
		if(out.getFirst().getPosition().distanceFast(cinematiqueInitiale.getPosition()) < ClothoidesComputer.PRECISION_TRACE_MM/2)
			memory.destroyNode(out.removeFirst());
		
		if(out.isEmpty())
			return null;
		
		return new ArcCourbeDynamique(out, longueur, VitesseBezier.BEZIER_QUAD);
	}

	@Override
	public void useConfig(Config config)
	{
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);		
	}

}
