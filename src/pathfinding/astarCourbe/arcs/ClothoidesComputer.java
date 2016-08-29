package pathfinding.astarCourbe.arcs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import container.Service;
import pathfinding.VitesseCourbure;
import robot.Cinematique;
import robot.RobotChrono;
import robot.Speed;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

/**
 * Classe qui s'occupe de tous les calculs concernant les clothoïdes
 * @author pf
 *
 */

public class ClothoidesComputer implements Service
{
	protected Log log;
	
	private BigDecimal x, y; // utilisés dans le calcul de trajectoire
	private static final int S_MAX = 10; // une valeur très grande pour dire qu'on trace beaucoup de points.
	private static final double PRECISION_TRACE = 0.02; // précision du tracé, en m (distance entre deux points consécutifs). Plus le tracé est précis, plus on couvre de point une même distance
	private static final int INDICE_MAX = (int) (S_MAX / PRECISION_TRACE);
	public static final int NB_POINTS = 25; // nombre de points dans un arc
	public static final double DISTANCE_ARC_COURBE = PRECISION_TRACE * NB_POINTS * 1000; // en mm
	public static final double DISTANCE_ARC_COURBE_M = PRECISION_TRACE * NB_POINTS; // en m
	private static final double d = PRECISION_TRACE * 1000 / 2; // utilisé pour la trajectoire circulaire

	private double courbureMax;
	
	@SuppressWarnings("unchecked")
	private Vec2<ReadOnly>[] trajectoire = (Vec2<ReadOnly>[]) new Vec2[2 * INDICE_MAX - 1];
	
	public ClothoidesComputer(Log log)
	{
		this.log = log;
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
	private void calculeXY(BigDecimal s)
	{
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
			trajectoire[s] = new Vec2<ReadOnly>(x.doubleValue(), y.doubleValue());
			System.out.println((s - INDICE_MAX + 1) * PRECISION_TRACE+" "+trajectoire[s]);

			// Non, car la fenêtre n'est pas encore créée
//			if(Config.graphicObstacles)
//				Fenetre.getInstance().addObstacleEnBiais(new ObstacleRectangular(new Vec2<ReadOnly>((int)(x.doubleValue()/2), (int)(1000+y.doubleValue()/2)), 10, 10, 0));
		}
	}

	public final ArcCourbeCubique cubicInterpolation(RobotChrono r, Cinematique arrivee, Speed vitesseMax, VitesseCourbure v)
	{
		return cubicInterpolation(r.getCinematique(), arrivee, vitesseMax, v);
	}
	
