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

package pathfinding.dstarlite;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.PriorityQueue;

import pathfinding.dstarlite.gridspace.Direction;
import pathfinding.dstarlite.gridspace.GridSpace;
import pathfinding.dstarlite.gridspace.PointDirige;
import pathfinding.dstarlite.gridspace.PointDirigeManager;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import pathfinding.dstarlite.gridspace.PointGridSpaceManager;
import robot.Cinematique;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2RO;
import container.Service;
import exceptions.PathfindingException;
import graphic.printable.Couleur;

/**
 * Recherche de chemin avec replanification rapide.
 * Fournit un chemin non courbe sous forme d'une ligne brisée.
 * En fait utilisé comme heuristique par l'AStarCourbe
 * N'est utilisé qu'avec le "vrai" robot et ne peut pas prendre en compte la disparition prochaine d'obstacle
 * @author pf
 *
 */

public class DStarLite implements Service
{
	protected Log log;
	private GridSpace gridspace;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	private boolean graphicDStarLite;

	private DStarLiteNode[] memory = new DStarLiteNode[PointGridSpace.NB_POINTS];

	private PriorityQueue<DStarLiteNode> openset = new PriorityQueue<DStarLiteNode>(PointGridSpace.NB_POINTS, new DStarLiteNodeComparator());
	private int km;
	private DStarLiteNode arrivee;
	private DStarLiteNode depart;
	private PointGridSpace lastDepart;
	private long nbPF = 0;
	
	private BitSet obstaclesConnus;
	
	private Cle knew = new Cle();
	private Cle kold = new Cle();
	private Cle tmp = new Cle();

	/**
	 * Le comparateur de DStarLiteNode, utilisé par la PriorityQueue
	 * @author pf
	 *
	 */
	private class DStarLiteNodeComparator implements Comparator<DStarLiteNode>
	{
		@Override
		public int compare(DStarLiteNode arg0, DStarLiteNode arg1)
		{
			// Ordre lexico : on compare d'abord first, puis second
			if(arg0.cle.first > arg1.cle.first)
				return 1;
			if(arg0.cle.first < arg1.cle.first)
				return -1;
			return (int) Math.signum(arg0.cle.second - arg1.cle.second);
		}		
	}
	
	/**
	 * Constructeur, rien à dire
	 * @param log
	 * @param gridspace
	 */
	public DStarLite(Log log, GridSpace gridspace, PointGridSpaceManager pointManager, PointDirigeManager pointDManager)
	{
		this.log = log;
		this.gridspace = gridspace;
		this.pointManager = pointManager;
		this.pointDManager = pointDManager;
		
		obstaclesConnus = new BitSet(PointGridSpace.NB_POINTS * 8);
		
		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			memory[i] = new DStarLiteNode(pointManager.get(i));
	}
		
	/**
	 * Met à jour la clé de s et la renvoie
	 * @param s
	 * @return
	 */
	private final Cle calcKey(DStarLiteNode s)
	{
		return calcKey(s, s.cle);
	}
	
	/**
	 * Met à jour la clé donnée en paramètre avec s et la renvoie
	 * @param s
	 * @param copy
	 * @return
	 */
	private final Cle calcKey(DStarLiteNode s, Cle copy)
	{
		copy.set(add(add(Math.min(s.g,s.rhs), distanceHeuristique(s.gridpoint)), km),
				Math.min(s.g, s.rhs));
		return copy;
	}

	private DStarLiteNode getFromMemory(PointGridSpace gridpoint)
	{
		// Il peut arriver qu'on sorte de la grille
		if(gridpoint == null)
			return null;
		
		DStarLiteNode out = memory[gridpoint.hashcode];
		out.update(nbPF);
		
		return out;
	}
	
	private void updateVertex(DStarLiteNode u)
	{
		/**
		 * C'est un peu différent de l'algo classique
		 */
		if(graphicDStarLite)
			gridspace.setColor(u.gridpoint, Couleur.BLEU);
		
		if(u.g != u.rhs)
		{
			calcKey(u);
			if(u.inOpenSet)
				openset.remove(u);
			else
				u.inOpenSet = true;
			openset.add(u);
		}
		else if(u.inOpenSet)
		{
			openset.remove(u);
			u.inOpenSet = false;
		}
		
	}
	
