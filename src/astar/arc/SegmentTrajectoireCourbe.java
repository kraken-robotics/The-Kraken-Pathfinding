package astar.arc;

public class SegmentTrajectoireCourbe implements Arc
{
	public final PathfindingNodes n;
	public final int differenceDistance;

	public SegmentTrajectoireCourbe(PathfindingNodes n, int differenceDistance)
	{
		this.n = n;
		this.differenceDistance = differenceDistance;
	}
	
}
