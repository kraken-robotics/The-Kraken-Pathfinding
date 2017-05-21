/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package pathfinding.astar.arcs;

import pathfinding.DirectionStrategy;
import pathfinding.SensFinal;
import pathfinding.astar.AStarCourbeNode;
import pathfinding.astar.arcs.vitesses.VitesseClotho;
import pathfinding.astar.arcs.vitesses.VitesseBezier;
import pathfinding.astar.arcs.vitesses.VitesseDemiTour;
import pathfinding.astar.arcs.vitesses.VitesseRameneVolant;
import pathfinding.astar.arcs.vitesses.VitesseCourbure;
import pathfinding.dstarlite.DStarLite;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.Speed;
import table.GameElementNames;
import table.RealTable;
import table.EtatElement;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.HighPFClass;
import exceptions.MemoryManagerException;
import graphic.PrintBufferInterface;
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.types.ObstacleArcCourbe;
import obstacles.types.ObstaclesFixes;
import utils.Log;

/**
 * Réalise des calculs pour l'A* courbe.
 * 
 * @author pf
 *
 */

public class ArcManager implements Service, HighPFClass
{
	protected Log log;
	private ClothoidesComputer clotho;
	protected BezierComputer bezier;
	private PrintBufferInterface buffer;
	private RealTable table;
	private AStarCourbeNode current;
	private DStarLite dstarlite;
	private ObstaclesIteratorPresent obstaclesProxIterator;
	private double courbureMax;
	private boolean printObs;
	private boolean useCercle;

	private DirectionStrategy directionstrategyactuelle;
	private SensFinal sens;
	private Cinematique arrivee = new Cinematique();
	private CercleArrivee cercle;
	private List<VitesseCourbure> listeVitesse = new ArrayList<VitesseCourbure>();
	private ListIterator<VitesseCourbure> iterator = listeVitesse.listIterator();
	private List<ObstaclesFixes> disabledObstaclesFixes = new ArrayList<ObstaclesFixes>();

