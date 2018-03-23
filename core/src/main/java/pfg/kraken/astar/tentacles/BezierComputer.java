/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.ArrayList;
import java.util.LinkedList;
import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.tentacles.types.BezierTentacle;
import pfg.kraken.astar.tentacles.types.ClothoTentacle;
import pfg.kraken.astar.tentacles.types.StraightingTentacle;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.memory.CinemObsPool;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.CinematiqueObs;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XY_RW;
import pfg.log.Log;
import static pfg.kraken.astar.tentacles.Tentacle.*;

/**
 * Classe qui s'occupe des calculs sur les courbes de Bézier
 * 
 * @author pf
 *
 */

public class BezierComputer implements TentacleComputer
{
	protected Log log;
	private CinemObsPool memory;
	private double courbureMax;
	private double rootedMaxAcceleration;
	private double maxCurvatureDerivative;
	private ClothoidesComputer clothocomputer;

	public BezierComputer(Log log, CinemObsPool memory, ClothoidesComputer clothocomputer, Config config, RectangularObstacle vehicleTemplate)
	{
		this.log = log;
		this.memory = memory;
		this.clothocomputer = clothocomputer;

		courbureMax = config.getDouble(ConfigInfoKraken.MAX_CURVATURE);
		rootedMaxAcceleration = Math.sqrt(config.getDouble(ConfigInfoKraken.MAX_LATERAL_ACCELERATION));
		maxCurvatureDerivative = config.getDouble(ConfigInfoKraken.MAX_CURVATURE_DERIVATIVE);
		
		int indexThreadMax = config.getInt(ConfigInfoKraken.THREAD_NUMBER);
		tmp = new StaticTentacle[indexThreadMax];
		delta = new XY_RW[indexThreadMax];
		vecteurVitesse = new XY_RW[indexThreadMax];
		a_tmp = new XY_RW[indexThreadMax];
		b_tmp = new XY_RW[indexThreadMax];
		c_tmp = new XY_RW[indexThreadMax];
		d_tmp = new XY_RW[indexThreadMax];
		acc = new XY_RW[indexThreadMax];
		vit = new XY_RW[indexThreadMax];
		tmpPos = new XY_RW[indexThreadMax];
		pointB = new XY_RW[indexThreadMax];
		pointC = new XY_RW[indexThreadMax];
		tmpPoint3 = new XY_RW[indexThreadMax];
		a_tmp = new XY_RW[indexThreadMax];
		b_tmp = new XY_RW[indexThreadMax];
		c_tmp = new XY_RW[indexThreadMax];
		d_tmp = new XY_RW[indexThreadMax];
		acc = new XY_RW[indexThreadMax];
		vit = new XY_RW[indexThreadMax];
		tmpPos = new XY_RW[indexThreadMax];
		debut = new Cinematique[indexThreadMax];
		
		for(int i = 0; i < indexThreadMax; i++)
		{
			tmp[i] = new StaticTentacle(vehicleTemplate);
			delta[i] = new XY_RW();
			vecteurVitesse[i] = new XY_RW();
			a_tmp[i] = new XY_RW();
			b_tmp[i] = new XY_RW();
			c_tmp[i] = new XY_RW();
			d_tmp[i] = new XY_RW();
			acc[i] = new XY_RW();
			vit[i] = new XY_RW();
			tmpPos[i] = new XY_RW();
			pointB[i] = new XY_RW();
			pointC[i] = new XY_RW();
			tmpPoint3[i] = new XY_RW();
			a_tmp[i] = new XY_RW();
			b_tmp[i] = new XY_RW();
			c_tmp[i] = new XY_RW();
			d_tmp[i] = new XY_RW();
			acc[i] = new XY_RW();
			vit[i] = new XY_RW();
			tmpPos[i] = new XY_RW();
			debut[i] = new Cinematique();
		}
		

	}

	private XY_RW[] delta, vecteurVitesse;
	private StaticTentacle[] tmp;

	private XY_RW[] pointB, pointC, tmpPoint3;
	private Cinematique[] debut;
	private XY_RW[] a_tmp, b_tmp, c_tmp, d_tmp, acc, vit, tmpPos;

