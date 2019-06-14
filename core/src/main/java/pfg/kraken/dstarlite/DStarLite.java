/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import pfg.config.Config;
import pfg.kraken.display.ColorKraken;
import pfg.kraken.display.Display;
import pfg.kraken.display.Layer;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode.SearchDirection;
import pfg.kraken.dstarlite.navmesh.Navmesh;
import pfg.kraken.dstarlite.navmesh.NavmeshEdge;
import pfg.kraken.dstarlite.navmesh.NavmeshNode;
import pfg.kraken.exceptions.NoPathException;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.struct.Kinematic;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XYO;

/**
 * Recherche de chemin avec replanification rapide.
 * Fournit un chemin non courbe sous forme d'une ligne brisée.
 * En fait utilisé comme heuristique par l'AStarCourbe
 * N'est utilisé qu'avec le "vrai" robot et ne peut pas prendre en compte la
 * disparition prochaine d'obstacle
 * TRAVAILLE EN μm !
 * 
 * @author pf
 *
 */

public final class DStarLite
{
	private Navmesh navmesh;
	private boolean graphicHeuristique;
	private DynamicObstacles dynObs;
	private StaticObstacles statObs;
	private XY[] positionArrivee = new XY[2];
	private List<Obstacle> previousObstacles = new ArrayList<Obstacle>(), newObstacles = new ArrayList<Obstacle>();

	private List<DStarLiteNode> overconsistentExpansion = new ArrayList<DStarLiteNode>(); 
	private List<DStarLiteNode> underconsistentExpansion = new ArrayList<DStarLiteNode>(); 
	
	private DStarLiteNode[][] memory;

	private EnhancedPriorityQueue[] openset;
	private DStarLiteNode[] arrivee = new DStarLiteNode[2];
	private Display buffer;
	private long nbPF = 0;
	private boolean printItineraire;

	private Key knew = new Key();
	private Key kold = new Key();
	private Key tmp = new Key();

	/**
	 * Constructeur, rien à dire
	 * 
	 * @param log
	 * @param gridspace
	 */
	public DStarLite(Navmesh navmesh, Display buffer, Config config, DynamicObstacles dynObs, StaticObstacles statObs)
	{
		this.navmesh = navmesh;
		this.buffer = buffer;
		this.dynObs = dynObs;
		this.statObs = statObs;
		
		int nbPoints = navmesh.mesh.nodes.length;
		openset = new EnhancedPriorityQueue[]{new EnhancedPriorityQueue(nbPoints), new EnhancedPriorityQueue(nbPoints)};
		
		memory = new DStarLiteNode[2][nbPoints];
		for(int i = 0; i < nbPoints; i++)
		{
			memory[0][i] = new DStarLiteNode(navmesh.mesh.nodes[i]);
			memory[1][i] = new DStarLiteNode(navmesh.mesh.nodes[i]);
		}

		graphicHeuristique = config.getBoolean(ConfigInfoKraken.GRAPHIC_HEURISTIC);
		printItineraire = config.getBoolean(ConfigInfoKraken.GRAPHIC_D_STAR_LITE);
	}

	/**
	 * Met à jour la clé de s et la renvoie
	 * 
	 * @param s
	 * @return
	 */
	private final Key calcKey(DStarLiteNode s)
	{
		return calcKey(s, s.cle);
	}

	/**
	 * Met à jour la clé donnée en paramètre avec s et la renvoie
	 * 
	 * @param s
	 * @param copy
	 * @return
	 */
	private final Key calcKey(DStarLiteNode s, Key copy)
	{
		copy.set(Math.min(s.g, s.rhs), Math.min(s.g, s.rhs));
		return copy;
	}

	private DStarLiteNode getFromMemory(NavmeshNode gridpoint, SearchDirection dir)
	{
		DStarLiteNode out = memory[dir.ordinal()][gridpoint.nb];
		out.update(nbPF);

		return out;
	}

