package hook.methods;

import permissions.ReadWrite;
import robot.Robot;
import robot.RobotChrono;
import robot.stm.HauteurBrasClap;
import strategie.GameState;
import exceptions.FinMatchException;
import exceptions.SerialConnexionException;
import hook.Executable;
import enums.Side;

/**
 * Lève un bras de clap
 * @author pf
 *
 */

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

	@Override
	public void updateGameState(GameState<RobotChrono,ReadWrite> state)
	{}

}
