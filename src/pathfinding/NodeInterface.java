package pathfinding;

public interface NodeInterface {

	public double distanceTo(NodeInterface other);
	
	public double heuristicCost(NodeInterface other);
	
}