	private void computeShortestPath() throws PathfindingException
	{
		DStarLiteNode u;
		while(!openset.isEmpty() && ((u = openset.peek()).cle.isLesserThan(calcKey(depart, tmp)) || depart.rhs > depart.g))
		{
			u.cle.copy(kold);
//			Cle kold = u.cle.clone();
			calcKey(u, knew);
			if(kold.isLesserThan(knew))
			{
//				log.debug("Cas 1");
				knew.copy(u.cle);
				openset.poll();
				openset.add(u);
				if(graphicDStarLite)
					gridspace.setColor(u.gridpoint, Couleur.BLEU);
			}
			else if(u.g > u.rhs)
			{
//				log.debug("Cas 2");
				u.g = u.rhs;
				openset.poll();
				u.inOpenSet = false;
				if(graphicDStarLite)
					gridspace.setColor(u.gridpoint, Couleur.ROUGE);
				for(Direction i : Direction.values())
				{
					DStarLiteNode s = getFromMemory(pointManager.getGridPointVoisin(u.gridpoint, i));

					if(s == null)
						continue;

					s.rhs = Math.min(s.rhs, add(distanceDynamiquePred(u.gridpoint, i), u.g));
					updateVertex(s);
				}
			}
			else
			{
//				log.debug("Cas 3");
				int gold = u.g;
				u.g = Integer.MAX_VALUE;
				for(Direction i : Direction.values())
				{
					DStarLiteNode s = getFromMemory(pointManager.getGridPointVoisin(u.gridpoint, i));
					if(s == null)
						continue;
					
					if(s.rhs == add(distanceDynamiquePred(u.gridpoint, i), gold) && !s.gridpoint.equals(arrivee.gridpoint))
					{
						s.rhs = Integer.MAX_VALUE;
						for(Direction j : Direction.values())
						{
							DStarLiteNode s2 = getFromMemory(pointManager.getGridPointVoisin(s.gridpoint, j));
							if(s2 == null)
								continue;
							
							s.rhs = Math.min(s.rhs, add(distanceDynamiqueSucc(s.gridpoint, j), s2.g));
						}
					}
					updateVertex(s);
				}
				// Dans la boucle, il faut aussi faire u.
				if(u.rhs == gold && !u.gridpoint.equals(arrivee.gridpoint))
				{
					u.rhs = Integer.MAX_VALUE;
					for(Direction i : Direction.values())
					{
						DStarLiteNode s2 = getFromMemory(pointManager.getGridPointVoisin(u.gridpoint, i));
						if(s2 == null)
							continue;
						
						u.rhs = Math.min(u.rhs, add(distanceDynamiqueSucc(u.gridpoint, i), s2.g));
					}
				}
				updateVertex(u);
			}

		}

		if(depart.rhs == Integer.MAX_VALUE)
			throw new PathfindingException("Aucun chemin n'a été trouvé");
	}

	/**
	 * Calcule un nouvel itinéraire.
	 * @param arrivee (un Vec2)
	 * @param depart (un Vec2)
	 * @throws PathfindingException 
	 */
	public void computeNewPath(Vec2RO depart, Vec2RO arrivee) throws PathfindingException
	{
		computeNewPath(pointManager.get(depart), pointManager.get(arrivee));
	}
	
	/**
	 * Calcule un nouvel itinéraire.
	 * @param arrivee (un gridpoint)
	 * @param depart (un gridpoint)
	 * @throws PathfindingException 
	 */
	private void computeNewPath(PointGridSpace depart, PointGridSpace arrivee) throws PathfindingException
	{
		if(graphicDStarLite)
			gridspace.reinitGraphicGrid();

//		log.debug("Calcul chemin D* Lite entre "+depart+" et "+gridspace.computeVec2(arrivee));
		nbPF++;
		km = 0;
		this.depart = getFromMemory(depart);
		lastDepart = this.depart.gridpoint;

		this.arrivee = getFromMemory(arrivee);
		this.arrivee.rhs = 0;
		this.arrivee.cle.set(distanceHeuristique(this.arrivee.gridpoint), 0);
		
		openset.clear();
		openset.add(this.arrivee);
		
		if(graphicDStarLite)
		{
			gridspace.setColor(this.arrivee.gridpoint, Couleur.JAUNE);
			gridspace.setColor(this.depart.gridpoint, Couleur.VIOLET);
		}

		obstaclesConnus.clear();
		obstaclesConnus.or(gridspace.getCurrentObstacles());
		
		computeShortestPath();
	}
	
