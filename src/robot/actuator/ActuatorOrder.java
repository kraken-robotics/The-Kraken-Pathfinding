package robot.actuator;

/**
 * Protocole des actionneurs
 * @author pf
 *
 */

public enum ActuatorOrder {
	TEST(false, AX12.AX12_TEST, AngleAX12.TRUC.angle);
	
	public final boolean symetrie;
	public final int id;
	public final int angle;
	
	private ActuatorOrder(boolean symetrie, AX12 ax, int angle)
	{
		this.id = ax.id;
		this.angle = angle;
		this.symetrie = symetrie;
	}
	
	/**
	 * Renvoie le symétrique de l'ordre
	 * @param symetrie
	 * @return
	 */
	public ActuatorOrder getSymetrie(boolean symetrie)
	{
		if(symetrie)
			return values()[ordinal()^1];
		else
			return this;
	}
	
}
