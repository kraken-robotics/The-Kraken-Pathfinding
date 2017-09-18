/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite;

import java.util.ArrayList;
import java.util.List;
import pfg.config.Config;
import pfg.graphic.PrintBuffer;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.dstarlite.navmesh.Navmesh;
import pfg.kraken.dstarlite.navmesh.NavmeshEdge;
import pfg.kraken.dstarlite.navmesh.NavmeshNode;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.utils.XY;
import pfg.log.Log;

/**
 * Recherche de chemin avec replanification rapide.
 * Fournit un chemin non courbe sous forme d'une ligne brisée.
 * En fait utilisé comme heuristique par l'AStarCourbe
 * N'est utilisé qu'avec le "vrai" robot et ne peut pas prendre en compte la
 * disparition prochaine d'obstacle
 * 
 * @author pf
 *
 */

public class DStarLite
{
	protected Log log;
	private Navmesh navmesh;
	private boolean graphicDStarLite, graphicHeuristique;
	private boolean rechercheEnCours = false;

	private DStarLiteNode[] memory;

	private EnhancedPriorityQueue openset;
	private int km;
	private DStarLiteNode arrivee;
	private DStarLiteNode depart;
	private NavmeshNode lastDepart;
	private PrintBuffer buffer;
	private long nbPF = 0;

	private Cle knew = new Cle();
	private Cle kold = new Cle();
	private Cle tmp = new Cle();

	/**
	 * Constructeur, rien à dire
	 * 
	 * @param log
	 * @param gridspace
	 */
	public DStarLite(Log log, Navmesh navmesh, PrintBuffer buffer, Config config)
	{
		this.log = log;
		this.navmesh = navmesh;
		this.buffer = buffer;
		int nbPoints = navmesh.mesh.nodes.length;
		openset = new EnhancedPriorityQueue(nbPoints);
		
		memory = new DStarLiteNode[nbPoints];
		for(int i = 0; i < nbPoints; i++)
			memory[i] = new DStarLiteNode(navmesh.mesh.nodes[i]);

		graphicDStarLite = config.getBoolean(ConfigInfoKraken.GRAPHIC_D_STAR_LITE);
		graphicHeuristique = config.getBoolean(ConfigInfoKraken.GRAPHIC_HEURISTIC);
	}

	/**
	 * Met à jour la clé de s et la renvoie
	 * 
	 * @param s
	 * @return
	 */
	private final Cle calcKey(DStarLiteNode s)
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
	private final Cle calcKey(DStarLiteNode s, Cle copy)
	{
		copy.set(add(add(Math.min(s.g, s.rhs), distanceHeuristique(s.node)), km), Math.min(s.g, s.rhs));
		return copy;
	}

	/**
	 * Récupère un nœud dans la mémoire. Ce nœud doit être à jour.
	 * 
	 * @param gridpoint
	 * @return
	 */
	private DStarLiteNode getFromMemoryUpdated(NavmeshNode gridpoint)
	{
		// Il peut arriver qu'on sorte de la grille
		if(gridpoint == null)
			return null;

		DStarLiteNode out = memory[gridpoint.nb];

		out.update(nbPF);
		updateStart(out);
		return out;
	}

	private DStarLiteNode getFromMemory(NavmeshNode gridpoint)
	{
		// Il peut arriver qu'on sorte de la grille
		assert gridpoint != null;
		if(gridpoint == null)
			return null;

		DStarLiteNode out = memory[gridpoint.nb];
		out.update(nbPF);

		return out;
	}

	private final void updateVertex(DStarLiteNode u)
	{
//		if(graphicDStarLite)
//			gridspace.setColor(u.gridpoint, Couleur.BLEU);

		if(u.g != u.rhs)
		{
			u.cle.copy(tmp);
			calcKey(u);
			if(u.inOpenSet)
			{
				// if(tmp.compare(u.cle) < 0)
				if(u.cle.greaterThan(tmp)) // la clé a augmenté
					openset.percolateDown(u);
				else
					openset.percolateUp(u);
			}
			else
			{
				u.inOpenSet = true;
				openset.add(u);
			}
		}
		else if(u.inOpenSet)
		{
			openset.remove(u);
			u.inOpenSet = false;
		}
	}

