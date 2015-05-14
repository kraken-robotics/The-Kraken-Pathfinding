package planification;

import java.util.ArrayList;

import planification.dstar.GridPoint;
import planification.dstar.LocomotionNode;

public interface Pathfinding {

	public ArrayList<LocomotionNode> computePath(GridPoint depart, GridPoint arrivee);
	
	public ArrayList<LocomotionNode> recomputePath(GridPoint positionActuelle);
	
	public void updatePath();
	
}
