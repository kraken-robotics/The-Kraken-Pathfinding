package robot;

/**
 * Protocole des actionneurs
 * La symétrie est gérée par la STM
 * @author pf
 *
 */

// DEPENDS_ON_RULES
public enum ActuatorOrder {
	BAISSE_TAPIS_GAUCHE("ptg"),
	BAISSE_TAPIS_DROIT("ptd"),
	LEVE_TAPIS_GAUCHE("rtg");

	private String serialOrder;
	
	private ActuatorOrder(String serialOrder)
	{
		this.serialOrder = serialOrder;
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
/*	public ActuatorOrder getSymmetry()
	{
		if(!symetrieExiste)
			return this;
		return ActuatorOrder.values()[ordinal()^1];
	}*/
	
/*	public boolean hasSymmetry()
	{
		return symetrieExiste;
	}*/
	
}
