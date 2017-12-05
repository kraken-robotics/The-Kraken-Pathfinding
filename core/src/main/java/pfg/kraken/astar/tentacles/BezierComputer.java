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
import pfg.graphic.log.Log;
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
	private double deltaCourbureMax;
	private ClothoidesComputer clothocomputer;

	public BezierComputer(Log log, CinemObsPool memory, ClothoidesComputer clothocomputer, Config config, RectangularObstacle vehicleTemplate)
	{
		this.log = log;
		this.memory = memory;
		this.clothocomputer = clothocomputer;

		courbureMax = config.getDouble(ConfigInfoKraken.MAX_CURVATURE);
		rootedMaxAcceleration = Math.sqrt(config.getDouble(ConfigInfoKraken.MAX_LATERAL_ACCELERATION));
		deltaCourbureMax = config.getDouble(ConfigInfoKraken.MAX_CURVATURE_DERIVATIVE) / config.getDouble(ConfigInfoKraken.DEFAULT_MAX_SPEED) * PRECISION_TRACE;
		
		int indexThreadMax = config.getInt(ConfigInfoKraken.THREAD_NUMBER);
		tmp = new StaticTentacle[indexThreadMax];
		delta = new XY_RW[indexThreadMax];
		vecteurVitesse = new XY_RW[indexThreadMax];
		tmpPoint = new XY_RW[indexThreadMax];
		debut = new Cinematique[indexThreadMax];
		
		for(int i = 0; i < indexThreadMax; i++)
		{
			tmp[i] = new StaticTentacle(vehicleTemplate);
			delta[i] = new XY_RW();
			vecteurVitesse[i] = new XY_RW();
			tmpPoint[i] = new XY_RW();
			debut[i] = new Cinematique();
		}

	}

	private XY_RW[] delta, vecteurVitesse;
	private Cinematique[] debut;
	private StaticTentacle[] tmp;
	private XY_RW[] tmpPoint;
	
	/**
	 * Interpolation de XYO à XYO
	 * @param cinematiqueInitiale
	 * @param arrivee
	 * @return
	 */
	public DynamicTentacle quadraticInterpolationXYO2XYO(Cinematique cinematiqueInitiale, XYO arrivee)
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
		
		return constructBezierQuad(a, tmpPoint[indexThread], c, cinematiqueInitiale.enMarcheAvant, cinematiqueInitiale);
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
		debut[indexThread] = cinematiqueInitiale;
		arrivee.copy(delta[indexThread]);
		delta[indexThread].minus(debut[indexThread].getPosition());
		vecteurVitesse[indexThread].setX(Math.cos(debut[indexThread].orientationGeometrique));
		vecteurVitesse[indexThread].setY(Math.sin(debut[indexThread].orientationGeometrique));
		vecteurVitesse[indexThread].rotate(0, 1); // orthogonal à la vitesse

		double d = vecteurVitesse[indexThread].dot(delta[indexThread]);

		DynamicTentacle prefixe = null;

		// il faut absolument que la courbure ait déjà le bon signe
		// si la courbure est nulle, il faut aussi annuler
		if(Math.abs(debut[indexThread].courbureGeometrique) < 0.1 || debut[indexThread].courbureGeometrique >= 0 ^ d >= 0)
		{
			if(Math.abs(debut[indexThread].courbureGeometrique) > 0.1)
			{
				// log.debug("Préfixe nécessaire !");
				prefixe = clothocomputer.getTrajectoireRamene(debut[indexThread], StraightingTentacle.RAMENE_VOLANT, indexThread);
			}
			if(prefixe == null)
				prefixe = new DynamicTentacle(new ArrayList<CinematiqueObs>(), BezierTentacle.BEZIER_XYOC_TO_XY);

			clothocomputer.getTrajectoire(prefixe.getNbPoints() > 0 ? prefixe.getLast() : cinematiqueInitiale, d > 0 ? ClothoTentacle.GAUCHE_1 : ClothoTentacle.DROITE_1, tmp[indexThread], indexThread);
			for(CinematiqueObs c : tmp[indexThread].arcselems)
			{
				CinematiqueObs o = memory.getNewNode();
				c.copy(o);
				prefixe.arcs.add(o);
			}

			debut[indexThread] = prefixe.getLast();

			arrivee.copy(delta[indexThread]);
			delta[indexThread].minus(debut[indexThread].getPosition());
			vecteurVitesse[indexThread].setX(Math.cos(debut[indexThread].orientationGeometrique));
			vecteurVitesse[indexThread].setY(Math.sin(debut[indexThread].orientationGeometrique));
			vecteurVitesse[indexThread].rotate(0, 1); // orthogonal à la vitesse
			d = vecteurVitesse[indexThread].dot(delta[indexThread]);
		}

		if(Math.abs(debut[indexThread].courbureGeometrique) < 0.1 || debut[indexThread].courbureGeometrique >= 0 ^ d >= 0)
		{
			memory.destroyNode(prefixe);
			return null;
		}

		vecteurVitesse[indexThread].rotate(0, -1);
		vecteurVitesse[indexThread].scalar(Math.sqrt(d / (2 * debut[indexThread].courbureGeometrique / 1000))); // c'est les maths qui le disent
		vecteurVitesse[indexThread].plus(debut[indexThread].getPosition());

		DynamicTentacle arc = constructBezierQuad(debut[indexThread].getPosition(), vecteurVitesse[indexThread], arrivee, debut[indexThread].enMarcheAvant, debut[indexThread]);

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
			prefixe.vitesse = BezierTentacle.BEZIER_XYOC_TO_XY;
			return prefixe;
		}
		return arc;
	}

	private XY_RW a_tmp = new XY_RW(), b_tmp = new XY_RW(), c_tmp = new XY_RW(), acc = new XY_RW();
	private XY_RW tmpPos = new XY_RW();
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
	private DynamicTentacle constructBezierQuad(XY A, XY B, XY C, boolean enMarcheAvant, Cinematique cinematiqueInitiale)
	{
		double t = 1;
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();

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
			a_tmp.scalar((1 - t) * (1 - t));
			B.copy(b_tmp);
			b_tmp.scalar(2 * (1 - t) * t);
			C.copy(c_tmp);
			c_tmp.scalar(t * t);

			a_tmp.copy(tmpPos);
			tmpPos.plus(b_tmp);
			tmpPos.plus(c_tmp);
			out.addFirst(obs);

			// Évalution de la vitesse en t
			A.copy(a_tmp);
			a_tmp.scalar(-2 * (1 - t));
			B.copy(b_tmp);
			b_tmp.scalar(2 * (1 - 2 * t));
			C.copy(c_tmp);
			c_tmp.scalar(2 * t);

			a_tmp.plus(b_tmp);
			a_tmp.plus(c_tmp);
			double vitesse = a_tmp.norm();
			double orientation = a_tmp.getFastArgument();
			a_tmp.rotate(0, 1);
			double accLongitudinale = a_tmp.dot(acc);

			double courbure =  accLongitudinale / (vitesse * vitesse);
			
			double deltaO = (orientation - lastOrientation) % (2 * Math.PI);
			if(deltaO > Math.PI)
				deltaO -= 2 * Math.PI;
			else if(deltaO < -Math.PI)
				deltaO += 2 * Math.PI;

			// on a dépassé la courbure maximale : on arrête tout
			if(Math.abs(courbure) > courbureMax || (!first && (Math.abs(courbure - lastCourbure) > deltaCourbureMax || Math.abs(deltaO) > 0.5)))
			{
				// log.debug("Courbure max dépassée :
				// "+obs.courbureGeometrique+"
				// "+Math.abs(obs.courbureGeometrique - lastCourbure)+"
				// "+obs.orientationGeometrique+" "+orientation+"
				// "+lastOrientation+" "+deltaO);
				for(CinematiqueObs c : out)
					memory.destroyNode(c);
				return null;
			}

			obs.update(tmpPos.getX(), // x
					tmpPos.getY(), // y
					orientation, enMarcheAvant, courbure, rootedMaxAcceleration, false); // Frenet

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

		if(!first && (Math.abs(cinematiqueInitiale.courbureGeometrique - lastCourbure) > deltaCourbureMax) || Math.abs(diffOrientation) > 0.5)
		{
			// log.debug("Erreur raccordement :
			// "+cinematiqueInitiale.courbureGeometrique+"
			// "+Math.abs(cinematiqueInitiale.courbureGeometrique -
			// lastCourbure)+" "+cinematiqueInitiale.orientationGeometrique+"
			// "+lastOrientation);
			for(CinematiqueObs c : out)
				memory.destroyNode(c);
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
			modified.cameFromArcDynamique = t;
			return true;
		}
		
		assert false;
		return false;
	}

	
}
