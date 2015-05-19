package threads;

import permissions.ReadOnly;
import utils.Vec2;

/**
 * Une donnée qui provient des capteurs
 * @author pf
 *
 */

public class IncomingData
{
	/** pointBrut est l'endroit où le capteur a vu quelque chose */
	public Vec2<ReadOnly> pointBrut;

	/** centreEnnemi est l'estimation du centre de l'ennemi */
	public Vec2<ReadOnly> centreEnnemi;
	
	/** Portion parcouru par le robot lors de la détection de cet obstacle */
	public int portion;

	public IncomingData(Vec2<ReadOnly> pointBrut, Vec2<ReadOnly> centreEnnemi, int portion)
	{
		this.pointBrut = pointBrut;
		this.centreEnnemi = centreEnnemi;
		this.portion = portion;
	}
	
}
