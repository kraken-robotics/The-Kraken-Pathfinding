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

package pathfinding.astar.arcs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.HighPFClass;
import exceptions.MemoryManagerException;
import graphic.PrintBufferInterface;
import memory.CinemObsMM;
import pathfinding.astar.arcs.vitesses.VitesseBezier;
import pathfinding.astar.arcs.vitesses.VitesseClotho;
import pathfinding.astar.arcs.vitesses.VitesseRameneVolant;
import robot.Cinematique;
import robot.CinematiqueObs;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Classe qui s'occupe des calculs sur les courbes de Bézier
 * 
 * @author pf
 *
 */

public class BezierComputer implements Service, HighPFClass
{
	protected Log log;
	protected CinemObsMM memory;
	protected PrintBufferInterface buffer;
	protected CercleArrivee cercle;
	protected double courbureMax;
	private static final double deltaCourbureMax = 0.2;
	private ClothoidesComputer clothocomputer;

	public BezierComputer(Log log, CinemObsMM memory, PrintBufferInterface buffer, ClothoidesComputer clothocomputer, CercleArrivee cercle, Config config)
	{
		this.log = log;
		this.memory = memory;
		this.buffer = buffer;
		this.clothocomputer = clothocomputer;
		this.cercle = cercle;

		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);

		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE) / 2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		int marge = config.getInt(ConfigInfo.DILATATION_OBSTACLE_ROBOT);
		tmp = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant, marge);
		for(int i = 0; i < pointsAvancer.length; i++)
			pointsAvancer[i] = new CinematiqueObs(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant, marge);
	}

	private Vec2RW delta = new Vec2RW(), vecteurVitesse = new Vec2RW();
	// private Vec2RW pointA = new Vec2RW(), pointB = new Vec2RW(), pointC = new
	// Vec2RW();
	private Cinematique debut;
	private ArcCourbeStatique tmp;

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
	 * @throws MemoryManagerException
	 * @throws InterruptedException
	 */
	public ArcCourbeDynamique interpolationQuadratique(Cinematique cinematiqueInitiale, Vec2RO arrivee) throws MemoryManagerException
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
				// log.debug("Préfixe nécessaire !");
				prefixe = clothocomputer.getTrajectoireRamene(debut, VitesseRameneVolant.RAMENE_VOLANT);
			}
			if(prefixe == null)
				prefixe = new ArcCourbeDynamique(new ArrayList<CinematiqueObs>(), 0, VitesseBezier.BEZIER_QUAD);

			tmp.vitesse = d > 0 ? VitesseClotho.GAUCHE_1 : VitesseClotho.DROITE_1;
			clothocomputer.getTrajectoire(prefixe.getNbPoints() > 0 ? prefixe.getLast() : cinematiqueInitiale, d > 0 ? VitesseClotho.GAUCHE_1 : VitesseClotho.DROITE_1, tmp.arcselems);
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
		vecteurVitesse.scalar(Math.sqrt(d / (2 * debut.courbureGeometrique / 1000))); // c'est
																						// les
																						// maths
																						// qui
																						// le
																						// disent
		vecteurVitesse.plus(debut.getPosition());

		ArcCourbeDynamique arc = constructBezierQuad(debut.getPosition(), vecteurVitesse, arrivee, debut.enMarcheAvant, debut);

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
	 * Essai d'arrêt sur cercle. Fait une interpolation quadratique classique et
	 * retire les points du cercle.
	 * Aucune assurance sur l'orientation d'arrivée.
	 * 
	 * @param cinematiqueInitiale
	 * @param vitesseMax
	 * @return
	 * @throws InterruptedException
	 */
	@Deprecated
	public ArcCourbeDynamique interpolationQuadratiqueCercle(Cinematique cinematiqueInitiale) throws MemoryManagerException
	{
		ArcCourbeDynamique out = interpolationQuadratique(cinematiqueInitiale, cercle.position);
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
	 * 
	 * @param cinematique
	 * @param vitesseMax
	 * @return
	 */
	/*
	 * public ArcCourbeDynamique interpolationQuadratiqueCercle(Cinematique
	 * cinematiqueInitiale, Speed vitesseMax)
	 * {
	 * ArcCourbeDynamique prefixe = initCourbe(cinematiqueInitiale,
	 * cercle.position, vitesseMax);
	 * cercle.position.copy(a_tmp);
	 * a_tmp.minus(vecteurVitesse); // le point B
	 * vecteurVitesse.copy(b_tmp);
	 * b_tmp.minus(cinematiqueInitiale.getPosition());
	 * double alpha = a_tmp.getArgument() - b_tmp.getArgument(); // une
	 * estimation seulement du résultat final
	 * double sin = Math.sin(alpha);
	 * double c = cinematiqueInitiale.courbureGeometrique;
	 * b_tmp.scalar(1/b.norm()); // normalisation de b
	 * cercle.position.copy(a_tmp);
	 * a_tmp.minus(cinematiqueInitiale.getPosition());
	 * double L = a_tmp.dot(b);
	 * b.rotate(0,1);
	 * double l = a_tmp.dot(b);
	 * double r = cercle.rayon;
	 * return null;
	 * }
	 */

	private Vec2RW a_tmp = new Vec2RW(), b_tmp = new Vec2RW(), c_tmp = new Vec2RW(), acc = new Vec2RW();

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
	private ArcCourbeDynamique constructBezierQuad(Vec2RO A, Vec2RO B, Vec2RO C, boolean enMarcheAvant, Cinematique cinematiqueInitiale) throws MemoryManagerException
	{
		/*
		 * buffer.addSupprimable(new ObstacleCircular(A, 15));
		 * buffer.addSupprimable(new ObstacleCircular(B, 15));
		 * buffer.addSupprimable(new ObstacleCircular(C, 15));
		 */

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
			a_tmp.scalar((1 - t) * (1 - t));
			B.copy(b_tmp);
			b_tmp.scalar(2 * (1 - t) * t);
			C.copy(c_tmp);
			c_tmp.scalar(t * t);

			a_tmp.copy(obs.getPositionEcriture());
			obs.getPositionEcriture().plus(b_tmp);
			obs.getPositionEcriture().plus(c_tmp);
			out.addFirst(obs);

			if(lastPos != null)
				longueur += lastPos.distanceFast(obs.getPosition());

			lastPos = obs.getPosition();

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

			obs.update(obs.getPosition().getX(), // x
					obs.getPosition().getY(), // y
					orientation, enMarcheAvant, accLongitudinale / (vitesse * vitesse)); // Frenet

			double deltaO = (obs.orientationGeometrique - lastOrientation) % (2 * Math.PI);
			if(deltaO > Math.PI)
				deltaO -= 2 * Math.PI;
			else if(deltaO < -Math.PI)
				deltaO += 2 * Math.PI;

			// on a dépassé la courbure maximale : on arrête tout
			if(Math.abs(obs.courbureGeometrique) > courbureMax || (!first && (Math.abs(obs.courbureGeometrique - lastCourbure) > deltaCourbureMax || Math.abs(deltaO) > 0.5)))
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

			lastOrientation = obs.orientationGeometrique;
			lastCourbure = obs.courbureGeometrique;
			first = false;
			t -= ClothoidesComputer.PRECISION_TRACE_MM / vitesse;
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

		if(out.getFirst().getPosition().distanceFast(cinematiqueInitiale.getPosition()) < ClothoidesComputer.PRECISION_TRACE_MM / 2)
			memory.destroyNode(out.removeFirst());

		if(out.isEmpty())
			return null;

		return new ArcCourbeDynamique(out, longueur, VitesseBezier.BEZIER_QUAD);
	}

	public ArcCourbeDynamique trajectoireCirculaireVersCentre(Cinematique cinematique) throws MemoryManagerException
	{
		return trajectoireCirculaireVersCentre(cinematique, cercle.rayon);
	}
	
	public ArcCourbeDynamique trajectoireCirculaireVersCentre(Cinematique cinematique, double rayonP) throws MemoryManagerException
	{
		Vec2RO centre = cercle.position;
		double rayon = rayonP;

		double orientationReelleDesiree = Math.atan2(centre.getY() - cinematique.getPosition().getY(), centre.getX() - cinematique.getPosition().getX());
		double deltaO = (orientationReelleDesiree - cinematique.orientationReelle) % (2 * Math.PI);
		if(deltaO > Math.PI)
			deltaO -= 2 * Math.PI;
		else if(deltaO < -Math.PI)
			deltaO += 2 * Math.PI;

		// log.debug("deltaO = "+deltaO);
		boolean enAvant = Math.abs(deltaO) < Math.PI / 2;
		// log.debug("enAvant = "+enAvant);

		// on regarde maintenant modulo PI pour savoir si on est aligné
		deltaO = deltaO % Math.PI;
		if(deltaO > Math.PI / 2)
			deltaO -= Math.PI;
		if(deltaO < -Math.PI / 2)
			deltaO += Math.PI;

		if(Math.abs(deltaO) < 0.001) // on est presque aligné
		{
			// log.debug("Presque aligné : "+deltaO);
			double distance = cinematique.getPosition().distance(centre) - rayon;
			if(!enAvant)
				distance = -distance;
			
			LinkedList<CinematiqueObs> out = avance(distance, cinematique);
			if(out.isEmpty())
				return null;

			return new ArcCourbeDynamique(out, distance, VitesseBezier.CIRCULAIRE_VERS_CERCLE);
		}

		double cos = Math.cos(cinematique.orientationReelle);
		double sin = Math.sin(cinematique.orientationReelle);
		if(!enAvant)
		{
			cos = -cos;
			sin = -sin;
		}

		Vec2RO a = cinematique.getPosition(), bp = centre;
		// le symétrique du centre bp
		Vec2RO ap = new Vec2RO(cinematique.getPosition().getX() - rayon * cos, cinematique.getPosition().getY() - rayon * sin);
		Vec2RO d = bp.plusNewVector(ap).scalar(0.5); // le milieu entre ap et
														// bp, sur l'axe de
														// symétrie
		Vec2RW u = bp.minusNewVector(ap);
		double n = u.norm();
		double ux = -u.getY() / n;
		double uy = u.getX() / n;
		double vx = -sin;
		double vy = cos;

		// log.debug(ap+" "+a+" "+d+" "+bp);

		double alpha = (uy * (d.getX() - a.getX()) + ux * (a.getY() - d.getY())) / (vx * uy - vy * ux);
		Vec2RO c = new Vec2RO(a.getX() + alpha * vx, a.getY() + alpha * vy);
		if(alpha > 0)
		{
			ux = -ux;
			uy = -uy;
			vx = -vx;
			vy = -vy;
		}

		double rayonTraj = c.distance(a);
		double courbure = 1000. / rayonTraj; // la courbure est en m^-1

		if(courbure > courbureMax)
		{
			return null;
			/*
			 * double distance = cinematique.getPosition().distanceFast(centre)
			 * - rayon;
			 * if(!enAvant)
			 * distance = -distance;
			 * return avanceVersCentreLineaire(distance, centre, cinematique);
			 */
		}

		Vec2RW delta = a.minusNewVector(c);

		// on n'utilise pas getFastArgument ici car la précision est cruciale
		// pour arriver correctement sur le cercle
		double angle = (2 * (new Vec2RO(ux, uy).getArgument() - new Vec2RO(vx, vy).getArgument())) % (2 * Math.PI);
		if(angle > Math.PI)
			angle -= 2 * Math.PI;
		else if(angle < -Math.PI)
			angle += 2 * Math.PI;

		// double angle = 2*Math.acos(ux * vx + uy * vy); // angle total
		double longueur = angle * rayonTraj;
		// log.debug("Angle : "+angle);

		int nbPoints = (int) Math.round(Math.abs(longueur) / ClothoidesComputer.PRECISION_TRACE_MM);

		cos = Math.cos(angle);
		sin = Math.sin(angle);

		delta.rotate(Math.cos(angle), Math.sin(angle)); // le tout dernier
														// point, B

		// log.debug("B : "+delta.plusNewVector(c));

		double anglePas = -angle / nbPoints;

		if(angle < 0)
			courbure = -courbure;

		cos = Math.cos(anglePas);
		sin = Math.sin(anglePas);

		// log.debug("nbPoints = "+nbPoints);

		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();

		for(int i = nbPoints - 1; i >= 0; i--)
		{
			double orientation = cinematique.orientationReelle;
			if(!enAvant)
				orientation += Math.PI; // l'orientation géométrique
			orientation -= (i + 1) * anglePas;
			CinematiqueObs obs = memory.getNewNode();
			obs.update(delta.getX() + c.getX(), delta.getY() + c.getY(), orientation, enAvant, courbure);
			out.addFirst(obs);
			delta.rotate(cos, sin);
		}

		if(out.isEmpty())
			return null;

		return new ArcCourbeDynamique(out, longueur, VitesseBezier.CIRCULAIRE_VERS_CERCLE);
	}
	
	private CinematiqueObs[] pointsAvancer = new CinematiqueObs[256];

	public LinkedList<CinematiqueObs> avance(double distance, Cinematique cinematique) throws MemoryManagerException
	{
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
		double cos = Math.cos(cinematique.orientationReelle);
		double sin = Math.sin(cinematique.orientationReelle);
		int nbPoint = (int) Math.round(Math.abs(distance) / ClothoidesComputer.PRECISION_TRACE_MM);
		double xFinal = cinematique.getPosition().getX() + distance * cos;
		double yFinal = cinematique.getPosition().getY() + distance * sin;
		boolean marcheAvant = distance > 0;
		if(nbPoint == 0)
		{
			// Le point est vraiment tout proche
			pointsAvancer[0].updateReel(xFinal, yFinal, cinematique.orientationReelle, marcheAvant, 0);
			out.add(pointsAvancer[0]);
		}
		else
		{
			double deltaX = ClothoidesComputer.PRECISION_TRACE_MM * cos;
			double deltaY = ClothoidesComputer.PRECISION_TRACE_MM * sin;
			if(distance < 0)
			{
				deltaX = -deltaX;
				deltaY = -deltaY;
			}
			for(int i = 0; i < nbPoint; i++)
				pointsAvancer[nbPoint - i - 1].updateReel(xFinal - i * deltaX, yFinal - i * deltaY, cinematique.orientationReelle, marcheAvant, 0);
			for(int i = 0; i < nbPoint; i++)
				out.add(pointsAvancer[i]);
		}
		
		return out;
	}
}
