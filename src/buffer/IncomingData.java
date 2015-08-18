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
	/** position du robot lors de la mesure */
	public Vec2<ReadOnly> positionRobot;

	/** orientation du robot lors de la mesure */
	public double orientationRobot;
	
	/** Portion parcouru par le robot lors de la détection de cet obstacle */
	public int portion;
	
	/** Ce que voit chacun des capteurs */
	public int[] mesures;
	
	/** Faut-il ignorer les capteurs ? */
	public boolean capteursOn;

	public IncomingData(Vec2<ReadOnly> positionRobot, double orientationRobot, int portion, int[] mesures, boolean capteursOn)
	{
		this.positionRobot = positionRobot;
		this.orientationRobot = orientationRobot;
		this.portion = portion;
		this.mesures = mesures;
		this.capteursOn = capteursOn;
	}
	
}
