package robot;

import permissions.ReadOnly;
import permissions.ReadWrite;
import utils.Vec2;

/**
 * Une structure qui regroupe des infos de cinématique
 * @author pf
 *
 */

public class Cinematique
{
	private final Vec2<ReadWrite> position = new Vec2<ReadWrite>();
	public volatile double orientation;
	public volatile boolean enMarcheAvant;
	public volatile double courbure;
	public volatile double vitesseTranslation;
	public volatile double vitesseRotation;
	
	public final Vec2<ReadOnly> getPosition()
	{
		return position.getReadOnly();
	}

	public final Vec2<ReadWrite> getPositionEcriture()
	{
		return position;
	}

	/**
	 * Renvoie vrai si this est proche de autre
	 * @param autre
	 * @return
	 */
	public boolean estProche(Cinematique autre)
	{
		System.out.println("Distance : "+position.squaredDistance(autre.position));
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
	}

	public void setVitesse(Speed speed)
	{
		vitesseRotation = speed.rotationalSpeed;
		vitesseTranslation = speed.translationalSpeed;
	}
	
	@Override
	public String toString()
	{
		return position+", "+orientation;
	}
}
