package pathfinding.dstarlite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import obstacles.types.ObstacleProximity;
import pathfinding.astarCourbe.HeuristiqueCourbe;
import permissions.ReadOnly;
import tests.graphicLib.Fenetre;
import utils.Config;
import utils.Log;
import utils.Vec2;
import container.Service;
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

public class DStarLite implements Service, HeuristiqueCourbe
{
	protected Log log;
	private GridSpace gridspace;
	private Fenetre fenetre;

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
	
	public DStarLite(Log log, GridSpace gridspace)
	{
		this.log = log;
		this.gridspace = gridspace;
		
		for(int i = 0; i < GridSpace.NB_POINTS; i++)
			memory[i] = new DStarLiteNode(i);

		if(Config.graphicDStarLite)
			fenetre = Fenetre.getInstance();
	}
	
	private DStarLiteNode[] memory = new DStarLiteNode[GridSpace.NB_POINTS];
//	private BitSet contained = new BitSet(GridSpace.NB_POINTS);

	private PriorityQueue<DStarLiteNode> openset = new PriorityQueue<DStarLiteNode>(GridSpace.NB_POINTS, new DStarLiteNodeComparator());
	private int km;
	private DStarLiteNode arrivee;
	private DStarLiteNode depart;
	private int lastDepart;
	private long nbPF = 0;
	private ArrayList<Integer> obstaclesConnus = new ArrayList<Integer>();
	private Cle knew = new Cle();
	private Cle inutile = new Cle();

	private final Cle calcKey(DStarLiteNode s, Cle copy)
	{
		copy.set(add(Math.min(s.g,s.rhs), distanceHeuristique(s.gridpoint), km),
				Math.min(s.g, s.rhs));
		return copy;
	}

