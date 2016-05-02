package robot;

import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

/**
 * Une structure qui regroupe des infos de cinématique
 * @author pf
 *
 */

public class Cinematique
{
	protected final Vec2<ReadWrite> position = new Vec2<ReadWrite>();
	public volatile double orientation;
	public volatile boolean enMarcheAvant;
	public volatile double courbure;
	public volatile double vitesseTranslation;
	public volatile Speed vitesseMax;
	public volatile double vitesseRotation;

	public Cinematique(double x, double y, double orientation, boolean enMarcheAvant, double courbure, double vitesseTranslation, double vitesseRotation, Speed vitesseMax)
	{
		position.x = x;
		position.y = y;
		this.orientation = orientation;
		this.enMarcheAvant = enMarcheAvant;
		this.courbure = courbure;
		this.vitesseTranslation = vitesseTranslation;
		this.vitesseRotation = vitesseRotation;
		this.vitesseMax = vitesseMax;
	}
	
	/**
	 * Constructeur par copie
	 * @param cinematique
	 */
	public Cinematique(Cinematique cinematique)
	{
		position.x = cinematique.position.x;
		position.y = cinematique.position.y;
		orientation = cinematique.orientation;
		enMarcheAvant = cinematique.enMarcheAvant;
		courbure = cinematique.courbure;
		vitesseTranslation = cinematique.vitesseTranslation;
		vitesseRotation = cinematique.vitesseRotation;
		vitesseMax = cinematique.vitesseMax;
	}

	/**
	 * Copie this dans autre
	 * @param autre
	 */
	public void copy(Cinematique autre)
	{
    	Vec2.copy(position.getReadOnly(), autre.position);
    	autre.orientation = orientation;
    	autre.enMarcheAvant = enMarcheAvant;
    	autre.courbure = courbure;
    	autre.vitesseRotation = vitesseRotation;
    	autre.vitesseTranslation = vitesseTranslation;
    	autre.vitesseMax = vitesseMax;
	}

	public void setVitesse(Speed speed)
	{
		vitesseRotation = speed.rotationalSpeed;
		vitesseTranslation = speed.translationalSpeed;
	}
	
	/**
	 * Renvoie vrai si this est proche de autre
	 * @param autre
	 * @return
	 */
	public boolean estProche(Cinematique autre)
	{
		return position.squaredDistance(autre.position) < 50*50; // TODO écrire
	}

	/**
	 * Renvoie vrai si this est proche de autre. Plus laxiste que "estProche"
	 * @param autre
	 * @return
	 */
	public boolean estProcheUrgence(Cinematique autre)
	{
		return position.squaredDistance(autre.position) < 50*50; // TODO écrire
	}

	public final Vec2<ReadOnly> getPosition()
	{
		return position.getReadOnly();
	}

	public final Vec2<ReadWrite> getPositionEcriture()
	{
		return position;
	}
	
	@Override
	public String toString()
	{
		return position+", "+orientation+", "+(enMarcheAvant ? "marche avant" : "marche arrière")+", vitesse : "+vitesseTranslation+" courbure : "+courbure;
	}
	
	@Override
	public int hashCode()
	{
		int codeCourbure, codeOrientation;
//		if(courbure < -5)
//			codeCourbure = 0;
//		else
		if(courbure < -2)
			codeCourbure = 1;
		else if(courbure < 0)
			codeCourbure = 2;
		else if(courbure < 2)
			codeCourbure = 3;
//		else if(courbure < 5)
			codeCourbure = 4;
//		else
//			codeCourbure = 5;
//		System.out.println("codeCourbure : "+codeCourbure+", "+courbure);
		orientation = orientation % (2*Math.PI);
		if(orientation < 0)
			orientation += 2*Math.PI;
		
		codeOrientation = (int)(orientation / (Math.PI / 6));
//		System.out.println("codeOrientation : "+codeOrientation+" "+orientation);
		
		return (((((int)position.x + 1500) / 15) * 150 + (int)position.y / 15) * 6 + codeCourbure) * 16 + codeOrientation;
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o.hashCode() == hashCode();
	}
	
}
