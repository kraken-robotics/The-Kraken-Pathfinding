package robot.actuator;

/**
 * Les angles des AX12
 * @author pf
 *
 */

public enum AngleAX12
{
	TRUC(0);
	
	public final int angle;
	public final int angleSym;
	
	
	private AngleAX12(int angle)
	{
		this.angle = angle;
		angleSym = 300 - angle;
	}
	
}
