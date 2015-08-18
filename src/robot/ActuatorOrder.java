package robot;

/**
 * Protocole des actionneurs
 * @author pf
 *
 */

// DEPENDS_ON_RULES
public enum ActuatorOrder {

	/**
	 * Il est important de mettre D'ABORD les ordres avec symétrie puis les
	 * ordres sans symétrie, et que les ordres avec symétrie soient suivis (ou précédés)
	 * de leur symétrique.
	 * D'une manière générale, un ordre en position n a son ordre symétrique en n+1 si n est pair, et en
	 * n-1 si n est impair (il faut inverser le bit de poids faible)
	 */
	
	// Ordres avec symétrie
	
	BAISSE_TAPIS_GAUCHE("ptg", 150, true),
	BAISSE_TAPIS_DROIT("ptd", 150, true),
	LEVE_TAPIS_GAUCHE("rtg", 150, true),
	LEVE_TAPIS_DROIT("rtd", 150, true),
	LEVE_CLAP_DROIT("cdh", 0, true),
	LEVE_CLAP_GAUCHE("cgh", 0, true),
	POSITIONNE_TAPE_CLAP_DROIT("cdm", 0, true),
	POSITIONNE_TAPE_CLAP_GAUCHE("cgm", 0, true),
	BAISSE_CLAP_DROIT("cdb", 150, true),
	BAISSE_CLAP_GAUCHE("cgb", 150, true),
	OUVRE_GUIDE_DROIT("ogd", 0, true),
	OUVRE_GUIDE_GAUCHE("ogg", 0, true),
	FERME_GUIDE_DROIT("fgd", 0, true),
	FERME_GUIDE_GAUCHE("fgg", 0, true),
	MILIEU_GUIDE_DROIT("gdi", 0, true),
	MILIEU_GUIDE_GAUCHE("ggi", 0, true),
	OUVRE_MACHOIRE_DROITE("omd", 0, true),
	OUVRE_MACHOIRE_GAUCHE("omg", 0, true),
	FERME_MACHOIRE_DROITE("fmd", 0, true),
	FERME_MACHOIRE_GAUCHE("fmg", 0, true),
	OUVRE_BRAS_DROIT("obd", 0, true),
	OUVRE_BRAS_GAUCHE("obg", 0, true),
	FERME_BRAS_DROIT("fbd", 0, true),
	FERME_BRAS_GAUCHE("fbg", 0, true),
	OUVRE_BRAS_DROIT_LENT("obdl", 0, true),
	OUVRE_BRAS_GAUCHE_LENT("obgl", 0, true),
	FERME_BRAS_DROIT_LENT("fbdl", 0, true),
	FERME_BRAS_GAUCHE_LENT("fbgl", 0, true),

	// Ordres sans symétrie
	
	ASCENSEUR_HAUT("ah", 0, false),
	ASCENSEUR_BAS("ab", 0, false),
	ASCENSEUR_SOL("as", 0, false),
	ASCENSEUR_ESTRADE("ae", 0, false);

	private String serialOrder;
	private int sleepValue;
	private boolean symetrieExiste;
	
	private ActuatorOrder(String serialOrder, int sleepValue, boolean symetrieExiste)
	{
		this.serialOrder = serialOrder;
		this.sleepValue = sleepValue;
		this.symetrieExiste = symetrieExiste;
	}
	
	/**
	 * Renvoie l'ordre à transmettre à la série
	 * @return
	 */
	public String getSerialOrder()
	{
		return serialOrder;
	}
	
	/**
	 * On retourne l'ordre symétrique s'il existe
	 * @return
	 */
	public ActuatorOrder getSymmetry()
	{
		if(!symetrieExiste)
			return this;
		return ActuatorOrder.values()[ordinal()^1];
	}
	
	public int getSleepValue()
	{
		return sleepValue;
	}
	
	public boolean hasSymmetry()
	{
		return symetrieExiste;
	}
	
}
