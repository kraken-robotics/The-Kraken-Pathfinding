package pathfinding.astar_courbe;

import robot.DirectionStrategy;
import container.Service;
import utils.Config;
import utils.Log;
import exceptions.FinMatchException;

/**
 * Réalise des calculs l'AStar.
 * @author pf
 *
 */

public class AStarCourbeArcManager implements Service
{
	protected Log log;

	public AStarCourbeArcManager(Log log)
	{
		this.log = log;
	}
	
	/**
	 * Renvoie la distance entre deux points.
	 */
	public int distanceTo(AStarCourbeNode node) throws FinMatchException
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


    public AStarCourbeNode next()
    {

    	return null;
    }
    
    public boolean hasNext()
    {
    	return false;
    }
    
    public void reinitIterator(AStarCourbeNode state, DirectionStrategy directionstrategyactuelle)
    {

    }

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

	public void setEjecteGameElement(boolean ejecteGameElement) {
		// TODO Auto-generated method stub
		
	}

	public int heuristicCostAStarCourbe(AStarCourbeNode successeur) {
		// TODO Auto-generated method stub
		return 0;
	}

}
