package pathfinding.thetastar;

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
	public Vec2<ReadOnly> destination; // TODO remettre en private
	private double angleConsigne;
	private RayonCourbure rayonCourbure;
	public final int gridpointArrivee;
	
	public LocomotionArc(Vec2<ReadOnly> pointDuDemiPlan,
			Vec2<ReadOnly> normaleAuDemiPlan, Vec2<ReadOnly> destination, double angleConsigne,
			RayonCourbure rayonCourbure, int gridpointArrivee)
	{
		this.pointDuDemiPlan = pointDuDemiPlan;
		this.normaleAuDemiPlan = normaleAuDemiPlan;
		this.destination = destination;
		this.angleConsigne = angleConsigne;
		this.rayonCourbure = rayonCourbure;
		this.gridpointArrivee = gridpointArrivee;
	}
	
	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add(String.valueOf(pointDuDemiPlan.x));
		out.add(String.valueOf(pointDuDemiPlan.y));
		out.add(String.valueOf(normaleAuDemiPlan.x));
		out.add(String.valueOf(normaleAuDemiPlan.y));
		out.add(String.valueOf(destination.x));
		out.add(String.valueOf(destination.y));
		out.add(String.valueOf(new String(Long.toString(Math.round(angleConsigne*1000)))));
		out.add(String.valueOf(rayonCourbure));
		return out;
	}

	public ArrayList<String> toSerialFirst()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add(String.valueOf(new String(Long.toString(Math.round(angleConsigne*1000)))));
		out.add(String.valueOf(rayonCourbure));
		return out;
	}

	
}
