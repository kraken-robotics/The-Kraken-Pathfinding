package astar.arcmanager;

import permissions.ReadOnly;
import permissions.ReadWrite;
import astar.AStarId;
import astar.MemoryManager;
import astar.arc.Arc;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import robot.RobotChrono;
import robot.Speed;
import strategie.GameState;
import utils.Log;
import utils.Config;
import utils.Vec2;
import exceptions.ArcManagerException;
import exceptions.FinMatchException;

/**
 * Réalise les calculs entre PathfindingNodes pour l'AStar.
 * @author pf
 *
 */

public class PathfindingArcManager extends ArcManager {

	private int iterator, id_node_iterator;
	private PathfindingNodes arrivee;
	protected Config config;
	protected Log log;
	private GameState<RobotChrono,ReadOnly> state_iterator;
	private boolean debutCourbe;

	public PathfindingArcManager(Log log, Config config, MemoryManager memorymanager)
	{
		super(AStarId.PATHFINDING_ASTAR, memorymanager);
		this.log = log;
		this.config = config;
	}
	
	/**
	 * Renvoie la distance entre deux points.
	 */
	@Override
	public int distanceTo(GameState<RobotChrono,ReadWrite> state, Arc arc) throws FinMatchException
	{
		/*
		 * Il n'y a pas d'utilisation de hook.
		 * En effet, les obstacles temporaires sont vérifiés à chaque copie (=chaque segment)
		 * Et la disparition éléments de jeu n'influence pas la recherche de chemin
		 * Par contre, à l'"exécution" par robotchrono du chemin entre deux scripts, là ils seront exécutés.
		 * Rappel: même quand on fait un appel à RobotChrono sans hook, le hook de fin de match est exécuté
		 */
		int temps_debut = GameState.getTempsDepuisDebutMatch(state.getReadOnly());
		GameState.va_au_point_pathfinding_no_hook(state, (SegmentTrajectoireCourbe)arc);
		return GameState.getTempsDepuisDebutMatch(state.getReadOnly()) - temps_debut;
	}

	/**
	 * Renvoie la distance à vol d'oiseau
	 */
	@Override
	public int heuristicCost(GameState<RobotChrono,ReadOnly> state1)
	{
		// durée de rotation minimale
		int duree = (int)GameState.calculateDelta(state1, GameState.getPositionPathfinding(state1).getOrientationFinale(arrivee)) * Speed.BETWEEN_SCRIPTS.invertedRotationnalSpeed;
		// durée de translation minimale
		duree += (int)GameState.getPositionPathfinding(state1).timeTo(arrivee);
		return duree;
	}

	/**
	 * Le hash d'un PathfindingNodes est son ordinal dans l'enum des PathfindingNodes.
	 */
	@Override
	public int getHash(GameState<RobotChrono,ReadOnly> state) throws ArcManagerException
	{
		return GameState.getPositionPathfinding(state).ordinal();
	}

	/**
	 * Aucune différence parce qu'il n'y a pas de création.
	 */
	@Override
	public int getHashAndCreateIfNecessary(GameState<RobotChrono,ReadOnly> state)
	{
		return GameState.getPositionPathfinding(state).ordinal();
	}

    @SuppressWarnings("unchecked")
	@Override
    public Arc next()
    {
    	if(!debutCourbe)
    		return new SegmentTrajectoireCourbe(PathfindingNodes.values[iterator]);
    	else
    		return GameState.getSegment(state_iterator);
    }
    
    @Override
    public boolean hasNext() throws FinMatchException
    {
    	/**
    	 * On alterne: nouvel iterator avec false, puis avec true, puis nouvel
    	 * iterator avec false, puis avec true, etc.
    	 */
    	debutCourbe = !debutCourbe; // OH LA JOLIE BASCULE
    	PathfindingNodes pn_id_node_iterator = PathfindingNodes.values[id_node_iterator];
    	debutCourbe = debutCourbe && GameState.isTraversableCourbe(state_iterator, PathfindingNodes.values[iterator], pn_id_node_iterator, new Vec2<ReadOnly>(GameState.getOrientation(state_iterator)), GameState.getTempsDepuisDebutMatch(state_iterator));

    	if(!debutCourbe)
    	{
	    	int max_value = PathfindingNodes.length;
	    	for(iterator++; iterator < max_value; iterator++)
	    	{
	    		if(iterator == id_node_iterator)
	    			continue;
	    		if(GameState.isTraversable(state_iterator, pn_id_node_iterator, PathfindingNodes.values[iterator], GameState.getTempsDepuisDebutMatch(state_iterator)))
	    			break;
	    	}
    	}
    	return iterator != PathfindingNodes.length;
    }
    
    @Override
    public void reinitIterator(GameState<RobotChrono,ReadOnly> state)
    {
//    	log.debug("Réinit pathfinding iterator!", this);
    	id_node_iterator = GameState.getPositionPathfinding(state).ordinal();
    	iterator = -1;
    	state_iterator = state;
    	debutCourbe = true;
    }

    /**
	 * Utilisé avant process, afin de pouvoir utiliser ensuite isArrivee
	 * @param n
	 */
	public void chargePointArrivee(PathfindingNodes n)
	{
		arrivee = n;
	}

	@Override
	public boolean isArrive(int hash)
	{
		return hash == arrivee.ordinal();
	}
	
	/**
	 * Non utilisé car on ne reconstruit normalement jamais un chemin partiel avec la recherche de chemin
	 * Sinon, on prendrait simplement le point le plus proche (le "-" vient du fait que l'AStar cherche à minimiser cette note)
	 * @param h
	 * @return
	 */
	public int getNoteReconstruct(int h)
	{
		return -(int)PathfindingNodes.values[h].distanceTo(arrivee);
	}

}
