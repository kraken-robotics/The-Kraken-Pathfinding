package robot;

/**
 * Protocole des actionneurs
 * La symétrie est gérée par la STM
 * @author pf
 *
 */

// DEPENDS_ON_RULES
public enum ActuatorOrder {
	TEST(true, 0, 100);

	public final boolean symetrie;
	public final int id;
	public final int angle;
	
	private ActuatorOrder(boolean symetrie, int id, int angle)
	{
		this.id = id;
		this.angle = angle;
		this.symetrie = symetrie;
	}
	
}