	/**
	 * Met à jour l'openset selon la cohérence de u
	 * afin de conserver l'invariant : consistent <=> in openset
	 * @param u
	 */
	private final void updateVertex(DStarLiteNode u, SearchDirection dir)
	{
		if(!u.isConsistent())
		{
			u.cle.copy(tmp);
			calcKey(u);
			if(u.inOpenSet)
			{
				assert openset[dir.ordinal()].contains(u);
				if(u.cle.greaterThan(tmp)) // la clé a augmenté
					openset[dir.ordinal()].percolateDown(u);
				else
					openset[dir.ordinal()].percolateUp(u);
			}
			else
			{
				assert !openset[dir.ordinal()].contains(u);
				u.inOpenSet = true;
				openset[dir.ordinal()].add(u);
			}
		}
		else if(u.inOpenSet)
		{
			assert openset[dir.ordinal()].contains(u);
			openset[dir.ordinal()].remove(u);
			u.inOpenSet = false;
		}
	}

	/**
	 * Renvoie true ssi un chemin a été trouvé
	 * @return
	 */
	private void updateHeuristic(SearchDirection dir)
	{
		overconsistentExpansion.clear();
		underconsistentExpansion.clear();
		DStarLiteNode u;
		String str;
		// on traite tous les nœuds une fois pour toute
		while(!openset[dir.ordinal()].isEmpty())
		{
			u = openset[dir.ordinal()].peek();
			assert ((str = checkExpansion(u)) == null) : str;
			assert ((str = checkInvariantRhs()) == null) : str;
			assert ((str = checkInvariantOpenset()) == null) : str;
			assert ((str = checkKey()) == null) : str;

			u.cle.copy(kold);
			calcKey(u, knew);
			if(kold.lesserThan(knew))
			{
				// la clé a augmenté
//				System.out.println("Cas 1");
				knew.copy(u.cle);
				openset[dir.ordinal()].percolateDown(u);
			}
			else if(u.g > u.rhs)
			{
//				System.out.println("Cas 2");
				u.g = u.rhs;
				openset[dir.ordinal()].poll();
				u.inOpenSet = false;

				int nbNeighbours = u.node.getNbNeighbours();
				for(int i = 0; i < nbNeighbours; i++)
				{
					DStarLiteNode s = getFromMemory(u.node.getNeighbour(i), dir);
					s.rhs = Math.min(s.rhs, add(u.node.getNeighbourEdge(i).getDistance(), u.g));
					updateVertex(s, dir);
				}

			}
			else
			{
//				System.out.println("Cas 3");
				int gold = u.g;
				u.g = Integer.MAX_VALUE;
				int nbNeighbours = u.node.getNbNeighbours();
				for(int i = 0; i < nbNeighbours; i++)
				{
					NavmeshNode n = u.node.getNeighbour(i);
					DStarLiteNode s = getFromMemory(n, dir);

					if(s.rhs == add(u.node.getNeighbourEdge(i).getDistance(), gold) && !s.equals(arrivee[dir.ordinal()]))
					{
						s.rhs = Integer.MAX_VALUE;
						int nbNeighbours2 = n.getNbNeighbours();
						for(int j = 0; j < nbNeighbours2; j++)
						{
							NavmeshNode n2 = n.getNeighbour(j);
							DStarLiteNode s2 = getFromMemory(n2, dir);

							s.rhs = Math.min(s.rhs, add(n.getNeighbourEdge(j).getDistance(), s2.g));
						}
					}
					updateVertex(s, dir);
				}
				// Dans la boucle, il faut aussi faire u.
				if(u.rhs == gold && !u.equals(arrivee[dir.ordinal()]))
				{
					u.rhs = Integer.MAX_VALUE;
					int nbNeighbours2 = u.node.getNbNeighbours();
					for(int i = 0; i < nbNeighbours2; i++)
					{
						NavmeshNode n = u.node.getNeighbour(i);
						DStarLiteNode s2 = getFromMemory(n, dir);

						u.rhs = Math.min(u.rhs, add(u.node.getNeighbourEdge(i).getDistance(), s2.g));
					}
				}

				updateVertex(u, dir);
			}

		}
		
		for(int i = 0; i < memory[dir.ordinal()].length; i++)
			if(memory[dir.ordinal()][i].nbPF == nbPF) // on met d'abord à jour tous les meilleurs voisins
				if(memory[dir.ordinal()][i] != arrivee[dir.ordinal()] && memory[dir.ordinal()][i].rhs != Integer.MAX_VALUE)
					memory[dir.ordinal()][i].bestVoisin = getBestVoisin(memory[dir.ordinal()][i].node, dir);

		for(int i = 0; i < memory[dir.ordinal()].length; i++)
			if(memory[dir.ordinal()][i].nbPF == nbPF) // ceux qui ne sont pas à jour auront de toute façon une heuristique nulle
				updateOrientationHeuristic(memory[dir.ordinal()][i], dir);
	}

