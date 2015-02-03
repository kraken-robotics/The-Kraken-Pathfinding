package astar.arc;

import java.util.ArrayList;

import scripts.ScriptAnticipableNames;

/**
 * Classe qui caractérise une décision.
 * C'est un script et ses paramètres.
 * Un arc pour l'AStar stratégique.
 * @author pf
 *
 */

public class Decision implements Arc {

	public final ScriptAnticipableNames script_name;
	public final PathfindingNodes version;
	public final ArrayList<SegmentTrajectoireCourbe> chemin;
	
	public Decision(ArrayList<SegmentTrajectoireCourbe> chemin, ScriptAnticipableNames s, PathfindingNodes version)
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
