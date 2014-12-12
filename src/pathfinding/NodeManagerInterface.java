package pathfinding;

public interface NodeManagerInterface {

	public void reinitIterator(NodeInterface n);

	public boolean hasNext();
	
	public NodeInterface next();
	
}
