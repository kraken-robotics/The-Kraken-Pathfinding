package pathfinding.astar;

import utils.Log;
import exceptions.FinMatchException;

/**
 * Réalise des calculs l'AStar.
 * @author pf
 *
 */

public class AStarArcManager
{
	protected Log log;

	public AStarArcManager(Log log)
	{
		this.log = log;
	}
	
	/**
	 * Renvoie la distance entre deux points.
	 */
	public int distanceTo(AStarNode node, int arc) throws FinMatchException
	{
		/*
		 * Il n'y a pas d'utilisation de hook.
		 * En effet, les obstacles temporaires sont vérifiés à chaque copie (=chaque segment)
		 * Et la disparition éléments de jeu n'influence pas la recherche de chemin
		 * Par contre, à l'"exécution" par robotchrono du chemin entre deux scripts, là ils seront exécutés.
		 * Rappel: même quand on fait un appel à RobotChrono sans hook, le hook de fin de match est exécuté
		 */
		long temps_debut = node.state.robot.getTempsDepuisDebutMatch();
		node.state.robot.vaAuPointAStar();
		return (int)(node.state.robot.getTempsDepuisDebutMatch() - temps_debut);
	}

	/**
	 * Renvoie la distance à vol d'oiseau
	 */
	public int heuristicCost(AStarNode state1)
	{
		// durée de rotation minimale
//		int duree = (int)GameState.calculateDelta(state1, GameState.getPositionPathfinding(state1).getOrientationFinale(arrivee)) * Speed.BETWEEN_SCRIPTS.invertedRotationnalSpeed;
		// durée de translation minimale
//		duree += (int)GameState.getPositionPathfinding(state1).timeTo(arrivee);
//		return duree;
		return 0;
	}

    public int next()
    {
//    	if(!debutCourbe)
 //   		return new SegmentTrajectoireCourbe(PathfindingNodes.values[iterator]);
  //  	else
   // 		return GameState.getSegment(state_iterator.getReadOnly());
    	return 0;
    }
    
    public boolean hasNext() throws FinMatchException
    {
    	/**
    	 * On alterne: nouvel iterator avec false, puis avec true, puis nouvel
    	 * iterator avec false, puis avec true, etc.
    	 */
/*    	debutCourbe = !debutCourbe; // OH LA JOLIE BASCULE
    	PathfindingNodes pn_id_node_iterator = PathfindingNodes.values[id_node_iterator];
    	debutCourbe = debutCourbe && GameState.isTraversableCourbe(state_iterator.getReadOnly(), PathfindingNodes.values[iterator], pn_id_node_iterator, new Vec2<ReadOnly>(GameState.getOrientation(state_iterator.getReadOnly())), GameState.getTempsDepuisDebutMatch(state_iterator.getReadOnly()));

    	if(!debutCourbe)
    	{
	    	int max_value = PathfindingNodes.length;
	    	for(iterator++; iterator < max_value; iterator++)
	    	{
	    		if(iterator == id_node_iterator)
	    			continue;
	    		if(GameState.isTraversable(state_iterator.getReadOnly(), pn_id_node_iterator, PathfindingNodes.values[iterator], GameState.getTempsDepuisDebutMatch(state_iterator.getReadOnly())))
	    			break;
	    	}
    	}
    	return iterator != PathfindingNodes.length;*/
    	return false;
    }
    
    public void reinitIterator(AStarNode state)
    {
////    	log.debug("Réinit pathfinding iterator!", this);
 //   	id_node_iterator = GameState.getPositionPathfinding(state).ordinal();
  //  	iterator = -1;
//		GameState.copy(state, state_iterator);
 //   	debutCourbe = true;
    }

}
