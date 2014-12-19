package hook.methods;

import robot.Robot;
import exceptions.FinMatchException;
import exceptions.serial.SerialConnexionException;
import hook.Executable;
import enums.HauteurBrasClap;
import enums.Side;

public class LeveClap implements Executable
{
	private Robot robot;
	private Side cote;
	
	public LeveClap(Robot robot, Side cote)
	{
		this.robot = robot;
		this.cote = cote;
	}
	
	@Override
	public void execute() throws FinMatchException {
		try {
			robot.bougeBrasClap(cote, HauteurBrasClap.TOUT_EN_HAUT, false);
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		}
	}

}