	/**
	 * Calcule un nouvel itinéraire.
	 * 
	 * @param arrivee (un Vec2)
	 * @param depart (un Vec2)
	 * @throws NoPathException 
	 */
	public boolean computeNewPath(XY depart, XY arrivee, boolean checkStartPoint, boolean checkEndPoint, boolean bidirect) throws NoPathException
	{
		List<Obstacle> obs = statObs.getObstacles();
		for(Obstacle o : obs)
			if(checkStartPoint && o.isInObstacle(depart))
				throw new NoPathException("Starting point in obstacle "+o);
			else if(checkEndPoint && o.isInObstacle(arrivee))
				throw new NoPathException("Finish point in obstacle "+o);
		
		changeGoal(arrivee, depart, bidirect);
		updateObstacles();
		updateHeuristic(SearchDirection.FORWARD);
		updateHeuristic(SearchDirection.BACKWARD);
		return getFromMemory(navmesh.getNearestAvailable(depart), SearchDirection.FORWARD).rhs != Integer.MAX_VALUE;
	}

	/**
	 * Met à jour la destination
	 * 
	 * @param positionArrivee
	 * @throws NoPathException 
	 */
	private synchronized void changeGoal(XY positionArrivee, XY positionDepart, boolean bidirect) throws NoPathException
	{
		if(!statObs.isInsideSearchDomain(positionArrivee))
			throw new NoPathException("The goal is outside the search domain !");
		
		nbPF++;

		int max = bidirect ? 2 : 1;
		
		for(int i = 0; i < max; i++)
		{
			SearchDirection dir = SearchDirection.values()[i];
			XY pos;
			if(i == 0)
				pos = positionArrivee;
			else
				pos = positionDepart;
			this.positionArrivee[i] = pos;
			arrivee[i] = getFromMemory(navmesh.getNearest(pos), dir);
			arrivee[i].rhs = 0;
			arrivee[i].cle.set(0, 0);
	
			openset[i].clear();
			openset[i].add(arrivee[i]);
			arrivee[i].inOpenSet = true;
		}
	}

	/**
	 * Met à jour le pathfinding
	 */
	public synchronized void updateObstacles()
	{
		Iterator<Obstacle> iter = dynObs.getCurrentDynamicObstacles();		
		while(iter.hasNext())
			newObstacles.add(iter.next());
		
		for(NavmeshEdge e: navmesh.mesh.edges)
		{
			boolean previousState = e.isBlocked();
			
			e.updateState(newObstacles);
			
			// Nothing change
			if(e.isBlocked() == previousState)
				continue;
			
//			System.out.println("État edge a changé, bloqué = "+e.isBlocked()+": "+e);

			if(!e.isBlocked())
			{
				for(SearchDirection dir : SearchDirection.values())
					for(int k = 0; k < 2; k++)
					{
						DStarLiteNode u = getFromMemory(e.points[k], dir);
						DStarLiteNode v = getFromMemory(e.points[(k+1)%2], dir);
		
						u.rhs = Math.min(u.rhs, add(e.getDistance(), v.g));
						updateVertex(u, dir);
					}
			}
			else
			{
				for(SearchDirection dir : SearchDirection.values())
					for(int k = 0; k < 2; k++)
					{
						DStarLiteNode u = getFromMemory(e.points[k], dir);
						DStarLiteNode v = getFromMemory(e.points[(k+1)%2], dir);
		
						// l'ancienne distance est la distance statique car c'est un
						// ajout d'obstacle
						if(u.rhs == add(e.getUnblockedDistance(), v.g) && !u.equals(arrivee[dir.ordinal()]))
						{
							u.rhs = Integer.MAX_VALUE;
							int nbNeighbours = u.node.getNbNeighbours();
							for(int i = 0; i < nbNeighbours; i++)
							{
								NavmeshNode s = u.node.getNeighbour(i);
								u.rhs = Math.min(u.rhs, add(u.node.getNeighbourEdge(i).getDistance(), getFromMemory(s, dir).g));
							}
						}
						updateVertex(u, dir);
					}
				
			}
		}

		// Inversion des deux listes
		List<Obstacle> tmp = previousObstacles;
		previousObstacles = newObstacles;
		newObstacles = tmp;
		newObstacles.clear();

		String str;
		assert ((str = checkInvariantRhs()) == null) : str;
		assert ((str = checkInvariantOpenset()) == null) : str;
	}

