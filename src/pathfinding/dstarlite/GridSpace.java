package pathfinding.dstarlite;

import java.util.BitSet;

import obstacles.ObstaclesIterator;
import obstacles.ObstaclesMemory;
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
 * Ordre des directions : NO, SE, NE, SO, N, S, O, E;
 * @author pf
 *
 */

public class GridSpace implements Service
{
	protected Log log;
	private ObstaclesIterator iterator;
	private ObstaclesMemory memory;
	private boolean ignoreElementJeu;
	private Table table;
	/**
	 * Comme on veut que le DStarLite recherche plus de noeuds qu'il n'y en aurait besoin, ce coeff ne vaut pas 1
	 */
//	private static final int COEFF_HEURISTIQUE = 2;

	public static final int PRECISION = 6;
	public static final int NB_POINTS_POUR_TROIS_METRES = (1 << PRECISION);
	public static final int NB_POINTS_POUR_DEUX_METRES = (int) ((1 << PRECISION)*2./3.);
	public static final double DISTANCE_ENTRE_DEUX_POINTS = 3000./NB_POINTS_POUR_TROIS_METRES;
	public static final int DISTANCE_ENTRE_DEUX_POINTS_1024 = 3000 << (10-PRECISION);
	public static final int NB_POINTS = NB_POINTS_POUR_DEUX_METRES * NB_POINTS_POUR_TROIS_METRES;
	private static final int X_MAX = NB_POINTS_POUR_TROIS_METRES - 1;
	private static final int Y_MAX = NB_POINTS_POUR_DEUX_METRES - 1;
	
	// cette grille est constante, c'est-à-dire qu'elle ne contient que les obstacles fixes
	private static BitSet grilleStatique = new BitSet(NB_POINTS);
	
	// grille des obstacles dynamiques
	private BitSet grilleDynamique = new BitSet(NB_POINTS);
	
	// grille des éléments de jeu
	private BitSet grilleElementJeu = new BitSet(NB_POINTS);
	
	private static BitSet masqueObs = null;
	private static int centreMasque;
	private static int tailleMasque;
	
	static
	{
		// A priori, tout est traversable
		for(int i = 0; i < NB_POINTS; i++)
			grilleStatique.set(i);
	}
	
	public GridSpace(Log log, ObstaclesMemory memory, Table table)
	{
		this.memory = memory;
		this.log = log;
		this.table = table;
		this.iterator = new ObstaclesIterator(log, memory);

		// A priori, tout est traversable
		for(int i = 0; i < NB_POINTS; i++)
		{
			grilleDynamique.set(i);
			grilleElementJeu.set(i);
		}

	}
	
	/**
	 * Constructeur privé utilisé par le clone.
	 * Comme ces gridspace cloné n'ont pas besoin du ObstaclesMemory, on lui donne pas
	 * @param log
	 * @param iterator
	 * @param table
	 */
	private GridSpace(Log log, ObstaclesIterator iterator, Table table)
	{
		this.log = log;
		this.table = table;
		this.iterator = iterator;

		// A priori, tout est traversable
		for(int i = 0; i < NB_POINTS; i++)
		{
			grilleDynamique.set(i);
			grilleElementJeu.set(i);
		}

	}
	
