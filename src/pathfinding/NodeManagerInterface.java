package pathfinding;

/**
 * Interface du NodeManager.
 * C'est lui qui s'occupe de calculer les voisins d'un sommet.
 * @author pf
 *
 */

public interface NodeManagerInterface {

	public void reinitIterator(NodeInterface n);

	public boolean hasNext();
	
	public NodeInterface next();
	
}
