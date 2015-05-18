package planification;

import java.util.ArrayList;

import container.Service;
import permissions.ReadOnly;
import planification.astar.arc.Arc;
import strategie.GameState;

public interface Path<A extends Arc> extends Service
{
	/**
	 * Mise à jour des coûts après une modification des obstacles
	 */
	public void updateCost();
	
	/**
	 * Recalcule un chemin à partir de la position actuelle. A faire après mise à jour.
	 * @param positionActuelle
	 * @return
	 */
	public ArrayList<A> getPath(GameState<?,ReadOnly> positionActuelle);

}
