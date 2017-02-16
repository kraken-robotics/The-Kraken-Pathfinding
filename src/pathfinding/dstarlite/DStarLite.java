/*
Copyright (C) 2013-2017 Pierre-François Gimenez

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
import java.util.List;

import pathfinding.astar.arcs.CercleArrivee;
import pathfinding.dstarlite.gridspace.Direction;
import pathfinding.dstarlite.gridspace.GridSpace;
import pathfinding.dstarlite.gridspace.PointDirige;
import pathfinding.dstarlite.gridspace.PointDirigeManager;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import pathfinding.dstarlite.gridspace.PointGridSpaceManager;
import robot.Cinematique;
import table.RealTable;
import utils.Log;
import utils.Vec2RO;
import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.LowPFClass;
import container.Service;
import exceptions.PathfindingException;
import graphic.PrintBuffer;
import graphic.printable.Couleur;

/**
 * Recherche de chemin avec replanification rapide.
 * Fournit un chemin non courbe sous forme d'une ligne brisée.
 * En fait utilisé comme heuristique par l'AStarCourbe
 * N'est utilisé qu'avec le "vrai" robot et ne peut pas prendre en compte la disparition prochaine d'obstacle
 * @author pf
 *
 */

public class DStarLite implements Service, Configurable, LowPFClass
{
	protected Log log;
	private GridSpace gridspace;
	private RealTable table;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	protected CercleArrivee cercle;
	private boolean graphicDStarLite, graphicDStarLiteFinal, graphicHeuristique;
	private boolean shoot = false;
	
	private DStarLiteNode[] memory = new DStarLiteNode[PointGridSpace.NB_POINTS];

	private EnhancedPriorityQueue openset = new EnhancedPriorityQueue();
	private int km;
	private DStarLiteNode arrivee;
	private DStarLiteNode depart;
	private PointGridSpace lastDepart;
	private PrintBuffer buffer;
	private long nbPF = 0;
	
	private double[][] atan2map = new double[19][19];
	
	private BitSet obstaclesConnus;
	
	private Cle knew = new Cle();
	private Cle kold = new Cle();
	private Cle tmp = new Cle();
	
