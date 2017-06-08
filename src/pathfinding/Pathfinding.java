package pathfinding;
import java.util.ArrayList;

import smartMath.Vec2;

/**
 * Classe encapsulant les calculs de pathfinding
 * @author Marsya
 *
 */

// TODO: pathfinding doit-il être un service ?
public class Pathfinding
{
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public Pathfinding()
	{
		// TODO écrire le pathfinding
	}
	
	public ArrayList<Vec2> computePath(Vec2 start, Vec2 end)
	{
		
		// TODO
		// voici un pathfinding math�matiquement d�montr� comme correct
		// correct au sens d'un chemin partant du d�part et allant a l'arriv�e
		
		// bon apr�s si vous chipottez pour les obstacles en chemin aussi...
		
		ArrayList<Vec2> out = new ArrayList<Vec2>();
		out.add(end);
		return out;
	}
}
