package buffer;

import permissions.ReadOnly;
import utils.Vec2;

/**
 * Un groupe de mesures qui proviennent des capteurs
 * @author pf
 *
 */

public class IncomingData
{
	public Vec2<ReadOnly> positionRobot;
	public double orientationRobot;
	public boolean enMarcheAvant;
	/** Ce que voit chacun des capteurs */
	public int[] mesures;
	
	public IncomingData(int[] mesures, Vec2<ReadOnly> positionRobot, double orientationRobot, boolean enMarcheAvant)
	{
		this.mesures = mesures;
		this.positionRobot = positionRobot;
		this.orientationRobot = orientationRobot;
	}
	
}
