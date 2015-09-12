package pathfinding;

/**
 * Les différentes vitesses de courbure qu'on peut suivre
 * @author pf
 *
 */

public enum VitesseCourbure
{
	GAUCHE_VITE(-2, false),
	GAUCHE_LENTEMENT(-1, false),
	COURBURE_IDENTIQUE(0, false),
	DROITE_LENTEMENT(1, false),
	DROITE_VITE(2, false),
	REBROUSSE_AVANT(0, true),
	REBROUSSE_ARRIERE(0, true);
	
	public final int vitesse;
	public final boolean faisableALArret;
	
	public final static VitesseCourbure[] values;

	static
	{
		values = values();
	}
	
	private VitesseCourbure(int vitesse, boolean faisableALArret)
	{
		this.vitesse = vitesse;
		this.faisableALArret = faisableALArret;
	}
	
	private void calculeXY(double s)
	{
		double x = 0, y = s;
		long factorielle = 1;
		double b = Math.sqrt(2) * vitesse;
		double b2 = b * b;
		double s2 = s * s;
		b = b2;
		s *= s2;
		x = s / (3 * b);
		
		for(int i = 1; i < 50; i++)
		{
			factorielle *= 2*i;
			b *= b2;
			s *= s2;
			double tmp = s / (factorielle * (4*i+1) * b);
			if((i & 1) == 0)
				y += tmp;
			else
				y -= tmp;
			
			factorielle *= (2*i+1);
			b *= b2;
			s *= s2;
			tmp = s / (factorielle * (4*i+1) * b);
			if((i & 1) == 0)
				x += tmp;
			else
				x -= tmp;
		}
			
	}
	
}
