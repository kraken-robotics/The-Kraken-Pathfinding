package pathfinding;

import java.util.ArrayList;

import permissions.ReadOnly;
import utils.Vec2;

/**
 * Un arc de trajectoire courbe
 * Utilisé par le pathfinding et envoyable par série
 * @author pf
 *
 */

public class LocomotionArc
{
	private Vec2<ReadOnly> pointDuDemiPlan;
	private Vec2<ReadOnly> normaleAuDemiPlan;
	private double angleConsigne;
	private RayonCourbure rayonCourbure;
	
	public LocomotionArc(Vec2<ReadOnly> pointDuDemiPlan,
			Vec2<ReadOnly> normaleAuDemiPlan, double angleConsigne,
			RayonCourbure rayonCourbure)
	{
		this.pointDuDemiPlan = pointDuDemiPlan;
		this.normaleAuDemiPlan = normaleAuDemiPlan;
		this.angleConsigne = angleConsigne;
		this.rayonCourbure = rayonCourbure;
	}
	
	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add(String.valueOf(pointDuDemiPlan.x));
		out.add(String.valueOf(pointDuDemiPlan.y));
		out.add(String.valueOf(normaleAuDemiPlan.x));
		out.add(String.valueOf(normaleAuDemiPlan.y));
		out.add(String.valueOf(angleConsigne));
		out.add(String.valueOf(rayonCourbure));
		return out;
	}

	public ArrayList<String> toSerialFirst()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add(String.valueOf(angleConsigne));
		out.add(String.valueOf(rayonCourbure));
		return out;
	}

	
}