	public boolean isThisNodeUptodate(int gridpoint)
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
		if(u.g != u.rhs && !u.done)
		{
			calcKey(u, u.cle);
//			if(contained.get(u.gridpoint))
//				openset.remove(u);
			openset.add(u);
//			contained.set(u.gridpoint);
			if(Config.graphicDStarLite)
				fenetre.setColor(u.gridpoint, Fenetre.Couleur.BLEU);
		}
	}
	
	private void computeShortestPath() throws PathfindingException
	{
		DStarLiteNode u;
		Cle kold = new Cle();
		// TODO : continuer à étendre des noeuds même après la fin de l'algo
		while(!openset.isEmpty() && ((u = openset.peek()).cle.isLesserThan(calcKey(depart, inutile)) || depart.rhs > depart.g))
		{
			if(u.done)
			{
				openset.poll();
				continue;
			}
			if(Config.graphicDStarLite)
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
				if(Config.graphicDStarLite)
					fenetre.setColor(u.gridpoint, Fenetre.Couleur.BLEU);
			}
			else if(u.g > u.rhs)
			{
//				log.debug("Cas 2");
				u.g = u.rhs;
				openset.poll();
				u.done = true;
				if(Config.graphicDStarLite)
					fenetre.setColor(u.gridpoint, Fenetre.Couleur.ROUGE);
				for(int i = 0; i < 8; i++)
				{
					int voisin = GridSpace.getGridPointVoisin(u.gridpoint, i);
					if(voisin < 0)
						continue;
					DStarLiteNode s = getFromMemory(voisin);
					s.rhs = Math.min(s.rhs, add(distanceDynamique(u.gridpoint, i), u.g));
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
					int voisin = GridSpace.getGridPointVoisin(u.gridpoint, i);
					if(voisin < 0)
						continue;
					DStarLiteNode s = getFromMemory(voisin);
//					if(s == null)
//						continue;
					if(s.rhs == add(distanceDynamique(u.gridpoint, i), gold) && s.gridpoint != arrivee.gridpoint)
					{
						s.rhs = Integer.MAX_VALUE;
						for(int j = 0; j < 8; j++)
						{
							voisin = GridSpace.getGridPointVoisin(s.gridpoint, j);
							if(voisin < 0)
								continue;
							DStarLiteNode s2 = getFromMemory(voisin);
							s.rhs = Math.min(s.rhs, add(distanceDynamique(s.gridpoint, j), s2.g));
						}
					}
					updateVertex(s);
				}
				if(u.rhs == gold && u.gridpoint != arrivee.gridpoint)
				{
					u.rhs = Integer.MAX_VALUE;
					for(int i = 0; i < 8; i++)
					{
						int voisin = GridSpace.getGridPointVoisin(u.gridpoint, i);
						if(voisin < 0)
							continue;
						DStarLiteNode s = getFromMemory(voisin);
						u.rhs = Math.min(u.rhs, add(distanceDynamique(u.gridpoint, i), s.g));
					}
				}
				updateVertex(u);
			}

		}

		if(depart.rhs == Integer.MAX_VALUE)
			throw new PathfindingException();
	}

	/**
	 * Calcule un nouvel itinéraire.
	 * @param arrivee (un Vec2)
	 * @param depart (un gridpoint)
	 * @throws PathfindingException 
	 */
	public void computeNewPath(Vec2<ReadOnly> depart, Vec2<ReadOnly> arrivee) throws PathfindingException
	{
		computeNewPath(GridSpace.computeGridPoint(depart), GridSpace.computeGridPoint(arrivee));
	}
	/**
	 * Calcule un nouvel itinéraire.
	 * @param arrivee (un Vec2)
	 * @param depart (un gridpoint)
	 * @throws PathfindingException 
	 */
	public void computeNewPath(int depart, int arrivee) throws PathfindingException
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
		if(Config.graphicDStarLite)
		{
			fenetre.setColor(this.arrivee.gridpoint, Fenetre.Couleur.JAUNE);
			fenetre.setColor(this.depart.gridpoint, Fenetre.Couleur.VIOLET);
		}

		obstaclesConnus = gridspace.startNewPathfinding();
		log.debug("Nb obstacles connus : "+obstaclesConnus.size());
		if(Config.graphicDStarLite)
			for(Integer i : obstaclesConnus)
				fenetre.setColor(GridSpace.getGridPointVoisin(i >> 3, i & 7), Fenetre.Couleur.NOIR);

		computeShortestPath();
		
		if(Config.graphicDStarLite)
			itineraireBrut();
	}
	
	private final int distanceHeuristique(int gridpoint)
	{
		return GridSpace.distanceHeuristiqueDStarLite(depart.gridpoint, gridpoint);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	/**
	 * Met à jour le pathfinding
	 * @throws PathfindingException 
	 */
	public void updatePath(Vec2<ReadOnly> positionRobot) throws PathfindingException
	{
		depart = getFromMemory(GridSpace.computeGridPoint(positionRobot));
		km += distanceHeuristique(lastDepart);
		lastDepart = depart.gridpoint;
		ArrayList<ObstacleProximity>[] obs = gridspace.getOldAndNewObstacles();
		
		for(ObstacleProximity o : obs[0])
		{
			for(Integer i : o.getMasque())
			{
				obstaclesConnus.remove(i);
				if(!obstaclesConnus.contains(i))
				{
					// Retrait d'un obstacle. Le coût va donc diminuer.
					int upoint = i >> GridSpace.DECALAGE_POUR_DIRECTION;
					DStarLiteNode u = getFromMemory(upoint);
					int dir = (i & ((1 << GridSpace.DECALAGE_POUR_DIRECTION) - 1));
					DStarLiteNode v = getFromMemory(GridSpace.getGridPointVoisin(upoint, dir));
					u.rhs = Math.min(u.rhs, add(v.g, gridspace.distanceStatique(upoint, dir)));
					updateVertex(u);
				}
			}
		}
		for(ObstacleProximity o : obs[1])
		{
			for(Integer i : o.getMasque())
			{
				if(!obstaclesConnus.contains(i))
				{
					// Ajout d'un obstacle
					int upoint = i >> GridSpace.DECALAGE_POUR_DIRECTION;
					DStarLiteNode u = getFromMemory(upoint);
					int dir = (i & ((1 << GridSpace.DECALAGE_POUR_DIRECTION) - 1));
					DStarLiteNode v = getFromMemory(GridSpace.getGridPointVoisin(upoint, dir));

					if(u.rhs == add(gridspace.distanceStatique(upoint, dir), v.g) && u.equals(arrivee))
					{
						for(int voisin = 0; voisin < 8; voisin++)
							u.rhs = Math.min(u.rhs, add(gridspace.distanceStatique(u.gridpoint, voisin), getFromMemory(GridSpace.getGridPointVoisin(u.gridpoint, i)).g));
					}
					updateVertex(u);
				}
				obstaclesConnus.add(i);
			}
		}

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
			trajet.add(GridSpace.computeVec2(node.gridpoint));

			if(Config.graphicDStarLite)
				fenetre.setColor(node.gridpoint, Fenetre.Couleur.VIOLET);

			coutMin = Integer.MAX_VALUE;
			
			for(int i = 0; i < 8; i++)
			{
				int voisin = GridSpace.getGridPointVoisin(node.gridpoint, i);
				if(voisin < 0)
					continue;
				DStarLiteNode s = getFromMemory(voisin);
				int coutTmp = add(gridspace.distanceStatique(node.gridpoint, i), s.g);
				if(coutTmp < coutMin)
				{
					coutMin = coutTmp;
					min = s;
				}
			}
			node = min;
		}
		trajet.add(GridSpace.computeVec2(arrivee.gridpoint));
		return trajet;
		
	}
	
	/**
	 * Renvoie l'heuristique au ThetaStar. Attention ! On suppose que le gridpoint est à jour.
	 * @param gridpoint
	 * @return
	 */
	@Override
	public int heuristicCostCourbe(Vec2<ReadOnly> position)
	{
		return getFromMemory(GridSpace.computeGridPoint(position)).rhs;
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
	private final int add(int a, int b, int c)
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
	// TODO : passer en tableau
	public ArrayList<Integer> getListVoisins(int gridpoint)
	{
		// TODO : plutôt un tableau
		voisinsTries.clear();
		notes.clear();
		for(int i = 0 ; i < 8 ; i++)
		{
			int voisin = GridSpace.getGridPointVoisin(gridpoint, i);
			if(voisin < 0 || !isThisNodeUptodate(voisin))
				continue;

			int c = gridspace.distanceStatique(gridpoint, i);
			if(c == Integer.MAX_VALUE)
				continue;
			
			int note = c + memory[voisin].g;
			addInVoisinsTries(note, voisin);
		}			
		return voisinsTries;
	}

	private void addInVoisinsTries(int note, int gridpoint)
	{
		int iterator = 0;
//		Iterator<Integer> iterator = notes.listIterator();
		while(iterator < notes.size())
		{
			if(note < notes.get(iterator))
			{
				notes.add(iterator, note);
				voisinsTries.add(iterator, gridpoint);
				return;
			}
			iterator++;
		}
		voisinsTries.add(gridpoint);
	}

	private int distanceDynamique(int point, int dir)
	{
		if(obstaclesConnus.contains((point << GridSpace.DECALAGE_POUR_DIRECTION) + dir))
			return Integer.MAX_VALUE;
		else
		{
//			log.debug("OK pour "+GridSpace.computeVec2(point));
			return gridspace.distanceStatique(point, dir);
		}
	}
	
}
