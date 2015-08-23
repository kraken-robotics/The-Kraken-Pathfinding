package pathfinding;

import permissions.ReadWrite;
import robot.RobotChrono;
import strategie.GameState;
import utils.Config;
import container.Service;

/**
 * S'occupe des arcs du ThetaStar
 * @author pf
 *
 */

public class ArcManager implements Service
{
	private MoteurPhysique moteur;
	private GridSpace gridspace;

	public ArcManager(MoteurPhysique moteur, GridSpace gridspace)
	{
		this.moteur = moteur;
		this.gridspace = gridspace;
	}
	

	@Override
	public void updateConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void useConfig(Config config) {
		// TODO Auto-generated method stub
		
	}


	public void reinitIterator(int positionGridSpace) {
		// TODO Auto-generated method stub
		
	}


	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}


	public LocomotionArc next() {
		// TODO Auto-generated method stub
		return null;
	}


	public int distanceTo(GameState<RobotChrono, ReadWrite> successeur,
			LocomotionArc voisin) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getNoteReconstruct(int h) {
		// TODO Auto-generated method stub
		return 0;
	}

}
