package constSearch;

import pathfinding.GridSpace;
import pathfinding.Pathfinding;
import utils.Log;
import container.Container;
import enums.PathfindingNodes;
import enums.ServiceNames;

public class PathfindingConst {

    public static void main(String[] args)
    {
    	try {
    		Container container = new Container();
			Pathfinding pathfinding = (Pathfinding)container.getService(ServiceNames.PATHFINDING);
			GridSpace gridspace = (GridSpace)container.getService(ServiceNames.GRID_SPACE);
			Log log = (Log)container.getService(ServiceNames.LOG);
			for(int i = 1; i < 10; i++)
			{
				pathfinding.setHeuristiqueCoeff(i);
				int compteur = 0;
				for(PathfindingNodes m: PathfindingNodes.values())
					for(PathfindingNodes n: PathfindingNodes.values())
					{
						pathfinding.computePath(m.getCoordonnees(), n, gridspace, false, true);
						compteur += pathfinding.getCompteur();
					}
				log.appel_static(i+": "+compteur);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