	/**
	 * Interpolation de XYO à XYO
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @return
	 */
	public DynamicTentacle quadraticInterpolationXYO2XYO(Cinematique cinematiqueInitiale, Cinematique arrivee, int indexThread)
	{
		XY a = cinematiqueInitiale.getPosition();
		XY c = arrivee.getPosition(); 
		double ux = Math.cos(cinematiqueInitiale.orientationGeometrique);
		double uy = Math.sin(cinematiqueInitiale.orientationGeometrique);
		double vx = Math.cos(arrivee.orientationGeometrique);
		double vy = Math.sin(arrivee.orientationGeometrique);

		// Les orientations sont parallèles : on ne peut pas calculer leur intersection
		if(Math.abs(vx*uy - vy*ux) < 0.1)
			return null;
		
		double distanceBC = (a.getX()*uy - a.getY()*ux + c.getY()*ux - c.getX()*uy) / (ux*vy - uy*vx);
		// on va arriver dans le mauvais sens
		if(distanceBC < 0)
			return null;

		pointB[indexThread].setX(arrivee.getPosition().getX() - distanceBC * vx);
		pointB[indexThread].setY(arrivee.getPosition().getY() - distanceBC * vy);

		// on part du mauvais sens
		if((pointB[indexThread].getX() - a.getX()) * ux + (pointB[indexThread].getY() - a.getY()) * uy <= 0)
			return null;
		
		return constructBezierQuad(cinematiqueInitiale.getPosition(), pointB[indexThread], arrivee.getPosition(), cinematiqueInitiale.enMarcheAvant, cinematiqueInitiale, indexThread);
	}
	
	/**
	 * Interpolation cubique de XYOC à XYO. La courbure à l'arrivée sera nulle.
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @return
	 */
	public DynamicTentacle cubicInterpolationXYOC2XYO(Cinematique cinematiqueInitiale, Cinematique arrivee, int indexThread)
	{
		// TODO orientation arrivée : réelle, pas géométrique
		XY a = cinematiqueInitiale.getPosition();
		XY d = arrivee.getPosition(); 
		double ux = Math.cos(cinematiqueInitiale.orientationGeometrique);
		double uy = Math.sin(cinematiqueInitiale.orientationGeometrique);
		double vx = Math.cos(arrivee.orientationGeometrique);
		double vy = Math.sin(arrivee.orientationGeometrique);

		// TODO : vérifier que D est bien du côté de la courbure actuelle
		
		// Les orientations sont parallèles : on ne peut pas calculer leur intersection
		if(Math.abs(ux*vy - uy*vx) < 0.1)
			return null;

		double distanceBD = (a.getX()*uy - a.getY()*ux + d.getY()*ux - d.getX()*uy) / (ux*vy - uy*vx);

		pointB[indexThread].setX(arrivee.getPosition().getX() - distanceBD * vx);
		pointB[indexThread].setY(arrivee.getPosition().getY() - distanceBD * vy);
		
		// on part du mauvais sens
		if((pointB[indexThread].getX() - a.getX()) * ux + (pointB[indexThread].getY() - a.getY()) * uy <= 0)
			return null;

		double sin = ux*vy - uy*vx;
		
		double distanceAB = a.distance(pointB[indexThread]);
		double courbureDepart = cinematiqueInitiale.courbureGeometrique / 1000.;
		
/*		// on vérifie que C est entre B et D
		if(courbureDepart * distanceAB >= 2*gamma*cos)
		{
			System.out.println("C pas entre B et D");
			return null;
		}*/
		
		double distanceBC = 1.5 * courbureDepart * distanceAB * distanceAB / sin;

		// si C est après D : on n'arrivera pas avec la bonne orientation
		if(distanceBC >= distanceBD)
			return null;

		pointC[indexThread].setX(pointB[indexThread].getX() + distanceBC * vx);
		pointC[indexThread].setY(pointB[indexThread].getY() + distanceBC * vy);

		return constructBezierCubique(a, pointB[indexThread], pointC[indexThread], d, cinematiqueInitiale.enMarcheAvant, cinematiqueInitiale, indexThread);
	}

