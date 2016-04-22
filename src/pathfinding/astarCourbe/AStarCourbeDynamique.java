package pathfinding.astarCourbe;

import exceptions.PathfindingException;
import pathfinding.CheminPathfinding;
import pathfinding.RealGameState;
import pathfinding.astarCourbe.arcs.AStarCourbeArcManager;
import pathfinding.dstarlite.DStarLite;
import robot.Cinematique;
import robot.DirectionStrategy;
import robot.RobotChrono;
import robot.Speed;
import utils.Log;

/**
 * AStar courbe utilisé pour la replanification rapide
 * @author pgimenez
 *
 */

public class AStarCourbeDynamique extends AStarCourbe
{
	private DStarLite dstarlite;
	private RealGameState state;
	
	public AStarCourbeDynamique(Log log, DStarLite dstarlite, AStarCourbeArcManager arcmanager, RealGameState state, CheminPathfinding chemin, AStarCourbeMemoryManager memorymanager)
	{
		super(log, arcmanager, memorymanager, chemin);
		this.state = state;
		this.dstarlite = dstarlite;
	}
	
	/**
	 * Calcul d'un chemin à partir d'un certain état (state) et d'un point d'arrivée (endNode).
	 * Le boolean permet de signaler au pathfinding si on autorise ou non le shootage d'élément de jeu pas déjà pris.
	 * @param state
	 * @param endNode
	 * @param shoot_game_element
	 * @return
	 * @throws PathfindingException 
	 */
	public void computeNewPath(Cinematique arrivee, boolean ejecteGameElement, DirectionStrategy directionstrategy) throws PathfindingException
	{
//		if(Config.graphicAStarCourbe)
//			fenetre.setColor(arrivee, Fenetre.Couleur.VIOLET);
		vitesseMax = Speed.STANDARD;
		this.directionstrategyactuelle = directionstrategy;
		arcmanager.setEjecteGameElement(ejecteGameElement);
		this.arrivee = arrivee;
		depart.init();
		state.copyAStarCourbe(depart.state);
		
		dstarlite.computeNewPath(((RobotChrono)depart.state.robot).getCinematique().getPosition(), arrivee.getPosition());
		process();
	}
	
	public synchronized void updatePath() throws PathfindingException
	{
		synchronized(state)
		{
			depart.init();
			state.copyAStarCourbe(depart.state);
		}
		vitesseMax = Speed.REPLANIF;
		
		dstarlite.updatePath(((RobotChrono)depart.state.robot).getCinematique().getPosition());
		chemin.clear();
		process();
	}

	protected boolean doitFixerCheminPartiel()
	{
		return chemin.isEmpty();
	}

}
