package debug;


/**
 * Info de debug de l'asser
 * @author pf
 *
 */

public class IncomingDataDebug
{
	public int PWMgauche;
	public int PWMdroit;
	public int vitesseGauche;
	public int vitesseDroite;
	public int distance;
	public int orientation;
	public int vitesseLineaire;
	public int courbure;

	public IncomingDataDebug(int PWMgauche, int PWMdroit, int vitesseGauche, int vitesseDroite, int distance,
			int orientation, int vitesseLineaire, int courbure) {
		this.PWMgauche = PWMgauche;
		this.PWMdroit = PWMdroit;
		this.vitesseGauche = vitesseGauche;
		this.vitesseDroite = vitesseDroite;
		this.distance = distance;
		this.orientation = orientation;
		this.vitesseLineaire = vitesseLineaire;
		this.courbure = courbure;
	}
	
}