	private DynamicTentacle constructBezierCubique(XY A, XY B, XY C, XY D, boolean enMarcheAvant, Cinematique cinematiqueInitiale, int indexThread)
	{
		double t = 1;
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
//		boolean first = true;
		double lastCourbure = 0;
//		double lastOrientation = 0;
		
		
		while(t > 0)
		{
			CinematiqueObs obs = memory.getNewNode();
			
			double tm = 1-t;
			double tm2 = tm * tm;
			double tm3 = tm * tm * tm;
			double t2 = t * t;
			double t3 = t * t * t;
			
			// Évaluation de la position en t
			A.copy(a_tmp[indexThread]);
			a_tmp[indexThread].scalar(tm3);
			B.copy(b_tmp[indexThread]);
			b_tmp[indexThread].scalar(3*tm2*t);
			C.copy(c_tmp[indexThread]);
			c_tmp[indexThread].scalar(3*tm*t2);
			D.copy(d_tmp[indexThread]);
			d_tmp[indexThread].scalar(t3);
			
			a_tmp[indexThread].copy(tmpPoint3[indexThread]);
			tmpPoint3[indexThread].plus(b_tmp[indexThread]);
			tmpPoint3[indexThread].plus(c_tmp[indexThread]);
			tmpPoint3[indexThread].plus(d_tmp[indexThread]);
			out.addFirst(obs);
			
			// Évalution de la vitesse en t
			A.copy(a_tmp[indexThread]);
			a_tmp[indexThread].scalar(-3*tm2);
			B.copy(b_tmp[indexThread]);
			b_tmp[indexThread].scalar(-6*tm*t+3*tm2);
			C.copy(c_tmp[indexThread]);
			c_tmp[indexThread].scalar(-3*t2+6*tm*t);
			D.copy(d_tmp[indexThread]);
			d_tmp[indexThread].scalar(3*t2);

			a_tmp[indexThread].copy(vit[indexThread]);
			vit[indexThread].plus(b_tmp[indexThread]);
			vit[indexThread].plus(c_tmp[indexThread]);
			vit[indexThread].plus(d_tmp[indexThread]);
			double vitesse = vit[indexThread].norm();
			double orientation = vit[indexThread].getFastArgument();
			
			// Évalutation de l'accélération en t
			A.copy(a_tmp[indexThread]);
			a_tmp[indexThread].scalar(6*tm);
			B.copy(b_tmp[indexThread]);
			b_tmp[indexThread].scalar(6*t-12*tm);
			C.copy(c_tmp[indexThread]);
			c_tmp[indexThread].scalar(-12*t+6*tm);
			D.copy(d_tmp[indexThread]);
			d_tmp[indexThread].scalar(6*t);
			
			a_tmp[indexThread].copy(acc[indexThread]);
			acc[indexThread].plus(b_tmp[indexThread]);
			acc[indexThread].plus(c_tmp[indexThread]);
			acc[indexThread].plus(d_tmp[indexThread]);
			
			double courbure = 1000*(vit[indexThread].getX() * acc[indexThread].getY() - vit[indexThread].getY() * acc[indexThread].getX()) / (vitesse * vitesse * vitesse);

			double deltaCourbure = Math.abs(courbure - lastCourbure);
			
			// on a dépassé la courbure maximale : on arrête tout
			if(Math.abs(courbure) > courbureMax)
			{
//				System.out.println("Courbure : "+courbure+" >? "+courbureMax);
				for(CinematiqueObs c : out)
					memory.destroyNode(c);
				return null;
			}
			
			double maxSpeed = maxCurvatureDerivative * PRECISION_TRACE / deltaCourbure;
			
			t -= PRECISION_TRACE_MM / vitesse;
			
			obs.updateWithMaxSpeed(
					tmpPoint3[indexThread].getX(), // x
					tmpPoint3[indexThread].getY(), // y
					orientation,
					enMarcheAvant,
					courbure,
					rootedMaxAcceleration,
					maxSpeed,
					false);
			
			lastCourbure = obs.courbureGeometrique;
		}

		if(out.getFirst().getPosition().distanceFast(cinematiqueInitiale.getPosition()) < PRECISION_TRACE_MM/2)
			memory.destroyNode(out.removeFirst());
		
		if(out.getFirst().getPosition().distanceFast(cinematiqueInitiale.getPosition()) > 32)
		{
			for(CinematiqueObs c : out)
				memory.destroyNode(c);
			return null;
		}
		
		if(out.isEmpty())
			return null;

		if(Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure) > 0.3)
			out.getFirst().stop = true;
		
