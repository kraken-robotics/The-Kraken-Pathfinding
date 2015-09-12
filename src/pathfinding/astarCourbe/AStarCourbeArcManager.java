package pathfinding.astarCourbe;

import obstacles.MoteurPhysique;
import pathfinding.GameState;
import pathfinding.VitesseCourbure;
import pathfinding.dstarlite.DStarLite;
import robot.DirectionStrategy;
import container.Service;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import exceptions.FinMatchException;

/**
 * Réalise des calculs pour l'AStarCourbe.
 * @author pf
 *
 */

public class AStarCourbeArcManager implements Service
{
	protected Log log;
	private MoteurPhysique moteur;
	private DStarLite dstarlite;
	
	private AStarCourbeNode current;
	private int courbureMax;
	private DirectionStrategy directionstrategyactuelle;
	private int nbVoisins = VitesseCourbure.values().length;
	private int iterator;
	private VitesseCourbure vitesseActuelle;
	
	public AStarCourbeArcManager(Log log, MoteurPhysique moteur, DStarLite dstarlite)
	{
		this.log = log;
		this.moteur = moteur;
		this.dstarlite = dstarlite;
	}
	
	/**
	 * Retourne vrai si l'exécution s'est bien passée, faux s'il y a collision 
	 * @param node
	 * @return
	 * @throws FinMatchException
	 */
	public boolean execute(AStarCourbeNode node) throws FinMatchException
	{
		if(node.came_from == null)
			return true;
		return moteur.isTraversableCourbe(node); // s'occupe de mettre à jour state également
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
//		long temps_debut = node.state.robot.getTempsDepuisDebutMatch();
//		node.state.robot.suitArcCourbe(node.came_from_arc);
//		return (int)(node.state.robot.getTempsDepuisDebutMatch() - temps_debut);
		return 1000;
	}


    public void next(AStarCourbeNode successeur)
    {
    	successeur.came_from = current;
    	successeur.came_from_arc.vitesseCourbure = VitesseCourbure.values[iterator];
		GameState.copyAStarCourbe(current.state.getReadOnly(), successeur.state);
    	iterator++;
		vitesseActuelle = VitesseCourbure.values[iterator];
    }
    
    private final boolean acceptable()
    {
    	if(current.state.robot.isArrete() && !vitesseActuelle.faisableALArret)
    		return false;
    	if((vitesseActuelle == VitesseCourbure.REBROUSSE_AVANT && directionstrategyactuelle == DirectionStrategy.FORCE_BACK_MOTION) ||
    			(vitesseActuelle == VitesseCourbure.REBROUSSE_ARRIERE && directionstrategyactuelle == DirectionStrategy.FORCE_FORWARD_MOTION))
    		return false;
    	double courbureFuture = current.state.robot.getCourbure() + vitesseActuelle.vitesse;
    	return courbureFuture >= -courbureMax && courbureFuture <= courbureMax;
    }
    
    public boolean hasNext()
    {
    	while(iterator < nbVoisins && !acceptable())
    	{
    		iterator++;
    		vitesseActuelle = VitesseCourbure.values[iterator];
    	}
    	return iterator < nbVoisins;
    }
    
    public void reinitIterator(AStarCourbeNode current, DirectionStrategy directionstrategyactuelle)
    {
    	this.directionstrategyactuelle = directionstrategyactuelle;
    	this.current = current;
    	iterator = 0;
    	vitesseActuelle = VitesseCourbure.values[0];
    }

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		courbureMax = config.getInt(ConfigInfo.COURBURE_MAX);		
	}

	public void setEjecteGameElement(boolean ejecteGameElement)
	{
		// TODO Auto-generated method stub
		
	}

	public int heuristicCost(AStarCourbeNode successeur)
	{
		return dstarlite.heuristicCostCourbe(successeur.state.robot.getPosition());
	}

}
