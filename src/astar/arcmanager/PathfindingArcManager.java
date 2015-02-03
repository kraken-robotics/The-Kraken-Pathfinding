package astar.arcmanager;

import astar.AStarId;
import astar.MemoryManager;
import astar.arc.Arc;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import container.Service;
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

public class PathfindingArcManager extends ArcManager implements Service {

	private int iterator, id_node_iterator;
	private PathfindingNodes arrivee;
	protected Config config;
	protected Log log;
	private GameState<RobotChrono> state_iterator;
	private boolean prochainDebutCourbe;

	public PathfindingArcManager(Log log, Config config, MemoryManager memorymanager)
	{
		super(AStarId.PATHFINDING_ASTAR, memorymanager);
		this.log = log;
		this.config = config;
		updateConfig();
	}
	
	/**
	 * Renvoie la distance entre deux points.
	 */
	@Override
	public int distanceTo(GameState<RobotChrono> state, Arc arc) throws FinMatchException
	{
		/*
		 * Il n'y a pas d'utilisation de hook.
		 * En effet, les obstacles temporaires sont vérifiés à chaque copie (=chaque segment)
		 * Et la disparition éléments de jeu n'influence pas la recherche de chemin
		 * Par contre, à l'"exécution" par robotchrono du chemin entre deux scripts, là ils seront exécutés.
		 * Rappel: même quand on fait un appel à RobotChrono sans hook, le hook de fin de match est exécuté
		 */
		int temps_debut = state.robot.getTempsDepuisDebutMatch();
		state.robot.va_au_point_pathfinding_no_hook(((SegmentTrajectoireCourbe)arc).n);
		return state.robot.getTempsDepuisDebutMatch() - temps_debut;
	}

	/**
	 * Renvoie la distance à vol d'oiseau
	 */
	@Override
	public int heuristicCost(GameState<RobotChrono> state1)
	{
		// durée de rotation minimale
		int duree = (int)state1.robot.calculateDelta(state1.robot.getPositionPathfinding().getOrientationFinale(arrivee)) * Speed.BETWEEN_SCRIPTS.invertedRotationnalSpeed;
		// durée de translation minimale
		duree += (int)state1.robot.getPositionPathfinding().timeTo(arrivee);
		return duree;
	}

	/**
	 * Le hash d'un PathfindingNodes est son ordinal dans l'enum des PathfindingNodes.
	 */
	@Override
	public int getHash(GameState<RobotChrono> state) throws ArcManagerException
	{
		return state.robot.getPositionPathfinding().ordinal();
	}

	/**
	 * Aucune différence parce qu'il n'y a pas de création.
	 */
	@Override
	public int getHashAndCreateIfNecessary(GameState<RobotChrono> state)
	{
		return state.robot.getPositionPathfinding().ordinal();
	}

    @SuppressWarnings("unchecked")
	@Override
    public Arc next()
    {
    	return new SegmentTrajectoireCourbe(PathfindingNodes.values[iterator], prochainDebutCourbe);
    }
    
    @Override
    public boolean hasNext()
    {
    	/**
    	 * On alterne: nouvel iterator avec false, puis avec true, puis nouvel
    	 * iterator avec false, puis avec true, etc.
    	 */
    	PathfindingNodes pn_id_node_iterator = PathfindingNodes.values[id_node_iterator];
    	prochainDebutCourbe = prochainDebutCourbe && state_iterator.gridspace.isTraversableCourbe(pn_id_node_iterator, new Vec2(state_iterator.robot.getOrientation()), new Vec2(pn_id_node_iterator.getOrientationFinale(PathfindingNodes.values[iterator])), state_iterator.robot.getTempsDepuisDebutMatch(), state_iterator.robot.getVitesse());
    	if(!prochainDebutCourbe)
    	{
	    	int max_value = PathfindingNodes.length, i;
	    	for(i = iterator+1; i < max_value; i++)
	    	{
	    		if(i == id_node_iterator)
	    			continue;
	    		if(state_iterator.gridspace.isTraversable(pn_id_node_iterator, PathfindingNodes.values[i], state_iterator.robot.getTempsDepuisDebutMatch()))
	    			break;
	    	}
	    	iterator = i;
    	}
    	prochainDebutCourbe = !prochainDebutCourbe; // OH LA JOLIE BASCULE
    	return iterator != PathfindingNodes.length;
    }
    
    @Override
    public void reinitIterator(GameState<RobotChrono> gamestate)
    {
//    	log.debug("Réinit pathfinding iterator!", this);
    	id_node_iterator = gamestate.robot.getPositionPathfinding().ordinal();
    	iterator = -1;
    	state_iterator = gamestate;
    	prochainDebutCourbe = false;
    }

	@Override
	public void updateConfig()
	{}

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
