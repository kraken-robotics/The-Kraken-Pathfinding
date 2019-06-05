/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.LinkedList;
import java.util.List;
import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.tentacles.types.BezierTentacle;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.dstarlite.DStarLite;
import pfg.kraken.memory.CinemObsPool;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.CinematiqueObs;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;
import pfg.kraken.utils.XY_RW;
import static pfg.kraken.astar.tentacles.Tentacle.*;

/**
 * Classe qui s'occupe des calculs sur les courbes de Bézier
 * 
 * @author pf
 *
 */

public final class BezierComputer implements TentacleComputer
{
	private CinemObsPool memory;
	private double courbureMax;
	private double rootedMaxAcceleration;
	private double maxCurvatureDerivative;
	private DStarLite dstarlite;

	public BezierComputer(DStarLite dstarlite, CinemObsPool memory, Config config, RectangularObstacle vehicleTemplate)
	{
		this.memory = memory;
		this.dstarlite = dstarlite;

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

		boolean rebrousse = false;
		if((c.getX() - a.getX()) * ux + (c.getY() - a.getY()) * uy < 0)
		{
			ux = -ux;
			uy = -uy;
			vx = -vx;
			vy = -vy;
			rebrousse = true;
		}
		
		boolean marcheAvant = rebrousse != cinematiqueInitiale.enMarcheAvant;

		// Les orientations sont parallèles : on ne peut pas calculer leur intersection
		if(Math.abs(vx*uy - vy*ux) < 0.1)
			return null;
		
		double distanceBC = (a.getX()*uy - a.getY()*ux + c.getY()*ux - c.getX()*uy) / (ux*vy - uy*vx);

		// on va arriver dans le mauvais sens
		if(distanceBC <= 0)
			return null;

		pointB[indexThread].setX(c.getX() - distanceBC * vx);
		pointB[indexThread].setY(c.getY() - distanceBC * vy);

		// on part du mauvais sens
		if((pointB[indexThread].getX() - a.getX()) * ux + (pointB[indexThread].getY() - a.getY()) * uy <= 0)
			return null;
		
		LinkedList<CinematiqueObs> out = constructBezierQuad(a, pointB[indexThread], c, marcheAvant, rebrousse, cinematiqueInitiale, indexThread);

		if(out == null)
			return null;
		
		return new DynamicTentacle(out, BezierTentacle.BEZIER_XYO_TO_XYO);
	}
	
	/**
	 * Interpolation cubique de XYOC à XYO. La courbure à l'arrivée sera nulle.
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @return
	 */
	public DynamicTentacle cubicInterpolationXYOC2XYOC0(Cinematique cinematiqueInitiale, Cinematique arrivee, int indexThread)
	{
		XY a = cinematiqueInitiale.getPosition();
		XY d = arrivee.getPosition(); 
		
		double ux = Math.cos(cinematiqueInitiale.orientationGeometrique);
		double uy = Math.sin(cinematiqueInitiale.orientationGeometrique);
		double vx = Math.cos(arrivee.orientationGeometrique);
		double vy = Math.sin(arrivee.orientationGeometrique);
		
		boolean rebrousse = false;
		if((d.getX() - a.getX()) * ux + (d.getY() - a.getY()) * uy < 0)
		{
			ux = -ux;
			uy = -uy;
			vx = -vx;
			vy = -vy;
			rebrousse = true;
		}
		
		boolean marcheAvant = rebrousse != cinematiqueInitiale.enMarcheAvant;
		
		if(!cinematiqueInitiale.enMarcheAvant)
		
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
				
		double distanceBC = 1.5 * courbureDepart * distanceAB * distanceAB / sin;

		// si C est après D : on n'arrivera pas avec la bonne orientation
		if(Math.abs(distanceBC) >= Math.abs(distanceBD))
			return null;

		pointC[indexThread].setX(pointB[indexThread].getX() + distanceBC * vx);
		pointC[indexThread].setY(pointB[indexThread].getY() + distanceBC * vy);
		
		LinkedList<CinematiqueObs> out = constructBezierCubique(a, pointB[indexThread], pointC[indexThread], d, marcheAvant, rebrousse, cinematiqueInitiale, indexThread);

		if(out == null)
			return null;
		
		return new DynamicTentacle(out, BezierTentacle.BEZIER_XYOC_TO_XYOC0);
	}

