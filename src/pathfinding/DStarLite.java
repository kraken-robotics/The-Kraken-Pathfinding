package pathfinding;

import java.util.ArrayList;
import java.util.Iterator;

import permissions.ReadOnly;
import utils.Config;
import utils.Log;
import utils.Vec2;
import container.Service;

/**
 * Recherche de chemin avec replanification rapide.
 * @author pf
 *
 */

public class DStarLite implements Service
{
	protected Log log;
	private GridSpace gridspace;

	public DStarLite(Log log, GridSpace gridspace)
	{
		this.log = log;
		this.gridspace = gridspace;
	}
	
	private ArrayList<DStarLiteNode> openset = new ArrayList<DStarLiteNode>();
	private int km;
	private DStarLiteNode arrivee;
	private DStarLiteNode depart;
	private int last;
	
	private Cle calcKey(DStarLiteNode s)
	{
		s.cle.set(Math.min(s.g,s.rhs) + distanceHeuristique(s) + km,
				Math.min(s.g, s.rhs));
		return s.cle;
	}

	/**
	 * Ajout dans une liste triée
	 * @param u
	 */
	private void addToOpenset(DStarLiteNode u)
	{
		Iterator<DStarLiteNode> iterator = openset.listIterator();
		int i = 0;
		while(iterator.hasNext())
		{
			if(iterator.next().cle.isLesserThan(u.cle))
			{
				openset.add(i, u);
				return;
			}
			i++;
		}
		openset.add(u);
/*		int borneInf = 0;
		int borneSup = openset.size()-1;
		int milieu = (borneInf + borneSup) / 2;
		
		if(!openset.get(borneInf).cle.isLesserThan(u.cle))
			openset.add(0, u);
		else if(!u.cle.isLesserThan(openset.get(borneSup).cle))
			openset.add(u);
		else
		{
			while(borneSup - borneInf > 1)
			{
				Cle c = openset.get(milieu).cle;
				if(c.isLesserThan(u.cle))
					borneInf = milieu;
				else if(u.cle.isLesserThan(c))
					borneSup = milieu;
				else
				{
					// On a trouvé une clé égale
					openset.add(milieu, u);
					return;
				}
				milieu = (borneInf + borneSup) / 2;
			}
			openset.add(borneSup, u);
		}*/
	}
	
	private void updateVertex(DStarLiteNode u)
	{
		boolean contains = openset.contains(u);
		if(u.g != u.rhs)
		{
			calcKey(u);
			if(!contains)
				addToOpenset(u);
		}
		else if(contains)
			openset.remove(u);
	}
	
	private void computeShortestPath()
	{
		DStarLiteNode u;
		while(!openset.isEmpty() && ((u = openset.get(0)).cle.isLesserThan(calcKey(depart)) || depart.rhs > depart.g))
		{
			Cle kold = u.cle.clone();
			Cle knew = calcKey(u);
			if(kold.isLesserThan(knew))
			{
				openset.remove(0);
				addToOpenset(u);
			}
			else if(u.g > u.rhs)
			{
				u.g = u.rhs;
				openset.remove(0);
				for(int i = 0; i < 8; i++)
				{
					DStarLiteNode s = u.getVoisin(i, gridspace);
					if(s == null)
						continue;
					if(gridspace.isTraversable(s.gridpoint, i))
						s.rhs = Math.min(s.rhs, gridspace.distance(i) + u.g);
					updateVertex(s);
				}
			}
			else
			{
				int gold = u.g;
				u.g = Integer.MAX_VALUE;
				for(int i = 0; i < 8; i++)
				{
					boolean condition = false;
					DStarLiteNode s = u.getVoisin(i, gridspace);
					if(s == null)
						continue;
					if(!gridspace.isTraversable(s.gridpoint, i))
						condition = s.rhs == Integer.MAX_VALUE;
					else if(s.rhs == (gridspace.distance(i) + gold) && s.gridpoint != arrivee.gridpoint)
						condition = true;
					if(condition)
					{
						s.rhs = Integer.MAX_VALUE;
						for(int j = 0; j < 8; j++)
						{
							DStarLiteNode s2 = s.getVoisin(j, gridspace);
							if(s2 == null)
								continue;
							s.rhs = Math.min(s.rhs, gridspace.distance(j) + s2.g);
						}
					}
					updateVertex(s);
				}
				if(u.rhs == gold && u.gridpoint != depart.gridpoint)
				{
					u.rhs = Integer.MAX_VALUE;
					for(int i = 0; i < 8; i++)
					{
						DStarLiteNode s = u.getVoisin(i, gridspace);
						if(s == null)
							continue;
						u.rhs = Math.min(u.rhs, gridspace.distance(i) + s.g);
					}
				}
				updateVertex(u);
			}

		}
	}

	/**
	 * Calcule un nouvel itinéraire.
	 * @param arrivee
	 * @param depart
	 */
	public void computeNewPath(Vec2<ReadOnly> arrivee, Vec2<ReadOnly> depart)
	{
		km = 0;
		this.depart = new DStarLiteNode(gridspace.computeGridPoint(depart));
		last = this.depart.gridpoint;

		this.arrivee = new DStarLiteNode(gridspace.computeGridPoint(arrivee));
		this.arrivee.rhs = 0;
		this.arrivee.cle.set(distanceHeuristique(this.arrivee), 0);

		openset.clear();
		openset.add(this.arrivee);

		computeShortestPath();
	}
	
	private int distanceHeuristique(DStarLiteNode noeud) {
		return distanceHeuristique(noeud.gridpoint);
	}

	private int distanceHeuristique(int gridpoint)
	{
		return gridspace.distanceHeuristique(depart.gridpoint, gridpoint);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	/**
	 * Met à jour le pathfinding
	 */
	public void updatePath(Vec2<ReadOnly> positionRobot)
	{
		depart.gridpoint = gridspace.computeGridPoint(positionRobot);
		km += distanceHeuristique(last);
		last = depart.gridpoint;
		
		
		
		computeShortestPath();
	}

}
