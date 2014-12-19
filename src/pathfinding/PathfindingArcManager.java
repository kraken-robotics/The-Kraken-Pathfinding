package pathfinding;

import container.Service;
import robot.RobotChrono;
import strategie.GameState;
import utils.Log;
import utils.Config;
import enums.PathfindingNodes;
import enums.Speed;
import exceptions.FinMatchException;

 public class PathfindingArcManager implements ArcManager, Service {

	private int iterator, id_node_iterator;
	private PathfindingNodes arrivee;
//	private Config config;
//	private Log log;

	public PathfindingArcManager(Log log, Config config)
	{
//		this.log = log;
//		this.config = config;
		updateConfig();
	}
	
	@Override
	public int distanceTo(GameState<RobotChrono> state, Arc arc) throws FinMatchException
	{
		/*
		 * Il n'y a pas d'utilisation de hook.
		 * En effet, les obstacles temporaires sont vérifiés à chaque copie (=chaque segment)
		 * Et la disparition éléments de jeu n'influence pas la recherche de chemin
		 * Par contre, à l'"exécution" par robotchrono du chemin entre deux scripts, là ils seront exécutés.
		 */
		int temps_debut = state.robot.getTempsDepuisDebutMatch();
		state.robot.va_au_point_pathfinding((PathfindingNodes)arc, null);
		return state.robot.getTempsDepuisDebutMatch() - temps_debut;
	}

	@Override
	public int heuristicCost(GameState<RobotChrono> state1)
	{
		// durée de rotation minimale
		int duree = (int)state1.robot.calculateDelta(state1.robot.getPositionPathfinding().getOrientationFinale(arrivee)) * Speed.BETWEEN_SCRIPTS.invertedRotationnalSpeed;
		// durée de translation minimale
		duree += (int)state1.robot.getPositionPathfinding().distanceTo(arrivee)*Speed.BETWEEN_SCRIPTS.invertedTranslationnalSpeed;
		return duree;
	}

	@Override
	public int getHash(GameState<RobotChrono> state) {
		return state.robot.getPositionPathfinding().ordinal();
	}

    @Override
    public PathfindingNodes next()
    {
    	return PathfindingNodes.values()[iterator];
    }
    
    @Override
    public boolean hasNext(GameState<RobotChrono> state)
    {
    	do {
    		iterator++;
    		// Ce point n'est pas bon si:
    		// c'est le noeud appelant (un noeud n'est pas son propre voisin)
    		// le noeud appelant et ce noeud ne peuvent être joints par une ligne droite

    	} while(iterator < PathfindingNodes.values().length
    			&& (iterator == id_node_iterator
    			|| !state.gridspace.isTraversable(PathfindingNodes.values()[id_node_iterator], PathfindingNodes.values()[iterator], state.robot.getTempsDepuisDebutMatch())));
    	return iterator != PathfindingNodes.values().length;
    }
    
    @Override
    public void reinitIterator(GameState<RobotChrono> gamestate)
    {
    	id_node_iterator = gamestate.robot.getPositionPathfinding().ordinal();
    	iterator = -1;
    }

	@Override
	public void updateConfig() {
	}

	public String toString()
	{
		return "Recherche de chemin";
	}
	
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
	 * @param h
	 * @return
	 */
	public int getNoteReconstruct(int h)
	{
		return (int)PathfindingNodes.values()[h].distanceTo(arrivee);
	}
}