	/**
	 * Constructeur, rien à dire
	 * @param log
	 * @param gridspace
	 */
	public DStarLite(Log log, GridSpace gridspace, PointGridSpaceManager pointManager, PointDirigeManager pointDManager, PrintBuffer buffer, RealTable table, CercleArrivee cercle)
	{
		this.log = log;
		this.gridspace = gridspace;
		this.pointManager = pointManager;
		this.pointDManager = pointDManager;
		this.buffer = buffer;
		this.table = table;
		this.cercle = cercle;
		
		obstaclesConnus = new BitSet(PointGridSpace.NB_POINTS * 8);
		obstaclesConnus.or(gridspace.getCurrentObstacles());

		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			memory[i] = new DStarLiteNode(pointManager.get(i));
		
		for(int x = -9; x <= 9; x++)
			for(int y = -9; y <= 9; y++)
				atan2map[x+9][y+9] = Math.atan2(y,x);
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

	/**
	 * Récupère un nœud dans la mémoire. Ce nœud doit être à jour.
	 * @param gridpoint
	 * @return
	 */
	private DStarLiteNode getFromMemoryUpdated(PointGridSpace gridpoint)
	{
		// Il peut arriver qu'on sorte de la grille
		if(gridpoint == null)
			return null;
		
		DStarLiteNode out = memory[gridpoint.hashcode];

		if(out.nbPF != nbPF)
			return null;
		
		return out;
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
	
	private final void updateVertex(DStarLiteNode u)
	{
		if(graphicDStarLite)
			gridspace.setColor(u.gridpoint, Couleur.BLEU);
		
		if(u.g != u.rhs)
		{
			u.cle.copy(tmp);
			calcKey(u);
			if(u.inOpenSet)
			{
//				if(tmp.compare(u.cle) < 0)
				if(u.cle.lesserThan(tmp)) // la clé a augmenté
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
//			Cle kold = u.cle.clone();
			calcKey(u, knew);
			if(kold.lesserThan(knew))
			{
				// la clé a augmenté
//				log.debug("Cas 1");
				knew.copy(u.cle);
				openset.percolateDown(u);
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
					gridspace.setColor(u.gridpoint, Couleur.BLEU);
				for(Direction i : Direction.values)
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
				for(Direction i : Direction.values)
				{
					DStarLiteNode s = getFromMemory(pointManager.getGridPointVoisin(u.gridpoint, i));
					if(s == null)
						continue;
					
					if(s.rhs == add(distanceDynamiquePred(u.gridpoint, i), gold) && !s.gridpoint.equals(arrivee.gridpoint))
					{
						s.rhs = Integer.MAX_VALUE;
						for(Direction j : Direction.values)
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
					for(Direction i : Direction.values)
					{
						DStarLiteNode s2 = getFromMemory(pointManager.getGridPointVoisin(u.gridpoint, i));
						if(s2 == null)
							continue;
						
						u.rhs = Math.min(u.rhs, add(distanceDynamiqueSucc(u.gridpoint, i), s2.g));
					}
				}
				
				// de toute façon, comme u sera forcément retiré de la liste dans updateVertex… autant le faire efficacement ici
//				openset.poll();
//				u.inOpenSet = false;
				updateVertex(u);
			}

		}

//		if(depart.rhs == Integer.MAX_VALUE)
//			throw new PathfindingException("Aucun chemin n'a été trouvé");
	}

	/**
	 * Calcule un nouvel itinéraire.
	 * @param arrivee (un Vec2)
	 * @param depart (un Vec2)
	 */
	public void computeNewPath(Vec2RO depart, Vec2RO arrivee, boolean shoot)
	{
		this.shoot = shoot;
		if(graphicDStarLite)
			gridspace.reinitGraphicGrid();
		
		updateGoalAndStart(depart, arrivee);
		updateObstaclesEnnemi();
		updateObstaclesTable();

		if(graphicDStarLite)
		{
			gridspace.setColor(this.arrivee.gridpoint, Couleur.JAUNE);
			gridspace.setColor(this.depart.gridpoint, Couleur.VIOLET);
		}

	}
	
	/**
	 * Heuristique (distance octile) entre le point de départ et ce gridpoint
	 * @param gridpoint
	 * @return
	 */
	private final int distanceHeuristique(PointGridSpace gridpoint)
	{
		return depart.gridpoint.distanceOctile(gridpoint);
	}

	@Override
	public void useConfig(Config config)
	{
		graphicDStarLite = config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE);
		graphicDStarLiteFinal = config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE_FINAL);
		graphicHeuristique = config.getBoolean(ConfigInfo.GRAPHIC_HEURISTIQUE);
	}
	
	/**
	 * Met à jour la destination
	 * @param positionArrivee
	 */
	public synchronized void updateGoalAndStart(Vec2RO positionRobot, Vec2RO positionArrivee)
	{
		nbPF++;
		km = 0;

		depart = getFromMemory(pointManager.get(positionRobot));
		lastDepart = depart.gridpoint;

		this.arrivee = getFromMemory(pointManager.get(positionArrivee));
		this.arrivee.rhs = 0;
		this.arrivee.cle.set(distanceHeuristique(this.arrivee.gridpoint), 0);
		
		openset.clear();
		openset.add(this.arrivee);
		
		computeShortestPath();
	}

	public synchronized void updateStart(Vec2RO positionRobot)
	{
		updateStart(pointManager.get(positionRobot));
	}
	
	/**
	 * Met à jour la position actuelle du robot
	 * @param positionRobot
	 */
	private synchronized void updateStart(PointGridSpace p)
	{
		depart = getFromMemory(p);
		km += distanceHeuristique(lastDepart);
		lastDepart = depart.gridpoint;
		
		computeShortestPath();
	}
	
	/**
	 * Met à jour les obstacles des éléments de jeux
	 */
	public synchronized void updateObstaclesTable()
	{
		updateObstacles(table.getOldAndNewObstacles(shoot));
	}
	
	/**
	 * Met à jour les obstacles des ennemies
	 */
	public synchronized void updateObstaclesEnnemi()
	{
		updateObstacles(gridspace.getOldAndNewObstacles());
	}
	
	/**
	 * Met à jour le pathfinding
	 */
	private synchronized void updateObstacles(BitSet[] obs)
	{
		
//		if((graphicDStarLite || graphicDStarLiteFinal) && (!obs[0].isEmpty() || !obs[1].isEmpty()))
//			gridspace.reinitGraphicGrid();

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
					for(Direction voisin : Direction.values)
					{
						PointGridSpace pointvoisin = pointManager.getGridPointVoisin(u.gridpoint,voisin);
						if(pointvoisin == null) // pas de bras…
							continue;
						u.rhs = Math.min(u.rhs, add(distanceDynamiqueSucc(u.gridpoint, voisin),
								getFromMemory(pointvoisin).g));
					}
				}				
				updateVertex(u);
			}
		}
		
