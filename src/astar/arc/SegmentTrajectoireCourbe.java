package astar.arc;

public class SegmentTrajectoireCourbe implements Arc
{
	public final PathfindingNodes n;
	public final boolean debuteAvecTrajectoireCourbe;

	public SegmentTrajectoireCourbe(PathfindingNodes n, boolean debuteAvecTrajectoireCourbe)
	{
		this.n = n;
		this.debuteAvecTrajectoireCourbe = debuteAvecTrajectoireCourbe;
	}
	
}