	private List<XYO> trajet = new ArrayList<XYO>();

	public synchronized List<XYO> itineraireBrut(XY depart)
	{
		trajet.clear();
		
		DStarLiteNode node = getFromMemory(navmesh.getNearest(depart), SearchDirection.FORWARD);
		DStarLiteNode min = null;
		int coutMin;

		assert node.rhs != Integer.MAX_VALUE : "No path found !";

		String str;
		assert ((str = checkInvariantRhs()) == null) : str;
		assert ((str = checkInvariantOpenset()) == null) : str;
		
		while(!node.equals(arrivee[SearchDirection.FORWARD.ordinal()]))
		{
			assert node.heuristiqueOrientation != null;
			XYO xyo = new XYO(node.node.position.clone(), (double)node.heuristiqueOrientation);
			
			// Le noeud de départ peut exceptionnellement être inconsistent
			assert node.isConsistent() : "A node in the path is not consistent !";
			assert !trajet.contains(xyo) : "Cyclic path !";
			
			trajet.add(xyo);

			coutMin = Integer.MAX_VALUE;

			int nbNeighbours = node.node.getNbNeighbours();
			int indexMin = -1;
			min = null;
			for(int i = 0; i < nbNeighbours; i++)
			{
				DStarLiteNode s = getFromMemory(node.node.getNeighbour(i), SearchDirection.FORWARD);
				int coutTmp = add(node.node.getNeighbourEdge(i).getDistance(), s.g);
				if(coutTmp < coutMin)
				{
					coutMin = coutTmp;
					min = s;
					indexMin = i;
				}
			}
			
			assert min.g < node.g : "The distance to the goal increased !";

			if(printItineraire)
				node.node.getNeighbourEdge(indexMin).highlight(true);
			
			node = min;
		}
		
		trajet.add(new XYO(arrivee[SearchDirection.FORWARD.ordinal()].node.position.clone(), 0)); // pas d'heuristique d'orientation sur le point d'arrivée

		return trajet;

	}

	public Double heuristicDistance(Kinematic c, boolean ignoreObstacle, SearchDirection dir)
	{
		NavmeshNode pos;
		if(ignoreObstacle)
			pos = navmesh.getNearestAvailable(c.getPosition());			
		else
		{
			if(!statObs.isInsideSearchDomain(c.getPosition()))
				return null;
			pos = navmesh.getNearest(c.getPosition());
		}
		
		// si on est arrivé… on est arrivé.
		if(pos.equals(arrivee[dir.ordinal()].node))
			return 0.;

		DStarLiteNode premier = getFromMemory(pos, dir);
		
		if(premier == arrivee[dir.ordinal()])
			return c.getPosition().distanceFast(positionArrivee[dir.ordinal()]);
		
		if(premier.heuristiqueOrientation == null)
			return null;
		
		NavmeshNode voisin = pos.getNeighbour(premier.bestVoisin);
		double erreurDistance = c.getPosition().distanceFast(voisin.position) + (memory[dir.ordinal()][voisin.nb].rhs) / 1000. + arrivee[dir.ordinal()].node.position.distanceFast(positionArrivee[dir.ordinal()]);
		
		return erreurDistance;	
	}
	
