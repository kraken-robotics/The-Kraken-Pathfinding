package pathfinding.astarCourbe;

import pathfinding.GameState;
import robot.DirectionStrategy;
import container.Service;
import utils.Config;
import utils.ConfigInfo;
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
	private AStarCourbeNode current;
	private int courbureMax;

	public AStarCourbeArcManager(Log log)
	{
		this.log = log;
	}
	
	public void execute(AStarCourbeNode node) throws FinMatchException
	{
		if(node.came_from == null)
			return;
		
		// exécution
	}
	
	/**
	 * Renvoie la distance entre deux points.
	 */
	public int distanceTo(AStarCourbeNode node)
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
		node.state.robot.suitArcCourbe(node.came_from_arc);
		return (int)(node.state.robot.getTempsDepuisDebutMatch() - temps_debut);
	}


    public void next(AStarCourbeNode successeur)
    {
    	successeur.came_from = current;
// prépare next : met son arccourbe, son père, copie le gamestate
		GameState.copyAStarCourbe(current.state.getReadOnly(), successeur.state);

		// TODO calculer l'arc
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
	{
		courbureMax = config.getInt(ConfigInfo.COURBURE_MAX);		
	}

	public void setEjecteGameElement(boolean ejecteGameElement) {
		// TODO Auto-generated method stub
		
	}

	public int heuristicCostAStarCourbe(AStarCourbeNode successeur) {
		// TODO Auto-generated method stub
		return 0;
	}

}
