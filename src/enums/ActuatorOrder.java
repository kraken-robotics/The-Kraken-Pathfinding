package enums;

/**
 * Protocole des actionneurs
 * @author pf
 *
 */

// DEPENDS_ON_RULES
public enum ActuatorOrder {

	BOUGER_BRAS("bb"),	// TODO
	DEPOSER_TAPIS("dt");

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
