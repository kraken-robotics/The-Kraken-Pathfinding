package planification;

import java.util.ArrayList;

import planification.dstar.GridPoint;
import planification.dstar.LocomotionNode;

public interface Pathfinding {

	/**
	 * Calcule un chemin
	 * @param depart
	 * @param arrivee
	 * @return
	 */
	public void computePath(GridPoint depart, GridPoint arrivee);
	
	/**
	 * Mise à jour des coûts après une modification des obstacles
	 */
	public void updateCost();
	
	/**
	 * Recalcule un chemin à partir de la position actuelle. A faire après mise à jour.
	 * @param positionActuelle
	 * @return
	 */
	public ArrayList<LocomotionNode> getPath(GridPoint positionActuelle);
	
}
