package pathfinding.astar;

import container.Service;
import utils.Config;
import utils.Log;
import exceptions.FinMatchException;

/**
 * Réalise des calculs l'AStar.
 * @author pf
 *
 */

public class AStarArcManager implements Service
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
		// TODO : si, il faut les exécuter
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

    	return 0;
    }
    
    public boolean hasNext() throws FinMatchException
    {
    	return false;
    }
    
    public void reinitIterator(AStarNode state)
    {

    }

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
