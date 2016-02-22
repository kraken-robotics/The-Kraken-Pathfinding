package pathfinding.dstarlite;

import java.util.ArrayList;
import java.util.BitSet;

import obstacles.ObstaclesFixes;
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.ObstacleProximity;
import permissions.ReadOnly;
import permissions.ReadWrite;
import table.GameElementNames;
import table.Table;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import container.Service;
import enums.Tribool;

/**
 * La classe qui contient la grille utilisée par le pathfinding.
 * Utilisée uniquement pour le pathfinding DStarLite.
 * Notifie quand il y a un changement d'obstacles
 * Ordre des directions : NO, SE, NE, SO, N, S, O, E;
 * @author pf
 *
 */

public class GridSpace implements Service
{
	protected Log log;
	private ObstaclesIteratorPresent iteratorDStarLite; // n'est présent que dans que gridspace du real gamestate
	private ObstaclesMemory obstaclesMemory;
	private Table table;

	/**
	 * Comme on veut que le DStarLite recherche plus de noeuds qu'il n'y en aurait besoin, ce coeff ne vaut pas 1
	 */
//	private static final int COEFF_HEURISTIQUE = 2;

	public static final int PRECISION = 6;
	public static final int DEUXIEME_POINT_COUPLE = 12;
	public static final int NB_POINTS_POUR_TROIS_METRES = (1 << PRECISION);
	public static final int NB_POINTS_POUR_DEUX_METRES = (int) ((1 << PRECISION)*2./3.);
	public static final double DISTANCE_ENTRE_DEUX_POINTS = 3000./NB_POINTS_POUR_TROIS_METRES;
	public static final int DISTANCE_ENTRE_DEUX_POINTS_1024 = 3000 << (10-PRECISION);
	public static final int NB_POINTS = NB_POINTS_POUR_DEUX_METRES * NB_POINTS_POUR_TROIS_METRES;
	private static final int X_MAX = NB_POINTS_POUR_TROIS_METRES - 1;
	private static final int Y_MAX = NB_POINTS_POUR_DEUX_METRES - 1;
	
	// cette grille est constante, c'est-à-dire qu'elle ne contient que les obstacles fixes
	private static BitSet grilleStatique = null;
	
	private static ArrayList<Integer> masque = new ArrayList<Integer>();
	private static int centreMasque;
	private long deathDateLastObstacle;
	
	public GridSpace(Log log, ObstaclesMemory obstaclesMemory, Table table)
	{
		this.obstaclesMemory = obstaclesMemory;
		this.log = log;
		this.table = table;
		this.iteratorDStarLite = new ObstaclesIteratorPresent(log, obstaclesMemory);

		if(grilleStatique == null)
		{
			// Initialisation, une fois pour toutes, de la grille statique
			grilleStatique = new BitSet(NB_POINTS);
			for(int i = 0; i < NB_POINTS; i++)
			{
				for(ObstaclesFixes o : ObstaclesFixes.values)
					if(o.getObstacle().isInObstacle(computeVec2(i)))
					{
						grilleStatique.set(i);
						break; // on ne vérifie pas les autres obstacles
					}
			}
			log.debug("Grille statique initialisée");
		}
	}
	
	/**
	 * Constructeur privé utilisé par le clone.
	 * Comme ces gridspace cloné n'ont pas besoin du ObstaclesMemory, on lui donne pas
	 * @param log
	 * @param iterator
	 * @param table
	 */
	private GridSpace(Log log, Table table)
	{
		this.log = log;
		this.table = table;
		// iteratorDStarLite n'est pas initialisé dans un clone
	}
	
