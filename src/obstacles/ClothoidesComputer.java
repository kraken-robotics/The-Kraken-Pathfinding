package obstacles;

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
	
	private Vec2<ReadOnly>[] trajectoire;
	private BigDecimal x, y; // utilisés dans le calcul de trajectoire

	public ClothoidesComputer(Log log)
	{
		this.log = log;
		init();
	}
	
	// Calcul grâce au développement limité d'Euler
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

	}
	
	// Calcule, une fois pour toutes, les clothoïdes
	private void init()
	{
		double pas = 0.5;
		int sMax = 1000;
		double somme = 0;
		/*
		for(int s = 1; s < (int) (sMax/pas); s++)
		{
			System.out.println(s+" "+somme);
			calculeXY(new BigDecimal(somme).setScale(15, RoundingMode.HALF_EVEN));
			// TODO : calculer trajectoire
			new ObstacleRectangular(new Vec2<ReadOnly>((int)(500*x.doubleValue()-500), (int)(1000+500*y.doubleValue())), 10, 10, 0);
			somme += Math.min(pas*1./(somme+1), pas);
		}/*
		for(int vitesse = 1 ; vitesse <= 2 ; vitesse++)
		{
			pas = pas * Math.abs(vitesse);
			for(int s = (int) (-sMax/pas); s < (int) (sMax/pas); s++)
			{
				calculeXY(new BigDecimal(s*pas).setScale(15, RoundingMode.HALF_EVEN));
				new ObstacleRectangular(new Vec2<ReadOnly>((int)(500*x.doubleValue()/vitesse+500), (int)(1000+500*y.doubleValue()/vitesse)), 10, 10, 0);
			}
		}*/
	}

	public ArrayList<Vec2<ReadOnly>> getTrajectoire(Vec2<ReadOnly> position, double orientation, double courbure, VitesseCourbure vitesse)
	{
		return null;
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
