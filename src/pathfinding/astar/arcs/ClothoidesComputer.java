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

import graphic.PrintBuffer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import memory.CinemObsMM;
import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import obstacles.types.ObstacleCircular;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.RobotChrono;
import robot.Speed;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Classe qui s'occupe de tous les calculs concernant les clothoïdes
 * @author pf
 *
 */

public class ClothoidesComputer implements Service, Configurable
{
	private Log log;
	private CinemObsMM memory;
	private PrintBuffer buffer;
	
	private BigDecimal x, y; // utilisés dans le calcul de trajectoire
	private static final int S_MAX = 10; // courbure max qu'on puisse gérer
	public static final double PRECISION_TRACE = 0.02; // précision du tracé, en m (distance entre deux points consécutifs). Plus le tracé est précis, plus on couvre de point une même distance
	public static final double PRECISION_TRACE_MM = PRECISION_TRACE * 1000; // précision du tracé, en mm
	private static final int INDICE_MAX = (int) (S_MAX / PRECISION_TRACE);
	public static final int NB_POINTS = 5; // nombre de points dans un arc
	public static final double DISTANCE_ARC_COURBE = PRECISION_TRACE_MM * NB_POINTS; // en mm
	static final double DISTANCE_ARC_COURBE_M = PRECISION_TRACE * NB_POINTS; // en m
//	private static final double VITESSE_ROT_AX12 = 4; // en rad / s. Valeur du constructeur : 5

	private double courbureMax;
//	private double distanceArriereAuRoues; // la distance entre la position du robot et ses roues directrices
	private Vec2RO[] trajectoire = new Vec2RO[2 * INDICE_MAX - 1];
	
	public ClothoidesComputer(Log log, CinemObsMM memory, PrintBuffer buffer)
	{
		this.memory = memory;
		this.log = log;
		this.buffer = buffer;
		if(!chargePoints()) // le calcul est un peu long, donc on le sauvegarde
		{
			init();
			sauvegardePoints();
		}
	}
	
	/**
	 * Calcul grâce au développement limité d'Euler
	 * Génère le point de la clothoïde unitaire de courbure = s
	 * @param s
	 */
	private void calculeXY(BigDecimal sparam)
	{
		BigDecimal s = sparam;
		x = s;
		BigDecimal factorielle = new BigDecimal(1).setScale(15, RoundingMode.HALF_EVEN);
		BigDecimal b2 = new BigDecimal(1).setScale(15, RoundingMode.HALF_EVEN);
		BigDecimal s2 = s.multiply(s);
		BigDecimal b = b2;
		s = s.multiply(s2);
		y = s.divide(b.multiply(new BigDecimal(3).setScale(15, RoundingMode.HALF_EVEN)), RoundingMode.HALF_EVEN);		
		BigDecimal seuil = new BigDecimal(0.000000000001).setScale(15, RoundingMode.HALF_EVEN);
		BigDecimal tmp;
		
		long i = 1;
		do
		{
			factorielle = factorielle.multiply(new BigDecimal(2*i).setScale(15, RoundingMode.HALF_EVEN));
			b = b.multiply(b2);
			s = s.multiply(s2);
			
			tmp = s.divide(factorielle.multiply(b).multiply(new BigDecimal(4*i+1).setScale(15, RoundingMode.HALF_EVEN)), RoundingMode.HALF_EVEN);

			if((i & 1) == 0)
				x = x.add(tmp);
			else
				x = x.subtract(tmp);
			
			factorielle = factorielle.multiply(new BigDecimal(2*i+1).setScale(15, RoundingMode.HALF_EVEN));
			
			b = b.multiply(b2);
			s = s.multiply(s2);
			tmp = s.divide(factorielle.multiply(b).multiply(new BigDecimal(4*i+3).setScale(15, RoundingMode.HALF_EVEN)), RoundingMode.HALF_EVEN);

			if((i & 1) == 0)
				y = y.add(tmp);
			else
				y = y.subtract(tmp);

			i++;
		} while(tmp.abs().compareTo(seuil) > 0);
		// On fait en sorte que tourner à gauche ait une courbure positive
		y = y.multiply(new BigDecimal(1000)); // On considère que x et y sont en millimètre et que la courbure est en mètre^-1
		x = x.multiply(new BigDecimal(1000));
	}
	