	/**
	 * Renvoie l'heuristique au A* courbe.
	 * L'heuristique est une distance en mm
	 * 
	 * @param c
	 * @return
	 */
	public Double heuristicOrientation(Kinematic c, boolean ignoreObstacle, SearchDirection dir)
	{
		NavmeshNode pos;
		if(ignoreObstacle)
			pos = navmesh.getNearestAvailable(c.getPosition());			
		else
		{
			if(!statObs.isInsideSearchDomain(c.getPosition()))
				return null;
			pos = navmesh.getNearest(c.getPosition());
		}
		
		// si on est arrivé… on est arrivé.
		if(pos.equals(arrivee[dir.ordinal()].node))
			return 0.;

		DStarLiteNode premier = getFromMemory(pos, dir);
		
		if(premier == arrivee[dir.ordinal()])
			return c.getPosition().distanceFast(positionArrivee[dir.ordinal()]);
		
		if(premier.heuristiqueOrientation == null)
			return null;
		
//		NavmeshNode voisin = pos.getNeighbour(premier.bestVoisin);
//		double erreurDistance = c.getPosition().distanceFast(voisin.position) + (memory[voisin.nb].rhs) / 1000. + arrivee.node.position.distanceFast(positionArrivee);
		
		double erreurOrientation = 0;
//		if(coeffAngleError > 0)
//		{
		double orientationOptimale = premier.heuristiqueOrientation;

		// l'orientation est vérifiée modulo 2*pi : aller vers la destination ou
		// s'en éloigner sont différenciés
		erreurOrientation = (c.orientationGeometrique - orientationOptimale) % (2 * Math.PI);
		if(erreurOrientation > Math.PI)
			erreurOrientation -= 2 * Math.PI;

		erreurOrientation = Math.abs(erreurOrientation);
//		}
		// il faut toujours majorer la vraie distance, afin de ne pas chercher
		// tous les trajets possibles…
		// le poids de l'erreur d'orientation doit rester assez faible. Car
		// vouloir trop coller à l'orientation, c'est risquer d'avoir une
		// courbure impossible…
		return erreurOrientation;		
	}
	
	/**
	 * Renvoie l'indice du meilleur voisin, i.e. le plus proche de l'arrivée
	 * @param node
	 * @return
	 */
	private int getBestVoisin(NavmeshNode node, SearchDirection dir)
	{
		int nbVoisins = node.getNbNeighbours();
		assert nbVoisins > 0; // un nœud a forcément au moins un voisin
		int bestVoisin = 0;
		int bestVoisinDistance = add(getFromMemory(node.getNeighbour(0), dir).rhs, node.getNeighbourEdge(0).getDistance());
		
		for(int i = 1; i < nbVoisins; i++)
		{
			NavmeshNode voisin = node.getNeighbour(i);
			int candidatDistance = add(getFromMemory(voisin, dir).rhs, node.getNeighbourEdge(i).getDistance());
			assert candidatDistance >= 0 : "Distance négative ! "+candidatDistance;
			if(candidatDistance < bestVoisinDistance)
			{
				bestVoisin = i;
				bestVoisinDistance = candidatDistance;
			}
		}
		assert bestVoisinDistance != Integer.MAX_VALUE;
		return bestVoisin;
	}
	
	/**
	 * Fournit une heuristique de l'orientation à prendre en ce point
	 * 
	 * @param p
	 * @return
	 */
	private void updateOrientationHeuristic(DStarLiteNode n, SearchDirection dir)
	{
		if(n == arrivee[dir.ordinal()]) // cas particulier
			n.heuristiqueOrientation = null;
		else if(n.rhs == Integer.MAX_VALUE) // pas de chemin
			n.heuristiqueOrientation = null;
		else
		{
			int nbBestVoisin = n.bestVoisin;
			assert nbBestVoisin == getBestVoisin(n.node, dir);
			NavmeshNode bestVoisin = n.node.getNeighbour(nbBestVoisin); 
			if(bestVoisin.equals(arrivee[dir.ordinal()].node))
				n.heuristiqueOrientation = n.node.getNeighbourEdge(nbBestVoisin).getOrientation(n.node);
			else
			{
				int nbBestVoisinDuVoisin = memory[dir.ordinal()][bestVoisin.nb].bestVoisin; //getBestVoisin(bestVoisin);
				double angle1 = n.node.getNeighbourEdge(nbBestVoisin).getOrientation(n.node);
				double angle2 = bestVoisin.getNeighbourEdge(nbBestVoisinDuVoisin).getOrientation(bestVoisin);
				double diff = (( angle1 - angle2 + 3 * Math.PI ) % (2 * Math.PI)) - Math.PI;
				double mean = (2 * Math.PI + angle2 + (diff / 2 )) % (2 * Math.PI);
				n.heuristiqueOrientation = mean;
			}
			if(graphicHeuristique)
			{
				if(dir == SearchDirection.FORWARD)
					buffer.addTemporaryPrintable(n, ColorKraken.FORWARD_HEURISTIQUE.color, Layer.MIDDLE.layer);
				else
					buffer.addTemporaryPrintable(n, ColorKraken.BACKWARD_HEURISTIQUE.color, Layer.MIDDLE.layer);
			}
		}
	}

