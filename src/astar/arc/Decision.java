package astar.arc;

import java.util.ArrayList;

import scripts.ScriptNames;

/**
 * Classe qui caractérise une décision.
 * C'est un script et ses paramètres.
 * Un arc pour l'AStar stratégique.
 * @author pf
 *
 */

public class Decision implements Arc {

	public final ScriptNames script_name;
	public final int version;
	public final ArrayList<PathfindingNodes> chemin;
	
	public Decision(ArrayList<PathfindingNodes> chemin, ScriptNames s, int version)
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