	/**
	 * On utilise la distance octile pour l'heuristique (surtout parce que c'est rapide)
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public final int distanceHeuristiqueDStarLite(int pointA, int pointB)
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
	public int getGridPointVoisin(int point, int direction)
	{
		int x = point & (NB_POINTS_POUR_TROIS_METRES - 1);
		int y = point >> PRECISION;

		switch(direction)
		{
		case 0:
//			NO
			if(x > 0 && y < Y_MAX)
				return point+NB_POINTS_POUR_TROIS_METRES-1;
			return -1; // hors table

		case 1:
//			SE
			if(x < X_MAX && y > 0)
				return point-NB_POINTS_POUR_TROIS_METRES+1;
			return -1; // hors table

		case 2:
//			NE
			if(x < X_MAX && y < Y_MAX)
				return point+NB_POINTS_POUR_TROIS_METRES+1;
			return -1; // hors table

		case 3:
//			SO
			if(x > 0 && y > 0)
				return point-NB_POINTS_POUR_TROIS_METRES-1;
			return -1; // hors table

		case 4:
//			N
			if(y < Y_MAX)
				return point+NB_POINTS_POUR_TROIS_METRES;
			return -1; // hors table

		case 5:
//			S
			if(y > 0)
				return point-NB_POINTS_POUR_TROIS_METRES;
			return -1; // hors table

		case 6:
//			O
			if(x > 0)
				return point-1;
			return -1; // hors table

//		case 7:
		default:
//			E
			if(x < X_MAX)
				return point+1;
			return -1; // hors table
		}
		
	}

	@Override
	public void useConfig(Config config)
	{
		int rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		int rayonPoint = (int) Math.round(rayonEnnemi / DISTANCE_ENTRE_DEUX_POINTS);
		tailleMasque = 2*(rayonPoint+1)+1;
		centreMasque = tailleMasque / 2;
		masqueObs = new BitSet(tailleMasque * tailleMasque);
		for(int i = 0; i < tailleMasque; i++)
			for(int j = 0; j < tailleMasque; j++)
				if((i-centreMasque) * (i-centreMasque) + (j-centreMasque) * (j-centreMasque) <= rayonPoint*rayonPoint)
				{
					int i2, j2;
					i2 = i - 1;
					j2 = j;
					if(i2 >= 0 && (i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) > rayonPoint*rayonPoint)
					{
						masqueObs.set(i*tailleMasque+j);
						continue;
					}
					i2 = i + 1;
					if(i2 < tailleMasque && (i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) > rayonPoint*rayonPoint)
					{
						masqueObs.set(i*tailleMasque+j);
						continue;
					}
					i2 = i;
					j2 = j - 1;
					if(j2 >= 0 && (i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) > rayonPoint*rayonPoint)
					{
						masqueObs.set(i*tailleMasque+j);
						continue;
					}
					j2 = j + 1;
					if(j2 < tailleMasque && (i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) > rayonPoint*rayonPoint)
					{
						masqueObs.set(i*tailleMasque+j);
						continue;
					}

			}
/*
		System.out.println("Masque : ");
		for(int i = 0; i < tailleMasque; i++)
		{
			for(int j = 0; j < tailleMasque; j++)
				if(masqueObs.get(i*tailleMasque+j))
					System.out.print("X");
				else
					System.out.print(".");
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
		return grilleStatique.get(getGridPointVoisin(gridpoint, direction));
	}

	/**
	 * Utilisé pour l'affichage
	 * @param gridpoint
	 * @param direction
	 * @return
	 */
	public boolean isTraversable(int gridpoint)
	{
		return grilleStatique.get(gridpoint);
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
		if(!isTraversable(point, i)) // TODO : et les obstacles dynamiques ?
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
	
	/**
	 * Update le gridspace à partir de la liste des obstacles et de la table
	 * Utilisé par un thread régulièrement
	 * Récupère la différence dans la grilleDynamique
	 * @return
	 */
	public BitSet update()
	{
		iterator.reinitNow();
		BitSet old = (BitSet) grilleDynamique.clone();
		updateGrilleDynamique();
		old.xor(grilleDynamique);
		return old;
	}

	public GridSpace clone(long date)
	{
		GridSpace out = new GridSpace(log, iterator.clone(date), table.clone());
		// Ceci est une copie. Si si.
		out.grilleDynamique.clear();
		out.grilleDynamique.or(grilleDynamique);
		return out;
	}

	/**
	 * Copie le gridspace, la table et l'iterator. En profite pour mettre l'iterator à jour
	 * @param other
	 * @param date
	 */
	public void copy(GridSpace other, long date)
	{
		// TODO vérifier hash de la table pour mise à jour grilleElementJeu
		table.copy(other.table);
		int hash = iterator.hashCode();
		iterator.copy(other.iterator, date);
		
		// Pas de modification de la grille dynamique, on recopie juste
		if(iterator.hashCode() == hash)
		{
			other.grilleDynamique.clear(); // copie de la grille dynamique
			other.grilleDynamique.or(grilleDynamique);
		}
		else
			updateGrilleDynamique();
	}
	
	/**
	 * Ajoute tous les obstacles à la grille.
	 * A l'appel de cette méthode, la grille est vide
	 */
	private void updateGrilleDynamique()
	{
		iterator.reinit();
		while(iterator.hasNext())
			setObstacle(iterator.next());
	}

	/**
	 * Ajoute le contour d'un obstacle de proximité dans la grille dynamique
	 * @param o
	 */
	private void setObstacle(ObstacleProximity o)
	{
		int x = getGridPointX(o.position);
		int y = getGridPointY(o.position);
		int x2, y2;
		for(int i = 0; i < tailleMasque; i++)
			for(int j = 0; j < tailleMasque; j++)
			{
				if(!masqueObs.get(i*tailleMasque + j))
					continue;
				x2 = x + i - centreMasque;
				y2 = y + j - centreMasque;
				if(x2 >= 0 && x2 <= X_MAX && y2 >= 0 && y2 <= Y_MAX)
					grilleDynamique.set(getGridPoint(x2, y2));					
			}
	}

	/**
	 * Appelé par GameState
	 * @return
	 */
	public ObstaclesIterator getIterator()
	{
		return iterator;
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
	public ObstacleProximity addObstacle(Vec2<ReadOnly> position,
			long dateActuelle, boolean urgent) {
		ObstacleProximity o = memory.add(position, dateActuelle, urgent);
		setObstacle(o);
		return o;
	}

	public Tribool isDoneTable(GameElementNames g)
	{
		return table.isDone(g);
	}

	public void setDoneTable(GameElementNames g, Tribool done)
	{
		// TODO modif grille
		table.setDone(g, done);
	}

	public void deleteOldObstacles()
	{
		memory.deleteOldObstacles();
		updateGrilleDynamique();
	}

	public long getNextDeathDate()
	{
		return memory.getNextDeathDate();
	}
}