	/**
	 * On utilise la distance octile pour l'heuristique (surtout parce que c'est rapide)
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public static final int distanceHeuristiqueDStarLite(int pointA, int pointB)
	{
		int dx = Math.abs((pointA & (NB_POINTS_POUR_TROIS_METRES - 1)) - (pointB & (NB_POINTS_POUR_TROIS_METRES - 1))); // ceci est un modulo
		int dy = Math.abs((pointA >> PRECISION) - (pointB >> PRECISION)); // ceci est une division
		return 1000 * Math.max(dx, dy) + 414 * Math.min(dx, dy);
//		return (int) Math.round(COEFF_HEURISTIQUE * 1000 * Math.hypot(dx, dy));
	}

	/**
	 * Récupère le voisin de "point" dans la direction indiquée.
	 * Renvoie -1 si un tel voisin est hors table
	 * @param point
	 * @param direction
	 * @return
	 */
	public static int getGridPointVoisin(int point, int direction)
	{
		int x = point & (NB_POINTS_POUR_TROIS_METRES - 1);
		int y = point >> PRECISION;

		switch(direction)
		{
		case 0:
//			NO
			if(x > 0 && y < Y_MAX)
				return point + NB_POINTS_POUR_TROIS_METRES - 1;
			return -1; // hors table

		case 1:
//			SE
			if(x < X_MAX && y > 0)
				return point - NB_POINTS_POUR_TROIS_METRES + 1;
			return -1; // hors table

		case 2:
//			NE
			if(x < X_MAX && y < Y_MAX)
				return point + NB_POINTS_POUR_TROIS_METRES + 1;
			return -1; // hors table

		case 3:
//			SO
			if(x > 0 && y > 0)
				return point - NB_POINTS_POUR_TROIS_METRES - 1;
			return -1; // hors table

		case 4:
//			N
			if(y < Y_MAX)
				return point + NB_POINTS_POUR_TROIS_METRES;
			return -1; // hors table

		case 5:
//			S
			if(y > 0)
				return point - NB_POINTS_POUR_TROIS_METRES;
			return -1; // hors table

		case 6:
//			O
			if(x > 0) // en fait, on pourrait directement renvoyer point - 1 dans tous les cas…
				return point - 1;
			return -1; // hors table

//		case 7:
		default:
//			E
			if(x < X_MAX)
				return point + 1;
			return -1; // hors table
		}
		
	}

	@Override
	public void useConfig(Config config)
	{
		int rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		int rayonPoint = (int) Math.round(rayonEnnemi / DISTANCE_ENTRE_DEUX_POINTS);
		int tailleMasque = 2*(rayonPoint+1)+1;
		centreMasque = tailleMasque / 2;
		for(int i = 1; i < tailleMasque-1; i++)
			for(int j = 1; j < tailleMasque-1; j++)
				if((i-centreMasque) * (i-centreMasque) + (j-centreMasque) * (j-centreMasque) <= rayonPoint*rayonPoint)
					for(int a = -1; a <= 1; a++)
						for(int b = -1; b <= 1; b++)
						{
							int i2 = i + a, j2 = j + b;
							if((i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) > rayonPoint*rayonPoint)
								masque.add((((j2 << PRECISION) +i2) << DEUXIEME_POINT_COUPLE) + (j << PRECISION) + i);
						}
/*		log.debug("Taille du masque : "+masque.size());
		System.out.println("Masque : ");
		for(int i = 0; i < tailleMasque; i++)
		{
			for(int j = 0; j < tailleMasque; j++)
			{
				boolean aff = false;
				for(Integer c : masque)
				{
					if((i << PRECISION) + j == (c & ((1 << DEUXIEME_POINT_COUPLE) - 1)))
					{
						aff = true;
						System.out.print("X");
						break;
					}
				}
				if(!aff)
					System.out.print(".");
			}
			System.out.println();
		}*/
	}

	@Override
	public void updateConfig(Config config)
	{}

