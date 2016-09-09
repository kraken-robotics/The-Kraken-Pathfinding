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
import java.util.Comparator;
import java.util.PriorityQueue;

import obstacles.types.ObstacleProximity;
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
import debug.Fenetre;
import exceptions.PathfindingException;

/**
 * Recherche de chemin avec replanification rapide.
 * Fournit un chemin non courbe sous forme d'une ligne brisée.
 * En fait utilisé comme heuristique par ThetaStar.
 * N'est utilisé qu'avec le "vrai" robot. Pour la planification à plus long terme,
 * on utilise l'AStarCourbe sans DStarLite mais avec une heuristique toute simple.
 * En effet, un souci de cette implémentation est qu'elle ne peut travailler qu'avec le robot réel et ne prévoit rien.
 * @author pf
 *
 */

public class DStarLite implements Service
{
	protected Log log;
	private GridSpace gridspace;
	private Fenetre fenetre;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	private boolean graphicDStarLite;

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
			int out = (arg0.cle.first - arg1.cle.first) << 1;
			if(arg0.cle.second > arg1.cle.second)
				out++;
			return out;
		}		
	}
	
	/**
	 * Constructeur, rien à dire
	 * @param log
	 * @param gridspace
	 */
	public DStarLite(Log log, GridSpace gridspace, PointGridSpaceManager pointManager, PointDirigeManager pointDManager, Fenetre fenetre)
	{
		this.log = log;
		this.gridspace = gridspace;
		this.pointManager = pointManager;
		this.pointDManager = pointDManager;
		this.fenetre = fenetre;
		
		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			memory[i] = new DStarLiteNode(pointManager.get(i));
	}
	
	private DStarLiteNode[] memory = new DStarLiteNode[PointGridSpace.NB_POINTS];
