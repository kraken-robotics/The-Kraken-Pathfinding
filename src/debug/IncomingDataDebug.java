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
		if(PWMgauche > 30000)
			PWMgauche -= 65536;
		if(PWMdroit > 30000)
			PWMdroit -= 65536;
		if(distance > 30000)
			distance -= 65536;
		if(orientation > 30000)
			orientation -= 65536;
		if(vitesseGauche > 30000)
			vitesseGauche -= 65536;
		if(vitesseDroite > 30000)
			vitesseDroite -= 65536;
		this.PWMgauche = PWMgauche;
		this.PWMdroit = PWMdroit;
		this.vitesseGauche = vitesseGauche;
		this.vitesseDroite = vitesseDroite;
		this.distance = distance;
		this.orientation = orientation;
		this.vitesseLineaire = vitesseLineaire;
		this.courbure = courbure;
	}
	
	@Override
	public String toString()
	{
		return "PWMgauche : "+PWMgauche+
				", PWMdroit : "+PWMdroit+
				", vitesseGauche : "+vitesseGauche+
				", vitesseDroite : "+vitesseDroite+
				", distance : "+distance+
				", orientation : "+orientation+
				", vitesseLineaire :"+ vitesseLineaire+
				", courbure : "+courbure;
	}
}
