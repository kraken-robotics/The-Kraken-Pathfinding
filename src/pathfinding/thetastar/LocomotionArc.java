package pathfinding.thetastar;

import java.util.ArrayList;

import pathfinding.dstarlite.GridSpace;
import permissions.ReadWrite;
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
	private Vec2<ReadWrite> pointDuDemiPlan;
	private Vec2<ReadWrite> normaleAuDemiPlan;
	private double orientationAuHook;
	private Vec2<ReadWrite> destination;
	private double angleConsigne;
	private RayonCourbure rayonCourbure;
	private int gridpointArrivee;
	private boolean enMarcheAvant;
	private int indiceMemoryManager;
	
	public LocomotionArc()
	{}
	
	public void copy(LocomotionArc other)
	{
		// TODO
	}
	
	public final int getGridpointArrivee()
	{
		return gridpointArrivee;
	}
	
	public void update(int pointDuDemiPlanGridpoint,
			double orientationAuHook, boolean enMarcheAvant,
			RayonCourbure rayonCourbure, int gridpointArrivee)
	{
		this.pointDuDemiPlanGridpoint = pointDuDemiPlanGridpoint;
		this.orientationAuHook = orientationAuHook;
//		this.angleConsigne = angleConsigne;
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
		gridspace.computeVec2(destination, gridpointArrivee);
		gridspace.computeVec2(pointDuDemiPlan, pointDuDemiPlanGridpoint);
		Vec2.setAngle(normaleAuDemiPlan, orientationAuHook);
		angleConsigne = Math.atan2(destination.y - pointDuDemiPlan.y, destination.x - pointDuDemiPlan.x);
		if(enMarcheAvant)
			angleConsigne += Math.PI;
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
//		out.add(String.valueOf(destination.x));
//		out.add(String.valueOf(destination.y));
		out.add(String.valueOf(new String(Long.toString(Math.round(angleConsigne*1000)))));
		out.add(String.valueOf(rayonCourbure));
		return out;
	}

	public String toString()
	{
		return "Arc de "+pointDuDemiPlan+" à "+destination+" avec courbure "+rayonCourbure.rayon;
	}

	public final RayonCourbure getRayonCourbure()
	{
		return rayonCourbure;
	}
	
}
