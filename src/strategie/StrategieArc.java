package strategie;

import java.util.ArrayList;

import pathfinding.thetastar.LocomotionArc;
import scripts.ScriptAnticipableNames;

/**
 * Un arc pour le planificateur de stratégie
 * @author pf
 *
 */

public class StrategieArc
{
	public final ScriptAnticipableNames script_name;
	public final int version;
	public final ArrayList<LocomotionArc> chemin;
	
	public StrategieArc(ArrayList<LocomotionArc> chemin, ScriptAnticipableNames s, int version)
	{
		this.chemin = chemin;
		this.script_name = s;
		this.version = version;
	}
	
	public String toString()
	{
		return script_name+", version "+version;
	}
}
