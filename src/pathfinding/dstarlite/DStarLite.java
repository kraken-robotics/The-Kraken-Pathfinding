package pathfinding.dstarlite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import permissions.ReadOnly;
import tests.graphicLib.Fenetre;
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
	private Fenetre fenetre;
	
	public DStarLite(Log log, GridSpace gridspace)
	{
		this.log = log;
		this.gridspace = gridspace;
		for(int i = 0; i < GridSpace.NB_POINTS; i++)
		{
			memory[i] = new DStarLiteNode(i);
		}
		if(Config.graphicDStarLite)
			fenetre = Fenetre.getInstance();
	}
	
	private DStarLiteNode[] memory = new DStarLiteNode[GridSpace.NB_POINTS];
//	private BitSet contained = new BitSet(GridSpace.NB_POINTS);

	private PriorityQueue<DStarLiteNode> openset = new PriorityQueue<DStarLiteNode>(GridSpace.NB_POINTS, new DStarLiteNodeComparator());
	private int km;
	private DStarLiteNode arrivee;
	private DStarLiteNode depart;
	private int last;
	private long nbPF = 0;
	
	private Cle knew = new Cle();
	private Cle inutile = new Cle();

	private Cle calcKey(DStarLiteNode s, Cle copy)
	{
		copy.set(add(Math.min(s.g,s.rhs), distanceHeuristique(s.gridpoint), km),
				Math.min(s.g, s.rhs));
		return copy;
	}

	private boolean isThisNodeUptodate(int gridpoint)
	{
		return memory[gridpoint].nbPF == nbPF;
	}
	
	private DStarLiteNode getFromMemory(int gridpoint)
	{
		DStarLiteNode out = memory[gridpoint];
		
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
		if(u.g != u.rhs)
		{
			calcKey(u, u.cle);
//			if(contained.get(u.gridpoint))
//				openset.remove(u);
			openset.add(u);
//			contained.set(u.gridpoint);
			if(Config.graphicDStarLite)
				fenetre.setColor(u.gridpoint, Fenetre.Couleur.JAUNE);
		}
	}
	
	private void computeShortestPath()
	{
		DStarLiteNode u;
		// TODO : continuer à étendre des noeuds même après la fin de l'algo
		while(!openset.isEmpty() && ((u = openset.peek()).cle.isLesserThan(calcKey(depart, inutile)) || depart.rhs > depart.g))
		{
			if(Config.graphicDStarLite)
				fenetre.setColor(u.gridpoint, Fenetre.Couleur.BLEU);
			if(u.done)
			{
				openset.poll();
				continue;
			}
			u.done = true;
			
			Cle kold = u.cle.clone();
			calcKey(u, knew);
			if(kold.isLesserThan(knew))
			{
//				log.debug("Cas 1");
				knew.copy(u.cle);
				openset.poll();
				openset.add(u);
				if(Config.graphicDStarLite)
					fenetre.setColor(u.gridpoint, Fenetre.Couleur.JAUNE);
			}
			else if(u.g > u.rhs)
			{
//				log.debug("Cas 2");
				u.g = u.rhs;
				openset.poll();
				if(Config.graphicDStarLite)
					fenetre.setColor(u.gridpoint, Fenetre.Couleur.ROUGE);
				for(int i = 0; i < 8; i++)
				{
					int voisin = gridspace.getGridPointVoisin(u.gridpoint, i);
					if(voisin < 0)
						continue;
					DStarLiteNode s = getFromMemory(voisin);
					s.rhs = Math.min(s.rhs, add(gridspace.distanceDStarLite(u.gridpoint, i), u.g));
					updateVertex(s);
				}
			}
			else
			{
//				log.debug("Cas 3");
				int gold = u.g;
				u.g = Integer.MAX_VALUE;
				for(int i = 0; i < 8; i++)
				{
					int voisin = gridspace.getGridPointVoisin(u.gridpoint, i);
					if(voisin < 0)
						continue;
					DStarLiteNode s = getFromMemory(voisin);
					if(s == null)
						continue;
					if(s.rhs == add(gridspace.distanceDStarLite(u.gridpoint, i), gold) && s.gridpoint != arrivee.gridpoint)
					{
						s.rhs = Integer.MAX_VALUE;
						for(int j = 0; j < 8; j++)
						{
							voisin = gridspace.getGridPointVoisin(s.gridpoint, j);
							if(voisin < 0)
								continue;
							DStarLiteNode s2 = getFromMemory(voisin);
							s.rhs = Math.min(s.rhs, add(gridspace.distanceDStarLite(s.gridpoint, j), s2.g));
						}
					}
					updateVertex(s);
				}
				if(u.rhs == gold && u.gridpoint != depart.gridpoint)
				{
					u.rhs = Integer.MAX_VALUE;
					for(int i = 0; i < 8; i++)
					{
						int voisin = gridspace.getGridPointVoisin(u.gridpoint, i);
						if(voisin < 0)
							continue;
						DStarLiteNode s = getFromMemory(voisin);
						u.rhs = Math.min(u.rhs, add(gridspace.distanceDStarLite(u.gridpoint, i), s.g));
					}
				}
				updateVertex(u);
				u.done = false;
			}

		}
	}

	/**
	 * Calcule un nouvel itinéraire.
	 * @param arrivee (un Vec2)
	 * @param depart (un gridpoint)
	 */
	public void computeNewPath(Vec2<ReadOnly> depart, int arrivee)
	{
		nbPF++;
		km = 0;
		this.depart = getFromMemory(gridspace.computeGridPoint(depart));
		last = this.depart.gridpoint;

		this.arrivee = getFromMemory(arrivee);
		this.arrivee.rhs = 0;
		this.arrivee.cle.set(distanceHeuristique(this.arrivee.gridpoint), 0);
		
		openset.clear();
		openset.add(this.arrivee);
		if(Config.graphicDStarLite)
			fenetre.setColor(this.arrivee.gridpoint, Fenetre.Couleur.JAUNE);

		computeShortestPath();
	}
	
	private int distanceHeuristique(int gridpoint)
	{
		return gridspace.distanceHeuristiqueDStarLite(depart.gridpoint, gridpoint);
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
		depart = getFromMemory(gridspace.computeGridPoint(positionRobot));
		km += distanceHeuristique(last);
		last = depart.gridpoint;
		
		// TODO
		
		computeShortestPath();
	}
	
	/**
	 * Utilisé pour l'affichage et le debug
	 * @return
	 */
	public ArrayList<Vec2<ReadOnly>> itineraireBrut()
	{
		ArrayList<Vec2<ReadOnly>> trajet = new ArrayList<Vec2<ReadOnly>>();

		DStarLiteNode node = depart;
		DStarLiteNode min = null;
		int coutMin;
		
		while(!node.equals(arrivee))
		{
			trajet.add(gridspace.computeVec2(node.gridpoint));
			log.debug(node);
			coutMin = Integer.MAX_VALUE;
			
			for(int i = 0; i < 8; i++)
			{
				int voisin = gridspace.getGridPointVoisin(node.gridpoint, i);
				if(voisin < 0)
					continue;
				DStarLiteNode s = getFromMemory(voisin);
				int coutTmp = add(gridspace.distanceDStarLite(node.gridpoint, i), s.g);
				if(coutTmp < coutMin)
				{
					coutMin = coutTmp;
					min = s;
				}
			}
			node = min;
		}
		trajet.add(gridspace.computeVec2(arrivee.gridpoint));
		return trajet;
		
	}
	
	public int heuristicCostThetaStar(int gridpoint)
	{
		return getFromMemory(gridpoint).g;
	}
	
	public int getHashDebut()
	{
		return depart.gridpoint;
	}

	public int getHashArrivee()
	{
		return arrivee.gridpoint;
	}

	/**
	 * Somme en faisant attention aux valeurs infinies
	 * @param a
	 * @param b
	 * @return
	 */
	private int add(int a, int b)
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
	private int add(int a, int b, int c)
	{
		if(a == Integer.MAX_VALUE || b  == Integer.MAX_VALUE || c  == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return a + b + c;
	}

	private ArrayList<Integer> voisinsTries = new ArrayList<Integer>();
	private ArrayList<Integer> notes = new ArrayList<Integer>();
	
	/**
	 * Renvoie la liste des nodes du plus court au plus long
	 * @param gridpoint
	 * @return
	 */
	public ArrayList<Integer> getListVoisins(int gridpoint)
	{
		voisinsTries.clear();
		notes.clear();
		for(int i = 0 ; i < 8 ; i++)
		{
			int voisin = gridspace.getGridPointVoisin(gridpoint, i);
			if(voisin < 0 || !isThisNodeUptodate(voisin))
				continue;

			int c = gridspace.distanceDStarLite(gridpoint, i);
			if(c == Integer.MAX_VALUE)
				continue;
			
			int note = c + memory[voisin].g;
			addInVoisinsTries(note, voisin);
		}			
		return voisinsTries;
	}

	private void addInVoisinsTries(int note, int gridpoint)
	{
		Iterator<Integer> iterator = notes.listIterator();
		int k = 0;
		while(iterator.hasNext())
		{
			if(note < iterator.next())
			{
				notes.add(k, note);
				voisinsTries.add(k, gridpoint);
				return;
			}
			k++;
		}
		voisinsTries.add(gridpoint);
	}
	
}
