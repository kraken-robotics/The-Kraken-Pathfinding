package pathfinding.thetastar;

import java.util.ArrayList;
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
import utils.Vec2;
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
	private int nbSuccesseur;
	private ArrayList<ScenarioThetaStar> scenarios;
	private ArrayList<Integer> voisins;
	private Iterator<Integer> voisinsIter;
	private LocomotionArc next;
	private Iterator<ScenarioThetaStar> scenarioIterator;
	private boolean accepted = true;
	private ScenarioThetaStar scenarioActuel;
	private boolean shootGameElement;
	
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
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		nbSuccesseurMax = config.getInt(ConfigInfo.NB_SUCCESSEUR_MAX);
		scenarios = new ArrayList<ScenarioThetaStar>();
		for(int i = 0; i < 2; i++)
			for(RayonCourbure r : RayonCourbure.values())
//				for(int s = 0; s < nbSuccesseurMax; s++)
				scenarios.add(new ScenarioThetaStar(i, r));	
	}

	public void reinitIterator(ThetaStarNode predecesseur, ThetaStarNode actuel)
	{
		scenarioIterator = scenarios.listIterator();
		nodes[PREDECESSEUR] = predecesseur;
		nodes[ACTUEL] = actuel;
		voisins = dstarlite.getListVoisins(actuel.hash);
		voisinsIter = voisins.iterator();
		nbSuccesseur = 0;
	}

	public boolean hasNext()
	{
		if(accepted == true)
			nbSuccesseur++;
		
		// Changement de scénario
		if(nbSuccesseur == nbSuccesseurMax || !voisinsIter.hasNext())
		{
			if(!scenarioIterator.hasNext())
				return false;
			scenarioActuel = scenarioIterator.next();
			voisinsIter = voisins.iterator();
		}

		// Création du LocomotionArc
		RobotChrono robot = nodes[scenarioActuel.noeudActuel].state.robot;
		Vec2<ReadOnly> positionRobot = robot.getPosition();
		int gridpointArrivee = voisinsIter.next();
		Vec2<ReadOnly> arrivee = gridspace.computeVec2(gridpointArrivee);
		double angleConsigne = Math.atan2(arrivee.y - positionRobot.y, arrivee.x - positionRobot.x);
		if(!robot.isEnMarcheAvant())
			angleConsigne += Math.PI;
		next = new LocomotionArc(positionRobot, new Vec2<ReadOnly>(robot.getOrientationAvance()), arrivee, angleConsigne, scenarioActuel.rayonCourbure, gridpointArrivee);
		return true;
	}

	public LocomotionArc nextProposition()
	{
		return next;
	}

	public boolean nextAccepted()
	{
		// TODO
		accepted = true;
		return accepted;
	}


	public int distanceTo(GameState<RobotChrono, ReadWrite> state,
			LocomotionArc voisin) {
		// TODO Auto-generated method stub
		return (int) state.robot.getPosition().distance(voisin.destination);
	}

	public int heuristicCostThetaStar(GameState<RobotChrono, ReadOnly> node) {
		// TODO: ajouter un coût pour l'orientation + prendre en compte l'accélération latérale
		return dstarlite.heuristicCostThetaStar(node.robot.getPositionGridSpace());
	}


	public boolean isFromPredecesseur()
	{
		return scenarioActuel.noeudActuel == PREDECESSEUR;
	}


	public void setShootGameElement(boolean shootGameElement)
	{
		this.shootGameElement = shootGameElement;
	}

	
}