	private void computeShortestPath()
	{
		DStarLiteNode u;
		while(!openset.isEmpty() && ((u = openset.peek()).cle.lesserThan(calcKey(depart, tmp)) || depart.rhs > depart.g))
		{
			u.cle.copy(kold);
			// Cle kold = u.cle.clone();
			calcKey(u, knew);
			if(kold.lesserThan(knew))
			{
				// la clé a augmenté
				// log.debug("Cas 1");
				knew.copy(u.cle);
				openset.percolateDown(u);
//				if(graphicDStarLite)
//					gridspace.setColor(u.gridpoint, Couleur.BLEU);
			}
			else if(u.g > u.rhs)
			{
				// log.debug("Cas 2");
				u.g = u.rhs;
				openset.poll();
				u.inOpenSet = false;
//				if(graphicDStarLite)
//					gridspace.setColor(u.gridpoint, Couleur.BLEU);
				int nbNeighbours = u.node.getNbNeighbours();
				for(int i = 0; i < nbNeighbours; i++)
				{
					NavmeshNode n = u.node.getNeighbour(i);
					DStarLiteNode s = getFromMemory(n);

					if(s == null)
						continue;

					s.rhs = Math.min(s.rhs, add(u.node.getNeighbourEdge(i).getDistance(), u.g));
					updateVertex(s);
				}
			}
			else
			{
				// log.debug("Cas 3");
				int gold = u.g;
				u.g = Integer.MAX_VALUE;
				int nbNeighbours = u.node.getNbNeighbours();
				for(int i = 0; i < nbNeighbours; i++)
				{
					NavmeshNode n = u.node.getNeighbour(i);
					DStarLiteNode s = getFromMemory(n);
					if(s == null)
						continue;

					if(s.rhs == add(u.node.getNeighbourEdge(i).getDistance(), gold) && !s.node.equals(arrivee.node))
					{
						s.rhs = Integer.MAX_VALUE;
						int nbNeighbours2 = n.getNbNeighbours();
						for(int j = 0; j < nbNeighbours2; j++)
						{
							NavmeshNode n2 = n.getNeighbour(i);
							DStarLiteNode s2 = getFromMemory(n2);
							if(s2 == null)
								continue;

							s.rhs = Math.min(s.rhs, add(n.getNeighbourEdge(j).getDistance(), s2.g));
						}
					}
					updateVertex(s);
				}
				// Dans la boucle, il faut aussi faire u.
				if(u.rhs == gold && !u.node.equals(arrivee.node))
				{
					u.rhs = Integer.MAX_VALUE;
					int nbNeighbours2 = u.node.getNbNeighbours();
					for(int i = 0; i < nbNeighbours2; i++)
					{
						NavmeshNode n = u.node.getNeighbour(i);
						DStarLiteNode s2 = getFromMemory(n);
						if(s2 == null)
							continue;

						u.rhs = Math.min(u.rhs, add(u.node.getNeighbourEdge(i).getDistance(), s2.g));
					}
				}

				// de toute façon, comme u sera forcément retiré de la liste
				// dans updateVertex… autant le faire efficacement ici
				// openset.poll();
				// u.inOpenSet = false;
				updateVertex(u);
			}

		}

		// if(depart.rhs == Integer.MAX_VALUE)
		// throw new PathfindingException("Aucun chemin n'a été trouvé");
	}

	/**
	 * Calcule un nouvel itinéraire.
	 * 
	 * @param arrivee (un Vec2)
	 * @param depart (un Vec2)
	 */
	public void computeNewPath(XY depart, XY arrivee)
	{
		rechercheEnCours = true;
//		if(graphicDStarLite)
//			gridspace.reinitGraphicGrid();

		updateGoalAndStart(depart, arrivee);
		updateObstacles();
/*
		if(graphicDStarLite)
		{
			gridspace.setColor(this.arrivee.gridpoint, Couleur.JAUNE);
			gridspace.setColor(this.depart.gridpoint, Couleur.VIOLET);
		}
*/
	}

	/**
	 * Heuristique (distance octile) entre le point de départ et ce gridpoint
	 * 
	 * @param gridpoint
	 * @return
	 */
	private final int distanceHeuristique(NavmeshNode gridpoint)
	{
		return (int) (depart.node.position.distanceOctile(gridpoint.position) * 1.2);
	}

