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
import robot.DirectionStrategy;

/**
 * S'occupe des arcs du ThetaStar
 * @author pf
 *
 */

// TODO: si besoin, modifier le hash du thetastar (inclure la direction actuelle?)

public class ArcManager implements Service
{
	protected Log log;
	private MoteurPhysique moteur;
	private GridSpace gridspace;
	private DStarLite dstarlite;
	private MemoryManager memorymanager;
	
	private int nbSuccesseurMax;
	private int nbSuccesseur;
	private ArrayList<ScenarioThetaStar> scenarios, scenariosFirst, scenariosSansRebroussement, scenariosFirstSansRebroussement;
	private ArrayList<Integer> voisins;
	private Iterator<Integer> voisinsIter;
	private LocomotionArc next;
	private Iterator<ScenarioThetaStar> scenarioIterator;
	private boolean accepted = true;
	private ScenarioThetaStar scenarioActuel;
	private boolean ejecteGameElement;
	private boolean pasDeVoisin;
	private Vec2<ReadWrite> positionRobot = new Vec2<ReadWrite>();
	private Vec2<ReadWrite> positionArrivee = new Vec2<ReadWrite>();
	
	private ThetaStarNode[] nodes = new ThetaStarNode[2];
	
	private final static int PREDECESSEUR = 0;
	private final static int ACTUEL = 1;
	
	public ArcManager(Log log, MoteurPhysique moteur, GridSpace gridspace, DStarLite dstarlite, MemoryManager memorymanager)
	{
		this.log = log;
		this.moteur = moteur;
		this.gridspace = gridspace;
		this.dstarlite = dstarlite;
		this.memorymanager = memorymanager; 
		
		// Scenario qui prend en compte le point actuel et son prédécesseur (selon le principe du Theta*)
		scenarios = new ArrayList<ScenarioThetaStar>();
		
		// Scenario qui prend en compte uniquement le point actuel. Utilisé lorsqu'il n'y a pas de successeur
		scenariosFirst = new ArrayList<ScenarioThetaStar>();

		// Scenario qui prend en compte le point actuel et son prédécesseur (selon le principe du Theta*)
		scenariosSansRebroussement = new ArrayList<ScenarioThetaStar>();
		
		// Scenario qui prend en compte uniquement le point actuel. Utilisé lorsqu'il n'y a pas de successeur
		scenariosFirstSansRebroussement = new ArrayList<ScenarioThetaStar>();

		for(int i = 0; i < 2; i++)
			for(RayonCourbure r : RayonCourbure.values())
				scenarios.add(new ScenarioThetaStar(i, r));	
		for(RayonCourbure r : RayonCourbure.values())
			scenariosFirst.add(new ScenarioThetaStar(ACTUEL, r));	

		for(int i = 0; i < 2; i++)
			for(RayonCourbure r : RayonCourbure.values())
				if(r != RayonCourbure.REBROUSSEMENT)
					scenariosSansRebroussement.add(new ScenarioThetaStar(i, r));	
		for(RayonCourbure r : RayonCourbure.values())
			if(r != RayonCourbure.REBROUSSEMENT)
				scenariosFirstSansRebroussement.add(new ScenarioThetaStar(ACTUEL, r));
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		nbSuccesseurMax = config.getInt(ConfigInfo.NB_SUCCESSEUR_MAX);
	}
	
	public void reinitIterator(ThetaStarNode predecesseur, ThetaStarNode actuel,
			DirectionStrategy directionstrategy)
	{
		// Choix des scénarios à utiliser
		if(directionstrategy.pointRebroussementPossible)
		{
			if(predecesseur == null)
				scenarioIterator = scenariosFirst.listIterator();
			else
				scenarioIterator = scenarios.listIterator();
		}
		else
		{
			if(predecesseur == null)
				scenarioIterator = scenariosFirstSansRebroussement.listIterator();
			else
				scenarioIterator = scenariosSansRebroussement.listIterator();			
		}
		
		nodes[PREDECESSEUR] = predecesseur;
		nodes[ACTUEL] = actuel;
		voisins = dstarlite.getListVoisins(actuel.hash);
		voisinsIter = voisins.iterator();
		nbSuccesseur = 0;
		scenarioActuel = scenarioIterator.next();
		pasDeVoisin = voisins.isEmpty();

	}

