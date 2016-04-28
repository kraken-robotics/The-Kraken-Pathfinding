package pathfinding.astarCourbe.arcs;

import obstacles.MoteurPhysique;
import pathfinding.VitesseCourbure;
import pathfinding.astarCourbe.AStarCourbeNode;
import pathfinding.astarCourbe.HeuristiqueCourbe;
import robot.Cinematique;
import robot.DirectionStrategy;
import robot.RobotChrono;
import robot.Speed;

import java.util.Arrays;
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
	private double courbureMax;
	private DirectionStrategy directionstrategyactuelle;
	private List<VitesseCourbure> listeVitesse = Arrays.asList(VitesseCourbure.values());
	private final static int TEMPS_REBROUSSEMENT = 500;
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
	public boolean isReachable(AStarCourbeNode node)
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
	public double distanceTo(AStarCourbeNode node)
	{
			// TODO : vérifier les hooks
			((RobotChrono)node.state.robot).suitArcCourbe(node.came_from_arc);
			double out = node.came_from_arc.getDuree();
			if(node.came_from_arc.rebrousse)
				out += TEMPS_REBROUSSEMENT;
			return out;
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

	/**
	 * Fournit le prochain successeur. On suppose qu'il existe
	 * @param successeur
	 */
    public boolean next(AStarCourbeNode successeur, Speed vitesseMax, Cinematique arrivee)
    {
    	VitesseCourbure v = iterator.next();
    	
		current.state.copyAStarCourbe(successeur.state);
		if(v == VitesseCourbure.DIRECT_COURBE || v == VitesseCourbure.DIRECT_COURBE_REBROUSSE)
		{
			ArcCourbe tmp;
			if(current.came_from_arc != null)
				tmp = clotho.cubicInterpolation(
						current.came_from_arc.getLast(),
						arrivee,
						vitesseMax,
						v);
			else
				tmp = clotho.cubicInterpolation(
						(RobotChrono)current.state.robot,
						arrivee,
						vitesseMax,
						v);
			if(tmp == null)
			{
//				log.debug("Interpolation impossible");
				return false;
			}

			successeur.came_from_arc = tmp;
		}
		else if(current.came_from_arc != null)
			clotho.getTrajectoire(current.came_from_arc,
					v,
					vitesseMax,
					(ArcCourbeClotho)successeur.came_from_arc);
		else // pas de prédécesseur
			clotho.getTrajectoire((RobotChrono)current.state.robot,
					v,
					vitesseMax,
					(ArcCourbeClotho)successeur.came_from_arc);

		return true;
    }
    
    /**
     * Renvoie "true" si cette vitesse est acceptable par rapport à "current".
     * @param vitesse
     * @return
     */
    private final boolean acceptable(VitesseCourbure vitesse)
    {
    	// il y a un problème si :
    	// - on veut rebrousser chemin
    	// ET
    	// - si :
    	//      - on n'est pas en fast, donc pas d'autorisation
    	//      ET
    	//      - on est dans la bonne direction, donc pas d'autorisation exceptionnelle de se retourner
    	if(vitesse.rebrousse && (directionstrategyactuelle != DirectionStrategy.FASTEST && directionstrategyactuelle.isPossible(((RobotChrono)current.state.robot).getCinematique().enMarcheAvant)))
    	{ 
    		log.debug(vitesse+" n'est pas acceptable (rebroussement interdit");
    		return false;
    	}
    	
    	// Si on ne rebrousse pas chemin alors que c'est nécessaire
    	if(!vitesse.rebrousse && !directionstrategyactuelle.isPossible(((RobotChrono)current.state.robot).getCinematique().enMarcheAvant))
    	{
    		log.debug(vitesse+" n'est pas acceptable (rebroussement nécessaire");
    		return false;
    	}

    	// On ne tente pas l'interpolation si on est trop loin
    	if((vitesse == VitesseCourbure.DIRECT_COURBE || vitesse == VitesseCourbure.DIRECT_COURBE_REBROUSSE) && heuristique.heuristicCostCourbe(((RobotChrono)current.state.robot).getCinematique()) > 100)
    	{
    		log.debug(vitesse+" n'est pas acceptable (on est trop loin)");
			return false;
    	}
    	
    	double courbureFuture = ((RobotChrono)current.state.robot).getCinematique().courbure + vitesse.vitesse * ClothoidesComputer.DISTANCE_ARC_COURBE_M;
    	if(courbureFuture >= -courbureMax && courbureFuture <= courbureMax)
    		return true;
    	else
    	{
    		log.debug(vitesse+" n'est acceptable (courbure trop grande");
    		return false;
    	}
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
    
    /**
     * Réinitialise l'itérateur à partir d'un nouvel état
     * @param current
     * @param directionstrategyactuelle
     */
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
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);		
	}

	public void setEjecteGameElement(boolean ejecteGameElement)
	{
		// TODO AStarCourbe
		
	}

	/**
	 * Renvoie le coût heuristique. L'implémentation dépend s'il s'agit d'un calcul stratégique ou dynamique
	 * @param successeur
	 * @return
	 */
	public double heuristicCost(AStarCourbeNode successeur)
	{
		return heuristique.heuristicCostCourbe(((RobotChrono)successeur.state.robot).getCinematique());
	}

}