	private final int distanceHeuristique(PointGridSpace gridpoint)
	{
		return depart.gridpoint.distanceOctile(gridpoint);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		graphicDStarLite = config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE);
	}
	
	private void updateGoal(Vec2RO positionRobot)
	{
		depart = getFromMemory(pointManager.get(positionRobot));
		km += distanceHeuristique(lastDepart);
		lastDepart = depart.gridpoint;
	}
	
	/**
	 * Met à jour le pathfinding
	 * @throws PathfindingException 
	 */
	public void updatePath(Vec2RO positionRobot) throws PathfindingException
	{
		if(graphicDStarLite)
			gridspace.reinitGraphicGrid();

		updateGoal(positionRobot);
		BitSet[] obs = gridspace.getOldAndNewObstacles();
		
		// Disparition d'un obstacle : le coût baisse
		for(int i = obs[0].nextSetBit(0); i >= 0; i = obs[0].nextSetBit(i+1))
		{
//			log.debug("Retrait de "+o);
			if(obstaclesConnus.get(i))
			{
				obstaclesConnus.clear(i);

				PointDirige p = pointDManager.get(i);
				DStarLiteNode u = getFromMemory(p.point);
				DStarLiteNode v = getFromMemory(pointManager.getGridPointVoisin(p));
				
				u.rhs = Math.min(u.rhs, add(v.g, gridspace.distanceStatique(p)));	
				updateVertex(u);
			}
			else
			{
				log.critical("Suppression d'un obstacle déjà supprimé ?");
			}
		}
		
		// Ajout d'un obstacle : le coût augmente
		for(int i = obs[1].nextSetBit(0); i >= 0; i = obs[1].nextSetBit(i+1))
		{
//			log.debug("Ajout de "+o);
			if(!obstaclesConnus.get(i))
			{
				obstaclesConnus.set(i);

				PointDirige p = pointDManager.get(i);
				// Ajout d'un obstacle
				DStarLiteNode u = getFromMemory(p.point);
				DStarLiteNode v = getFromMemory(pointManager.getGridPointVoisin(p));

				// l'ancienne distance est la distance statique car c'est un ajout d'obstacle
				if(u.rhs == add(v.g, gridspace.distanceStatique(p)) && !u.equals(arrivee))
				{
					u.rhs = Integer.MAX_VALUE;
					for(Direction voisin : Direction.values())
						u.rhs = Math.min(u.rhs, add(distanceDynamiqueSucc(u.gridpoint, voisin),
								getFromMemory(pointManager.getGridPointVoisin(u.gridpoint,voisin)).g));
				}
				updateVertex(u);
			}
			else
			{
				log.critical("Ajout d'un obstacle déjà ajouté ?");
			}
		}
		
		computeShortestPath();
	}
	
	/**
	 * Utilisé pour l'affichage et le debug
	 * @return
	 */
	public ArrayList<Vec2RO> itineraireBrut() throws PathfindingException
	{
		ArrayList<Vec2RO> trajet = new ArrayList<Vec2RO>();

		log.debug("depart : "+depart.gridpoint.computeVec2());
		DStarLiteNode node = depart;
		DStarLiteNode min = null;
		int coutMin;
		
		while(!node.equals(arrivee))
		{
			trajet.add(node.gridpoint.computeVec2());
			log.debug(node.gridpoint.computeVec2());
			if(graphicDStarLite)
				gridspace.setColor(node.gridpoint, Couleur.VERT);

			coutMin = Integer.MAX_VALUE;
			
			for(Direction i : Direction.values())
			{
				PointGridSpace voisin = pointManager.getGridPointVoisin(node.gridpoint,i);
				if(voisin == null)
					continue;
				DStarLiteNode s = getFromMemory(voisin);
				int coutTmp = add(distanceDynamiqueSucc(node.gridpoint, i), s.g);
				if(coutTmp < coutMin && coutTmp < node.g)
				{
					coutMin = coutTmp;
					min = s;
				}
			}
			
			if(coutMin == Integer.MAX_VALUE)
				throw new PathfindingException("Itinéraire brut : aucun chemin !");
			
			node = min;
		}
		trajet.add(arrivee.gridpoint.computeVec2());
		log.debug("Arrivée : "+arrivee.gridpoint.computeVec2());

		return trajet;
		
	}
	
	/**
	 * Renvoie l'heuristique au ThetaStar. Attention ! On suppose que le gridpoint est à jour.
	 * @param gridpoint
	 * @return
	 */
	public double heuristicCostCourbe(Cinematique c)
	{
		PointGridSpace gridpoint = pointManager.get(c.getPosition());
		
		// Si ce n'est pas à jour, on recalcule
		if(memory[gridpoint.hashcode].nbPF != nbPF)
		{
			updateGoal(c.getPosition());
			try {
				computeShortestPath();
			} catch (PathfindingException e) {
				// Pas de chemin ? Alors distance infinie
				return Integer.MAX_VALUE;
			}
		}

		return getFromMemory(gridpoint).rhs; // TODO unité ?
	}
	
	/**
	 * Somme en faisant attention aux valeurs infinies
	 * @param a
	 * @param b
	 * @return
	 */
	private final int add(int a, int b)
	{
		if(a == Integer.MAX_VALUE || b  == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return a + b;
	}
	
	/**
	 * Donne la distance c(voisin, point)
	 * @param point
	 * @param dir
	 * @return
	 */
	private final int distanceDynamiquePred(PointGridSpace point, Direction dir)
	{
		PointGridSpace voisin = pointManager.getGridPointVoisin(point, dir);
		return distanceDynamiqueSucc(voisin, dir.getOppose());
	}

	/**
	 * Donne la distance c(point, voisin)
	 * @param point
	 * @param dir
	 * @return
	 */
	private final int distanceDynamiqueSucc(PointGridSpace point, Direction dir)
	{
		if(obstaclesConnus.get(pointDManager.get(point, dir).hashCode()))
			return Integer.MAX_VALUE;
		return gridspace.distanceStatique(pointDManager.get(point, dir));
	}
	
}
