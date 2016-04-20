package pathfinding.astarCourbe;

import obstacles.MoteurPhysique;
import pathfinding.VitesseCourbure;
import robot.DirectionStrategy;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
	private List<VitesseCourbure> listeVitesse = Arrays.asList(VitesseCourbure.values());
	
	private ListIterator<VitesseCourbure> iterator = listeVitesse.listIterator();
	
	public AStarCourbeArcManager(Log log, MoteurPhysique moteur, HeuristiqueCourbe heuristique, ClothoidesComputer clotho)
	{
		this.log = log;
		this.moteur = moteur;
		this.heuristique = heuristique;
		this.clotho = clotho;
	}
	
	/**
	 * Retourne faux si un obstacle est sur la route
	 * @param node
	 * @return
	 * @throws FinMatchException
	 */
	public boolean isReachable(AStarCourbeNode node) throws FinMatchException
	{
		if(node.came_from == null)
			return true;
		return moteur.isTraversableCourbe(node);
	}
	
	/**
	 * Renvoie la distance entre deux points. Et par distance, j'entends "durée".
	 * Heureusement, les longueurs des arcs de clothoïdes qu'on considère sont égales.
	 * Ne reste plus qu'à prendre en compte la vitesse, qui dépend de la courbure.
	 * Il faut exécuter tout ce qui se passe pendant ce trajet
	 */
	public int distanceTo(AStarCourbeNode node)
	{
		// TODO : vérifier les hooks
		node.state.robot.suitArcCourbe(node.came_from_arc);
		System.out.println(ClothoidesComputer.DISTANCE_ARC_COURBE+", "+node.came_from_arc.arcselems[0].vitesseTranslation);
		return (int) (ClothoidesComputer.DISTANCE_ARC_COURBE / node.came_from_arc.arcselems[0].vitesseTranslation);
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
		current.state.copyAStarCourbe(successeur.state);
		if(current.came_from_arc != null)
			clotho.getTrajectoire(current.came_from_arc,
					iterator.next(),
					successeur.came_from_arc);
		else // pas de prédécesseur
			clotho.getTrajectoire(current.state.robot,
					iterator.next(),
					successeur.came_from_arc);
    }
    
    private final boolean acceptable(VitesseCourbure vitesse)
    {
    	// Pas le droit de rebrousser chemin, sauf quand on est en "fastest"
    	if(vitesse.rebrousse && directionstrategyactuelle != DirectionStrategy.FASTEST)
    		return false;

    	double courbureFuture = current.state.robot.getCourbure() + vitesse.vitesse;
    	return courbureFuture >= -courbureMax && courbureFuture <= courbureMax;
    }
    
    /**
     * Y a-t-il encore une vitesse possible ?
     * On vérifie ici si certaines vitesses sont interdites (à cause de la courbure trop grande ou du rebroussement)
     * @return
     */
    public boolean hasNext()
    {
    	while(iterator.hasNext())
    		if(acceptable(iterator.next()))
    		{
    			iterator.previous();
    			return true;
    		}
    	return false;
    }
    
    public void reinitIterator(AStarCourbeNode current, DirectionStrategy directionstrategyactuelle)
    {
    	this.directionstrategyactuelle = directionstrategyactuelle;
    	this.current = current;
    	iterator = listeVitesse.listIterator();
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