//	private BitSet contained = new BitSet(GridSpace.NB_POINTS);

	private PriorityQueue<DStarLiteNode> openset = new PriorityQueue<DStarLiteNode>(PointGridSpace.NB_POINTS, new DStarLiteNodeComparator());
	private int km;
	private DStarLiteNode arrivee;
	private DStarLiteNode depart;
	private PointGridSpace lastDepart;
	private long nbPF = 0;
	private ArrayList<PointDirige> obstaclesConnus = new ArrayList<PointDirige>();
	private Cle knew = new Cle();
	private Cle inutile = new Cle();

	private final Cle calcKey(DStarLiteNode s, Cle copy)
	{
		copy.set(add(Math.min(s.g,s.rhs), distanceHeuristique(s.gridpoint), km),
				Math.min(s.g, s.rhs));
		return copy;
	}

	private boolean isThisNodeUptodate(PointGridSpace gridpoint)
	{
		return memory[gridpoint.hashCode()].nbPF == nbPF;
	}

	private DStarLiteNode getFromMemory(PointGridSpace gridpoint)
	{
		DStarLiteNode out = memory[gridpoint.hashCode()];
		
		/**
		 * Si ce point n'a pas encore été utilisé pour ce pathfinding, on l'initialise
		 */
		if(out.nbPF != nbPF)
		{
			out.g = Integer.MAX_VALUE;
			out.rhs = Integer.MAX_VALUE;
			out.done = false;
			out.nbPF = nbPF;
		}
		return out;
	}
		
	private void updateVertex(DStarLiteNode u)
	{
//		log.debug("Update de "+GridSpace.computeVec2(u.gridpoint));
		/**
		 * C'est un peu différent de l'algo classique
		 */
		if(u.g != u.rhs)
		{
			calcKey(u, u.cle);
			if(!u.done)
				openset.remove(u);
			else
				u.done = false;
			openset.add(u);
//			contained.set(u.gridpoint);
			if(graphicDStarLite)
				fenetre.setColor(u.gridpoint, Fenetre.Couleur.BLEU);
		}
		else if(!u.done)
		{
			openset.remove(u);
			u.done = true;
		}
	}
	
	private void computeShortestPath() throws PathfindingException
	{
		DStarLiteNode u;
		Cle kold = new Cle();
		while(!openset.isEmpty() && ((u = openset.peek()).cle.isLesserThan(calcKey(depart, inutile)) || depart.rhs > depart.g))
		{
			if(u.done)
			{
				openset.poll();
				continue;
			}
			if(graphicDStarLite)
				fenetre.setColor(u.gridpoint, Fenetre.Couleur.ROUGE);
			
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
					fenetre.setColor(u.gridpoint, Fenetre.Couleur.BLEU);
			}
			else if(u.g > u.rhs)
			{
//				log.debug("Cas 2");
				u.g = u.rhs;
				openset.poll();
				u.done = true;
				if(graphicDStarLite)
					fenetre.setColor(u.gridpoint, Fenetre.Couleur.ROUGE);
				for(Direction i : Direction.values())
				{
					PointGridSpace voisin = pointManager.getGridPointVoisin(u.gridpoint, i);
					if(voisin == null)
						continue;
					DStarLiteNode s = getFromMemory(voisin);
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
					PointGridSpace voisin = pointManager.getGridPointVoisin(u.gridpoint, i);
					if(voisin == null)
						continue;
					DStarLiteNode s = getFromMemory(voisin);
//					if(s == null)
//						continue;
					if(s.rhs == add(distanceDynamiquePred(u.gridpoint, i), gold) && s.gridpoint != arrivee.gridpoint)
					{
						s.rhs = Integer.MAX_VALUE;
						for(Direction j : Direction.values())
						{
							voisin = pointManager.getGridPointVoisin(s.gridpoint, j);
							if(voisin == null)
								continue;
							DStarLiteNode s2 = getFromMemory(voisin);
							s.rhs = Math.min(s.rhs, add(distanceDynamiquePred(s.gridpoint, j), s2.g));
						}
					}
					updateVertex(s);
				}
				// Dans la boucle, il faut aussi faire u.
				if(u.rhs == gold && u.gridpoint != arrivee.gridpoint)
				{
					u.rhs = Integer.MAX_VALUE;
					for(Direction i : Direction.values())
					{
						PointGridSpace voisin = pointManager.getGridPointVoisin(u.gridpoint, i);
						if(voisin == null)
							continue;
						DStarLiteNode s = getFromMemory(voisin);
						u.rhs = Math.min(u.rhs, add(distanceDynamiquePred(u.gridpoint, i), s.g));
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
	 * @param depart (un gridpoint)
	 * @throws PathfindingException 
	 */
	public void computeNewPath(Vec2RO depart, Vec2RO arrivee) throws PathfindingException
	{
		computeNewPath(pointManager.get(depart), pointManager.get(arrivee));
	}
	/**
	 * Calcule un nouvel itinéraire.
	 * @param arrivee (un Vec2)
	 * @param depart (un gridpoint)
	 * @throws PathfindingException 
	 */
	private void computeNewPath(PointGridSpace depart, PointGridSpace arrivee) throws PathfindingException
	{
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
			fenetre.setColor(this.arrivee.gridpoint, Fenetre.Couleur.JAUNE);
			fenetre.setColor(this.depart.gridpoint, Fenetre.Couleur.VIOLET);
		}

		obstaclesConnus = gridspace.getCurrentObstacles();

		computeShortestPath();

		if(graphicDStarLite)
			for(PointDirige i : obstaclesConnus)
				fenetre.setColor(pointManager.getGridPointVoisin(i.point, i.dir), Fenetre.Couleur.NOIR);

	}
	
	private final int distanceHeuristique(PointGridSpace gridpoint)
	{
		return depart.gridpoint.distanceHeuristiqueDStarLite(gridpoint);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		graphicDStarLite = config.getBoolean(ConfigInfo.GRAPHIC_DSTARLISTE);
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
		updateGoal(positionRobot);
		ArrayList<ObstacleProximity>[] obs = gridspace.getOldAndNewObstacles();
		
		for(ObstacleProximity o : obs[0])
		{
//			log.debug("Retrait de "+o);
			for(PointDirige i : o.getMasque())
			{
				obstaclesConnus.remove(i);
				if(!obstaclesConnus.contains(i))
				{
					// Retrait d'un obstacle. Le coût va donc diminuer.
					PointGridSpace upoint = i.point;
					DStarLiteNode u = getFromMemory(upoint);
					Direction dir = i.dir;
					DStarLiteNode v = getFromMemory(pointManager.getGridPointVoisin(upoint,dir));
					u.rhs = Math.min(u.rhs, add(v.g, gridspace.distanceStatique(pointDManager.get(upoint, dir))));
					updateVertex(u);
				}
			}
		}
		for(ObstacleProximity o : obs[1])
		{
//			log.debug("Ajout de "+o);
			for(PointDirige i : o.getMasque())
			{
				if(!obstaclesConnus.contains(i))
				{
					obstaclesConnus.add(i);
					// Ajout d'un obstacle
					PointGridSpace upoint = i.point;
					DStarLiteNode u = getFromMemory(upoint);
					Direction dir = i.dir;
					DStarLiteNode v = getFromMemory(pointManager.getGridPointVoisin(upoint,dir));

					// l'ancienne distance est la distance statique car c'est un ajout d'obstacle
					if(u.rhs == add(gridspace.distanceStatique(pointDManager.get(upoint, dir)), v.g) && !u.equals(arrivee))
					{
						u.rhs = Integer.MAX_VALUE;
						for(Direction voisin : Direction.values())
							u.rhs = Math.min(u.rhs, add(distanceDynamiqueSucc(u.gridpoint, voisin), getFromMemory(pointManager.getGridPointVoisin(u.gridpoint,i.dir)).g));
					}
					updateVertex(u);
				}
				else
					obstaclesConnus.add(i);

			}
		}
		if(graphicDStarLite)
			for(PointDirige i : obstaclesConnus)
				fenetre.setColor(pointManager.getGridPointVoisin(i.point, i.dir), Fenetre.Couleur.NOIR);

		computeShortestPath();
	}
	
	/**
	 * Utilisé pour l'affichage et le debug
	 * @return
	 */
	public ArrayList<Vec2RO> itineraireBrut()
	{
		ArrayList<Vec2RO> trajet = new ArrayList<Vec2RO>();

		log.debug("depart : "+PointGridSpace.computeVec2(depart.gridpoint));
		DStarLiteNode node = depart;
		DStarLiteNode min = null;
		int coutMin;
		
		while(!node.equals(arrivee))
		{
			trajet.add(PointGridSpace.computeVec2(node.gridpoint));

			if(graphicDStarLite)
				fenetre.setColor(node.gridpoint, Fenetre.Couleur.VIOLET);

			coutMin = Integer.MAX_VALUE;
			
			for(Direction i : Direction.values())
			{
				PointGridSpace voisin = pointManager.getGridPointVoisin(node.gridpoint,i);
				if(voisin == null)
					continue;
				DStarLiteNode s = getFromMemory(voisin);
				int coutTmp = add(distanceDynamiqueSucc(node.gridpoint, i), s.g);
				if(coutTmp < coutMin)
				{
					coutMin = coutTmp;
					min = s;
				}
			}
			node = min;
		}
		trajet.add(PointGridSpace.computeVec2(arrivee.gridpoint));
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
		if(isThisNodeUptodate(gridpoint))
		{
			updateGoal(c.getPosition());
			try {
				computeShortestPath();
			} catch (PathfindingException e) {
				// Pas de chemin ? Alors distance infinie
				return Integer.MAX_VALUE;
			}
		}

		return getFromMemory(gridpoint).rhs;
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
	 * Somme en faisant attention aux valeurs infinies
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private final static int add(int a, int b, int c)
	{
		if(a == Integer.MAX_VALUE || b  == Integer.MAX_VALUE || c  == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return a + b + c;
	}
	
	/**
	 * Donne la distance c(voisin, point)
	 * @param point
	 * @param dir
	 * @return
	 */
	private int distanceDynamiquePred(PointGridSpace point, Direction dir)
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
	private int distanceDynamiqueSucc(PointGridSpace point, Direction dir)
	{
		if(obstaclesConnus.contains(pointDManager.get(point, dir)))
			return Integer.MAX_VALUE;
		return gridspace.distanceStatique(pointDManager.get(point, dir));
	}
	
}
