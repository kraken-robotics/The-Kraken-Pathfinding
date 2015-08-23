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
	
	private ArrayList<DStarLiteNode> openset = new ArrayList<DStarLiteNode>();	 // The set of tentative nodes to be evaluated
	private double km;
	private DStarLiteNode arrivee;
	private DStarLiteNode depart;
	
	private Cle calcKey(DStarLiteNode s)
	{
		s.cle.set(Math.min(s.g,s.rhs) + distanceHeuristique(s.gridpoint) + km,
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
			u.cle = calcKey(u);
			if(!contains)
				addToOpenset(u);
		}
		else if(contains)
		{
			openset.remove(u);
		}
	}
	
	private void computeShortestPath()
	{
		DStarLiteNode u;
		while(!openset.isEmpty() && ((u = openset.get(0)).cle.isLesserThan(calcKey(depart)) || depart.rhs > depart.g))
		{
			Cle kold = u.cle;
			Cle knew = calcKey(u);
			openset.remove(0);
			if(kold.isLesserThan(knew))
			{
				u.cle = knew;
				addToOpenset(u);
			}
			else if(u.g > u.rhs)
			{
				u.g = u.rhs;
//				openset.remove(u); // déjà removed
				for(int i = 0; i < 8; i++)
				{
					DStarLiteNode s = u.getVoisin(i, gridspace);
					if(gridspace.isTraversable(s.gridpoint, i))
						s.rhs = Math.min(s.rhs, DirectionGridSpace.distance(i) + u.g);
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
					if(!gridspace.isTraversable(s.gridpoint, i))
						condition = s.rhs == Integer.MAX_VALUE;
					else if(s.rhs == (DirectionGridSpace.distance(i) + gold) && s.gridpoint != arrivee.gridpoint)
						condition = true;
					if(condition)
					{
						s.rhs = DirectionGridSpace.distance(0) + s.getVoisin(0, gridspace).g;
						for(int j = 1; j < 8; j++)
						{
							DStarLiteNode s2 = s.getVoisin(j, gridspace);
							s.rhs = Math.min(s.rhs, DirectionGridSpace.distance(j) + s2.g);
						}
					}
					updateVertex(s);
				}
			}

		}
	}

	/**
	 * Calcule un nouvel itinéraire.
	 * @param arrivee
	 * @param depart
	 */
	public void computeNewPathfinding(Vec2<ReadOnly> arrivee, Vec2<ReadOnly> depart)
	{
		km = 0;
		DStarLiteNode.reinitHash();
		this.depart = new DStarLiteNode(gridspace.computeGridPoint(depart));
		this.arrivee = new DStarLiteNode(gridspace.computeGridPoint(arrivee));
		this.arrivee.rhs = 0;
		this.arrivee.cle.set(distanceHeuristique(this.arrivee.gridpoint), 0);
		openset.clear();
		openset.add(this.arrivee);
		computeShortestPath();
	}
	
	private double distanceHeuristique(int gridpoint) {
		return gridspace.distanceHeuristique(this.depart.gridpoint, gridpoint);
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
	public void updatePath()
	{
		
		computeShortestPath();
	}

}