		return new DynamicTentacle(out, BezierTentacle.BEZIER_XYOC_TO_XYO);
	}
	
	/**
	 * Interpolation avec des courbes de Bézier quadratique. La solution est
	 * unique.
	 * Est assuré : la continuinité de la position, de l'orientation, de la
	 * courbure, et l'arrivée à la bonne position
	 * 
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @param vitesseMax
	 * @return
	 * @throws MemoryPoolException
	 * @throws InterruptedException
	 */
	public DynamicTentacle quadraticInterpolationXYOC2XY(Cinematique cinematiqueInitiale, XY arrivee, int indexThread)
	{
		cinematiqueInitiale.copy(debut[indexThread]);
		Cinematique firstCinematique = debut[indexThread];

		arrivee.copy(delta[indexThread]);
		delta[indexThread].minus(firstCinematique.getPosition());
		vecteurVitesse[indexThread].setX(Math.cos(firstCinematique.orientationGeometrique));
		vecteurVitesse[indexThread].setY(Math.sin(firstCinematique.orientationGeometrique));
		vecteurVitesse[indexThread].rotate(0, 1); // orthogonal à la vitesse

		double d = vecteurVitesse[indexThread].dot(delta[indexThread]);

		DynamicTentacle prefixe = null;

		// il faut absolument que la courbure ait déjà le bon signe
		// si la courbure est nulle, il faut aussi annuler
		if(Math.abs(firstCinematique.courbureGeometrique) < 0.1 || firstCinematique.courbureGeometrique >= 0 ^ d >= 0)
		{
			if(Math.abs(firstCinematique.courbureGeometrique) > 0.1)
			{
				// log.debug("Préfixe nécessaire !");
				prefixe = clothocomputer.getTrajectoireRamene(firstCinematique, StraightingTentacle.RAMENE_VOLANT, indexThread);
			}

			if(prefixe == null)
				prefixe = new DynamicTentacle(new ArrayList<CinematiqueObs>(), BezierTentacle.BEZIER_XYOC_TO_XY);

			// Seconde partie du préfixe : on tourne un peu dans le bon sens afin d'une une courbure correcte
			clothocomputer.getTrajectoire(prefixe.getNbPoints() > 0 ? prefixe.getLast() : cinematiqueInitiale, d > 0 ? ClothoTentacle.GAUCHE_1 : ClothoTentacle.DROITE_1, tmp[indexThread], indexThread);
			
			// Cette seconde partie est créée dans un arc statique. On copie ça dans le préfixe.
			for(CinematiqueObs c : tmp[indexThread].arcselems)
			{
				CinematiqueObs o = memory.getNewNode();
				c.copy(o);
				prefixe.arcs.add(o);
			}

			prefixe.getLast().copy(debut[indexThread]);

			arrivee.copy(delta[indexThread]);
			delta[indexThread].minus(firstCinematique.getPosition());
			vecteurVitesse[indexThread].setX(Math.cos(firstCinematique.orientationGeometrique));
			vecteurVitesse[indexThread].setY(Math.sin(firstCinematique.orientationGeometrique));
			vecteurVitesse[indexThread].rotate(0, 1); // orthogonal à la vitesse
			d = vecteurVitesse[indexThread].dot(delta[indexThread]);
		}

		// il est possible que le prefixe fait que bien que la courbure soit dans le sens voulu, l'objectif ait changé de côté…
		if(Math.abs(debut[indexThread].courbureGeometrique) < 0.1 || (debut[indexThread].courbureGeometrique >= 0 != d >= 0))
		{
			memory.destroy(prefixe.arcs);
			return null;
		}

		vecteurVitesse[indexThread].rotate(0, -1);
		vecteurVitesse[indexThread].scalar(Math.sqrt(d / (2 * firstCinematique.courbureGeometrique / 1000))); // c'est les maths qui le disent
		vecteurVitesse[indexThread].plus(firstCinematique.getPosition());

		DynamicTentacle arc = constructBezierQuad(debut[indexThread].getPosition(), vecteurVitesse[indexThread], arrivee, debut[indexThread].enMarcheAvant, debut[indexThread], indexThread);

		if(arc == null)
		{
			if(prefixe != null)
				memory.destroy(prefixe.arcs);
			return null;
		}

		// on lui colle son préfixe si besoin est
		if(prefixe != null)
		{
			prefixe.arcs.addAll(arc.arcs);
			prefixe.vitesse = BezierTentacle.BEZIER_XYOC_TO_XY;
			return prefixe;
		}
		return arc;
	}
	
	/**
	 * Construit la suite de points de la courbure de Bézier quadratique de
	 * points de contrôle A, B et C.
	 * On place la discontinuité au début
	 * 
	 * @param position
	 * @param vecteurVitesse2
	 * @param position2
	 * @return
	 * @throws InterruptedException
	 */
	private DynamicTentacle constructBezierQuad(XY A, XY B, XY C, boolean enMarcheAvant, Cinematique cinematiqueInitiale, int indexThread)
	{
		double t = 1;
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();

		// l'accélération est constante pour une courbe quadratique
		A.copy(a_tmp[indexThread]);
		a_tmp[indexThread].scalar(2);
		B.copy(b_tmp[indexThread]);
		b_tmp[indexThread].scalar(-4);
		C.copy(c_tmp[indexThread]);
		c_tmp[indexThread].scalar(2);
		a_tmp[indexThread].copy(acc[indexThread]);
		acc[indexThread].plus(b_tmp[indexThread]);
		acc[indexThread].plus(c_tmp[indexThread]);
		double lastCourbure = 0;

		while(t > 0)
		{
			CinematiqueObs obs = memory.getNewNode();

			double tm = 1-t;
			double tm2 = tm * tm;
			double t2 = t * t;

			// Évaluation de la position en t
			A.copy(a_tmp[indexThread]);
			a_tmp[indexThread].scalar(tm2);
			B.copy(b_tmp[indexThread]);
			b_tmp[indexThread].scalar(2 * tm * t);
			C.copy(c_tmp[indexThread]);
			c_tmp[indexThread].scalar(t2);

			a_tmp[indexThread].copy(tmpPos[indexThread]);
			tmpPos[indexThread].plus(b_tmp[indexThread]);
			tmpPos[indexThread].plus(c_tmp[indexThread]);
			out.addFirst(obs);

			// Évalution de la vitesse en t
			A.copy(vit[indexThread]);
			vit[indexThread].scalar(-2 * tm);
			B.copy(b_tmp[indexThread]);
			b_tmp[indexThread].scalar(2 - 4 * t);
			C.copy(c_tmp[indexThread]);
			c_tmp[indexThread].scalar(2 * t);

			vit[indexThread].plus(b_tmp[indexThread]);
			vit[indexThread].plus(c_tmp[indexThread]);
			
			// a_tmp contient le vecteur vitesse
			double vitesse = vit[indexThread].norm();

			double courbure = 1000*(vit[indexThread].getX() * acc[indexThread].getY() - vit[indexThread].getY() * acc[indexThread].getX()) / (vitesse * vitesse * vitesse);

			double orientation = vit[indexThread].getFastArgument();
			
			double deltaCourbure = Math.abs(courbure - lastCourbure);
			
			// on a dépassé la courbure maximale : on arrête tout
			if(Math.abs(courbure) > courbureMax)
			{
				memory.destroy(out);
				return null;
			}
			
			double maxSpeed = maxCurvatureDerivative * PRECISION_TRACE / deltaCourbure;

			t -= PRECISION_TRACE_MM / vitesse;
			
			obs.updateWithMaxSpeed(tmpPos[indexThread].getX(), // x
					tmpPos[indexThread].getY(), // y
					orientation, enMarcheAvant, courbure, rootedMaxAcceleration, maxSpeed, false);
			
			lastCourbure = obs.courbureGeometrique;
		}
		
		if(out.getFirst().getPosition().distanceFast(cinematiqueInitiale.getPosition()) < PRECISION_TRACE_MM / 2)
			memory.destroyNode(out.removeFirst());

		if(out.isEmpty())
			return null;

		if(Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure) > 0.3)
			out.getFirst().stop = true;
		
		return new DynamicTentacle(out, BezierTentacle.BEZIER_XYOC_TO_XY);
	}

	@Override
	public boolean compute(AStarNode current, TentacleType tentacleType, Cinematique arrival, AStarNode modified, int indexThread)
	{
		assert tentacleType instanceof BezierTentacle : tentacleType;
		if(tentacleType == BezierTentacle.BEZIER_XYOC_TO_XY)
		{
			DynamicTentacle t = quadraticInterpolationXYOC2XY(current.robot.getCinematique(), arrival.getPosition(), indexThread);
			if(t == null)
				return false;
			assert modified.cameFromArcDynamique == null;
			modified.cameFromArcDynamique = t;
			return true;
		}
		else if(tentacleType == BezierTentacle.BEZIER_XYOC_TO_XYO)
		{
			DynamicTentacle t = cubicInterpolationXYOC2XYO(current.robot.getCinematique(), arrival, indexThread);
			if(t == null)
				return false;
			modified.cameFromArcDynamique = t;
			return true;
		}
		else if(tentacleType == BezierTentacle.BEZIER_XYO_TO_XYO)
		{
			DynamicTentacle t = quadraticInterpolationXYO2XYO(current.robot.getCinematique(), arrival, indexThread);
			if(t == null)
				return false;
			modified.cameFromArcDynamique = t;
			return true;
		}
		
		assert false;
		return false;
	}

	
}