	/**
	 * Signale si on peut passer d'un point à un de ses voisins.
	 * On suppose que ce voisin n'est pas hors table (sinon, ça lève une exception)
	 * @param gridpoint
	 * @param direction
	 * @return
	 */
	private boolean isTraversable(int gridpoint, int direction)
	{
		int voisin = getGridPointVoisin(gridpoint, direction);
		if(grilleStatique.get(voisin))
			return false;
		int couple = (gridpoint << DEUXIEME_POINT_COUPLE) + voisin;
		iteratorDStarLite.reinit();
		while(iteratorDStarLite.hasNext())
		{
			ObstacleProximity o = iteratorDStarLite.next();
			if(o.getMasque().contains(couple))
				return false;
		}
		return true;
	}

	public static int getGridPointX(Vec2<ReadOnly> p)
	{
		return (int) Math.round((p.x+1500) / GridSpace.DISTANCE_ENTRE_DEUX_POINTS);
	}

	public static int getGridPointY(Vec2<ReadOnly> p)
	{
		return (int) Math.round(p.y / GridSpace.DISTANCE_ENTRE_DEUX_POINTS);
	}

	public static int getGridPoint(int x, int y)
	{
		return y << PRECISION + x;
	}

	/**
	 * Renvoie l'indice du gridpoint le plus proche de cette position
	 * @param p
	 * @return
	 */
	public static int computeGridPoint(Vec2<ReadOnly> p)
	{
		return (int) (NB_POINTS_POUR_TROIS_METRES*(int) Math.round(p.y / GridSpace.DISTANCE_ENTRE_DEUX_POINTS) + Math.round((p.x+1500) / GridSpace.DISTANCE_ENTRE_DEUX_POINTS));
	}

	/**
	 * Renvoie la distance en fonction de la direction
	 * @param i
	 * @return
	 */
	public int distanceDStarLite(int point, int i) {
		// TODO
		if(!isTraversable(point, i))
			return Integer.MAX_VALUE;
		if(i < 4) // cf ordre des directions
			return 1414;
		else
			return 1000;
	}

