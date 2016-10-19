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
import utils.Log;
import utils.Vec2RO;
import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import exceptions.DStarLiteException;
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

public class DStarLite implements Service, Configurable
{
	protected Log log;
	private GridSpace gridspace;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	private boolean graphicDStarLite, graphicDStarLiteFinal;

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
		obstaclesConnus.or(gridspace.getCurrentObstacles());

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
	
	private void computeShortestPath()
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
				
				// de toute façon, comme u sera forcément retiré de la liste dans updateVertex… autant le faire efficacement ici
				openset.poll();
				u.inOpenSet = false;
				updateVertex(u);
			}

		}

//		if(depart.rhs == Integer.MAX_VALUE)
//			throw new PathfindingException("Aucun chemin n'a été trouvé");
	}

	/**
	 * Calcule un nouvel itinéraire.
	 * Utilisé pour les tests
	 * @param arrivee (un Vec2)
	 * @param depart (un Vec2)
	 */
	public void computeNewPath(Vec2RO depart, Vec2RO arrivee)
	{
		if(graphicDStarLite)
			gridspace.reinitGraphicGrid();
		
		updateGoalAndStart(depart, arrivee);
		updateObstacles();

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
	 * Met à jour le pathfinding
	 */
	public synchronized void updateObstacles()
	{
		if(graphicDStarLite || graphicDStarLiteFinal)
			gridspace.reinitGraphicGrid();

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
		}
		
		computeShortestPath();
	}
	
	/**
	 * Utilisé pour l'affichage et le debug
	 * @return
	 */
	public synchronized ArrayList<Vec2RO> itineraireBrut() throws PathfindingException
	{
		ArrayList<Vec2RO> trajet = new ArrayList<Vec2RO>();

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
				gridspace.setColor(node.gridpoint, Couleur.VERT);

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
		if(nbMax < 0)
			throw new PathfindingException("Erreur : on aurait dû trouver un chemin… et non.");

		trajet.add(arrivee.gridpoint.computeVec2());
//		log.debug("Arrivée : "+arrivee.gridpoint.computeVec2());

		return trajet;
		
	}
	
/*	private Vec2RW delta = new Vec2RW();
	private Vec2RW deltaTmp = new Vec2RW();
	private Vec2RW centreCercle = new Vec2RW();
	private Vec2RW posRobot = new Vec2RW();
	private Vec2RW pos = new Vec2RW();*/

	/**
	 * Renvoie l'heuristique au A* courbe.
	 * L'heuristique est une distance en mm
	 * @param c
	 * @return
	 */
	public synchronized Double heuristicCostCourbe(Cinematique c) throws DStarLiteException
	{
		if(c.getPosition().isHorsTable())
			throw new DStarLiteException("Heuristique : hors table");
		
		PointGridSpace pos = pointManager.get(c.getPosition());
		
		updateStart(pos);
		DStarLiteNode premier = getFromMemoryUpdated(pos);

//		log.debug(premier.rhs+" "+premier.gridpoint.distanceOctile(arrivee.gridpoint));
//		return premier.rhs / 1000. * PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS;
		
		// si on est arrivée… on est arrivée.
		if(pos.equals(arrivee.gridpoint))
			return 0.;
		
		double erreurCourbure = Math.abs(c.courbureGeometrique - getCourbureHeuristique(pos, c.orientationGeometrique)); // erreur en m^-1
		double erreurOrientation = Math.abs((c.orientationGeometrique - getOrientationHeuristique(pos)) % (2 * Math.PI)); // erreur en radian
		double erreurDistance = premier.rhs / 1000. * PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS; // distance en mm
		
		if(premier.rhs == Integer.MAX_VALUE)
			throw new DStarLiteException("Heuristique : inaccessible");

//		log.debug("rhs : "+premier.rhs);
//		log.debug("erreurCourbure : "+erreurCourbure+" "+c.courbureGeometrique+" "+getCourbureHeuristique(pos, c.orientationGeometrique));
//		log.debug("erreurOrientation : "+erreurOrientation);
//		log.debug("erreurDistance : "+erreurDistance);
		
		return erreurDistance + 5*erreurCourbure + 10*erreurOrientation; // TODO : coeff
	}

	/**
	 * Estime la courbure à prendre en ce point
	 * @param p
	 * @param orientation
	 * @return
	 */
	private double getCourbureHeuristique(PointGridSpace p, double orientation)
	{
		Direction d = Direction.getDirection(orientation);
		PointGridSpace voisinApres = pointManager.getGridPointVoisin(p, d);
		PointGridSpace voisinAvant = pointManager.getGridPointVoisin(p, d.getOppose());

		double courbureAvant = 0, courbureApres = 0;
		
		if(voisinAvant != null)
		{
			double tmp = getOrientationHeuristique(voisinAvant);
			double angle = (orientation - tmp) % (2 * Math.PI);
			courbureAvant = angle / d.distance_m;
		}
		
		if(voisinApres != null)
		{
			double tmp = getOrientationHeuristique(voisinApres);
			double angle = (tmp - orientation) % (2 * Math.PI);
			courbureApres = angle / d.distance_m;
		}

		// Moyenne des deux
		if(voisinAvant != null && voisinApres != null)
			return (courbureAvant + courbureApres) / 2.;
		
		return courbureAvant + courbureApres; // l'un des deux est nul
	}
	
	/**
	 * Fournit une heuristique de l'orientation à prendre en ce point
	 * @param p
	 * @return
	 */
	private double getOrientationHeuristique(PointGridSpace p)
	{
		updateStart(p); // on met à jour
		double directionX = 0;
		double directionY = 0;
		int score = getFromMemoryUpdated(p).rhs;
		
		for(Direction d : Direction.values())
		{
			DStarLiteNode voisin = getFromMemoryUpdated(pointManager.getGridPointVoisin(p, d));
			if(voisin == null)
				continue;
			int scoreVoisin = voisin.rhs;
//			directionX += (scoreVoisin - score) * d.deltaX / d.distance;
//			directionY += (scoreVoisin - score) * d.deltaY / d.distance;
			// ce devrait être équivalent
			directionX += Math.signum(scoreVoisin - score) * d.deltaX;
			directionY += Math.signum(scoreVoisin - score) * d.deltaY;
		}
		
		if(directionX == 0 && directionY == 0) // si on a aucune info, on utilise une heuristique plus simple (trajet à vol d'oiseau)
		{
			directionX = arrivee.gridpoint.x - p.x;
			directionY = arrivee.gridpoint.y - p.y;
		}
		
		return Math.atan2(directionX, directionY);
	}
	
	/**
	 * On regarde si le robot peut reculer un peu
	 * @param c
	 * @return
	 */
