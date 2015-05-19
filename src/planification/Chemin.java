package planification;

import java.util.ArrayList;

import container.Service;
import permissions.ReadOnly;
import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * Un chemin issu de la recherche de chemin
 * @author pf
 *
 */

public class Chemin implements Service
{
	Pathfinding pathfinding;
	
	public Chemin(Log log, Config config, Pathfinding pathfinding)
	{
	}
	
	/**
	 * Recalcule complètement un nouveau chemin
	 * @param depart
	 * @param arrivee
	 */
	public void compute(GameState<?,ReadOnly> depart, GameState<?,ReadOnly> arrivee)
	{
		pathfinding.computePath(this, depart, arrivee);
	}
	
	/**
	 * Mise à jour des coûts après une modification des obstacles
	 */
	public void updateCost()
	{
		// ne met pas à jour le chemin (au cas où ce ne soit pas nécessaire...)
	}
	
	/**
	 * Recalcule un chemin à partir de la position actuelle. A faire après mise à jour.
	 * @param positionActuelle
	 * @return
	 */
	public ArrayList<LocomotionArc> getPath(GameState<?,ReadOnly> positionActuelle)
	{
		// met à jour si nécessaire
		return null;
	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}

}