	public static Vec2<ReadOnly> computeVec2(int gridpoint)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>();
		computeVec2(out, gridpoint);
		return out.getReadOnly();
	}

	public static void computeVec2(Vec2<ReadWrite> v, int gridpoint)
	{
		v.x = (((gridpoint & (NB_POINTS_POUR_TROIS_METRES - 1)) * GridSpace.DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10) - 1500;
		v.y = ((gridpoint >> PRECISION) * GridSpace.DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10;
	}

	public GridSpace clone()
	{
		GridSpace out = new GridSpace(log, table.clone());
		return out;
	}

	/**
	 * Copie le gridspace, la table et l'iterator. En profite pour mettre l'iterator à jour
	 * @param other
	 * @param date
	 */
	public void copy(GridSpace other)
	{
		table.copy(other.table);
	}

	/**
	 * Ajoute le contour d'un obstacle de proximité dans la grille dynamique
	 * @param o
	 */
	private ArrayList<Integer> getMasqueObstacle(Vec2<ReadOnly> position)
	{
		int x = getGridPointX(position);
		int y = getGridPointY(position);
//		log.debug("xy : "+x+" "+y);
		int xC1, yC1, xC2, yC2;
		ArrayList<Integer> out = new ArrayList<Integer>();
		for(Integer c : masque)
		{
//			log.debug("c : "+c);
			int p1 = c >> DEUXIEME_POINT_COUPLE;
			int p2 = c & ((1 << DEUXIEME_POINT_COUPLE) - 1);
			xC1 = (p1 & (NB_POINTS_POUR_TROIS_METRES - 1)) + x - centreMasque;
			yC1 = (p1 >> PRECISION) + y - centreMasque;
			xC2 = (p2 & (NB_POINTS_POUR_TROIS_METRES - 1)) + x - centreMasque;
			yC2 = (p2 >> PRECISION) + y - centreMasque;

//			log.debug("Obtenu : "+((((yC1 << PRECISION) +xC1) << DEUXIEME_POINT_COUPLE) + (yC2 << PRECISION) +xC2));
			
//			log.debug("Lecture masque : "+xC1+" "+yC1+" "+xC2+" "+yC2);
//			log.debug("Lecture masque : "+computeVec2((yC1 << PRECISION) + xC2));

			if(xC1 >= 0 && xC1 <= X_MAX && yC1 >= 0 && yC1 <= Y_MAX
					&& xC2 >= 0 && xC2 <= X_MAX && yC2 >= 0 && yC2 <= Y_MAX)
			{
				out.add(getGridPoint(xC1,yC1) << DEUXIEME_POINT_COUPLE + getGridPoint(xC2,yC2));
//				log.debug("Ajout !");
			}
		}
		return out;
	}

	/**
	 * Appelé par GameState
	 * @return
	 */
	public Table getTable()
	{
		return table;
	}

	/**
	 * Appelé par le thread des capteurs par l'intermédiaire de la classe capteurs
	 * Ajoute l'obstacle à la mémoire et dans le gridspace
	 * @param position
	 * @param dateActuelle
	 * @param urgent
	 * @return
	 */
	public synchronized ObstacleProximity addObstacle(Vec2<ReadOnly> position, boolean urgent) {
		ArrayList<Integer> masque = getMasqueObstacle(position);
		ObstacleProximity o = obstaclesMemory.add(position, urgent, masque);
		// pour un ajout, pas besoin de tout régénérer
		notify(); // changement de la grille dynamique !
		return o;
	}

	public Tribool isDoneTable(GameElementNames g)
	{
		return table.isDone(g);
	}

	public void setDoneTable(GameElementNames g, Tribool done)
	{
		table.setDone(g, done);
	}

	public synchronized void deleteOldObstacles()
	{
		log.debug("Appel de deleteOldObstacles");
		// S'il y a effectivement suppression, on régénère la grille
		if(obstaclesMemory.deleteOldObstacles())
			notify(); // changement de la grille dynamique !
	}

	public long getNextDeathDate()
	{
		return obstaclesMemory.getNextDeathDate();
	}

	/**
	 * Utilisé uniquement pour les tests
	 * @param point
	 * @return
	 */
	public boolean isTraversable(int point)
	{
		for(int i = 0; i < 8; i++)
			if(!isTraversable(point, i))
				return false;
		return true;
	}
	
	/**
	 * Un nouveau DStarLite commence. Il faut lui fournir les obstacles actuels
	 * @return
	 */
	public ArrayList<ObstacleProximity> startNewPathfinding()
	{
		iteratorDStarLite.reinit();
		ArrayList<ObstacleProximity> out = new ArrayList<ObstacleProximity>();
		ObstacleProximity o = null;
		while(iteratorDStarLite.hasNext())
		{
			o = iteratorDStarLite.next();
			out.add(o);
		}
		if(o != null)
			deathDateLastObstacle = o.getDeathDate();
		else
			deathDateLastObstacle = 0;
		
		return out;
	}
	
	/**
	 * Retourne les obstacles à supprimer (indice 0) et ceux à ajouter (indice 1) dans le DStarLite
	 */
	public ArrayList<ObstacleProximity>[] getOldAndNewObstacles()
	{
		@SuppressWarnings("unchecked")
		ArrayList<ObstacleProximity>[] out = (ArrayList<ObstacleProximity>[]) new ArrayList[2];
		out[0] = new ArrayList<ObstacleProximity>();
		out[1] = new ArrayList<ObstacleProximity>();

		while(iteratorDStarLite.hasNextDead())
			out[0].add(iteratorDStarLite.next());
//		iteratorDStarLite.save();
//		iteratorDStarLite.init(System.currentTimeMillis());

		long tmp = deathDateLastObstacle;
		while(iteratorDStarLite.hasNext())
		{
			ObstacleProximity o = iteratorDStarLite.next();
			long deathDate = o.getDeathDate();
			if(deathDate > deathDateLastObstacle)
			{
				tmp = deathDate;
				out[1].add(o);
			}
		}
		deathDateLastObstacle = tmp;

		return out;
	}
}
