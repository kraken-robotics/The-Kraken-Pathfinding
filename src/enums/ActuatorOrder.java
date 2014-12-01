package enums;

/**
 * Protocole des actionneurs
 * @author pf
 *
 */

public enum ActuatorOrder {

	// Syntaxe: NOM_METHODE("protocole_serie")

	BOUGER_BRAS("bb"),
	DEPOSER_TAPIS("dt");	// ce sont des exemples, vous pouvez les virer

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