	private LinkedList<CinematiqueObs> constructBezierCubique(XY A, XY B, XY C, XY D, boolean enMarcheAvant, boolean rebrousse, Cinematique cinematiqueInitiale, int indexThread)
	{
		if(A.squaredDistance(B) <= minimalDistance*minimalDistance
				|| B.squaredDistance(C) <= minimalDistance*minimalDistance
				|| C.squaredDistance(D) <= minimalDistance*minimalDistance)
			return null;

		double t = 1;
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
//		boolean first = true;
		double lastCourbure = 0;
//		double lastOrientation = 0;
		
		while(t > 0)
		{
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
			
			CinematiqueObs obs = memory.getNewNode();
			out.addFirst(obs);
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

		if(out.isEmpty())
			return null;

		assert out.getFirst().getPosition().distanceFast(cinematiqueInitiale.getPosition()) <= 32;
		if(out.getFirst().getPosition().distanceFast(cinematiqueInitiale.getPosition()) > 32)
		{
			for(CinematiqueObs c : out)
				memory.destroyNode(c);
			return null;
		}

		if(Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure) > 0.3 || rebrousse)
			out.getFirst().stop = true;
		
		return out;
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
	 */
	public DynamicTentacle quadraticInterpolationXYOC2XY(Cinematique cinematiqueInitiale, XY arrivee, int indexThread)
	{
		XY a = cinematiqueInitiale.getPosition();
		
		double ux = Math.cos(cinematiqueInitiale.orientationGeometrique);
		double uy = Math.sin(cinematiqueInitiale.orientationGeometrique);
		double courbureInitiale = cinematiqueInitiale.courbureGeometrique;
		
		boolean rebrousse = false;
		if((arrivee.getX() - a.getX()) * ux + (arrivee.getY() - a.getY()) * uy < 0)
		{
			ux = -ux;
			uy = -uy;
			rebrousse = true;
		}
		
		boolean marcheAvant = rebrousse != cinematiqueInitiale.enMarcheAvant;
		
		cinematiqueInitiale.copy(debut[indexThread]);
		Cinematique firstCinematique = debut[indexThread];

		arrivee.copy(delta[indexThread]);
		delta[indexThread].minus(firstCinematique.getPosition());
		vecteurVitesse[indexThread].setX(ux);
		vecteurVitesse[indexThread].setY(uy);
		vecteurVitesse[indexThread].rotate(0, 1); // orthogonal à la vitesse

		double d = vecteurVitesse[indexThread].dot(delta[indexThread]);
		
		// Si on rebrousse chemin ou que c'est nécessaire, on peut choisir la courbure initiale
		if(Math.abs(courbureInitiale) < 0.1 || (courbureInitiale >= 0 != d >= 0) || rebrousse)
		{
			courbureInitiale = 1000./d; // heuristique qui a l'air de marcher
			if(Math.abs(courbureInitiale) >= courbureMax)
				return null;
		}
		
/*		DynamicTentacle prefixe = null;

		// il faut absolument que la courbure ait déjà le bon signe
		// si la courbure est nulle, il faut aussi annuler
		if(Math.abs(courbureInitiale) < 0.1 || courbureInitiale >= 0 != d >= 0)
		{
			if(Math.abs(courbureInitiale) > 0.1)
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
			courbureInitiale = debut[indexThread].courbureGeometrique;
			
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
*/
		XY_RW b = vecteurVitesse[indexThread];
		b.rotate(0, -1);
		b.scalar(Math.sqrt(d / (2 * courbureInitiale / 1000))); // c'est les maths qui le disent
		b.plus(firstCinematique.getPosition());

		LinkedList<CinematiqueObs> out = constructBezierQuad(debut[indexThread].getPosition(), b, arrivee, marcheAvant, rebrousse, debut[indexThread], indexThread);
		if(out == null)
		{
//			if(prefixe != null)
//				memory.destroy(prefixe.arcs);
			return null;
		}
		
		return new DynamicTentacle(out, BezierTentacle.BEZIER_XYOC_TO_XY);

		// on lui colle son préfixe si besoin est
/*		if(prefixe != null)
		{
			prefixe.arcs.addAll(arc.arcs);
			prefixe.vitesse = BezierTentacle.BEZIER_XYOC_TO_XY;
			return prefixe;
		}*/
	}
	
	public static final int minimalDistance = 30;
	
	/**
	 * Construit la suite de points de la courbure de Bézier quadratique de
	 * points de contrôle A, B et C.
	 * On place la discontinuité au début
	 * 
	 * @param position
	 * @param vecteurVitesse2
	 * @param position2
	 * @return
	 */
	private LinkedList<CinematiqueObs> constructBezierQuad(XY A, XY B, XY C, boolean enMarcheAvant, boolean rebrousse, Cinematique cinematiqueInitiale, int indexThread)
	{
		/*
		 * Avoir une certaine distance entre les points permet d'éviter les cas dégénérés où la courbure est trop grande
		 */
		if(A.squaredDistance(B) <= minimalDistance*minimalDistance || B.squaredDistance(C) <= minimalDistance*minimalDistance)
			return null;

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

		if(Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure) > 0.3 || rebrousse)
			out.getFirst().stop = true;
		
		return out;
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
		else if(tentacleType == BezierTentacle.BEZIER_XYOC_TO_XYOC0)
		{
			DynamicTentacle t = cubicInterpolationXYOC2XYOC0(current.robot.getCinematique(), arrival, indexThread);
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
		
		else if(tentacleType == BezierTentacle.INTERMEDIATE_BEZIER_XYO_TO_XYO)
		{
			DynamicTentacle t = intermediateXYO2XYO(current, indexThread);
			if(t == null)
				return false;
			assert modified.cameFromArcDynamique == null;
			modified.cameFromArcDynamique = t;
			return true;
		}
		else if(tentacleType == BezierTentacle.INTERMEDIATE_BEZIER_XYOC_TO_XY)
		{
			DynamicTentacle t = intermediateXYOC2XY(current, indexThread);
			if(t == null)
				return false;
			assert modified.cameFromArcDynamique == null;
			modified.cameFromArcDynamique = t;
			return true;
		}
		
		assert false;
		return false;
	}


	public DynamicTentacle intermediateXYO2XYO(AStarNode current, int indexThread)
	{
		List<XYO> listePositions = dstarlite.itineraireBrut(current.robot.getCinematique().getPosition());
		
		if(listePositions.size() < 2)
			return null;
		
		XYO middle = listePositions.get(listePositions.size() / 2);
		DynamicTentacle out = quadraticInterpolationXYO2XYO(current.robot.getCinematique(), new Cinematique(middle), indexThread);
		if(out != null)
			out.vitesse = BezierTentacle.INTERMEDIATE_BEZIER_XYO_TO_XYO;
		return out;
	}
	
	public DynamicTentacle intermediateXYOC2XY(AStarNode current, int indexThread)
	{
		List<XYO> listePositions = dstarlite.itineraireBrut(current.robot.getCinematique().getPosition());
		
		if(listePositions.size() < 2)
			return null;
		
		XYO middle = listePositions.get(listePositions.size() / 2);
		DynamicTentacle out = quadraticInterpolationXYOC2XY(current.robot.getCinematique(), middle.position, indexThread);
		if(out != null)
			out.vitesse = BezierTentacle.INTERMEDIATE_BEZIER_XYOC_TO_XY;
		return out;
	}
	
}