	public final ArcCourbeCubique cubicInterpolation(Cinematique cinematiqueInitiale, Cinematique arrivee, Speed vitesseMax, VitesseCourbure v)
	{
//		log.debug(v);
		double alphas[] = {100, 300, 600, 800, 1000, 1200};
//		log.debug("Interpolation cubique, rebrousse : "+v.rebrousse);
		double sin, cos;
		double courbure = cinematiqueInitiale.courbure;
		if(v.rebrousse) // si on rebrousse, la courbure est nulle
		{
			sin = Math.sin(cinematiqueInitiale.orientation + Math.PI);
			cos = Math.cos(cinematiqueInitiale.orientation + Math.PI);
			courbure = 0;
		}
		else
		{
			sin = Math.sin(cinematiqueInitiale.orientation);
			cos = Math.cos(cinematiqueInitiale.orientation);			
		}
		
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
			double dx = cinematiqueInitiale.getPosition().x;
			double cx = cos*alpha;
			double ax = arrivee.getPosition().x - bx - cx - dx;
			
			// idem pour y
			double dy = cinematiqueInitiale.getPosition().y;
			double cy = sin*alpha;
			double ay = arrivee.getPosition().y - by - cy - dy;
	
			ArrayList<Cinematique> out = new ArrayList<Cinematique>();
			double t = 0, tnext = 0, lastCourbure = courbure;
			double longueur = 0;
			Cinematique last = null, actuel;
			boolean error = false;
			
			tnext += PRECISION_TRACE*1000/alpha;
			while(t < 1.)
			{
				t = tnext;
				
				tnext += PRECISION_TRACE*1000/(Math.hypot(3*ax*t*t+2*bx*t+cx, 3*ay*t*t+2*by*t+cy));
				if(tnext > 1)
					t = 1; // on veut finir à 1 exactement
	
				double x = ax*t*t*t + bx*t*t + cx*t + dx;
				double y = ay*t*t*t + by*t*t + cy*t + dy;
				double vx = 3*ax*t*t+2*bx*t+cx;
				double vy = 3*ay*t*t+2*by*t+cy;
				double acx = 6*ax*t+2*bx;
				double acy = 6*ay*t+2*by;
				
				actuel = new Cinematique(
						x, // x
						y, // y
						Math.atan2(vy, vx), // orientation = atan2(vy, vx)
						v.rebrousse ^ cinematiqueInitiale.enMarcheAvant,
						1000*(vx * acy - vy * acx) / Math.pow(vx*vx + vy*vy, 1.5), // formule de la courbure d'une courbe paramétrique
						vitesseMax.translationalSpeed, // vitesses calculées classiquement
						vitesseMax.rotationalSpeed,
						vitesseMax);
		
//				log.debug(t);
				
				// Il faut faire attention à ne pas dépasser la coubure maximale !
				// On prend de la marge
				if(x < -1500 || x > 1500 || y < 0 || y > 2000 || Math.abs(actuel.courbure) > courbureMax*0.7 || Math.abs(actuel.courbure - lastCourbure) > 3)
				{
//					log.debug("ERREUR");
					error = true;
					break;
				}
				
				
//				log.debug(x+" "+y);
				lastCourbure = actuel.courbure;
				
				// calcul de la longueur de l'arc
				if(last != null)
					longueur += actuel.getPosition().distance(last.getPosition());
	
				out.add(actuel);
				last = actuel;
			}
			if(error) // on essaye un autre alpha
				continue;
			
			return new ArcCourbeCubique(out, longueur, v.rebrousse, v.rebrousse);
		}
		return null;
	}
	
	public void getTrajectoire(ArcCourbe depart, VitesseCourbure vitesse, Speed vitesseMax, ArcCourbeClotho modified)
	{
		Cinematique last = depart.getLast();
		getTrajectoire(last, vitesse, vitesseMax, modified);
	}
	
	/**
	 * Première trajectoire. On considère que la vitesse initiale du robot est nulle
	 * @param robot
	 * @param vitesse
	 * @param modified
	 */
	public final void getTrajectoire(RobotChrono robot, VitesseCourbure vitesse, Speed vitesseMax, ArcCourbeClotho modified)
	{
		getTrajectoire(robot.getCinematique(), vitesse, vitesseMax, modified);
	}
	
	/**
	 * ATTENTION ! La courbure est en m^-1 et pas en mm^-1
	 * En effet, comme le rayon de courbure sera souvent plus petit que le mètre, on aura une courbure souvent plus grande que 1
	 * Le contenu est mis dans l'arccourbe directement
	 * @param position
	 * @param orientation
	 * @param courbure
	 * @param vitesse
	 * @param distance
	 * @return
	 */
	public final void getTrajectoire(Cinematique cinematiqueInitiale, VitesseCourbure vitesse, Speed vitesseMax, ArcCourbeClotho modified)
	{
//		modified.v = vitesse;
//		log.debug(vitesse);
		double courbure = cinematiqueInitiale.courbure;
		double orientation = cinematiqueInitiale.orientation;

		if(vitesse.rebrousse)
		{
			courbure = 0;
			orientation += Math.PI;
		}
		
		// si la dérivée de la courbure est nulle, on est dans le cas particulier d'une trajectoire rectiligne ou circulaire
		if(vitesse.vitesse == 0)
		{
			if(courbure < 0.00001 && courbure > -0.00001)
				getTrajectoireLigneDroite(cinematiqueInitiale.getPosition(), orientation, vitesseMax, modified, vitesse.rebrousse ^ cinematiqueInitiale.enMarcheAvant);
			else
				getTrajectoireCirculaire(cinematiqueInitiale.getPosition(), orientation, courbure, vitesseMax, modified, vitesse.rebrousse ^ cinematiqueInitiale.enMarcheAvant);
			return;
		}
		
		double coeffMultiplicatif = 1. / vitesse.squaredRootVitesse;
		double sDepart = courbure / vitesse.squaredRootVitesse; // sDepart peut parfaitement être négatif
		if(!vitesse.positif)
			sDepart = -sDepart;
		int pointDepart = (int) Math.round(sDepart / PRECISION_TRACE) + INDICE_MAX - 1;
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

 			Vec2.copy(
				Vec2.plus(
					Vec2.rotate(
						Vec2.Ysym(
							Vec2.scalar(
								trajectoire[pointDepart + vitesse.squaredRootVitesse * (i + 1)].minusNewVector(trajectoire[pointDepart]),
							coeffMultiplicatif),
						!vitesse.positif),
					cos, sin),
				cinematiqueInitiale.getPosition()).getReadOnly(),
			modified.arcselems[i].getPositionEcriture());

 			double orientationClotho = sDepart * sDepart;
 			if(!vitesse.positif)
 				orientationClotho = - orientationClotho;
			modified.arcselems[i].orientation = baseOrientation + orientationClotho;
			modified.arcselems[i].courbure = sDepart * vitesse.squaredRootVitesse;
			
			// TODO : doit dépendre de la courbure !
			modified.arcselems[i].vitesseRotation = vitesseMax.rotationalSpeed;
			modified.arcselems[i].vitesseTranslation = vitesseMax.translationalSpeed;
			modified.rebrousse = vitesse.rebrousse;
			
			modified.arcselems[i].enMarcheAvant = vitesse.rebrousse ^ cinematiqueInitiale.enMarcheAvant;
			modified.arcselems[i].vitesseMax = vitesseMax;
 			if(!vitesse.positif)
 				modified.arcselems[i].courbure = - modified.arcselems[i].courbure;
		}
	}

	private Vec2<ReadWrite> deltaTmp = new Vec2<ReadWrite>();
	private Vec2<ReadOnly> delta = new Vec2<ReadOnly>();
	private Vec2<ReadOnly> centreCercle = new Vec2<ReadOnly>();
	
	/**
	 * Calcule la trajectoire dans le cas particulier d'une trajectoire circulaire
	 * @param position
	 * @param orientation
	 * @param courbure
	 * @param modified
	 */
	private void getTrajectoireCirculaire(Vec2<ReadOnly> position,
			double orientation, double courbure, Speed vitesseMax, ArcCourbeClotho modified, boolean enMarcheAvant)
	{		
		// rappel = la courbure est l'inverse du rayon de courbure
		// le facteur 1000 vient du fait que la courbure est en mètre^-1
		double rayonCourbure = 1000. / courbure;
		delta.x = (int)(Math.cos(orientation + Math.PI / 2) * rayonCourbure);
		delta.y = (int) (Math.sin(orientation + Math.PI / 2) * rayonCourbure);
		
		centreCercle.x = position.x + delta.x;
		centreCercle.y = position.y + delta.y;

		
		double cos = Math.sqrt(rayonCourbure * rayonCourbure - d * d) / rayonCourbure;
		double sin = Math.abs(d / rayonCourbure);
		sin = 2 * sin * cos; // sin(a) devient sin(2a)
		cos = 2 * cos * cos - 1; // cos(a) devient cos(2a)
		double cosSauv = cos;
		double sinSauv = sin;
		double angle = Math.asin(sin);
		sin = 0;
		cos = 1;
		for(int i = 0; i < NB_POINTS; i++)
		{
			double tmp = sin;
			sin = sin * cosSauv + sinSauv * cos; // sin vaut sin(2a*(i+1))
			cos = cos * cosSauv - tmp * sinSauv;
			Vec2.copy(delta, deltaTmp);
			Vec2.rotate(deltaTmp, cos, sin);
			Vec2.copy(centreCercle, modified.arcselems[i].getPositionEcriture());
			Vec2.minus(modified.arcselems[i].getPositionEcriture(), deltaTmp);
			modified.arcselems[i].orientation = orientation + angle * (i + 1);
			modified.arcselems[i].courbure = courbure;
			// TODO : doit dépendre de la courbure !
			modified.arcselems[i].vitesseRotation = vitesseMax.rotationalSpeed;
			modified.arcselems[i].vitesseTranslation = vitesseMax.translationalSpeed;
			modified.arcselems[i].enMarcheAvant = enMarcheAvant;
		}
	}

	/**
	 * Calcule la trajectoire dans le cas particulier d'une ligne droite
	 * @param position
	 * @param orientation
	 * @param modified
	 */
	private void getTrajectoireLigneDroite(Vec2<ReadOnly> position, double orientation, Speed vitesseMax, ArcCourbeClotho modified, boolean enMarcheAvant)
	{
		double cos = Math.cos(orientation);
		double sin = Math.sin(orientation);

		for(int i = 0; i < NB_POINTS; i++)
		{
			double distance = (i + 1) * PRECISION_TRACE * 1000;
			modified.arcselems[i].getPositionEcriture().x = (int) Math.round(position.x + distance * cos);
			modified.arcselems[i].getPositionEcriture().y = (int) Math.round(position.y + distance * sin);
			modified.arcselems[i].orientation = orientation;
			modified.arcselems[i].courbure = 0;
			modified.arcselems[i].vitesseRotation = vitesseMax.rotationalSpeed;
			modified.arcselems[i].vitesseTranslation = vitesseMax.translationalSpeed;
			modified.arcselems[i].enMarcheAvant = enMarcheAvant;
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

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
    	log.debug("Sauvegarde des points de la clothoïde");
        try {
            java.io.File fichier_creation;
            FileOutputStream fichier;
            ObjectOutputStream oos;

            fichier_creation = new java.io.File("clotho.dat");
            fichier_creation.createNewFile();
            fichier = new FileOutputStream("clotho.dat");
            oos = new ObjectOutputStream(fichier);
            oos.writeObject(trajectoire);
            oos.flush();
            oos.close();
        	log.debug("Sauvegarde terminée");
        }
        catch(Exception e)
        {
            log.critical("Erreur lors de la sauvegarde des points de la clothoïde !");
            e.printStackTrace();
        }
    }
	
    /**
     * Chargement des points de la clothoïde unitaire
     * @return
     */
	@SuppressWarnings("unchecked")
	private boolean chargePoints()
    {
    	log.debug("Chargement des points de la clothoïde");
        try {
            FileInputStream fichier = new FileInputStream("clotho.dat");
            ObjectInputStream ois = new ObjectInputStream(fichier);
            trajectoire = (Vec2<ReadOnly>[]) ois.readObject();
            ois.close();
            return true;
        }
        catch(Exception e)
        {
        	log.critical("Chargement échoué !");
        }
        return false;
    }
 
}
