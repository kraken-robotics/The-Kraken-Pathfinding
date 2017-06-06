/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.astar.arcs;

import java.util.LinkedList;

import config.Config;
import kraken.ConfigInfoKraken;
import kraken.exceptions.MemoryManagerException;
import kraken.memory.CinemObsMM;
import kraken.pathfinding.astar.arcs.vitesses.VitesseBezier;
import kraken.robot.Cinematique;
import kraken.robot.CinematiqueObs;
import kraken.utils.XY;
import kraken.utils.XY_RW;

/**
 * Some circle computer
 * @author pf
 *
 */

public class CircleComputer {

	private CercleArrivee cercle;
	private CinematiqueObs[] pointsAvancer = new CinematiqueObs[256];
	private double courbureMax;
	private CinemObsMM memory;
	
	public CircleComputer(Config config, CercleArrivee cercle, CinemObsMM memory)
	{
		this.cercle = cercle;
		this.memory = memory;
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);
		courbureMax = config.getDouble(ConfigInfoKraken.COURBURE_MAX);
		int demieLargeurNonDeploye = config.getInt(ConfigInfoKraken.LARGEUR_NON_DEPLOYE) / 2;
		int demieLongueurArriere = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		for(int i = 0; i < pointsAvancer.length; i++)
			pointsAvancer[i] = new CinematiqueObs(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

	}
	
	public ArcCourbeDynamique trajectoireCirculaireVersCentre(Cinematique cinematique) throws MemoryManagerException
	{
		return trajectoireCirculaireVersCentre(cinematique, cercle.rayon);
	}
	
	public ArcCourbeDynamique trajectoireCirculaireVersCentre(Cinematique cinematique, double rayonP) throws MemoryManagerException
	{
		XY centre = cercle.position;
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

		XY a = cinematique.getPosition(), bp = centre;
		// le symétrique du centre bp
		XY ap = new XY(cinematique.getPosition().getX() - rayon * cos, cinematique.getPosition().getY() - rayon * sin);
		XY d = bp.plusNewVector(ap).scalar(0.5); // le milieu entre ap et
														// bp, sur l'axe de
														// symétrie
		XY_RW u = bp.minusNewVector(ap);
		double n = u.norm();
		double ux = -u.getY() / n;
		double uy = u.getX() / n;
		double vx = -sin;
		double vy = cos;

		// log.debug(ap+" "+a+" "+d+" "+bp);

		double alpha = (uy * (d.getX() - a.getX()) + ux * (a.getY() - d.getY())) / (vx * uy - vy * ux);
		XY c = new XY(a.getX() + alpha * vx, a.getY() + alpha * vy);
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

		XY_RW delta = a.minusNewVector(c);

		// on n'utilise pas getFastArgument ici car la précision est cruciale
		// pour arriver correctement sur le cercle
		double angle = (2 * (new XY(ux, uy).getArgument() - new XY(vx, vy).getArgument())) % (2 * Math.PI);
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
