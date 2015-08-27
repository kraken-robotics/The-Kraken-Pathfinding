package pathfinding.thetastar;

import java.util.Iterator;

import pathfinding.MoteurPhysique;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import robot.RobotChrono;
import strategie.GameState;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import container.Service;

/**
 * S'occupe des arcs du ThetaStar
 * @author pf
 *
 */

public class ArcManager implements Service
{
	private Log log;
	private MoteurPhysique moteur;
	private GridSpace gridspace;
	private DStarLite dstarlite;
	
	private int nbSuccesseurMax;
	private int[] scenarios;
	private int nbScenarios;
	private int noeudEnCours;
	private Iterator<Integer> voisins;
	
	private ThetaStarNode[] nodes = new ThetaStarNode[2];
	
	private final static int PREDECESSEUR = 0;
	private final static int ACTUEL = 1;
	
	public ArcManager(Log log, MoteurPhysique moteur, GridSpace gridspace, DStarLite dstarlite)
	{
		this.log = log;
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
		scenarios = new int[2*nbSuccesseurMax*RayonCourbure.length];
		nbScenarios = 2*nbSuccesseurMax*RayonCourbure.length;
		int k = 0;
		for(int i = 0; i < 2; i++)
			for(int r = 0; r < RayonCourbure.length; r++)
				for(int s = 0; s < nbSuccesseurMax; s++)
					scenarios[k++] = s+r*nbSuccesseurMax+i*RayonCourbure.length*nbSuccesseurMax;				
	}

	public void reinitIterator(ThetaStarNode predecesseur, ThetaStarNode actuel)
	{
		nodes[PREDECESSEUR] = predecesseur;
		nodes[ACTUEL] = actuel;
		noeudEnCours = PREDECESSEUR;
		voisins = dstarlite.getIterator(predecesseur.hash);
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
		
		// si c'est vrai, scenarioActuel++
		return false;
	}


	public int distanceTo(GameState<RobotChrono, ReadWrite> successeur,
			LocomotionArc voisin) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int heuristicCostThetaStar(GameState<RobotChrono, ReadOnly> node) {
		// TODO: ajouter un coût pour l'orientation + prendre en compte l'accélération latérale
		return dstarlite.heuristicCostThetaStar(node.robot.getPositionGridSpace());
	}


	public boolean isFromPredecesseur() {
		// TODO Auto-generated method stub
		return false;
	}


	public void setShootGameElement(boolean shootGameElement) {
		// TODO Auto-generated method stub
		
	}

	
}
