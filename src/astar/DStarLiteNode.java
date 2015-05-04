package astar;

import astar.arc.GridPoint;
import robot.RobotChrono;
import strategie.GameState;

public class DStarLiteNode {

	public GameState<RobotChrono> state;
	public int hash;
	public GridPoint gridpoint;
	public Cle cle;
	public int g, rhs;
	
	public DStarLiteNode(GameState<RobotChrono> state, int hash, GridPoint gridpoint, Cle cle, int g, int rhs)
	{
		this.state = state;
		this.hash = hash;
		this.gridpoint = gridpoint;
		this.cle = cle;
		this.g = g;
		this.rhs = rhs;
	}
	
}