	/**
	 * Somme en faisant attention aux valeurs infinies
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static final int add(int a, int b)
	{
		if(a == Integer.MAX_VALUE || b == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return a + b;
	}
	
	/* Assertions below */

	/**
	 * D* lite must expand, at most, each node twice : at most once if is overconsistent
	 * and at most once if it is underconsistent
	 * @return
	 */
	private String checkExpansion(DStarLiteNode u)	
	{
		assert !u.isConsistent() : u.g+" "+u.rhs;
		if(u.g > u.rhs)
		{
			if(overconsistentExpansion.contains(u))
				return "An overconsistent node has been expanded twice : "+u;
			overconsistentExpansion.add(u);
		}
		else
		{
			if(underconsistentExpansion.contains(u))
				return "An underconsistent node has been expanded twice : "+u;
			underconsistentExpansion.add(u);
		}
		return null;
	}

	private String checkKey()
	{
		for(SearchDirection dir : SearchDirection.values())
			for(int i = 0; i < memory[dir.ordinal()].length; i++)
			{
				DStarLiteNode n = getFromMemory(memory[dir.ordinal()][i].node, dir);
				if(n.inOpenSet && !n.cle.isEqualsTo(calcKey(n, tmp)))
					return "A key is wrong ! "+n.cle+" "+tmp;
			}
		return null;
	}
	
	private String checkInvariantOpenset()
	{
		for(SearchDirection dir : SearchDirection.values())
			for(int i = 0; i < memory[dir.ordinal()].length; i++)
			{
				DStarLiteNode n = getFromMemory(memory[dir.ordinal()][i].node, dir);
				if(n.inOpenSet != openset[dir.ordinal()].contains(n))
				{
					if(n.inOpenSet)
						return "inOpenSet = true mais n'y appartient pas !";
					else
						return "inOpenSet = false mais y appartient !";
				}
				if(n.inOpenSet && n.isConsistent())
					return "Node "+n+" in the openset but consistent !";
				if(!n.inOpenSet && !n.isConsistent())
					return "Node "+n+" not in the openset but inconsistent !";
			}
		return null;
	}
	
	private String checkInvariantRhs()
	{
		for(SearchDirection dir : SearchDirection.values())
			for(int i = 0; i < memory[dir.ordinal()].length; i++)
			{
				DStarLiteNode n = getFromMemory(memory[dir.ordinal()][i].node, dir);
				if(n == arrivee[dir.ordinal()])
				{
					if(arrivee[dir.ordinal()].rhs != 0)
						return "rhs de l'arrivée non nul ! "+arrivee[dir.ordinal()].rhs+" "+dir;
				}
				else
				{
					int best = Integer.MAX_VALUE;
					NavmeshNode s = n.node;
					for(int j = 0; j < s.getNbNeighbours(); j++)
					{
						int candidat = add(getFromMemory(s.getNeighbour(j), dir).g, s.getNeighbourEdge(j).getDistance());
						if(candidat < best)
							best = candidat;
					}
					if(n.rhs != best)
						return "rhs invariant broken ! rhs = "+n.rhs+", min = "+best+" "+n+" "+dir;
				}
			}
		return null;
	}
	
}