	public ArcManager(Log log, ClothoidesComputer clotho, RealTable table, PrintBufferInterface buffer, DStarLite dstarlite, BezierComputer bezier, CercleArrivee cercle, Config config, ObstaclesIteratorPresent obstaclesProxIterator)
	{
		this.obstaclesProxIterator = obstaclesProxIterator;
		this.bezier = bezier;
		this.table = table;
		this.log = log;
		this.clotho = clotho;
		this.buffer = buffer;
		this.dstarlite = dstarlite;
		this.cercle = cercle;

		for(VitesseCourbure v : VitesseClotho.values())
			listeVitesse.add(v);
		for(VitesseCourbure v : VitesseBezier.values())
			listeVitesse.add(v);
		for(VitesseCourbure v : VitesseDemiTour.values())
			listeVitesse.add(v);
		for(VitesseCourbure v : VitesseRameneVolant.values())
			listeVitesse.add(v);

		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);
		printObs = config.getBoolean(ConfigInfo.GRAPHIC_ROBOT_COLLISION);
	}

	private ObstacleArcCourbe obs = new ObstacleArcCourbe();

	/**
	 * Retourne faux si un obstacle est sur la route
	 * 
	 * @param node
	 * @return
	 * @throws FinMatchException
	 */
	public boolean isReachable(AStarCourbeNode node, boolean shoot)
	{
		// le tout premier nœud n'a pas de parent
		if(node.parent == null)
			return true;

		/**
		 * On agrège les obstacles en un seul pour simplifier l'écriture des
		 * calculs
		 */
		obs.ombresRobot.clear();
		for(int i = 0; i < node.getArc().getNbPoints(); i++)
			obs.ombresRobot.add(node.getArc().getPoint(i).obstacle);

		if(printObs)
			buffer.addSupprimable(obs);

		// Collision avec un obstacle fixe?
		for(ObstaclesFixes o : ObstaclesFixes.values())
			if(!disabledObstaclesFixes.contains(o) && o.getObstacle().isColliding(obs))
			{
				// log.debug("Collision avec "+o);
				return false;
			}

		// Collision avec un obstacle de proximité ?

		obstaclesProxIterator.reinit();
		while(obstaclesProxIterator.hasNext())
			if(obstaclesProxIterator.next().isColliding(obs))
			{
				// log.debug("Collision avec un obstacle de proximité.");
				return false;
			}
		/*
		 * node.state.iterator.reinit();
		 * while(node.state.iterator.hasNext())
		 * if(node.state.iterator.next().isColliding(obs))
		 * {
		 * // log.debug("Collision avec un obstacle de proximité.");
		 * return false;
		 * }
		 */

		// On vérifie si on collisionne un élément de jeu (sauf si on shoot)
		if(!shoot)
			for(GameElementNames g : GameElementNames.values())
				if(table.isDone(g).hash <= EtatElement.PRIS_PAR_ENNEMI.hash && g.obstacle.isColliding(obs))
				{
					// log.debug("Collision avec "+g);
					return false;
				}

		return true;
	}

	/**
	 * Renvoie la distance entre deux points. Et par distance, j'entends
	 * "durée".
	 * Heureusement, les longueurs des arcs de clothoïdes qu'on considère sont
	 * égales.
	 * Ne reste plus qu'à prendre en compte la vitesse, qui dépend de la
	 * courbure.
	 * Il faut exécuter tout ce qui se passe pendant ce trajet
	 */
	public double distanceTo(AStarCourbeNode node, Speed vitesse)
	{
		node.state.robot.suitArcCourbe(node.getArc(), vitesse.translationalSpeed);
		return node.getArc().getDuree(vitesse.translationalSpeed);
	}

	/**
	 * Fournit le prochain successeur. On suppose qu'il existe
	 * 
	 * @param successeur
	 * @throws InterruptedException
	 */
	public boolean next(AStarCourbeNode successeur) throws MemoryManagerException
	{
		VitesseCourbure v = iterator.next();

		current.state.copyAStarCourbe(successeur.state);

		if(v instanceof VitesseBezier)
		{
			// TODO que signifie cette condition ?
			if(current.getArc() == null)
				return false;

			if(v == VitesseBezier.BEZIER_QUAD && !useCercle)
			{
				ArcCourbeDynamique tmp;
				tmp = bezier.interpolationQuadratique(current.state.robot.getCinematique(), arrivee.getPosition());
				if(tmp == null)
					return false;

				successeur.cameFromArcDynamique = tmp;
			}

			else if(v == VitesseBezier.CIRCULAIRE_VERS_CERCLE && useCercle)
			{
				ArcCourbeDynamique tmp;
				tmp = bezier.trajectoireCirculaireVersCentre(current.state.robot.getCinematique());
				if(tmp == null)
					return false;

				successeur.cameFromArcDynamique = tmp;
			}

			else
				return false;

			/*
			 * else // cette interpolation est réservée à l'arrivée sur un
			 * cercle
			 * {
			 * if(current.state.robot.getCinematique().enMarcheAvant) // on doit
			 * arriver en marche arrière
			 * return false;
			 * ArcCourbeDynamique tmp;
			 * tmp = bezier.interpolationQuadratiqueCercle(
			 * current.state.robot.getCinematique());
			 * if(tmp == null)
			 * return false;
			 * successeur.cameFromArcDynamique = tmp;
			 * }
			 */
		}

		/**
		 * Si on veut ramener le volant au milieu
		 */
		else if(v instanceof VitesseRameneVolant)
		{
			if(current.getArc() == null)
				return false;

			ArcCourbeDynamique tmp = clotho.getTrajectoireRamene(successeur.state.robot.getCinematique(), (VitesseRameneVolant) v);
			if(tmp == null)
				return false;
			successeur.cameFromArcDynamique = tmp;
		}

		/**
		 * Si on veut faire un demi-tour
		 */
		else if(v instanceof VitesseDemiTour)
		{
			if(current.getArc() == null)
				return false;

			successeur.cameFromArcDynamique = clotho.getTrajectoireDemiTour(successeur.state.robot.getCinematique(), (VitesseDemiTour) v);
		}

		/**
		 * Si on fait une interpolation par clothoïde
		 */
		else if(v instanceof VitesseClotho)
		{
			// si le robot est arrêté (début de trajectoire), et que la vitesse
			// n'est pas prévue pour un arrêt ou un rebroussement, on annule
			if(current.getArc() == null && (!((VitesseClotho) v).arret && !((VitesseClotho) v).rebrousse))
				return false;

			clotho.getTrajectoire(successeur.state.robot.getCinematique(), (VitesseClotho) v, successeur.cameFromArcStatique);
		}
		else
			log.critical("Vitesse " + v + " inconnue ! ");

		return true;
	}

	/**
	 * Initialise l'arc manager avec les infos donnée
	 * 
	 * @param directionstrategyactuelle
	 * @param sens
	 * @param arrivee
	 */
	public void configureArcManager(DirectionStrategy directionstrategyactuelle, SensFinal sens, Cinematique arrivee)
	{
		this.sens = sens;
		this.directionstrategyactuelle = directionstrategyactuelle;
		arrivee.copy(this.arrivee);
		useCercle = false;
	}

	/**
	 * Initialise l'arc manager avec le cercle
	 * 
	 * @param directionstrategyactuelle
	 */
	public void configureArcManagerWithCircle(DirectionStrategy directionstrategyactuelle)
	{
		sens = cercle.sens;
		this.directionstrategyactuelle = directionstrategyactuelle;
		useCercle = true;
	}

	/**
	 * Renvoie "true" si cette vitesse est acceptable par rapport à "current".
	 * 
	 * @param vitesse
	 * @return
	 */
	private final boolean acceptable(VitesseCourbure vitesse)
	{
		return vitesse.isAcceptable(current.state.robot.getCinematique(), directionstrategyactuelle, courbureMax);
	}

	/**
	 * Y a-t-il encore une vitesse possible ?
	 * On vérifie ici si certaines vitesses sont interdites (à cause de la
	 * courbure trop grande ou du rebroussement)
	 * 
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
	 * 
	 * @param current
	 * @param directionstrategyactuelle
	 */
	public void reinitIterator(AStarCourbeNode current)
	{
		this.current = current;
		iterator = listeVitesse.listIterator();
	}

	public synchronized Double heuristicCostCourbe(Cinematique c)
	{
		return dstarlite.heuristicCostCourbe(c/* , useCercle */);
	}

	public boolean isArrived(AStarCourbeNode successeur)
	{
		return successeur.getArc() != null && isArrivedPF(successeur.getArc().getLast());
	}
	
	public boolean isArrivedPF(Cinematique successeur)
	{
		if(useCercle)
			return cercle.isArrivedPF(successeur);
		return successeur.getPosition().squaredDistance(arrivee.getPosition()) < 5 && sens.isOK(successeur.enMarcheAvant);
	}

	/**
	 * Permet de savoir si on peut s'ajuster sur un cercle
	 * @param cinematique
	 * @return
	 */
	public boolean isAlmostArrived(Cinematique cinematique)
	{
		if(useCercle)
			return cercle.isAlmostArrived(cinematique);
		return false;
	}

	
	public boolean isArrivedAsser(Cinematique successeur)
	{
		if(useCercle)
			return cercle.isArrivedAsser(successeur);
		return successeur.getPosition().squaredDistance(arrivee.getPosition()) < 25 && sens.isOK(successeur.enMarcheAvant);
	}
	
	/**
	 * heuristique de secours
	 * 
	 * @param cinematique
	 * @return
	 */
	public double heuristicDirect(Cinematique cinematique)
	{
		return 3 * cinematique.getPosition().distanceFast(arrivee.getPosition());
	}

	public void disableObstaclesFixes(CinematiqueObs obs)
	{
		disabledObstaclesFixes.clear();
		for(ObstaclesFixes o : ObstaclesFixes.values())
			if(!o.bordure && o.getObstacle().isColliding(obs.obstacle))
			{
				log.warning("Désactivation de l'obstacle fixe : " + o + ". Obs : " + obs);
				disabledObstaclesFixes.add(o);
			}
		if(!disabledObstaclesFixes.isEmpty())
			dstarlite.disableObstaclesFixes(obs.getPosition());
		else
			dstarlite.disableObstaclesFixes(null);
	}

	public boolean isToCircle()
	{
		return useCercle;
	}

}
