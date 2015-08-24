package pathfinding.thetastar;

import pathfinding.MoteurPhysique;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadWrite;
import robot.RobotChrono;
import strategie.GameState;
import utils.Config;
import utils.ConfigInfo;
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
	private DStarLite dstarlite;
	
	private int nbSuccesseurMax;
	
	public ArcManager(MoteurPhysique moteur, GridSpace gridspace, DStarLite dstarlite)
	{
		this.moteur = moteur;
		this.gridspace = gridspace;
		this.dstarlite = dstarlite;
	}
	

	@Override
	public void updateConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void useConfig(Config config)
	{
		nbSuccesseurMax = config.getInt(ConfigInfo.NB_SUCCESSEUR_MAX);
	}


	public void reinitIterator(ThetaStarNode predecesseur, ThetaStarNode actuel)
	{
		
	}


	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}


	public LocomotionArc nextProposition() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean nextAccepted() {
		// TODO Auto-generated method stub
		return false;
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


	public int heuristicCostThetaStar(ThetaStarNode node) {
		return dstarlite.heuristicCostThetaStar(node);
	}


	public boolean isFromPredecesseur() {
		// TODO Auto-generated method stub
		return false;
	}

	
}
