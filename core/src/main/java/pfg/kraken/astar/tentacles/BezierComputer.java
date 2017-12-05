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
import pfg.kraken.utils.XYO;
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
		tmpPoint = new XY_RW[indexThreadMax];
		a_tmp = new XY_RW[indexThreadMax];
		b_tmp = new XY_RW[indexThreadMax];
		c_tmp = new XY_RW[indexThreadMax];
		acc = new XY_RW[indexThreadMax];
		tmpPos = new XY_RW[indexThreadMax];
		debut = new Cinematique[indexThreadMax];
		
		for(int i = 0; i < indexThreadMax; i++)
		{
			tmp[i] = new StaticTentacle(vehicleTemplate);
			delta[i] = new XY_RW();
			vecteurVitesse[i] = new XY_RW();
			tmpPoint[i] = new XY_RW();
			a_tmp[i] = new XY_RW();
			b_tmp[i] = new XY_RW();
			c_tmp[i] = new XY_RW();
			acc[i] = new XY_RW();
			tmpPos[i] = new XY_RW();
			debut[i] = new Cinematique();
		}

	}

	private XY_RW[] delta, vecteurVitesse;
	private Cinematique[] debut;
	private StaticTentacle[] tmp;
	private XY_RW[] tmpPoint;
	private XY_RW[] a_tmp, b_tmp, c_tmp, acc;
	private XY_RW[] tmpPos;

	/**
	 * Interpolation de XYO à XYO
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @return
	 */
	public DynamicTentacle quadraticInterpolationXYO2XYO(Cinematique cinematiqueInitiale, XYO arrivee, int indexThread)
	{
		XY a = cinematiqueInitiale.getPosition();
		XY c = arrivee.position; 
		double ux = Math.cos(cinematiqueInitiale.orientationGeometrique);
		double uy = Math.sin(cinematiqueInitiale.orientationGeometrique);
		double vx = Math.cos(arrivee.orientation);
		double vy = Math.sin(arrivee.orientation);

		// Les orientations sont parallèles : on ne peut pas calculer leur intersection
		if(Math.abs(vx*uy - vx*uy) < 0.1)
			return null;
		
		double gamma = (a.getX()*vy - a.getY()*ux - (c.getX()*uy - c.getY()*ux)) / (vx*uy - vx*uy);
		tmpPoint[indexThread].setX(arrivee.position.getX() + gamma * vx);
		tmpPoint[indexThread].setY(arrivee.position.getY() + gamma * vy);
		
		return constructBezierQuad(a, tmpPoint[indexThread], c, cinematiqueInitiale.enMarcheAvant, cinematiqueInitiale, indexThread);
	}
	
	/**
	 * Interpolation cubique de XYOC à XYO. La courbure à l'arrivée sera nulle.
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @return
	 */
	public DynamicTentacle cubicInterpolationXYOC2XYO(Cinematique cinematiqueInitiale, Cinematique arrivee, int indexThread)
	{
		// TODO
		return null;
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
		arrivee.copy(delta[indexThread]);
		delta[indexThread].minus(debut[indexThread].getPosition());
		vecteurVitesse[indexThread].setX(Math.cos(debut[indexThread].orientationGeometrique));
		vecteurVitesse[indexThread].setY(Math.sin(debut[indexThread].orientationGeometrique));
		vecteurVitesse[indexThread].rotate(0, 1); // orthogonal à la vitesse

		double d = vecteurVitesse[indexThread].dot(delta[indexThread]);

		DynamicTentacle prefixe = null;

		// il faut absolument que la courbure ait déjà le bon signe
		// si la courbure est non nulle, il faut aussi annuler
		if(Math.abs(debut[indexThread].courbureGeometrique) < 0.1 || (debut[indexThread].courbureGeometrique >= 0 != d >= 0))
		{
			// Première partie du préfixe (optionnelle) : on annule la courbure
			if(Math.abs(debut[indexThread].courbureGeometrique) > 0.1)
				prefixe = clothocomputer.getTrajectoireRamene(debut[indexThread], StraightingTentacle.RAMENE_VOLANT, indexThread);
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
			delta[indexThread].minus(debut[indexThread].getPosition());
			vecteurVitesse[indexThread].setX(Math.cos(debut[indexThread].orientationGeometrique));
			vecteurVitesse[indexThread].setY(Math.sin(debut[indexThread].orientationGeometrique));
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
		vecteurVitesse[indexThread].scalar(Math.sqrt(d / (2 * debut[indexThread].courbureGeometrique / 1000))); // c'est les maths qui le disent
		vecteurVitesse[indexThread].plus(debut[indexThread].getPosition());

		DynamicTentacle arc = constructBezierQuad(debut[indexThread].getPosition(), vecteurVitesse[indexThread], arrivee, debut[indexThread].enMarcheAvant, debut[indexThread], indexThread);
		
		// la construction a échoué
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
		boolean first = true;
		double lastCourbure = 0;
		double lastOrientation = 0;

		while(t > 0)
		{
			CinematiqueObs obs = memory.getNewNode();

			// Évaluation de la position en t
			A.copy(a_tmp[indexThread]);
			a_tmp[indexThread].scalar((1 - t) * (1 - t));
			B.copy(b_tmp[indexThread]);
			b_tmp[indexThread].scalar(2 * (1 - t) * t);
			C.copy(c_tmp[indexThread]);
			c_tmp[indexThread].scalar(t * t);

			a_tmp[indexThread].copy(tmpPos[indexThread]);
			tmpPos[indexThread].plus(b_tmp[indexThread]);
			tmpPos[indexThread].plus(c_tmp[indexThread]);
			out.addFirst(obs);

			// Évalution de la vitesse en t
			A.copy(a_tmp[indexThread]);
			a_tmp[indexThread].scalar(-2 * (1 - t));
			B.copy(b_tmp[indexThread]);
			b_tmp[indexThread].scalar(2 * (1 - 2 * t));
			C.copy(c_tmp[indexThread]);
			c_tmp[indexThread].scalar(2 * t);

			a_tmp[indexThread].plus(b_tmp[indexThread]);
			a_tmp[indexThread].plus(c_tmp[indexThread]);
			double vitesse = a_tmp[indexThread].norm();
			double orientation = a_tmp[indexThread].getFastArgument();
			a_tmp[indexThread].rotate(0, 1);
			double accLongitudinale = a_tmp[indexThread].dot(acc[indexThread]);

			double courbure =  accLongitudinale / (vitesse * vitesse);
			
			double deltaO = (orientation - lastOrientation) % (2 * Math.PI);
			if(deltaO > Math.PI)
				deltaO -= 2 * Math.PI;
			else if(deltaO < -Math.PI)
				deltaO += 2 * Math.PI;

			double deltaCourbure = Math.abs(courbure - lastCourbure);
			
			// on a dépassé la courbure maximale : on arrête tout
			if(Math.abs(courbure) > courbureMax || (!first && Math.abs(deltaO) > 0.5))
			{
				memory.destroy(out);
				return null;
			}
			
			double maxSpeed = maxCurvatureDerivative * PRECISION_TRACE / deltaCourbure;

			obs.updateWithMaxSpeed(tmpPos[indexThread].getX(), // x
					tmpPos[indexThread].getY(), // y
					orientation, enMarcheAvant, courbure, rootedMaxAcceleration, maxSpeed, false);

			lastOrientation = obs.orientationGeometrique;
			lastCourbure = obs.courbureGeometrique;
			first = false;
			t -= PRECISION_TRACE_MM / vitesse;
		}

		double diffOrientation = (Math.abs(cinematiqueInitiale.orientationGeometrique - lastOrientation)) % (2 * Math.PI);
		if(diffOrientation > Math.PI)
			diffOrientation -= 2 * Math.PI;
		else if(diffOrientation < -Math.PI)
			diffOrientation += 2 * Math.PI;

		if(!first && (Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure) > 0.3) || Math.abs(diffOrientation) > 0.5)
		{
//			System.out.println("Delta fin : "+Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure)+" >? "+0.3);
			// log.debug("Erreur raccordement :
			// "+cinematiqueInitiale.courbureGeometrique+"
			// "+Math.abs(cinematiqueInitiale.courbureGeometrique -
			// lastCourbure)+" "+cinematiqueInitiale.orientationGeometrique+"
			// "+lastOrientation);
			memory.destroy(out);
			return null;
		}

		if(out.getFirst().getPosition().distanceFast(cinematiqueInitiale.getPosition()) < PRECISION_TRACE_MM / 2)
			memory.destroyNode(out.removeFirst());

		if(out.isEmpty())
			return null;

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
		
		assert false;
		return false;
	}

	
}
