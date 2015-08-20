package robot;

/**
 * Protocole des actionneurs
 * La symétrie est gérée par la STM
 * @author pf
 *
 */

// DEPENDS_ON_RULES
public enum ActuatorOrder {
	BAISSE_TAPIS_GAUCHE("ptg", true, "ssc32 abwa"),
	BAISSE_TAPIS_DROIT("ptd", true, "ssc32 obwo"),
	LEVE_TAPIS_GAUCHE("rtg", false, "ssc32 ABWA");

	private String serialOrder;
	private String ordreSSC32;
	private boolean symetrie;
	
	private ActuatorOrder(String serialOrder, boolean symetrie, String ordreSSC32)
	{
		this.serialOrder = serialOrder;
		this.ordreSSC32 = ordreSSC32;
		this.symetrie = symetrie;
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
	public String getOrdreSSC32()
	{
		return ordreSSC32;
	}
	
	public boolean hasSymmetry()
	{
		return symetrie;
	}
	
}
