package robot.cardsWrappers.enums;

/**
 * Protocole des actionneurs
 * @author pf
 *
 */

// DEPENDS_ON_RULES
public enum ActuatorOrder {

	BAISSE_TAPIS_GAUCHE("ptg", 150),
	BAISSE_TAPIS_DROIT("ptd", 150),
	LEVE_TAPIS_GAUCHE("rtg", 150),
	LEVE_TAPIS_DROIT("rtd", 150),
	LEVE_CLAP_DROIT("cdh", 0),
	POSITIONNE_TAPE_CLAP_DROIT("cdm", 0),
	BAISSE_CLAP_DROIT("cdb", 150),
	LEVE_CLAP_GAUCHE("cgh", 0),
	POSITIONNE_TAPE_CLAP_GAUCHE("cgm", 0),
	BAISSE_CLAP_GAUCHE("cgb", 150),
	OUVRE_GUIDE_DROIT("ogd", 0),
	OUVRE_GUIDE_GAUCHE("ogg", 0),
	FERME_GUIDE_DROIT("fgd", 0),
	FERME_GUIDE_GAUCHE("fgg", 0),
	MILIEU_GUIDE_DROIT("gdi", 0),
	MILIEU_GUIDE_GAUCHE("ggi", 0),
	ASCENSEUR_HAUT("ah", 0),
	ASCENSEUR_BAS("ab", 0),
	ASCENSEUR_SOL("as", 0),
	ASCENSEUR_ESTRADE("ae", 0),
	OUVRE_MACHOIRE_DROITE("omd", 0),
	OUVRE_MACHOIRE_GAUCHE("omg", 0),
	FERME_MACHOIRE_DROITE("fmd", 0),
	FERME_MACHOIRE_GAUCHE("fmg", 0),
	OUVRE_BRAS_DROIT("obd", 0),
	OUVRE_BRAS_GAUCHE("obg", 0),
	FERME_BRAS_DROIT("fbd", 0),
	FERME_BRAS_GAUCHE("fbg", 0),
	OUVRE_BRAS_DROIT_LENT("obdl", 0),
	OUVRE_BRAS_GAUCHE_LENT("obgl", 0),
	FERME_BRAS_DROIT_LENT("fbdl", 0),
	FERME_BRAS_GAUCHE_LENT("fbgl", 0);
	
	private String serialOrder;
	private int sleepValue;
	
	private ActuatorOrder(String serialOrder, int sleepValue)
	{
		this.serialOrder = serialOrder;
		this.sleepValue = sleepValue;
	}
	
	public String getSerialOrder()
	{
		return serialOrder;
	}
	
	public int getSleepValue()
	{
		return sleepValue;
	}
	
}