	/**
	 * Met à jour la destination
	 * 
	 * @param positionArrivee
	 */
	public synchronized void updateGoalAndStart(XY positionRobot, XY positionArrivee)
	{
		nbPF++;
		km = 0;

		depart = getFromMemory(navmesh.getNearest(positionRobot));
		lastDepart = depart.node;

		this.arrivee = getFromMemory(navmesh.getNearest(positionArrivee));
		this.arrivee.rhs = 0;
		this.arrivee.cle.set(distanceHeuristique(this.arrivee.node), 0);

		openset.clear();
		openset.add(this.arrivee);

		computeShortestPath();
	}

	public synchronized void updateStart(XY positionRobot)
	{
		updateStart(navmesh.getNearest(positionRobot));
	}

	private synchronized final void updateStart(NavmeshNode p)
	{
		updateStart(getFromMemory(p));
	}
	/**
	 * Met à jour la position actuelle du robot
	 * 
	 * @param positionRobot
	 */
	private synchronized final void updateStart(DStarLiteNode p)
	{
		// p is inconsistent iff p is in the open set
		if(p.inOpenSet)
		{
			depart = p;
			km += distanceHeuristique(lastDepart);
			lastDepart = depart.node;
	
			computeShortestPath();
		}
	}

	/**
	 * Met à jour le pathfinding
	 */
	public synchronized void updateObstacles()
	{
		for(NavmeshEdge e: navmesh.mesh.edges)
		{
			if(!e.hasChanged())
				continue;
			
			if(e.isBlocked())
			{
				// Ajout d'un obstacle : le coût augmente
				DStarLiteNode u = getFromMemory(e.points[0]);
				DStarLiteNode v = getFromMemory(e.points[1]);

				// l'ancienne distance est la distance statique car c'est un
				// ajout d'obstacle
				if(u.rhs == v.g && !u.equals(arrivee))
				{
					u.rhs = Integer.MAX_VALUE;
					int nbNeighbours = u.node.getNbNeighbours();
					for(int i = 0; i < nbNeighbours; i++)
					{
						NavmeshNode n = u.node.getNeighbour(i);
						u.rhs = Math.min(u.rhs, add(u.node.getNeighbourEdge(i).getDistance(), getFromMemory(n).g));
					}
				}
				updateVertex(u);
				
			}
			else
			{
				// Disparition d'un obstacle : le coût baisse
				DStarLiteNode u = getFromMemory(e.points[0]);
				DStarLiteNode v = getFromMemory(e.points[1]);

				u.rhs = Math.min(u.rhs, v.g);
				updateVertex(u);
			}
		}
		computeShortestPath();
	}

	/**
	 * Utilisé pour l'affichage et le debug
	 * 
	 * @return
	 */
	public synchronized List<XY> itineraireBrut()
	{
		List<XY> trajet = new ArrayList<XY>();

		// log.debug("depart : "+depart.gridpoint.computeVec2());
		DStarLiteNode node = depart;
		DStarLiteNode min = null;
		int coutMin;

		int nbMax = 500;

		assert depart.rhs != Integer.MAX_VALUE;
//		log.write("Length : "+depart.rhs, LogCategoryKraken.TEST);
		if(depart.rhs == Integer.MAX_VALUE)
			return null;
		// log.critical("rhs infini : pas de chemin");

		while(!node.equals(arrivee) && --nbMax > 0)
		{
			trajet.add(node.node.position);
			// log.debug(node.gridpoint.computeVec2());
//			if(graphicDStarLiteFinal)
//				gridspace.setColor(node.gridpoint, Couleur.ROUGE);

			coutMin = Integer.MAX_VALUE;

			int nbNeighbours = node.node.getNbNeighbours();
			int indexMin = 0;
			for(int i = 0; i < nbNeighbours; i++)
			{
				NavmeshNode voisin = node.node.getNeighbour(i);
				if(voisin == null)
					continue;
				DStarLiteNode s = getFromMemory(voisin);
				int coutTmp = add(node.node.getNeighbourEdge(i).getDistance(), s.g);
				if(coutTmp < coutMin)
				{
					coutMin = coutTmp;
					min = s;
					indexMin = i;
				}
			}
			if(graphicDStarLite)
				node.node.getNeighbourEdge(indexMin).highlight(true);
			node = min;
		}
		assert nbMax >= 0;
		
		if(nbMax < 0)
			return null;
		// throw new PathfindingException("Erreur : on aurait dû trouver un
		// chemin… et non.");

		trajet.add(arrivee.node.position);
		// log.debug("Arrivée : "+arrivee.gridpoint.computeVec2());

		return trajet;

	}

