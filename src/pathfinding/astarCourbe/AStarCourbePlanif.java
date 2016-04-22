package pathfinding.astarCourbe;

import java.util.Collection;

import pathfinding.ChronoGameState;
import pathfinding.astarCourbe.arcs.AStarCourbeArcManager;
import pathfinding.astarCourbe.arcs.ArcCourbe;
import container.Service;
import exceptions.PathfindingException;
import robot.Cinematique;
import robot.DirectionStrategy;
import robot.Speed;
import utils.Config;
import utils.Log;

/**
 * AStar* simplifié, qui lisse le résultat du D* Lite et fournit une trajectoire courbe
 * On suppose qu'il n'y a jamais collision de noeuds
 * (je parle de collision dans le sens "égalité", pas "robot qui fonce dans le mur"…)
 * Cette classe est utilisée tel quel pour la version "planification". La version "dynamique" est dans une autre classe.
 * @author pf
 *
 */

public class AStarCourbePlanif extends AStarCourbe implements Service
{
	private HeuristiqueSimple heuristique;

	/**
	 * Constructeur du AStarCourbePlanif
	 */
	public AStarCourbePlanif(Log log, AStarCourbeArcManager arcmanager, AStarCourbeMemoryManager memorymanager, Collection<ArcCourbe> chemin, HeuristiqueSimple heuristique)
	{
		super(log, arcmanager, memorymanager, chemin);
		this.heuristique = heuristique;
	}
	
	public synchronized void computeNewPath(ChronoGameState stateDepart, Cinematique arrivee, boolean ejecteGameElement, DirectionStrategy directionstrategy) throws PathfindingException
	{
		log.debug("Recherche de chemin entre "+stateDepart.robot+" et "+arrivee);
		this.directionstrategyactuelle = directionstrategy;
		arcmanager.setEjecteGameElement(ejecteGameElement);
		this.arrivee = arrivee;
		depart.init();
		heuristique.setPositionArrivee(arrivee.getPosition());
		if(depart.state == null)
			depart.state = stateDepart.cloneGameState();
		else
			stateDepart.copyAStarCourbe(depart.state);

		vitesseMax = Speed.STANDARD;

		process();
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
		
	protected boolean doitFixerCheminPartiel()
	{
		return false;
	}
	
}