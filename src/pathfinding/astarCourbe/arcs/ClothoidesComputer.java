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

package pathfinding.astarCourbe.arcs;

import graphic.PrintBuffer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

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
	private static final int INDICE_MAX = (int) (S_MAX / PRECISION_TRACE);
	public static final int NB_POINTS = 5; // nombre de points dans un arc
	public static final double DISTANCE_ARC_COURBE = PRECISION_TRACE * NB_POINTS * 1000; // en mm
	static final double DISTANCE_ARC_COURBE_M = PRECISION_TRACE * NB_POINTS; // en m
	public static final double d = PRECISION_TRACE * 1000 / 2; // utilisé pour la trajectoire circulaire

	private double[] sinMap = new double[120];
	private double[] cosMap = new double[120];
	private double[] angleMap = new double[120];
	
	private double courbureMax;
	
	private Vec2RO[] trajectoire = new Vec2RO[2 * INDICE_MAX - 1];
	
	public ClothoidesComputer(Log log, CinemObsMM memory, PrintBuffer buffer)
	{
		for(int i = 0; i < 120; i++)
		{
			double courbure = (i-60) / 10;
			double rayonCourbure = 1000. / courbure;
			double cos = Math.sqrt(rayonCourbure * rayonCourbure - d * d) / rayonCourbure;
			double sin = Math.abs(d / rayonCourbure);
			sinMap[i] = 2 * sin * cos; // sin(a) devient sin(2a)
			cosMap[i] = 2 * cos * cos - 1; // cos(a) devient cos(2a)
			angleMap[i] = Math.atan2(cos, sin);
		}
		
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

	public final ArcCourbeCubique cubicInterpolation(RobotChrono robot, Cinematique arrivee, Speed vitesseMax, VitesseCourbure v)
	{
		return cubicInterpolation(robot, robot.getCinematique(), arrivee, vitesseMax, v);
	}
	
	public final ArcCourbeCubique cubicInterpolation(RobotChrono robot, Cinematique cinematiqueInitiale, Cinematique arrivee, Speed vitesseMax, VitesseCourbure v)
	{
//		log.debug(v);
		double alphas[] = {100, 300, 600, 800, 1000, 1200};
//		log.debug("Interpolation cubique, rebrousse : "+v.rebrousse);
		double sin, cos;
		double courbure = cinematiqueInitiale.courbureGeometrique;
		if(v.rebrousse) // si on rebrousse, la courbure est nulle
		{
			sin = Math.sin(cinematiqueInitiale.orientationGeometrique + Math.PI);
			cos = Math.cos(cinematiqueInitiale.orientationGeometrique + Math.PI);
			courbure = 0;
		}
		else
		{
			sin = Math.sin(cinematiqueInitiale.orientationGeometrique);
			cos = Math.cos(cinematiqueInitiale.orientationGeometrique);			
		}
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
	
			double t = 1, tnext = 1, lastCourbure = courbure;
			double longueur = 0;
			CinematiqueObs last = null, actuel;
			boolean error = false;
			out.clear();
			
			while(t > 0.)
			{
				t = tnext;
				
				tnext -= PRECISION_TRACE*1000/(Math.hypot(3*ax*t*t+2*bx*t+cx, 3*ay*t*t+2*by*t+cy));

				if(tnext < 0)
					t = 0; // on veut finir à 0 exactement
	
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
						vitesseMax,
						robot);

//				log.debug(t);
				
				// Il faut faire attention à ne pas dépasser la coubure maximale !
				// On prend de la marge
				if(x < -1500 || x > 1500 || y < 0 || y > 2000 || Math.abs(actuel.courbureGeometrique) > courbureMax*0.7 || Math.abs(actuel.courbureGeometrique - lastCourbure) > 3)
				{
//					log.debug("ERREUR");
					error = true;
					break;
				}
				
				//				log.debug(x+" "+y);
				lastCourbure = actuel.courbureGeometrique;
				
				// calcul de la longueur de l'arc
				if(last != null)
					longueur += actuel.getPosition().distance(last.getPosition());
				// TODO : faire un pas plus petit, puis sélectionner seulement ceux qui correspondent à la bonne précision

				
				out.addFirst(actuel); // vu qu'on génère la trajectoire cubique en partant de la fin
				last = actuel;
			}
			
			// Parfois, l'interpolation cubique donne très peu de points (à cause du pas de t qui est une approximation)
			// dans ce cas, on ignore l'arc

			if(error || out.size() < (cinematiqueInitiale.getPosition().distance(arrivee.getPosition()) / (1000 * PRECISION_TRACE))) // on essaye un autre alpha
			{
				// on détruit les cinématiques qui ne seront pas utilisées
				for(CinematiqueObs c : out)
					memory.destroyNode(c);
				continue;
			}
			
			return new ArcCourbeCubique(out, longueur, v.rebrousse, v.rebrousse);
		}
		return null;
	}
	
	public void getTrajectoire(RobotChrono robot, ArcCourbe depart, VitesseCourbure vitesse, Speed vitesseMax, ArcCourbeClotho modified)
	{
		CinematiqueObs last = depart.getLast();
		getTrajectoire(robot, last, vitesse, vitesseMax, modified);
	}
	
	/**
	 * Première trajectoire. On considère que la vitesse initiale du robot est nulle
	 * @param robot
	 * @param vitesse
	 * @param modified
	 */
	public final void getTrajectoire(RobotChrono robot, VitesseCourbure vitesse, Speed vitesseMax, ArcCourbeClotho modified)
	{
		getTrajectoire(robot, robot.getCinematique(), vitesse, vitesseMax, modified);
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
	public final void getTrajectoire(RobotChrono robot, Cinematique cinematiqueInitiale, VitesseCourbure vitesse, Speed vitesseMax, ArcCourbeClotho modified)
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
		
		// si la dérivée de la courbure est nulle, on est dans le cas particulier d'une trajectoire rectiligne ou circulaire
		if(vitesse.vitesse == 0)
		{
			if(courbure < 0.00001 && courbure > -0.00001)
				getTrajectoireLigneDroite(robot, cinematiqueInitiale.getPosition(), orientation, vitesseMax, modified, vitesse.rebrousse ^ cinematiqueInitiale.enMarcheAvant);
			else
				getTrajectoireCirculaire(robot, cinematiqueInitiale.getPosition(), orientation, courbure, vitesseMax, modified, vitesse.rebrousse ^ cinematiqueInitiale.enMarcheAvant);
			return;
		}
		
		double coeffMultiplicatif = 1. / vitesse.squaredRootVitesse;
		double sDepart = courbure / vitesse.squaredRootVitesse; // sDepart peut parfaitement être négatif
		if(!vitesse.positif)
			sDepart = -sDepart;
		int pointDepart = (int) Math.round(sDepart / PRECISION_TRACE) + INDICE_MAX - 1;
		
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

			trajectoire[pointDepart + vitesse.squaredRootVitesse * (i + 1)].minusNewVector(trajectoire[pointDepart])
			.scalar(coeffMultiplicatif)
			.Ysym(!vitesse.positif)
			.rotate(cos, sin)
			.plus(cinematiqueInitiale.getPosition())
			.copy(modified.arcselems[i].getPositionEcriture());

 			double orientationClotho = sDepart * sDepart;
 			if(!vitesse.positif)
 				orientationClotho = - orientationClotho;
 			
 			boolean marcheAvant = vitesse.rebrousse ^ cinematiqueInitiale.enMarcheAvant;
 			
			modified.arcselems[i].orientationGeometrique = baseOrientation + orientationClotho;
			modified.arcselems[i].courbureGeometrique = sDepart * vitesse.squaredRootVitesse;

 			if(marcheAvant)
 			{
				modified.arcselems[i].orientationReelle = modified.arcselems[i].orientationGeometrique;
				modified.arcselems[i].courbureReelle = modified.arcselems[i].courbureGeometrique;
 			}
 			else
 			{
				modified.arcselems[i].orientationReelle = modified.arcselems[i].orientationGeometrique + Math.PI;
				modified.arcselems[i].courbureReelle = - modified.arcselems[i].courbureGeometrique;
 			}
 			
			modified.rebrousse = vitesse.rebrousse;
			
			modified.arcselems[i].enMarcheAvant = marcheAvant;
			
			// TODO : vitesse max dépend de la courbure !
			modified.arcselems[i].vitesseMax = vitesseMax;
 			if(!vitesse.positif)
 				modified.arcselems[i].courbureGeometrique = - modified.arcselems[i].courbureGeometrique;
 			modified.arcselems[i].obstacle.update(modified.arcselems[i].getPosition(), modified.arcselems[i].orientationReelle, robot);
		}
	}

	private Vec2RW deltaTmp = new Vec2RW();
	private Vec2RW delta = new Vec2RW();
	private Vec2RW centreCercle = new Vec2RW();
	
	/**
	 * Calcule la trajectoire dans le cas particulier d'une trajectoire circulaire
	 * @param position
	 * @param orientation
	 * @param courbure
	 * @param modified
	 */
	private void getTrajectoireCirculaire(RobotChrono robot, Vec2RO position,
			double orientation, double courbure, Speed vitesseMax, ArcCourbeClotho modified, boolean enMarcheAvant)
	{		
		// rappel = la courbure est l'inverse du rayon de courbure
		// le facteur 1000 vient du fait que la courbure est en mètre^-1
		double rayonCourbure = 1000. / courbure;
		delta.setX(Math.cos(orientation + Math.PI / 2) * rayonCourbure);
		delta.setY(Math.sin(orientation + Math.PI / 2) * rayonCourbure);
		
		centreCercle.setX(position.getX() + delta.getX());
		centreCercle.setY(position.getY() + delta.getY());

		
//		double cos = Math.sqrt(rayonCourbure * rayonCourbure - d * d) / rayonCourbure;
//		double sin = Math.abs(d / rayonCourbure);
//		sin = 2 * sin * cos; // sin(a) devient sin(2a)
//		cos = 2 * cos * cos - 1; // cos(a) devient cos(2a)
		double cos = cosMap[(int)(courbure*10)+60];
		double sin = sinMap[(int)(courbure*10)+60];
		double angle = angleMap[(int)(courbure*10)+60];
		double cosSauv = cos;
		double sinSauv = sin;
//		double angle = Math.atan2(cos, sin);
		sin = 0;
		cos = 1;

		for(int i = 0; i < NB_POINTS; i++)
		{
			double tmp = sin;
			sin = sin * cosSauv + sinSauv * cos; // sin vaut sin(2a*(i+1))
			cos = cos * cosSauv - tmp * sinSauv;
			delta.copy(deltaTmp);
			deltaTmp.rotate(cos, sin);
			centreCercle.copy(modified.arcselems[i].getPositionEcriture());
			modified.arcselems[i].getPositionEcriture().minus(deltaTmp);
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

			// TODO : doit dépendre de la courbure !
 			modified.arcselems[i].vitesseMax = vitesseMax;
			modified.arcselems[i].enMarcheAvant = enMarcheAvant;
			modified.arcselems[i].obstacle.update(modified.arcselems[i].getPosition(), modified.arcselems[i].orientationReelle, robot);
		}
	}

	/**
	 * Calcule la trajectoire dans le cas particulier d'une ligne droite
	 * @param position
	 * @param orientation
	 * @param modified
	 */
	private void getTrajectoireLigneDroite(RobotChrono robot, Vec2RO position, double orientation, Speed vitesseMax, ArcCourbeClotho modified, boolean enMarcheAvant)
	{
		double cos = Math.cos(orientation);
		double sin = Math.sin(orientation);

		for(int i = 0; i < NB_POINTS; i++)
		{
			double distance = (i + 1) * PRECISION_TRACE * 1000;
			modified.arcselems[i].getPositionEcriture().setX(position.getX() + distance * cos);
			modified.arcselems[i].getPositionEcriture().setY(position.getY() + distance * sin);
			modified.arcselems[i].orientationGeometrique = orientation;
			modified.arcselems[i].courbureGeometrique = 0;
			modified.arcselems[i].courbureReelle = 0;
			
 			if(enMarcheAvant)
				modified.arcselems[i].orientationReelle = modified.arcselems[i].orientationGeometrique;
 			else
				modified.arcselems[i].orientationReelle = modified.arcselems[i].orientationGeometrique + Math.PI;
 			
 			// TODO : doit dépendre de la courbure !
 			modified.arcselems[i].vitesseMax = vitesseMax;
			modified.arcselems[i].enMarcheAvant = enMarcheAvant;
			modified.arcselems[i].obstacle.update(modified.arcselems[i].getPosition(), modified.arcselems[i].orientationReelle, robot);
		}
	}

	@Override
	public void useConfig(Config config)
	{
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);
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
 
}
