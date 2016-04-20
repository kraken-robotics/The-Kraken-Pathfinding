package pathfinding.astarCourbe;

import obstacles.MoteurPhysique;
import pathfinding.VitesseCourbure;
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
	private HeuristiqueCourbe heuristique;
	private ClothoidesComputer clotho;
	
	private AStarCourbeNode current;
	private int courbureMax;
	private DirectionStrategy directionstrategyactuelle;
	private int nbVoisins = VitesseCourbure.values().length;
	private int iterator;
	private VitesseCourbure vitesseActuelle;
	
	public AStarCourbeArcManager(Log log, MoteurPhysique moteur, HeuristiqueCourbe heuristique, ClothoidesComputer clotho)
	{
		this.log = log;
		this.moteur = moteur;
		this.heuristique = heuristique;
		this.clotho = clotho;
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
	 * Renvoie la distance entre deux points. Et par distance, j'entends "durée".
	 * Heureusement, les longueurs des arcs de clothoïdes qu'on considère sont égales.
	 * Ne reste plus qu'à prendre en compte la vitesse, qui dépend de la courbure.
	 */
	public int distanceTo(AStarCourbeNode node)
	{
		// TODO : vérifier les hooks
		// TODO : vitesse
		return (int) (ClothoidesComputer.DISTANCE_ARC_COURBE * 1.);
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
	}


    public void next(AStarCourbeNode successeur)
    {
    	log.debug(current+" "+successeur);
		current.state.copyAStarCourbe(successeur.state);
    	iterator++;
		vitesseActuelle = VitesseCourbure.values[iterator];
		if(current.came_from_arc != null)
			clotho.getTrajectoire(current.came_from_arc,
					VitesseCourbure.values[iterator],
					successeur.came_from_arc);
		else // pas de prédécesseur
			clotho.getTrajectoire(current.state.robot,
					VitesseCourbure.values[iterator],
					successeur.came_from_arc);
    }
    
    private final boolean acceptable()
    {
    	if(vitesseActuelle.rebrousse && directionstrategyactuelle != DirectionStrategy.FASTEST)
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
		// TODO AStarCourbe
		
	}

	public int heuristicCost(AStarCourbeNode successeur)
	{
		return heuristique.heuristicCostCourbe(successeur.state.robot.getPosition());
	}

}
