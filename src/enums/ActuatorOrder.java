package enums;

/**
 * Protocole des actionneurs
 * @author pf
 *
 */

// DEPENDS_ON_RULES
public enum ActuatorOrder {

	BAISSE_TAPIS_GAUCHE("ptg"),
	BAISSE_TAPIS_DROIT("ptd"),
	LEVE_TAPIS_GAUCHE("rtg"),
	LEVE_TAPIS_DROIT("rtd"),
	LEVE_CLAP_DROIT("cdh"),
	POSITION_TAPE_CLAP_DROIT("cdm"),
	BAISSE_CLAP_DROIT("cdb"),
	LEVE_CLAP_GAUCHE("cgh"),
	POSITION_TAPE_CLAP_GAUCHE("cgm"),
	BAISSE_CLAP_GAUCHE("cgb"),
	OUVRE_GUIDE_DROIT("ogd"),
	OUVRE_GUIDE_GAUCHE("ogg"),
	FERME_GUIDE_DROIT("fgd"),
	FERME_GUIDE_GAUCHE("fgg"),
	MILIEU_GUIDE_DROIT("gdi"),
	MILIEU_GUIDE_GAUCHE("ggi"),
	ASCENSEUR_HAUT("ah"),
	ASCENSEUR_BAS("ab"),
	ASCENSEUR_SOL("as"),
	ASCENSEUR_ESTRADE("ae"),
	OUVRE_MACHOIRE_DROITE("omd"),
	OUVRE_MACHOIRE_GAUCHE("omg"),
	FERME_MACHOIRE_DROITE("fmd"),
	FERME_MACHOIRE_GAUCHE("fmg"),
	OUVRE_BRAS_DROIT("obd"),
	OUVRE_BRAS_GAUCHE("obg"),
	FERME_BRAS_DROIT("fbd"),
	FERME_BRAS_GAUCHE("fbg"),
	OUVRE_BRAS_DROIT_LENT("obdl"),
	OUVRE_BRAS_GAUCHE_LENT("obgl"),
	FERME_BRAS_DROIT_LENT("fbdl"),
	FERME_BRAS_GAUCHE_LENT("fbgl");
	
	private String serialOrder;
	
	private ActuatorOrder(String serialOrder)
	{
		this.serialOrder = serialOrder;
	}
	
	public String getSerialOrder()
	{
		return serialOrder;
	}
	
}