	/**
	 * Calcule, une fois pour toutes, les points de la clothoïde unitaire
	 */
	private void init()
	{
		for(int s = 0; s < 2 * INDICE_MAX - 1; s++)
		{
			calculeXY(new BigDecimal((s - INDICE_MAX + 1) * PRECISION_TRACE).setScale(15, RoundingMode.HALF_EVEN));
			trajectoire[s] = new Vec2RO(x.doubleValue(), y.doubleValue());
			trajectoire[2 * INDICE_MAX - 2 - s] = new Vec2RO(-x.doubleValue(), -y.doubleValue());
			System.out.println((s - INDICE_MAX + 1) * PRECISION_TRACE+" "+trajectoire[s]);

			buffer.addSupprimable(new ObstacleCircular(new Vec2RO(x.doubleValue(), 1000+y.doubleValue()), 5));
			buffer.addSupprimable(new ObstacleCircular(new Vec2RO(-x.doubleValue(), 1000-y.doubleValue()), 5));
		}
	}

	public final ArcCourbeDynamique cubicInterpolation(RobotChrono robot, Cinematique arrivee, Speed vitesseMax, VitesseCourbure v)
	{
		return cubicInterpolation(robot.getCinematique(), arrivee, vitesseMax, v);
	}
	
	public final ArcCourbeDynamique cubicInterpolation(Cinematique cinematiqueInitiale, Cinematique arrivee, Speed vitesseMax, VitesseCourbure v)
	{
//		log.debug(v);
		double alphas[] = {100, 300, 600, 800, 1000, 1200};
//		log.debug("Interpolation cubique, rebrousse : "+v.rebrousse);
		double sin, cos;
		double courbure = cinematiqueInitiale.courbureGeometrique,
				orientation = cinematiqueInitiale.orientationGeometrique;

		if(v.rebrousse) // si on rebrousse, la courbure est nulle
		{
			orientation += Math.PI;
			courbure = 0;
		}
		
		Speed vitesse = vitesseMax;
		if(!(v.rebrousse ^ cinematiqueInitiale.enMarcheAvant))
			vitesse = Speed.values()[vitesseMax.ordinal()+1];
		
		sin = Math.sin(orientation);
		cos = Math.cos(orientation);

		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
		
		for(int i = 0; i < alphas.length; i++)
		{
			double alpha = alphas[i];
			
			double bx, by;
			
			if(Math.abs(cos) < Math.abs(sin))
			{
				bx = -courbure/1000*alpha*alpha/(2*sin);
				by = 0;
			}
			else
			{
				bx = 0;
				by = courbure/1000*alpha*alpha/(2*cos);
			}
	
			// les paramètres du polynomes x = ax * t^3 + bx * t^2 + cx * t + dx,
			// en fixant la position en 0 (départ) et en 1 (arrivée), la dérivée en 0 (vecteur vitesse) et la dérivée seconde en 0 (courbure)
			double dx = cinematiqueInitiale.getPosition().getX();
			double cx = cos*alpha;
			double ax = arrivee.getPosition().getX() - bx - cx - dx;
			
			// idem pour y
			double dy = cinematiqueInitiale.getPosition().getY();
			double cy = sin*alpha;
			double ay = arrivee.getPosition().getY() - by - cy - dy;
			boolean first = true;
			double t = 1, tnext = 1, lastCourbure = 0, lastOrientation = 0;
			double longueur = 0;
			CinematiqueObs last = null, actuel;
			boolean error = false;
			out.clear();
			
			while(t > 0.)
			{
				double tmp1 = 3*ax*t*t+2*bx*t+cx;
				double tmp2 = 3*ay*t*t+2*by*t+cy;
				
				tnext -= PRECISION_TRACE_MM / Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);

				if(tnext < 0)
					tnext = 0; // le 0 ne doit pas être utilisé (c'est le dernier point de l'arc précédent)
	
				double t2 = t*t;
				double x = ax*t*t2 + bx*t2 + cx*t + dx;
				double y = ay*t*t2 + by*t2 + cy*t + dy;
				double vx = 3*ax*t2+2*bx*t+cx;
				double vy = 3*ay*t2+2*by*t+cy;
				double acx = 6*ax*t+2*bx;
				double acy = 6*ay*t+2*by;
				
				actuel = memory.getNewNode();
				actuel.update(
						x, // x
						y, // y
						Math.atan2(vy, vx), // orientation = atan2(vy, vx)
						v.rebrousse ^ cinematiqueInitiale.enMarcheAvant,
						1000*(vx * acy - vy * acx) / Math.pow(vx*vx + vy*vy, 1.5), // formule de la courbure d'une courbe paramétrique
						vitesse.translationalSpeed);
//				log.debug(t);
				
				// Il faut faire attention à ne pas dépasser la coubure maximale !
				// On prend de la marge
				if(x < -1500 || x > 1500 || y < 0 || y > 2000 || Math.abs(actuel.courbureGeometrique) > courbureMax*0.7 || (first && (Math.abs(actuel.courbureGeometrique - courbure) > 1)) || (!first && (Math.abs(actuel.courbureGeometrique - lastCourbure) > 1 || Math.abs(actuel.orientationGeometrique - lastOrientation) > 1)))
				{
//					log.debug("ERREUR");
					error = true;
					break;
				}
				
				//				log.debug(x+" "+y);
				lastCourbure = actuel.courbureGeometrique;
				lastOrientation = actuel.orientationGeometrique;				
				// calcul de la longueur de l'arc
				if(last != null)
				{
					longueur += actuel.getPosition().distanceFast(last.getPosition());
				}
				// TODO : faire un pas plus petit, puis sélectionner seulement ceux qui correspondent à la bonne précision

				
				out.addFirst(actuel); // vu qu'on génère la trajectoire cubique en partant de la fin
				last = actuel;
				first = false;
				t = tnext;
			}			
			
			// Parfois, l'interpolation cubique donne très peu de points (à cause du pas de t qui est une approximation)
			// dans ce cas, on ignore l'arc

			if(error || out.size() < (cinematiqueInitiale.getPosition().distanceFast(arrivee.getPosition()) / PRECISION_TRACE_MM)) // on essaye un autre alpha
			{
				// on détruit les cinématiques qui ne seront pas utilisées
				for(CinematiqueObs c : out)
					memory.destroyNode(c);
				continue;
			}
			
			// si le premier point est trop proche du point de départ, on ne le met pas
			if(out.getFirst().getPosition().squaredDistance(cinematiqueInitiale.getPosition()) < (PRECISION_TRACE_MM * PRECISION_TRACE_MM)*0.7)
				out.removeFirst();

			return new ArcCourbeDynamique(out, longueur, v.rebrousse);
		}
		return null;
	}
	
	public void getTrajectoire(ArcCourbe depart, VitesseCourbure vitesse, Speed vitesseMax, ArcCourbeStatique modified)
	{
		CinematiqueObs last = depart.getLast();
		getTrajectoire(last, vitesse, vitesseMax, modified);
	}
	
	/**
	 * Première trajectoire. On considère que la vitesse initiale du robot est nulle
	 * @param robot
	 * @param vitesse
	 * @param modified
	 */
	public final void getTrajectoire(RobotChrono robot, VitesseCourbure vitesse, Speed vitesseMax, ArcCourbeStatique modified)
	{
		getTrajectoire(robot.getCinematique(), vitesse, vitesseMax, modified);
	}
	
	/**
	 * ATTENTION ! La courbure est en m^-1 et pas en mm^-1
	 * En effet, comme le rayon de courbure sera souvent plus petit que le mètre, on aura une courbure souvent plus grande que 1
	 * Le contenu est mis dans l'arccourbe directement
	 * @param position
	 * @param orientationGeometrique
	 * @param courbureGeometrique
	 * @param vitesse
	 * @param distance_mm
	 * @return
	 */
	public final void getTrajectoire(Cinematique cinematiqueInitiale, VitesseCourbure vitesse, Speed vitesseMax, ArcCourbeStatique modified)
	{
//		modified.v = vitesse;
//		log.debug(vitesse);
		double courbure = cinematiqueInitiale.courbureGeometrique;
		double orientation = cinematiqueInitiale.orientationGeometrique;
		if(vitesse.rebrousse)
		{
			courbure = 0;
			orientation += Math.PI;
		}
		modified.rebrousse = vitesse.rebrousse;

		boolean marcheAvant = vitesse.rebrousse ^ cinematiqueInitiale.enMarcheAvant;

		// Gestion de la vitesse en marche arrière
		Speed vitesseTr = vitesseMax;
		if(!marcheAvant)
			vitesseTr = Speed.values()[vitesseMax.ordinal()+1];

		// si la dérivée de la courbure est nulle, on est dans le cas particulier d'une trajectoire rectiligne ou circulaire
		if(vitesse.vitesse == 0)
		{
			if(courbure < 0.00001 && courbure > -0.00001)
				getTrajectoireLigneDroite(cinematiqueInitiale.getPosition(), orientation, vitesseTr, modified, marcheAvant);
			else
				getTrajectoireCirculaire(cinematiqueInitiale.getPosition(), orientation, courbure, vitesseTr, modified, marcheAvant);
			return;
		}
		
		double coeffMultiplicatif = 1. / vitesse.squaredRootVitesse;
		double sDepart = courbure / vitesse.squaredRootVitesse; // sDepart peut parfaitement être négatif
		if(!vitesse.positif)
			sDepart = -sDepart;
		int pointDepart = (int) ((sDepart / PRECISION_TRACE) + INDICE_MAX - 1 + 0.5); // le 0.5 vient du fait qu'on fait ici un arrondi
		
		if(pointDepart < 0 || pointDepart >= trajectoire.length)
			log.critical("Sorti de la clothoïde précalculée !");
		
		double orientationClothoDepart = sDepart * sDepart; // orientation au départ
		if(!vitesse.positif)
			orientationClothoDepart = - orientationClothoDepart;
			
		double baseOrientation = orientation - orientationClothoDepart;
		double cos = Math.cos(baseOrientation);
		double sin = Math.sin(baseOrientation);

//		for(int i = 0; i < NB_POINTS; i++)
//			log.debug("Clotho : "+trajectoire[vitesse.squaredRootVitesse * (i + 1)]);
	
		// le premier point n'est pas position, mais le suivant
		// (afin de ne pas avoir de doublon quand on enchaîne les arcs, entre le dernier point de l'arc t et le premier de l'arc t+1)		
		for(int i = 0; i < NB_POINTS; i++)
		{
			sDepart += vitesse.squaredRootVitesse * PRECISION_TRACE;
			computePoint(pointDepart, vitesse, sDepart, coeffMultiplicatif, i, baseOrientation, cos, sin, marcheAvant, vitesseTr, cinematiqueInitiale.getPosition(), modified.arcselems[i]);
		}

	}
	
	/**
	 * Construit un arc courbe dynamique qui ramène la courbure à 0
	 * @param position
	 * @param orientation
	 * @param courbure
	 * @param vitesseTr
	 * @param modified
	 * @param enMarcheAvant
	 * @param vitesse
	 * @return 
	 */
	public final ArcCourbeDynamique getTrajectoireRamene(Cinematique cinematiqueInitiale, VitesseCourbure vitesse, Speed vitesseMax)
	{
		double courbure = cinematiqueInitiale.courbureGeometrique;
		double orientation = cinematiqueInitiale.orientationGeometrique;

		boolean marcheAvant = cinematiqueInitiale.enMarcheAvant;

		// Gestion de la vitesse en marche arrière
		Speed vitesseTr = vitesseMax;
		if(!marcheAvant)
			vitesseTr = Speed.values()[vitesseMax.ordinal()+1];

		double coeffMultiplicatif = 1. / vitesse.squaredRootVitesse;
		double sDepart = courbure / vitesse.squaredRootVitesse; // sDepart peut parfaitement être négatif
		if(courbure > 0)
			sDepart = -sDepart;
		int pointDepart = (int) ((sDepart / PRECISION_TRACE) + INDICE_MAX - 1 + 0.5); // le 0.5 vient du fait qu'on fait ici un arrondi
		
		if(pointDepart < 0 || pointDepart >= trajectoire.length)
			log.critical("Sorti de la clothoïde précalculée !");
		
		double orientationClothoDepart = sDepart * sDepart; // orientation au départ
		if(courbure > 0)
			orientationClothoDepart = - orientationClothoDepart;
			
		double baseOrientation = orientation - orientationClothoDepart;
		double cos = Math.cos(baseOrientation);
		double sin = Math.sin(baseOrientation);

//		for(int i = 0; i < NB_POINTS; i++)
//			log.debug("Clotho : "+trajectoire[vitesse.squaredRootVitesse * (i + 1)]);
	
		// le premier point n'est pas position, mais le suivant
		// (afin de ne pas avoir de doublon quand on enchaîne les arcs, entre le dernier point de l'arc t et le premier de l'arc t+1)		
		double sDepartPrecedent;
		int i = 0;
		List<CinematiqueObs> out = new ArrayList<CinematiqueObs>();
		while(true)
		{
			sDepartPrecedent = sDepart;
			sDepart += vitesse.squaredRootVitesse * PRECISION_TRACE;
			if(Math.abs(sDepart) > Math.abs(sDepartPrecedent)) // on vérifie la courbure
				break;
			CinematiqueObs obs = memory.getNewNode();
			out.add(obs);
			computePoint(pointDepart, vitesse, sDepart, coeffMultiplicatif, i, baseOrientation, cos, sin, marcheAvant, vitesseTr, cinematiqueInitiale.getPosition(), obs);
			i++;
		}
		
		return new ArcCourbeDynamique(out, i*PRECISION_TRACE_MM, false);
	}
	
	/**
	 * Calcul un point à partir de ces quelques paramètres
	 * @param pointDepart : l'indice du point de depart dans trajectoire[]
	 * @param vitesse : la vitesse de courbure
	 * @param sDepart : la valeur de "s" au point de départ
	 * @param coeffMultiplicatif : issu de la vitesse de courbure
	 * @param i : quel point dans la trajectoire
	 * @param baseOrientation : l'orientation au début du mouvement
	 * @param cos : le cos de baseOrientation
	 * @param sin : son sin
	 * @param marcheAvant : si le trajet est fait en marche avant
	 * @param vitesseTr : la vitesse translatoire souhaitée
	 * @param positionInitiale : la position au début du mouvement
	 * @param c
	 */
	private void computePoint(int pointDepart, VitesseCourbure vitesse, double sDepart, double coeffMultiplicatif, int i, double baseOrientation, double cos, double sin, boolean marcheAvant, Speed vitesseTr, Vec2RO positionInitiale, CinematiqueObs c)
	{
		trajectoire[pointDepart + vitesse.squaredRootVitesse * (i + 1)].copy(c.getPositionEcriture());
		c.getPositionEcriture().minus(trajectoire[pointDepart])
		.scalar(coeffMultiplicatif)
		.Ysym(!vitesse.positif)
		.rotate(cos, sin)
		.plus(positionInitiale);

		double orientationClotho = sDepart * sDepart;
		if(!vitesse.positif)
			orientationClotho = - orientationClotho;
			 			
		c.orientationGeometrique = baseOrientation + orientationClotho;
		c.courbureGeometrique = sDepart * vitesse.squaredRootVitesse;
		
		if(!vitesse.positif)
			c.courbureGeometrique = - c.courbureGeometrique;

		if(marcheAvant)
		{
			c.orientationReelle = c.orientationGeometrique;
			c.courbureReelle = c.courbureGeometrique;
		}
		else
		{
			c.orientationReelle = c.orientationGeometrique + Math.PI;
			c.courbureReelle = - c.courbureGeometrique;
		}
		
		c.enMarcheAvant = marcheAvant;
		
		c.vitesseMax = vitesseTr.translationalSpeed; //computeVitesseMax(vitesseTr.translationalSpeed, vitesse.vitesse);
		c.obstacle.update(c.getPosition(), c.orientationReelle);
	}

	private Vec2RW delta = new Vec2RW();
	private Vec2RW centreCercle = new Vec2RW();
	
	/**
	 * Calcule la trajectoire dans le cas particulier d'une trajectoire circulaire
	 * @param position
	 * @param orientation
	 * @param courbure
	 * @param modified
	 */
	private void getTrajectoireCirculaire(Vec2RO position, double orientation, double courbure, Speed vitesseMax, ArcCourbeStatique modified, boolean enMarcheAvant)
	{		
//		log.debug("Trajectoire circulaire !");
		// rappel = la courbure est l'inverse du rayon de courbure
		// le facteur 1000 vient du fait que la courbure est en mètre^-1
		double rayonCourbure = 1000. / courbure;
		delta.setX(Math.cos(orientation + Math.PI / 2) * rayonCourbure);
		delta.setY(Math.sin(orientation + Math.PI / 2) * rayonCourbure);
		
		centreCercle.setX(position.getX() + delta.getX());
		centreCercle.setY(position.getY() + delta.getY());
		
		double angle = PRECISION_TRACE * courbure; // périmètre = angle * rayon
		
		double cos = Math.cos(angle); // l'angle de rotation autour du cercle est le même que l'angle dont le robot tourne
		double sin = Math.sin(angle);
		
		for(int i = 0; i < NB_POINTS; i++)
		{
			delta.rotate(cos, sin);
			centreCercle.copy(modified.arcselems[i].getPositionEcriture());
			modified.arcselems[i].getPositionEcriture().minus(delta);
			modified.arcselems[i].orientationGeometrique = orientation + angle * (i + 1);
			modified.arcselems[i].courbureGeometrique = courbure;

 			if(enMarcheAvant)
 			{
				modified.arcselems[i].orientationReelle = modified.arcselems[i].orientationGeometrique;
				modified.arcselems[i].courbureReelle = modified.arcselems[i].courbureGeometrique;
 			}
 			else
 			{
				modified.arcselems[i].orientationReelle = modified.arcselems[i].orientationGeometrique + Math.PI;
				modified.arcselems[i].courbureReelle = - modified.arcselems[i].courbureGeometrique;
 			}

 			modified.arcselems[i].vitesseMax = vitesseMax.translationalSpeed;
			modified.arcselems[i].enMarcheAvant = enMarcheAvant;
			modified.arcselems[i].obstacle.update(modified.arcselems[i].getPosition(), modified.arcselems[i].orientationReelle);
		}
	}

	/**
	 * Calcule la trajectoire dans le cas particulier d'une ligne droite
	 * @param position
	 * @param orientation
	 * @param modified
	 */
	private void getTrajectoireLigneDroite(Vec2RO position, double orientation, Speed vitesseMax, ArcCourbeStatique modified, boolean enMarcheAvant)
	{
		double cos = Math.cos(orientation);
		double sin = Math.sin(orientation);

		for(int i = 0; i < NB_POINTS; i++)
		{
			double distance = (i + 1) * PRECISION_TRACE_MM;
			modified.arcselems[i].getPositionEcriture().setX(position.getX() + distance * cos);
			modified.arcselems[i].getPositionEcriture().setY(position.getY() + distance * sin);
			modified.arcselems[i].orientationGeometrique = orientation;
			modified.arcselems[i].courbureGeometrique = 0;
			modified.arcselems[i].courbureReelle = 0;
			
 			if(enMarcheAvant)
				modified.arcselems[i].orientationReelle = modified.arcselems[i].orientationGeometrique;
 			else
				modified.arcselems[i].orientationReelle = modified.arcselems[i].orientationGeometrique + Math.PI;
 			
 			modified.arcselems[i].vitesseMax = vitesseMax.translationalSpeed;
			modified.arcselems[i].enMarcheAvant = enMarcheAvant;
			modified.arcselems[i].obstacle.update(modified.arcselems[i].getPosition(), modified.arcselems[i].orientationReelle);
		}
	}

	@Override
	public void useConfig(Config config)
	{
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);
//		int tmpx = config.getInt(ConfigInfo.CENTRE_ROTATION_ROUE_X);
//		int tmpy = config.getInt(ConfigInfo.CENTRE_ROTATION_ROUE_Y);
//		distanceArriereAuRoues = Math.sqrt(tmpx*tmpx + tmpy*tmpy) / 1000;
	}

	/**
	 * Sauvegarde les points de la clothoïde unitaire
	 */
    private void sauvegardePoints()
    {
    	log.debug("Sauvegarde des points de la clothoïde unitaire");
        try {
            java.io.File fichier_creation;
            FileOutputStream fichier;
            ObjectOutputStream oos;

            fichier_creation = new java.io.File("clotho-"+S_MAX+".dat");
            fichier_creation.createNewFile();
            fichier = new FileOutputStream("clotho-"+S_MAX+".dat");
            oos = new ObjectOutputStream(fichier);
            oos.writeObject(trajectoire);
            oos.flush();
            oos.close();
        	log.debug("Sauvegarde terminée");
        }
        catch(IOException e)
        {
            log.critical("Erreur lors de la sauvegarde des points de la clothoïde ! "+e);
        }
    }
	
    /**
     * Chargement des points de la clothoïde unitaire
     * @return
     */
	private boolean chargePoints()
    {
    	log.debug("Chargement des points de la clothoïde");
        try {
            FileInputStream fichier = new FileInputStream("clotho-"+S_MAX+".dat");
            ObjectInputStream ois = new ObjectInputStream(fichier);
            trajectoire = (Vec2RO[]) ois.readObject();
            ois.close();
            return true;
        }
        catch(IOException | ClassNotFoundException e)
        {
        	log.critical("Chargement échoué !");
        }
        return false;
    }

	private Vec2RW vecteurOrientationDepart = new Vec2RW();
	private Vec2RW vecteurOrientation = new Vec2RW();
	
	/**
	 * Construit un arc courbe qui fait faire un demi-tour au robot
	 * @param cinematiqueInitiale
	 * @param vitesse
	 * @param vitesseMax
	 * @return
	 */
	public final ArcCourbeDynamique getTrajectoireDemiTour(Cinematique cinematiqueInitiale, VitesseCourbure vitesse, Speed vitesseMax)
	{
		List<CinematiqueObs> trajet = getTrajectoireQuartDeTour(cinematiqueInitiale, vitesse, vitesseMax, false);
		trajet.addAll(getTrajectoireQuartDeTour(trajet.get(trajet.size()-1), vitesse, vitesseMax, true)); // on reprend à la fin du premier quart de tour
		return new ArcCourbeDynamique(trajet, trajet.size()*PRECISION_TRACE_MM, false); // TODO : rebrousse est faux…
	}
	
	/**
	 * Construit un arc courbe qui fait un quart de tour au robot
	 * @param position
	 * @param orientation
	 * @param courbure
	 * @param vitesseTr
	 * @param modified
	 * @param enMarcheAvant
	 * @param vitesse
	 * @return 
	 */
	private final List<CinematiqueObs> getTrajectoireQuartDeTour(Cinematique cinematiqueInitiale, VitesseCourbure vitesse, Speed vitesseMax, boolean rebrousse)
	{
		double courbure = cinematiqueInitiale.courbureGeometrique;
		double orientation = cinematiqueInitiale.orientationGeometrique;
		if(rebrousse)
		{
			courbure = 0;
			orientation += Math.PI;
		}

		vecteurOrientationDepart.setX(Math.cos(orientation));
		vecteurOrientationDepart.setY(Math.sin(orientation));
		
		boolean marcheAvant = rebrousse ^ cinematiqueInitiale.enMarcheAvant;

		// Gestion de la vitesse en marche arrière
		Speed vitesseTr = vitesseMax;
		if(!marcheAvant)
			vitesseTr = Speed.values()[vitesseMax.ordinal()+1];

		double coeffMultiplicatif = 1. / vitesse.squaredRootVitesse;
		double sDepart = courbure / vitesse.squaredRootVitesse; // sDepart peut parfaitement être négatif
		if(!vitesse.positif)
			sDepart = -sDepart;
		int pointDepart = (int) ((sDepart / PRECISION_TRACE) + INDICE_MAX - 1 + 0.5); // le 0.5 vient du fait qu'on fait ici un arrondi
		
		if(pointDepart < 0 || pointDepart >= trajectoire.length)
			log.critical("Sorti de la clothoïde précalculée !");
		
		double orientationClothoDepart = sDepart * sDepart; // orientation au départ
		if(!vitesse.positif)
			orientationClothoDepart = - orientationClothoDepart;

		double baseOrientation = orientation - orientationClothoDepart;
		double cos = Math.cos(baseOrientation);
		double sin = Math.sin(baseOrientation);

		// le premier point n'est pas position, mais le suivant
		// (afin de ne pas avoir de doublon quand on enchaîne les arcs, entre le dernier point de l'arc t et le premier de l'arc t+1)		
		int i = 0;

		List<CinematiqueObs> out = new ArrayList<CinematiqueObs>();
		Vec2RO positionInit = cinematiqueInitiale.getPosition();
		do
		{
			sDepart += vitesse.squaredRootVitesse * PRECISION_TRACE;
			CinematiqueObs obs = memory.getNewNode();
			out.add(obs);
			computePoint(pointDepart, vitesse, sDepart, coeffMultiplicatif, i, baseOrientation, cos, sin, marcheAvant, vitesseTr, positionInit, obs);
			vecteurOrientation.setX(Math.cos(obs.orientationGeometrique));
			vecteurOrientation.setY(Math.sin(obs.orientationGeometrique));
			i++;
		} while(vecteurOrientation.dot(vecteurOrientationDepart) >= 0);
		return out;
	}
	
	// TODO
	
	/**
	 * Calcule la vitesse translatoire maximale pour une vitesse de courbure donnée et la vitesse de rotation des servo des roues directrices
	 * La formule n'est pas exacte… mais ça suffit (la formule ne fonctionne plus pour des courbures très grandes)
	 * Pour le moon-rover, il n'y aura jamais de problème
	 * @param vitesseMaxDesiree
	 * @param vitesseCourbure
	 * @return
	 */
/*	private double computeVitesseMax(double vitesseMaxDesiree, double vitesseCourbure)
	{
		return Math.min(vitesseMaxDesiree, Math.abs(vitesseCourbure / VITESSE_ROT_AX12 / distanceArriereAuRoues));
	}*/
	
}
