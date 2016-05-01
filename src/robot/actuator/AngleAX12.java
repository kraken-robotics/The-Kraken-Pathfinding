package robot.actuator;

/**
 * Les angles des AX12
 * @author pf
 *
 */

public enum AngleAX12
{
	FERME(5),
	OUVERT_90(45),
	OUVERT1(135),
	OUVERT2(100),
	VERR1(210),
	VERR2(216),
	
	POISSON_BAS(140),
	POISSON_MILIEU(105),
	POISSON_HAUT(60),
	
	POISSON_OUVRE(195),
	POISSON_FERME(145);

	
	public final int angle;
	public final int angleSym;
	
	
	private AngleAX12(int angle)
	{
		this.angle = angle;
		angleSym = 300 - angle;
	}
	
}
