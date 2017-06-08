package exceptions.strategie;

/**
 * Exception levée par robotchrono si sa durée est négative
 * @author pf
 *
 */
public class RobotChronoException extends Exception {

	private static final long serialVersionUID = 4219866343407002284L;

	public RobotChronoException()
	{
		super();
	}
	
	public RobotChronoException(String m)
	{
		super(m);
	}

}
