/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.LogCategoryKraken;
import pfg.kraken.SeverityCategoryKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.tentacles.types.BezierTentacle;
import pfg.kraken.astar.tentacles.types.ClothoTentacle;
import pfg.kraken.astar.tentacles.types.StraightingTentacle;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.astar.tentacles.types.TurnoverTentacle;
import pfg.kraken.dstarlite.DStarLite;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XY_RW;
import pfg.log.Log;

/**
 * Réalise des calculs pour l'A* courbe.
 * 
 * @author pf
 *
 */

public class TentacleManager
{
	protected Log log;
	private ClothoidesComputer clotho;
	private BezierComputer bezier;
//	private CircleComputer circlecomputer;
	private AStarNode current;
	private DStarLite dstarlite;
	private DynamicObstacles dynamicObs;
	private double courbureMax;
	private int tempsArret;
	private StaticObstacles fixes;
	
	private DirectionStrategy directionstrategyactuelle;
	private XY_RW arrivee = new XY_RW();
	private List<TentacleType> listeVitesse = new ArrayList<TentacleType>();
	private ListIterator<TentacleType> iterator = listeVitesse.listIterator();
//	private List<StaticObstacles> disabledObstaclesFixes = new ArrayList<StaticObstacles>();

	public TentacleManager(Log log, StaticObstacles fixes, ClothoidesComputer clotho, DStarLite dstarlite, BezierComputer bezier, Config config, DynamicObstacles dynamicObs)
	{
//		this.circlecomputer = circlecomputer;
		this.fixes = fixes;
		this.dynamicObs = dynamicObs;
		this.bezier = bezier;
		this.log = log;
		this.clotho = clotho;
		this.dstarlite = dstarlite;

		for(TentacleType v : ClothoTentacle.values())
			listeVitesse.add(v);
		for(TentacleType v : BezierTentacle.values())
			listeVitesse.add(v);
//		for(TentacleType v : TurnoverTentacle.values())
//			listeVitesse.add(v);
		
		// TODO les StraightingTentacle seront fonctionnellement remplacés par le lissage en postprocess
//		for(TentacleType v : StraightingTentacle.values())
//			listeVitesse.add(v);

		courbureMax = config.getDouble(ConfigInfoKraken.MAX_CURVATURE);
		tempsArret = config.getInt(ConfigInfoKraken.STOP_DURATION);
		coins[0] = fixes.getBottomLeftCorner();
		coins[2] = fixes.getTopRightCorner();
		coins[1] = new XY(coins[0].getX(), coins[2].getY());
		coins[3] = new XY(coins[2].getX(), coins[0].getY());
	}

	private List<RectangularObstacle> ombresRobot = new ArrayList<RectangularObstacle>();
	private XY[] coins = new XY[4];
	
