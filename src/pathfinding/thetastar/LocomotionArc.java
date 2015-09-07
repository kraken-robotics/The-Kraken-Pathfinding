package pathfinding.thetastar;

import java.util.ArrayList;

import pathfinding.dstarlite.GridSpace;
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
	private int pointDuDemiPlanGridpoint;
	private double orientationAuHook;
	private RayonCourbure rayonCourbure;
	private int gridpointArrivee;
	private int indiceMemoryManager;
	
	public LocomotionArc()
	{}
		
	public void copy(LocomotionArc other)
	{
		other.pointDuDemiPlanGridpoint= pointDuDemiPlanGridpoint;
		other.orientationAuHook = orientationAuHook;
		other.rayonCourbure = rayonCourbure;
		other.gridpointArrivee = gridpointArrivee;
	}
	
	public final double getOrientationAuHook()
	{
		return orientationAuHook;
	}
	
	public final int getGridpointArrivee()
	{
		return gridpointArrivee;
	}
	
	public void update(int pointDuDemiPlanGridpoint,
			double orientationAuHook,
			RayonCourbure rayonCourbure, int gridpointArrivee)
	{
		this.pointDuDemiPlanGridpoint = pointDuDemiPlanGridpoint;
		this.orientationAuHook = orientationAuHook;
		this.rayonCourbure = rayonCourbure;
		this.gridpointArrivee = gridpointArrivee;
	}

	public final int getIndiceMemoryManager()
	{
		return indiceMemoryManager;
	}
	
	public final void setIndiceMemoryManager(int indiceMemoryManager)
	{
		this.indiceMemoryManager = indiceMemoryManager;
	}
	
	public void completeArc(GridSpace gridspace)
	{
	}
	
	public ArrayList<String> toSerial(GridSpace gridspace)
	{
		Vec2<ReadOnly> destination = gridspace.computeVec2(gridpointArrivee);
		Vec2<ReadOnly> pointDuDemiPlan = gridspace.computeVec2(pointDuDemiPlanGridpoint);
		Vec2<ReadOnly> normaleAuDemiPlan = new Vec2<ReadOnly>(orientationAuHook);
//		angleConsigne = Math.atan2(destination.y - pointDuDemiPlan.y, destination.x - pointDuDemiPlan.x);
//		if(enMarcheAvant)
//			angleConsigne += Math.PI;
		ArrayList<String> out = new ArrayList<String>();
		out.add(String.valueOf(pointDuDemiPlan.x));
		out.add(String.valueOf(pointDuDemiPlan.y));
		out.add(String.valueOf(normaleAuDemiPlan.x));
		out.add(String.valueOf(normaleAuDemiPlan.y));
		out.add(String.valueOf(destination.x));
		out.add(String.valueOf(destination.y));
//		out.add(String.valueOf(new String(Long.toString(Math.round(angleConsigne*1000)))));
		out.add(String.valueOf(rayonCourbure));
		return out;
	}

	public ArrayList<String> toSerialFirst(GridSpace gridspace)
	{
		Vec2<ReadOnly> destination = gridspace.computeVec2(gridpointArrivee);
		ArrayList<String> out = new ArrayList<String>();
		out.add(String.valueOf(destination.x));
		out.add(String.valueOf(destination.y));
//		out.add(String.valueOf(new String(Long.toString(Math.round(angleConsigne*1000)))));
		out.add(String.valueOf(rayonCourbure));
		return out;
	}

	public String toString()
	{
		return "Arc de "+pointDuDemiPlanGridpoint+" à "+gridpointArrivee+" avec courbure "+rayonCourbure.rayon;
	}

	public final RayonCourbure getRayonCourbure()
	{
		return rayonCourbure;
	}
	
}
