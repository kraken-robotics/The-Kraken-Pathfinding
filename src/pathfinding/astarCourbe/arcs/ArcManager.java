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

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import graphic.PrintBuffer;
import obstacles.types.ObstacleArcCourbe;
import obstacles.types.ObstaclesFixes;
import utils.Log;

/**
 * Réalise des calculs pour l'A* courbe.
 * @author pf
 *
 */

public class ArcManager implements Service, Configurable
{
	protected Log log;
	private DStarLite heuristique;
	private ClothoidesComputer clotho;
	private PrintBuffer buffer;
	private Table table;
	private AStarCourbeNode current;
	private double courbureMax;
	private boolean printObs;
	
	private DirectionStrategy directionstrategyactuelle;
	private List<VitesseCourbure> listeVitesse = Arrays.asList(VitesseCourbure.values());
	private ListIterator<VitesseCourbure> iterator = listeVitesse.listIterator();
	
	public ArcManager(Log log, DStarLite heuristique, ClothoidesComputer clotho, Table table, PrintBuffer buffer)
	{
		this.table = table;
		this.log = log;
		this.heuristique = heuristique;
		this.clotho = clotho;
		this.buffer = buffer;
	}

	private ObstacleArcCourbe obs = new ObstacleArcCourbe();
	
	/**
	 * Retourne faux si un obstacle est sur la route
	 * @param node
	 * @return
	 * @throws FinMatchException
	 */
	public boolean isReachable(AStarCourbeNode node)
	{
		// le tout premier nœud n'a pas de parent
		if(node.parent == null)
			return true;

		/**
		 * On agrège les obstacles en un seul pour simplifier l'écriture des calculs
		 */
		obs.ombresRobot.clear();
		for(int i = 0; i < node.getArc().getNbPoints(); i++)
			obs.ombresRobot.add(node.getArc().getPoint(i).obstacle);
		
		if(printObs)
			buffer.addSupprimable(obs);
		
		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values())
    		if(o.getObstacle().isColliding(obs))
    		{
//				log.debug("Collision avec "+o);
    			return false;
    		}

    	// Collision avec un obstacle de proximité ?
    	node.state.iterator.reinit();
    	while(node.state.iterator.hasNext())
           	if(node.state.iterator.next().isColliding(obs))
    		{
//				log.debug("Collision avec un obstacle de proximité.");
    			return false;
    		}
    	
    	// On vérifie si on collisionne un élément de jeu
		for(GameElementNames g : GameElementNames.values())
			if(table.isDone(g) != Tribool.FALSE && g.obstacle.isColliding(obs))
    		{
//    			log.debug("Collision avec "+g);
    			return false;
    		}

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
		node.state.robot.suitArcCourbe(node.getArc());
		return node.getArc().getDuree();
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
		if(v == VitesseCourbure.DIRECT_COURBE)// || v == VitesseCourbure.DIRECT_COURBE_REBROUSSE)
		{
//			log.debug("Recherche arc cubique");
			ArcCourbeCubique tmp;
			if(current.getArc() != null)
				tmp = clotho.cubicInterpolation(
						current.state.robot,
						current.getArc().getLast(),
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

			successeur.cameFromArcCubique = tmp;
		}
		
		/**
		 * Si on fait une interpolation par clothoïde
		 */
		else if(current.parent != null)
			clotho.getTrajectoire(current.state.robot,
					current.cameFromArc,
					v,
					vitesseMax,
					successeur.cameFromArc);
		else // pas de prédécesseur
			clotho.getTrajectoire(successeur.state.robot,
					v,
					vitesseMax,
					successeur.cameFromArc);

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
		if((vitesse == VitesseCourbure.DIRECT_COURBE /*|| vitesse == VitesseCourbure.DIRECT_COURBE_REBROUSSE*/))
		{
			Double h = heuristique.heuristicCostCourbe((current.state.robot).getCinematique());
			if(h == null || h > 250)
//				log.debug(vitesse+" n'est pas acceptable (on est trop loin)");
			return false;
		}
    	
    	// TODO
    	double courbureFuture = (current.state.robot).getCinematique().courbureGeometrique + vitesse.vitesse * ClothoidesComputer.DISTANCE_ARC_COURBE_M;
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
	public void useConfig(Config config)
	{
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);
		printObs = config.getBoolean(ConfigInfo.GRAPHIC_ROBOT_COLLISION);
	}

}
