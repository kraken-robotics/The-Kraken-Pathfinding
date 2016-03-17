package pathfinding.astarCourbe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

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
	private static final int S_MAX = 100; // une valeur très grande pour dire qu'on trace beaucoup de points.
	private static final double PRECISION_TRACE = 0.001; // précision du tracé. Plus le tracé est précis, plus on couvre de point une même distance
	private static final int DISTANCE_ARC_COURBE = 50;
	
	@SuppressWarnings("unchecked")
	private Vec2<ReadOnly>[] trajectoire = (Vec2<ReadOnly>[]) new Vec2[(int) (2 * S_MAX / PRECISION_TRACE - 1)];
	
	public ClothoidesComputer(Log log)
	{
		this.log = log;
		init();
	}
	
	// Calcul grâce au développement limité d'Euler
	// Génère la courbe pour laquelle s = courbure
	private void calculeXY(BigDecimal s)
	{
		y = s;
		BigDecimal factorielle = new BigDecimal(1).setScale(15, RoundingMode.HALF_EVEN);
		BigDecimal b = new BigDecimal(Math.sqrt(2)).setScale(15, RoundingMode.HALF_EVEN);
		BigDecimal b2 = b.multiply(b);
		BigDecimal s2 = s.multiply(s);
		b = b2;
		s = s.multiply(s2);
		x = s.divide(b.multiply(new BigDecimal(3).setScale(15, RoundingMode.HALF_EVEN)), RoundingMode.HALF_EVEN);		
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
				y = y.add(tmp);
			else
				y = y.subtract(tmp);
			
			factorielle = factorielle.multiply(new BigDecimal(2*i+1).setScale(15, RoundingMode.HALF_EVEN));
			
			b = b.multiply(b2);
			s = s.multiply(s2);
			tmp = s.divide(factorielle.multiply(b).multiply(new BigDecimal(4*i+3).setScale(15, RoundingMode.HALF_EVEN)), RoundingMode.HALF_EVEN);

			if((i & 1) == 0)
				x = x.add(tmp);
			else
				x = x.subtract(tmp);

			i++;
		} while(tmp.abs().compareTo(seuil) > 0);
		// On fait en sorte que tourner à gauche ait une courbure positive
		x = x.multiply(new BigDecimal(-1000)); // On considère que x et y sont en millimètre et que la courbure est en mètre^-1
		y = y.multiply(new BigDecimal(1000));
	}
	
	// Calcule, une fois pour toutes, les clothoïdes
	private void init()
	{
		for(int s = 0; s < 2*(int) (S_MAX / PRECISION_TRACE)-1; s++)
		{
			calculeXY(new BigDecimal((s-(int) (S_MAX / PRECISION_TRACE)+1)*PRECISION_TRACE).setScale(15, RoundingMode.HALF_EVEN));
			trajectoire[s] = new Vec2<ReadOnly>((int)Math.round(x.doubleValue()), (int)Math.round(y.doubleValue()));
			System.out.println(s*PRECISION_TRACE+" "+trajectoire[s]);
			
			if(Config.graphicObstacles)
				Fenetre.getInstance().addObstacleEnBiais(new ObstacleRectangular(new Vec2<ReadOnly>((int)(x.doubleValue()/2-500), (int)(1000+y.doubleValue()/2)), 10, 10, 0));
		}
	}

	/**
	 * ATTENTION ! La courbure est en m^-1 et pas en mm^-1
	 * En effet, comme le rayon de courbure sera souvent plus petit que le mètre, on aura une courbure souvent plus grande que 1
	 * Distance est en mm
	 * Le contenu est mis dans l'arccourbe directement
	 * @param position
	 * @param orientation
	 * @param courbure
	 * @param vitesse
	 * @param distance
	 * @return
	 */
	public void getTrajectoire(Vec2<ReadOnly> position, double orientation, double courbure, VitesseCourbure vitesse, int distance, ArcCourbe modified)
	{
		// rappel : s est en mètre
		int nbPoints = (int) Math.round(distance / 1000.); // TODO pas si simple
		double coeffMultiplicatif = 1./Math.sqrt(vitesse.vitesse);
		double sDepart = courbure / Math.sqrt(vitesse.vitesse);
		int pointDepart = (int) Math.round(sDepart / PRECISION_TRACE);
		double orientationClotho = sDepart * sDepart / 2;
		
		// Le premier point est un cas particulier
		Vec2.copy(position, modified.arcselems[0].pointDepart);
		modified.arcselems[0].thetaDepart = orientation;
		
		double cos = Math.cos(orientation - orientationClotho);
		double sin = Math.sin(orientation - orientationClotho);
		for(int i = 1; i < nbPoints; i++)
		{
			Vec2.copy(
				Vec2.plus(
					Vec2.rotate(
							Vec2.scalar(
								trajectoire[pointDepart + i].minusNewVector(trajectoire[pointDepart]),
							coeffMultiplicatif),
					cos, sin),
				position).getReadOnly(),
			modified.arcselems[i].pointDepart);
			modified.arcselems[i].thetaDepart = orientation + orientationClotho;
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
