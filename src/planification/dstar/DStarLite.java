package planification.dstar;

import java.util.LinkedList;

import utils.Config;
import utils.Log;
import container.Service;
import exceptions.MemoryManagerException;

/**
 * DStarLite. Uniquement pour la recherche de chemin pour le moment.
 * @author pf
 * 
 */

public class DStarLite implements Service {
	
	private static final int nb_max_element = 100; // le nombre d'élément différent de l'arbre qu'on parcourt. A priori, 100 paraît suffisant.

	private LinkedList<DStarLiteNode> openset = new LinkedList<DStarLiteNode>();	 // The set of tentative nodes to be evaluated
	private double km;
	private int[] g_score = new int[nb_max_element];
	private int[] rhs = new int[nb_max_element];
	private DStarLiteNode arrivee;
	private DStarLiteNode depart;
	
	private DStarArcManager dstararcmanager;
	protected Log log;
	protected Config config;
	
	public DStarLite(Log log, Config config, DStarArcManager dstararcmanager)
	{
		this.log = log;
		this.config = config;
		this.dstararcmanager = dstararcmanager;
	}
	
	@Override
	public void updateConfig()
	{
		// TODO Auto-generated method stub
		
	}
	
	private Cle calcKey(DStarLiteNode s)
	{
		return new Cle(Math.min(s.g,s.rhs)+dstararcmanager.heuristicCost(depart, s),Math.min(s.g,s.rhs)); // TODO
	}
	
	private void initialize()
	{
		openset.clear();
		double km = 0;
		for(int i = 0; i < nb_max_element; i++)
		{
			g_score[i] = Integer.MAX_VALUE;
			rhs[i] = Integer.MAX_VALUE;
		}
		rhs[arrivee.hash] = 0;
		arrivee.cle = new Cle(dstararcmanager.heuristicCost(depart, arrivee),0);
		openset.add(arrivee);
	}
	
	private void updateVertex(DStarLiteNode u)
	{
		// TODO: appartenance à openset? (U)
		if(u.g != u.rhs)
			u.cle = calcKey(u);
	}
	
	private void computeShortestPath()
	{
		while(!openset.isEmpty() && (openset.getFirst().cle.isLesserThan(calcKey(depart)) || depart.rhs > depart.g))
		{
			DStarLiteNode u = openset.getFirst();
			Cle kold = u.cle;
			Cle knew = calcKey(u);
			if(kold.isLesserThan(knew))
				u.cle = knew;
			else if(u.g > u.rhs)
			{
				u.g = u.rhs;
				openset.removeFirst();
				try {
					dstararcmanager.reinitIterator(u.state);
				} catch (MemoryManagerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while(dstararcmanager.hasNext())
				{
					DStarLiteNode s = dstararcmanager.getPredecessor();
					s.rhs = Math.min(s.rhs, dstararcmanager.distanceTo(s.state, u.gridpoint));
					updateVertex(s);
				}
			}
			else
			{
				int gold = u.g;
				u.g = Integer.MAX_VALUE;

				try {
					dstararcmanager.reinitIterator(u.state);
				} catch (MemoryManagerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while(dstararcmanager.hasNext())
				{
					DStarLiteNode s = dstararcmanager.getPredecessor();
					if(s.rhs == dstararcmanager.distanceTo(s.state, u.gridpoint) + gold)
						if(s.hash != arrivee.hash)
					s.rhs = Math.min(s.rhs, dstararcmanager.distanceTo(u.state, s.gridpoint));
					updateVertex(s);
				}
			}

		}
	}

}
