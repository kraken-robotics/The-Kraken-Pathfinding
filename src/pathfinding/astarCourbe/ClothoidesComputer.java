package pathfinding.astarCourbe;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

import container.Service;
import obstacles.types.ObstacleRectangular;
import pathfinding.VitesseCourbure;
import permissions.ReadOnly;
import tests.graphicLib.Fenetre;
import utils.Config;
import utils.Log;
import utils.Vec2;

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
	private static final double PRECISION_TRACE = 0.02; // précision du tracé. Plus le tracé est précis, plus on couvre de point une même distance
	private static final int INDICE_MAX = (int) (S_MAX / PRECISION_TRACE);
	public static final int NB_POINTS = 10;

	//	public static final int NB_POINTS = 10; // nombre de points dans un arc
	public static final double DISTANCE_ARC_COURBE = PRECISION_TRACE * NB_POINTS; // en mm
	
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
	
	// Calcul grâce au développement limité d'Euler
	// Génère la courbe pour laquelle s = courbure
	private void calculeXY(BigDecimal s)
	{
		x = s;
		BigDecimal factorielle = new BigDecimal(1).setScale(15, RoundingMode.HALF_EVEN);
		BigDecimal b = new BigDecimal(Math.sqrt(2)).setScale(15, RoundingMode.HALF_EVEN);
		BigDecimal b2 = b.multiply(b);
		BigDecimal s2 = s.multiply(s);
		b = b2;
		s = s.multiply(s2);
		y = s.divide(b.multiply(new BigDecimal(3).setScale(15, RoundingMode.HALF_EVEN)), RoundingMode.HALF_EVEN);		
		BigDecimal seuil = new BigDecimal(0.0001).setScale(15, RoundingMode.HALF_EVEN);
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
	
	// Calcule, une fois pour toutes, les clothoïdes
	private void init()
	{
		for(int s = 0; s < 2 * INDICE_MAX - 1; s++)
		{
			calculeXY(new BigDecimal((s - INDICE_MAX + 1) * PRECISION_TRACE).setScale(15, RoundingMode.HALF_EVEN));
			trajectoire[s] = new Vec2<ReadOnly>((int)Math.round(x.doubleValue()), (int)Math.round(y.doubleValue()));
			System.out.println((s - INDICE_MAX + 1) * PRECISION_TRACE+" "+trajectoire[s]);
			
			if(Config.graphicObstacles)
				Fenetre.getInstance().addObstacleEnBiais(new ObstacleRectangular(new Vec2<ReadOnly>((int)(x.doubleValue()/2), (int)(1000+y.doubleValue()/2)), 10, 10, 0));
		}
	}

	public void getTrajectoire(ArcCourbe depart, VitesseCourbure vitesse, ArcCourbe modified)
	{
		ArcElem last = depart.arcselems[NB_POINTS - 1];
		getTrajectoire(last.point.getReadOnly(), depart.marcheAvant, last.theta, last.courbure, vitesse, modified);
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
	public void getTrajectoire(Vec2<ReadOnly> position, boolean marcheAvant, double orientation, double courbure, VitesseCourbure vitesse, ArcCourbe modified)
	{
		if(vitesse.rebrousse)
		{
			courbure = 0;
			orientation += Math.PI;
			modified.marcheAvant = !marcheAvant;
		}
		else
			modified.marcheAvant = marcheAvant;
		
		modified.vitesseCourbure = vitesse;
		int nbPoints = NB_POINTS;
		double coeffMultiplicatif = 1./vitesse.squaredRootVitesse;
		double sDepart = courbure / vitesse.squaredRootVitesse; // sDepart peut parfaitement être négatif
		if(!vitesse.positif)
			sDepart = -sDepart;
		int pointDepart = (int) Math.round(sDepart / PRECISION_TRACE) + INDICE_MAX - 1;
		double orientationClothoDepart = sDepart * sDepart / 2; // orientation au départ
		if(!vitesse.positif)
			orientationClothoDepart = - orientationClothoDepart;
			
		log.debug("coeffMultiplicatif : "+coeffMultiplicatif);
		
		double cos = Math.cos(orientation - orientationClothoDepart);
		double sin = Math.sin(orientation - orientationClothoDepart);

		for(int i = 0; i < nbPoints; i++)
			log.debug("Clotho : "+trajectoire[vitesse.squaredRootVitesse * (i + 1)]);
	
		// le premier point n'est pas position, mais le suivant
		// (afin de ne pas avoir de doublon quand on enchaîne les arcs, entre le dernier point de l'arc t et le premier de l'arc t+1)		
		for(int i = 0; i < nbPoints; i++)
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
				position).getReadOnly(),
			modified.arcselems[i].point);

 			double orientationClotho = sDepart * sDepart / 2;
 			if(!vitesse.positif)
 				orientationClotho = - orientationClotho;
			modified.arcselems[i].theta = orientation + orientationClotho - orientationClothoDepart;
			modified.arcselems[i].courbure = sDepart * vitesse.squaredRootVitesse;
 			if(!vitesse.positif)
 				modified.arcselems[i].courbure = - modified.arcselems[i].courbure;
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

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
            e.printStackTrace();
        }
        return false;
    }
 
}