	/**
	 * Renvoie l'heuristique au A* courbe.
	 * L'heuristique est une distance en mm
	 * 
	 * @param c
	 * @return
	 */
	public synchronized Double heuristicCostCourbe(Cinematique c)
	{
		// TODO
/*		if(c.getPosition().isHorsTable())
		{
			// log.debug("Hors table ! "+c);
			return null;
		}*/

		NavmeshNode pos = navmesh.getNearest(c.getPosition());

		// si on est arrivé… on est arrivé.
		if(pos.equals(arrivee.node))
			return 0.;

		/*
		 * S'occupe de la mise à jour
		 */
		DStarLiteNode premier = getFromMemoryUpdated(pos);
		assert premier != null;
		
		if(premier.rhs == Integer.MAX_VALUE)
		{
			// log.debug("Inaccessible : "+c.getPosition());
			return null;
		}
		
		double erreurDistance = premier.rhs / 1000.;


		double orientationOptimale = getOrientationHeuristique(premier);

		// l'orientation est vérifiée modulo 2*pi : aller vers la destination ou
		// s'en éloigner sont différenciés
		double erreurOrientation = (c.orientationGeometrique - orientationOptimale) % (2 * Math.PI);
		if(erreurOrientation > Math.PI)
			erreurOrientation -= 2 * Math.PI;

		erreurOrientation = Math.abs(erreurOrientation);

		// il faut toujours majorer la vraie distance, afin de ne pas chercher
		// tous les trajets possibles…
		// le poids de l'erreur d'orientation doit rester assez faible. Car
		// vouloir trop coller à l'orientation, c'est risquer d'avoir une
		// courbure impossible…
		return 1.3 * erreurDistance + 5 * erreurOrientation;
	}

	/**
	 * Renvoie l'indice du meilleur voisin, i.e. le plus proche de l'arrivée
	 * @param node
	 * @return
	 */
	private int getBestVoisin(NavmeshNode node)
	{
		int nbVoisins = node.getNbNeighbours();
		assert nbVoisins > 0; // un nœud a forcément au moins un voisin
		int bestVoisin = 0;
		int bestVoisinDistance = add(getFromMemory(node.getNeighbour(0)).rhs, node.getNeighbourEdge(0).getDistance());
		
		for(int i = 1; i < nbVoisins; i++)
		{
			NavmeshNode voisin = node.getNeighbour(i);
			int candidatDistance = add(getFromMemory(voisin).rhs, node.getNeighbourEdge(i).getDistance());
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
	private double getOrientationHeuristique(DStarLiteNode n)
	{
		// réutilisation de la dernière valeur si possible
		if(n.heuristiqueOrientation != null)
			return n.heuristiqueOrientation;
		
		assert !n.node.equals(arrivee.node);
		
		int nbBestVoisin = getBestVoisin(n.node);
		NavmeshNode bestVoisin = n.node.getNeighbour(nbBestVoisin); 
		if(bestVoisin.equals(arrivee.node))
			n.heuristiqueOrientation = n.node.getNeighbourEdge(nbBestVoisin).getOrientation(n.node);
		else
		{
			int nbBestVoisinDuVoisin = getBestVoisin(bestVoisin);
			double angle1 = n.node.getNeighbourEdge(nbBestVoisin).getOrientation(n.node);
			double angle2 = bestVoisin.getNeighbourEdge(nbBestVoisinDuVoisin).getOrientation(bestVoisin);
			double diff = (( angle1 - angle2 + 3 * Math.PI ) % (2 * Math.PI)) - Math.PI;
			double mean = (2 * Math.PI + angle2 + (diff / 2 )) % (2 * Math.PI);
			n.heuristiqueOrientation = mean;
		}
		if(graphicHeuristique)
			buffer.addSupprimable(n);

		return n.heuristiqueOrientation;
	}

	/**
	 * Somme en faisant attention aux valeurs infinies
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private final int add(int a, int b)
	{
		if(a == Integer.MAX_VALUE || b == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return a + b;
	}
	
	/**
	 * Le chemin a été entièrement parcouru.
	 */
	public synchronized void stopContinuousSearch()
	{
		rechercheEnCours = false;
	}

}