		computeShortestPath();
	}
	
	/**
	 * Utilisé pour l'affichage et le debug
	 * @return
	 */
	public synchronized List<Vec2RO> itineraireBrut() throws PathfindingException
	{
		List<Vec2RO> trajet = new ArrayList<Vec2RO>();

//		log.debug("depart : "+depart.gridpoint.computeVec2());
		DStarLiteNode node = depart;
		DStarLiteNode min = null;
		int coutMin;
		
		int nbMax = 500;
		
		if(depart.rhs == Integer.MAX_VALUE)
			throw new PathfindingException("rhs infini : pas de chemin");
		
		while(!node.equals(arrivee) && --nbMax>0)
		{
			trajet.add(node.gridpoint.computeVec2());
//			log.debug(node.gridpoint.computeVec2());
			if(graphicDStarLiteFinal)
				gridspace.setColor(node.gridpoint, Couleur.ROUGE);

			coutMin = Integer.MAX_VALUE;
			
			for(Direction i : Direction.values)
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
		if(nbMax < 0)
			throw new PathfindingException("Erreur : on aurait dû trouver un chemin… et non.");

		trajet.add(arrivee.gridpoint.computeVec2());
//		log.debug("Arrivée : "+arrivee.gridpoint.computeVec2());

		return trajet;
		
	}
	
	/**
	 * Renvoie l'heuristique au A* courbe.
	 * L'heuristique est une distance en mm
	 * @param c
	 * @return
	 */
	public synchronized Double heuristicCostCourbe(Cinematique c, boolean useCercle)
	{
		if(c.getPosition().isHorsTable())
		{
//			log.debug("Hors table !");
			return null;
		}
		
		PointGridSpace pos = pointManager.get(c.getPosition());
		
		if(gridspace.isInGrilleStatique(pos)) // si on est dans un obstacle, D* Lite va chercher partout une solution… qui n'existe pas
		{
//			log.debug("Dans un obstacle");
			return null;
		}

		updateStart(pos);
		DStarLiteNode premier = getFromMemoryUpdated(pos);
		
		// si on est arrivé… on est arrivé.
		if(pos.equals(arrivee.gridpoint))
			return 0.;
		
		double orientationOptimale = getOrientationHeuristique(pos);
		
		// l'orientation est vérifiée modulo 2*pi : aller vers la destination ou s'en éloigner sont différenciés
		double erreurOrientation = (c.orientationGeometrique - orientationOptimale) % (2*Math.PI);
		if(erreurOrientation > Math.PI)
			erreurOrientation -= 2*Math.PI;

		erreurOrientation = Math.abs(erreurOrientation);

		double erreurDistance = premier.rhs / 1000. * PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS; // distance en mm
		
		if(useCercle)
		{
			/*
			 * Le cas du cercle est un peu particulier… il faut surtout veiller à l'orientation et au sens
			 */
/*			double erreurSens = 0;
			if(c.enMarcheAvant)
				erreurSens = 500;
			return 1.1*erreurDistance + erreurSens + 20*erreurOrientation;*/
			if(c.enMarcheAvant)
				return 1.3*erreurDistance + 5*erreurOrientation + 1800;
		}

		if(premier.rhs == Integer.MAX_VALUE)
		{
//			log.debug("Inaccessible : "+c.getPosition());
			return null;
		}
//			throw new DStarLiteException("Heuristique : inaccessible");

		// il faut toujours majorer la vraie distance, afin de ne pas chercher tous les trajets possibles…
		// le poids de l'erreur d'orientation doit rester assez faible. Car vouloir trop coller à l'orientation, c'est risquer d'avoir une courbure impossible…
		// on cherche une faible courbure. ça évite les trajectoires complexes
		return 1.3*erreurDistance + 5*erreurOrientation;
	}
	
	/**
	 * Fournit une heuristique de l'orientation à prendre en ce point
	 * @param p
	 * @return
	 */
	private double getOrientationHeuristique(PointGridSpace p)
	{
		// réutilisation de la dernière valeur si possible
		DStarLiteNode n = getFromMemoryUpdated(p);
		if(n != null && n.heuristiqueOrientation != null)
			return n.heuristiqueOrientation;
		
		updateStart(p); // on met à jour
		double directionX = 0;
		double directionY = 0;
		
		n = getFromMemoryUpdated(p);
		
		int score = n.rhs;
		
		for(Direction d : Direction.values)
		{
			DStarLiteNode voisin = getFromMemoryUpdated(pointManager.getGridPointVoisin(p, d));
			if(voisin == null)
				continue;
			// TODO : vérifier si ce cas arrive souvent
			int scoreVoisin = voisin.rhs;
			// ce devrait être équivalent
			double s = Math.signum(score-scoreVoisin);
			directionX += s * d.deltaX;
			directionY += s * d.deltaY;
			n.heuristiqueOrientation = atan2map[(int)directionX+9][(int)directionY+9];
		}
		
		if(directionX == 0 && directionY == 0) // si on a aucune info, on utilise une heuristique plus simple (trajet à vol d'oiseau)
		{
			directionX = arrivee.gridpoint.x - p.x;
			directionY = arrivee.gridpoint.y - p.y;
			n.heuristiqueOrientation = Math.atan2(directionY, directionX);
		}
		
		if(graphicHeuristique)
			buffer.addSupprimable(n);
		
		return n.heuristiqueOrientation;
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
