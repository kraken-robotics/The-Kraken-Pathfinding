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
	public Vec2<ReadOnly> pointBrut;
	public Vec2<ReadOnly> centreEnnemi;

	public IncomingData(Vec2<ReadOnly> pointBrut, Vec2<ReadOnly> centreEnnemi)
	{
		this.pointBrut = pointBrut;
		this.centreEnnemi = centreEnnemi;
	}
	
}
