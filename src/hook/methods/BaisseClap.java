package hook.methods;

import robot.Robot;
import exceptions.FinMatchException;
import exceptions.serial.SerialConnexionException;
import hook.Executable;
import enums.HauteurBrasClap;
import enums.Side;

/**
 * Méthode pour baisser un clap
 * @author pf
 *
 */

public class BaisseClap implements Executable
{
	private Robot robot;
	private Side cote;
	
	public BaisseClap(Robot robot, Side cote)
	{
		this.robot = robot;
		this.cote = cote;
	}
	
	@Override
	public void execute() throws FinMatchException {
		try {
			robot.bougeBrasClap(cote, HauteurBrasClap.FRAPPE_CLAP, false);
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		}
	}

}
