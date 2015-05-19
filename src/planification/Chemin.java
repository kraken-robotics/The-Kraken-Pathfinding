package planification;

import java.util.ArrayList;

import container.Service;
import permissions.ReadOnly;
import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * Un chemin issu de la recherche de chemin.
 * Une instance spéciale de chemin est présente dans le container: le chemin actuel.
 * Le chemin actuel est utilisé pour se déplacer, les autres chemins sont uniquement générés par la
 * recherche stratégique.
 * @author pf
 *
 */

public class Chemin implements Service
{
	protected Log log;
	protected Config config;
	private Pathfinding pathfinding;
	
	private boolean eviteElementsJeu;
	
	public Chemin(Log log, Config config, Pathfinding pathfinding)
	{
		this.log = log;
		this.config = config;
		this.pathfinding = pathfinding;
	}
	
	/**
	 * Recalcule complètement un nouveau chemin
	 * @param depart
	 * @param arrivee
	 */
	public void compute(GameState<?,ReadOnly> depart, GameState<?,ReadOnly> arrivee, boolean eviteElementsJeu)
	{
		this.eviteElementsJeu = eviteElementsJeu;
		pathfinding.computePath(this, depart, arrivee, eviteElementsJeu);
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
	public void updateConfig()
	{}

}