	/**
	 * Retourne faux si un obstacle est sur la route
	 * 
	 * @param node
	 * @return
	 * @throws FinMatchException
	 */
	public boolean isReachable(AStarNode node)
	{
		// le tout premier nœud n'a pas de parent
		if(node.parent == null)
			return true;

		/**
		 * On agrège les obstacles en un seul pour simplifier l'écriture des
		 * calculs
		 */
		ombresRobot.clear();
		for(int i = 0; i < node.getArc().getNbPoints(); i++)
			ombresRobot.add(node.getArc().getPoint(i).obstacle);

		// On vérifie la collision avec les murs
		for(RectangularObstacle obs : ombresRobot)
			for(int i = 0; i < 4; i++)
				if(obs.isColliding(coins[i], coins[(i+1)&3]))
					return false;
		
		// Collision avec un obstacle fixe?
		for(Obstacle o : fixes.getObstacles())
			for(RectangularObstacle obs : ombresRobot)
				if(/*!disabledObstaclesFixes.contains(o) && */o.isColliding(obs))
				{
					// log.debug("Collision avec "+o);
					return false;
				}

		// Collision avec un obstacle de proximité ?

		try {
			Iterator<Obstacle> iter = dynamicObs.getFutureDynamicObstacles(0); // TODO date !
			while(iter.hasNext())
			{
				Obstacle n = iter.next();
				for(RectangularObstacle obs : ombresRobot)
					if(n.isColliding(obs))
					{
						// log.debug("Collision avec un obstacle de proximité.");
						return false;
					}
			}
		} catch(NullPointerException e)
		{
			log.write(e.toString(), SeverityCategoryKraken.CRITICAL, LogCategoryKraken.PF);
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
	public double distanceTo(AStarNode node, double vitesseMax)
	{
		double duration = node.getArc().getDuree(vitesseMax, tempsArret);
		node.robot.suitArcCourbe(node.getArc(), duration);
		return duration;
	}

	/**
	 * Fournit le prochain successeur. On suppose qu'il existe
	 * 
	 * @param successeur
	 * @throws InterruptedException
	 */
	public boolean next(AStarNode successeur)
	{
		TentacleType v = iterator.next();

		current.robot.copy(successeur.robot);

		if(v instanceof BezierTentacle)
		{
			// TODO que signifie cette condition ?
			if(current.getArc() == null)
				return false;

			if(v == BezierTentacle.BEZIER_QUAD)
			{
				DynamicTentacle tmp;
				tmp = bezier.quadraticInterpolationXYOC2XY(current.robot.getCinematique(), arrivee);
				if(tmp == null)
					return false;

				successeur.cameFromArcDynamique = tmp;
			}

/*			else if(v == VitesseBezier.CIRCULAIRE_VERS_CERCLE && useCercle)
			{
				ArcCourbeDynamique tmp;
				tmp = circlecomputer.trajectoireCirculaireVersCentre(current.robot.getCinematique());
				if(tmp == null)
					return false;

				successeur.cameFromArcDynamique = tmp;
			}*/

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
		else if(v instanceof StraightingTentacle)
		{
			if(current.getArc() == null)
				return false;

			DynamicTentacle tmp = clotho.getTrajectoireRamene(successeur.robot.getCinematique(), (StraightingTentacle) v);
			if(tmp == null)
				return false;
			successeur.cameFromArcDynamique = tmp;
		}

		/**
		 * Si on veut faire un demi-tour
		 */
		else if(v instanceof TurnoverTentacle)
		{
			if(current.getArc() == null)
				return false;

			successeur.cameFromArcDynamique = clotho.getTrajectoireDemiTour(successeur.robot.getCinematique(), (TurnoverTentacle) v);
		}

		/**
		 * Si on fait une interpolation par clothoïde
		 */
		else if(v instanceof ClothoTentacle)
		{
			// si le robot est arrêté (début de trajectoire), et que la vitesse
			// n'est pas prévue pour un arrêt ou un rebroussement, on annule
			if(current.getArc() == null && (!((ClothoTentacle) v).arret && !((ClothoTentacle) v).rebrousse))
				return false;

			clotho.getTrajectoire(successeur.robot.getCinematique(), (ClothoTentacle) v, successeur.cameFromArcStatique);
		}
		else
			assert false;

		return true;
	}

	/**
	 * Initialise l'arc manager avec les infos donnée
	 * 
	 * @param directionstrategyactuelle
	 * @param sens
	 * @param arrivee
	 */
	public void configureArcManager(DirectionStrategy directionstrategyactuelle, XY arrivee)
	{
		this.directionstrategyactuelle = directionstrategyactuelle;
		arrivee.copy(this.arrivee);
	}

	/**
	 * Initialise l'arc manager avec le cercle
	 * 
	 * @param directionstrategyactuelle
	 */
	public void configureArcManagerWithCircle(DirectionStrategy directionstrategyactuelle)
	{
		this.directionstrategyactuelle = directionstrategyactuelle;
	}

	/**
	 * Renvoie "true" si cette vitesse est acceptable par rapport à "current".
	 * 
	 * @param vitesse
	 * @return
	 */
	private final boolean acceptable(TentacleType vitesse)
	{
		// TODO faire du tri avec une heuristique ! exemple :
		// - voir si le dernier point est dans un obstacle ou pas
		// - voir si l'orientation heuristique préconise certains mouvement (ne pas faire de marche arrière, voire tourner à gauche / à droite)
		return vitesse.isAcceptable(current.robot.getCinematique(), directionstrategyactuelle, courbureMax);
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
	public void reinitIterator(AStarNode current)
	{
		this.current = current;
		iterator = listeVitesse.listIterator();
	}

	public synchronized Double heuristicCostCourbe(Cinematique c)
	{
		return dstarlite.heuristicCostCourbe(c/* , useCercle */);
	}

	public boolean isArrived(AStarNode successeur)
	{
		return successeur.getArc() != null && isArrivedPF(successeur.getArc().getLast());
	}
	
	public boolean isArrivedPF(Cinematique successeur)
	{
		return successeur.getPosition().squaredDistance(arrivee) < 5;
	}
	
	public boolean isArrivedAsser(Cinematique successeur)
	{
		return successeur.getPosition().squaredDistance(arrivee) < 25;
	}
	
	/**
	 * heuristique de secours
	 * 
	 * @param cinematique
	 * @return
	 */
	public double heuristicDirect(Cinematique cinematique)
	{
		return 3 * cinematique.getPosition().distanceFast(arrivee);
	}

/*	public void setTentacle(List<TentacleType> tentacleTypesUsed)
	{
		// TODO Auto-generated method stub
	}*/

/*	public void disableObstaclesFixes(boolean symetrie, CinematiqueObs obs)
	{
		disabledObstaclesFixes.clear();
		ObstaclesFixes depart;
		boolean vide = true;
		if(symetrie)
			depart = ObstaclesFixes.ZONE_DEPART_GAUCHE_CENTRE;
		else
			depart = ObstaclesFixes.ZONE_DEPART_DROITE_CENTRE;
		
		for(ObstaclesFixes o : ObstaclesFixes.values())
			if(!o.bordure && o.getObstacle().isColliding(obs.obstacle))
			{
				vide = false;
				log.warning("Désactivation de l'obstacle fixe : " + o + ". Obs : " + obs);
				disabledObstaclesFixes.add(o);
			}
		
		if(!disabledObstaclesFixes.contains(depart))
			disabledObstaclesFixes.add(depart);
		
		if(!vide)
			dstarlite.disableObstaclesFixes(obs.getPosition(), depart.getObstacle());
		else
			dstarlite.disableObstaclesFixes(null, depart.getObstacle());
	}*/

}
