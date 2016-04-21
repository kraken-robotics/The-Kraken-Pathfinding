package robot;

/**
 * Une structure qui regroupe des infos de cinématique
 * @author pf
 *
 */

public class Cinematique extends CinematiqueSansVitesse
{
	public volatile double vitesseTranslation;
	public volatile double vitesseRotation;
	
	public Cinematique(CinematiqueSansVitesse c, double vitesseTranslation, double vitesseRotation)
	{
		super(c);
		this.vitesseRotation = vitesseRotation;
		this.vitesseTranslation = vitesseTranslation;
	}
	
	public Cinematique(int x, int y, double orientation, boolean enMarcheAvant, double courbure, double vitesseTranslation, double vitesseRotation)
	{
		super(x, y, orientation, enMarcheAvant, courbure);
		this.vitesseTranslation = vitesseTranslation;
		this.vitesseRotation = vitesseRotation;
	}
	/**
	 * Copie this dans autre
	 * @param autre
	 */
	public void copy(Cinematique autre)
	{
		super.copy(autre);
    	autre.vitesseRotation = vitesseRotation;
    	autre.vitesseTranslation = vitesseTranslation;
	}

	public void setVitesse(Speed speed)
	{
		vitesseRotation = speed.rotationalSpeed;
		vitesseTranslation = speed.translationalSpeed;
	}
	
}
