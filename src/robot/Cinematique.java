package robot;

import permissions.ReadWrite;
import utils.Vec2;

public class Cinematique
{
	public final Vec2<ReadWrite> position = new Vec2<ReadWrite>();
	public double orientation;
	public boolean enMarcheAvant;
	public double courbure;
	public Speed vitesse;
	
	/**
	 * Renvoie vrai si this est proche de autre
	 * @param autre
	 * @return
	 */
	public boolean estProche(Cinematique autre)
	{
		return true;
	}

	/**
	 * Renvoie vrai si this est proche de autre. Plus laxiste que "estProche"
	 * @param autre
	 * @return
	 */
	public boolean estProcheUrgence(Cinematique autre)
	{
		return true;
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
    	autre.vitesse = vitesse;
	}
}
