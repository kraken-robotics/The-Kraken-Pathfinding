package robot.actuator;

/**
 * Enumération des AX12 du robot avec leur ID
 * @author pf
 *
 */

public enum AX12
{
	AX12_TEST(0);
	
	public final int id;
	
	private AX12(int id)
	{
		this.id = id;
	}
}