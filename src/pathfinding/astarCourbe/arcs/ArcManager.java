/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package pathfinding.astarCourbe.arcs;

import pathfinding.VitesseCourbure;
import pathfinding.astarCourbe.AStarCourbeNode;
import pathfinding.dstarlite.DStarLite;
import robot.Cinematique;
import robot.DirectionStrategy;
import robot.Speed;
import table.GameElementNames;
import table.Table;
import table.Tribool;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import container.Service;
import obstacles.ObstaclesFixes;
import obstacles.types.ObstacleArcCourbe;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;

/**
 * Réalise des calculs pour l'AStarCourbe.
 * @author pf
 *
 */

public class ArcManager implements Service
{
	protected Log log;
	private DStarLite heuristique;
	private ClothoidesComputer clotho;
	private Table table;
	private boolean shoot;
	private AStarCourbeNode current;
	private double courbureMax;
	private DirectionStrategy directionstrategyactuelle;
	private List<VitesseCourbure> listeVitesse = Arrays.asList(VitesseCourbure.values());
	private final static int TEMPS_REBROUSSEMENT = 700;
	private ListIterator<VitesseCourbure> iterator = listeVitesse.listIterator();
	
	public ArcManager(Log log, DStarLite heuristique, ClothoidesComputer clotho, Table table)
	{
		this.table = table;
		this.log = log;
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
		// le tout premier nœud n'a pas de parent
		if(node.came_from == null)
			return true;

		ObstacleArcCourbe obs = node.came_from_arc.obstacle;

		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values())
    		if(o.getObstacle().isColliding(obs))
    			return false;

    	// Collision avec un obstacle de proximité ?
    	node.state.iterator.reinit();
    	while(node.state.iterator.hasNext())
           	if(node.state.iterator.next().isColliding(obs))
        		return false;
    	
    	// On vérifie si on collisionne un élément de jeu
    	if(!shoot)
    		for(GameElementNames g : GameElementNames.values())
    			if(table.isDone(g) != Tribool.FALSE && g.obstacle.isColliding(obs))
    				return false;

    	return true;
	}
	
	/**
	 * Renvoie la distance entre deux points. Et par distance, j'entends "durée".
	 * Heureusement, les longueurs des arcs de clothoïdes qu'on considère sont égales.
	 * Ne reste plus qu'à prendre en compte la vitesse, qui dépend de la courbure.
	 * Il faut exécuter tout ce qui se passe pendant ce trajet
	 */
	public double distanceTo(AStarCourbeNode node)
	{
		node.state.robot.suitArcCourbe(node.came_from_arc);
		double out = node.came_from_arc.getDuree();
		if(node.came_from_arc.rebrousse)
			out += TEMPS_REBROUSSEMENT;
		return out;
	}

	/**
	 * Fournit le prochain successeur. On suppose qu'il existe
	 * @param successeur
	 */
    public boolean next(AStarCourbeNode successeur, Speed vitesseMax, Cinematique arrivee)
    {
    	VitesseCourbure v = iterator.next();

		current.state.copyAStarCourbe(successeur.state);

		/**
		 * Si on est proche et qu'on tente une interpolation cubique
		 */
		if(v == VitesseCourbure.DIRECT_COURBE || v == VitesseCourbure.DIRECT_COURBE_REBROUSSE)
		{
			ArcCourbe tmp;
			if(current.came_from_arc != null)
				tmp = clotho.cubicInterpolation(
						current.state.robot,
						current.came_from_arc.getLast(),
						arrivee,
						vitesseMax,
						v);
			else
				tmp = clotho.cubicInterpolation(
						current.state.robot,
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
		
		/**
		 * Si on fait une interpolation par clothoïde
		 */
		else if(current.came_from_arc != null)
			clotho.getTrajectoire(current.state.robot,
					current.came_from_arc,
					v,
					vitesseMax,
					(ArcCourbeClotho)successeur.came_from_arc);
		else // pas de prédécesseur
			clotho.getTrajectoire(successeur.state.robot,
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
    	if(vitesse.rebrousse && (directionstrategyactuelle != DirectionStrategy.FASTEST && directionstrategyactuelle.isPossible((current.state.robot).getCinematique().enMarcheAvant)))
    	{ 
//    		log.debug(vitesse+" n'est pas acceptable (rebroussement interdit");
    		return false;
    	}
    	
    	// Si on ne rebrousse pas chemin alors que c'est nécessaire
    	if(!vitesse.rebrousse && !directionstrategyactuelle.isPossible((current.state.robot).getCinematique().enMarcheAvant))
    	{
//    		log.debug(vitesse+" n'est pas acceptable (rebroussement nécessaire");
    		return false;
    	}

    	// On ne tente pas l'interpolation si on est trop loin
    	if((vitesse == VitesseCourbure.DIRECT_COURBE || vitesse == VitesseCourbure.DIRECT_COURBE_REBROUSSE) && heuristique.heuristicCostCourbe((current.state.robot).getCinematique()) > 100)
    	{
//    		log.debug(vitesse+" n'est pas acceptable (on est trop loin)");
			return false;
    	}
    	
    	double courbureFuture = (current.state.robot).getCinematique().courbure + vitesse.vitesse * ClothoidesComputer.DISTANCE_ARC_COURBE_M;
    	if(courbureFuture >= -courbureMax && courbureFuture <= courbureMax)
    		return true;

//    	log.debug(vitesse+" n'est acceptable (courbure trop grande");
		return false;
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
		shoot = ejecteGameElement;
	}

	/**
	 * Renvoie le coût heuristique. L'implémentation dépend s'il s'agit d'un calcul stratégique ou dynamique
	 * @param successeur
	 * @return
	 */
	public double heuristicCost(AStarCourbeNode successeur)
	{
		return heuristique.heuristicCostCourbe((successeur.state.robot).getCinematique());
	}

}
