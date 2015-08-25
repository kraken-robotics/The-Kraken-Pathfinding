package robot;

/**
 * Protocole des actionneurs
 * La symétrie est gérée par la STM
 * @author pf
 *
 */

// DEPENDS_ON_RULES
public enum ActuatorOrder {
	BAISSE_TAPIS_GAUCHE(true, "ssc32 abwa"),
	BAISSE_TAPIS_DROIT(true, "ssc32 obwo"),
	LEVE_TAPIS_GAUCHE(false, "ssc32 ABWA");

	private String ordreSSC32;
	private boolean symetrie;
	
	private ActuatorOrder(boolean symetrie, String ordreSSC32)
	{
		this.ordreSSC32 = ordreSSC32;
		this.symetrie = symetrie;
	}
	
	/**
	 * On retourne l'ordre symétrique s'il existe
	 * @return
	 */
	public String getOrdreSSC32()
	{
		return ordreSSC32;
	}
	
	public boolean hasSymmetry()
	{
		return symetrie;
	}
	
}