/*	private boolean heuristiqueMarcheArriere(Cinematique c)
	{
		double d = ClothoidesComputer.PRECISION_TRACE * 1000; // passage de m en mm

		// Si on va en marche avant, on teste la marche arrière, et inversement
		if(c.enMarcheAvant)
			d = -d;
		
		pos = c.getPosition().clone();
		delta.set(d, c.orientation);
		DStarLiteNode n = null;
		
		for(int i = 0; i < 5; i++)
		{
			pos.plus(delta);

			PointGridSpace p = pointManager.get(pos);
			updateStart(c.getPosition());
			n = getFromMemoryUpdated(p);
			
			if(n == null || n.rhs == Integer.MAX_VALUE)
				return false;
		}
		
		return true;
	}*/

	/**
	 * On regarde si le robot peut avancer un peu avec sa courbure actuelle
	 * @param c
	 * @return
	 */
/*	private boolean heuristiqueCercle(Cinematique c)
	{
		double rayonCourbure = 1000. / c.courbure;
		delta.setX(Math.cos(c.orientation + Math.PI / 2) * rayonCourbure);
		delta.setY(Math.sin(c.orientation + Math.PI / 2) * rayonCourbure);
		
		centreCercle.setX(c.getPosition().getX() + delta.getX());
		centreCercle.setY(c.getPosition().getY() + delta.getY());
		
		double cos = Math.sqrt(rayonCourbure * rayonCourbure - ClothoidesComputer.d * ClothoidesComputer.d) / rayonCourbure;
		double sin = Math.abs(ClothoidesComputer.d / rayonCourbure);
		sin = 2 * sin * cos; // sin(a) devient sin(2a)
		cos = 2 * cos * cos - 1; // cos(a) devient cos(2a)
		double cosSauv = cos;
		double sinSauv = sin;
		sin = 0;
		cos = 1;
		
		DStarLiteNode n = null;
		
		// On trace un (petit) arc de cercle dans la continuité de l'arc pour voir s'il se prend un mur ou pas
		for(int i = 0; i < 5; i++)
		{
			double tmp = sin;
			sin = sin * cosSauv + sinSauv * cos; // sin vaut sin(2a*(i+1))
			cos = cos * cosSauv - tmp * sinSauv;
			delta.copy(deltaTmp);
			deltaTmp.rotate(cos, sin);
			centreCercle.copy(posRobot);
			posRobot.minus(deltaTmp);
			
			PointGridSpace p = pointManager.get(posRobot);
			updateStart(c.getPosition());
			n = getFromMemoryUpdated(p);
			
			if(n == null || n.rhs == Integer.MAX_VALUE)
				return false;
		}
		
		return true; // pas de problème
	}*/
	
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