	public boolean hasNext()
	{
		if(pasDeVoisin)
			return false;
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
//		Vec2<ReadOnly> positionRobot = gridspace.computeVec2(robot.getPositionGridSpace());
		int gridpointArrivee = voisinsIter.next();
//		Vec2<ReadOnly> arrivee = gridspace.computeVec2(gridpointArrivee);
//		double angleConsigne = Math.atan2(arrivee.y - positionRobot.y, arrivee.x - positionRobot.x);

//		if(!robot.isEnMarcheAvant())
//			angleConsigne += Math.PI;

		next = memorymanager.getNewArc();
		next.update(robot.getPositionGridSpace(), robot.getOrientationAvance(), robot.isEnMarcheAvant(), /*angleConsigne,*/ scenarioActuel.rayonCourbure, gridpointArrivee);

//		log.debug("next : "+next);
		
		return true;
	}

//	public final void destroyArc(LocomotionArc arc)
//	{
//		memorymanager.destroyArc(arc);
//	}
	
	public LocomotionArc nextProposition()
	{
		return next;
	}

	public boolean nextAccepted()
	{
		// Si le dstarlite ne fournit pas d'heuristique… pour le moment on abandonne juste
		if(dstarlite.heuristicCostThetaStar(next.getGridpointArrivee()) == Integer.MAX_VALUE)
			return false;
		// TODO
		// il faut: vérifier s'il y a collision
		// vérifier si la collision ne peut pas se transformer en prise d'objet (par script de hook). Attention à la direction actuelle !
		// vérifier si le script de hook est faisable. Auquel cas, exécution du script si possible et il n'y a pas de collision
		// on peut modifier l'ordre de ces vérifications afin d'accélérer le test (direction puis faisabilité script puis collision ?)
 //		accepted = moteur.isAccessibleCourbe(next.pointDuDemiPlan, next.destination, next.normaleAuDemiPlan, next.rayonCourbure);
		return accepted;
	}

	public int distanceTo(GameState<RobotChrono, ReadWrite> state,
			LocomotionArc voisin)
	{
		// TODO Auto-generated method stub
		gridspace.computeVec2(positionRobot, state.robot.getPositionGridSpace());
		gridspace.computeVec2(positionArrivee, voisin.getGridpointArrivee());
		
		int out = (int) positionRobot.distance(positionArrivee);
		state.robot.setPositionGridSpace(voisin.getGridpointArrivee());
		if(voisin.getRayonCourbure() == RayonCourbure.REBROUSSEMENT)
		{
			state.robot.stopper();
			state.robot.inverseSensMarche();
		}
		return out;
	}

	public int heuristicCostThetaStar(GameState<RobotChrono, ReadOnly> node)
	{
		// TODO: ajouter un coût pour l'orientation (si, selon dstarlite, on est bien orienté ou non) + prendre en compte l'accélération latérale
//		log.debug("Heuristique pour "+gridspace.computeVec2(node.robot.getPositionGridSpace())+": "+dstarlite.heuristicCostThetaStar(node.robot.getPositionGridSpace()));
		return dstarlite.heuristicCostThetaStar(node.robot.getPositionGridSpace());
	}


	public ThetaStarNode noeudPere()
	{
		return nodes[scenarioActuel.noeudActuel];
//		return scenarioActuel.noeudActuel == PREDECESSEUR;
	}


	public void setEjecteGameElement(boolean ejecteGameElement)
	{
		this.ejecteGameElement = ejecteGameElement;
	}

	public void emptyMemoryManager()
	{
		memorymanager.empty();
	}

	
}
