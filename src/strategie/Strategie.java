package strategie;

import java.util.ArrayList;

import permissions.ReadOnly;
import container.Service;

/**
 * Algorithme de planification qui recherche une stratégie
 * LPA*, dont l'heuristique est un nombre de points
 * @author pf
 *
 */

public class Strategie implements Service {

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
	public ArrayList<StrategieArc> getPath(GameState<?,ReadOnly> positionActuelle)
	{
		// met à jour si nécessaire
		return null;
	}
	
	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}

}
