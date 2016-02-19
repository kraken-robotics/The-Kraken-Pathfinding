package pathfinding.dstarlite;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.ListIterator;

import obstacles.ObstaclesFixes;
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
 * Notifie quand il y a un changement d'obstacles
 * Ordre des directions : NO, SE, NE, SO, N, S, O, E;
 * @author pf
 *
 */

public class GridSpace implements Service
{
	protected Log log;
	private ObstaclesIterator iterator;
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
	
	// couples de points dont le coût a été changé
	private ArrayList<Integer> couplesCout = new ArrayList<Integer>();
	
	private static ArrayList<Integer> masque = new ArrayList<Integer>();
	private static int centreMasque;
	private int nbCouplesEnvoyes = 0;
	
	public GridSpace(Log log, ObstaclesMemory obstaclesMemory, Table table)
	{
		this.obstaclesMemory = obstaclesMemory;
		this.log = log;
		this.table = table;
		this.iterator = new ObstaclesIterator(log, obstaclesMemory);

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
	private GridSpace(Log log, ObstaclesIterator iterator, Table table)
	{
		this.log = log;
		this.table = table;
		this.iterator = iterator;
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
				{
					int i2, j2;
					i2 = i - 1;
					j2 = j;
					if((i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) > rayonPoint*rayonPoint)
					{
//						log.debug("1 Dans masque : "+i+" "+j+" "+i2+" "+j2);
						masque.add((((j2 << PRECISION) +i2) << DEUXIEME_POINT_COUPLE) + (j << PRECISION) + i);
					}
					i2 = i + 1;
					if((i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) > rayonPoint*rayonPoint)
					{
//						log.debug("2 Dans masque : "+i+" "+j+" "+i2+" "+j2);
						masque.add((((j2 << PRECISION) +i2) << DEUXIEME_POINT_COUPLE) + (j << PRECISION) + i);
					}
					i2 = i;
					j2 = j - 1;
					if((i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) > rayonPoint*rayonPoint)
					{
//						log.debug("3 Dans masque : "+i+" "+j+" "+i2+" "+j2);
						masque.add((((j2 << PRECISION) +i2) << DEUXIEME_POINT_COUPLE) + (j << PRECISION) + i);
					}
					j2 = j + 1;
					if((i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) > rayonPoint*rayonPoint)
					{
//						log.debug("4 Dans masque : "+i+" "+j+" "+i2+" "+j2);
						masque.add((((j2 << PRECISION) +i2) << DEUXIEME_POINT_COUPLE) + (j << PRECISION) + i);
					}
			}

		/*
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
		int point = getGridPointVoisin(gridpoint, direction);
		// TODO
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
	
	/**
	 * Utilisé par un thread régulièrement
	 * Récupère la différence dans la grilleDynamique
	 * @return
	 */
	public synchronized ListIterator<Integer> getWhatChanged()
	{
//		log.debug(couplesCout.size()+" "+nbCouplesEnvoyes);
		ListIterator<Integer> out = couplesCout.listIterator(nbCouplesEnvoyes);
		nbCouplesEnvoyes = couplesCout.size();
		return out;
	}

	public GridSpace clone(long date)
	{
		GridSpace out = new GridSpace(log, iterator.clone(date), table.clone());
		out.couplesCout.clear();
		out.couplesCout.addAll(couplesCout);
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
			other.couplesCout.clear();
			other.couplesCout.addAll(couplesCout);
		}
		else
			other.regenereCouplesCout();
	}
	
	/**
	 * Ajoute tous les obstacles à la grille.
	 */
	private void regenereCouplesCout()
	{
		iterator.reinit();
		couplesCout.clear();
		while(iterator.hasNext())
			setObstacle(iterator.next());
		nbCouplesEnvoyes = 0; // il va falloir tout renvoyer
	}

	/**
	 * Ajoute le contour d'un obstacle de proximité dans la grille dynamique
	 * @param o
	 */
	private void setObstacle(ObstacleProximity o)
	{
		int x = getGridPointX(o.position);
		int y = getGridPointY(o.position);
		log.debug("xy : "+x+" "+y);
		int xC1, yC1, xC2, yC2;
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
				couplesCout.add(getGridPoint(xC1,yC1) << DEUXIEME_POINT_COUPLE + getGridPoint(xC2,yC2));
//				log.debug("Ajout !");
			}
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
	public synchronized ObstacleProximity addObstacle(Vec2<ReadOnly> position, boolean urgent) {
		ObstacleProximity o = obstaclesMemory.add(position, urgent);
		// pour un ajout, pas besoin de tout régénérer
		setObstacle(o);
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
		// S'il y a effectivement suppression, on régénère la grille
		if(obstaclesMemory.deleteOldObstacles())
			regenereCouplesCout();
		notify(); // changement de la grille dynamique !
	}

	public long getNextDeathDate()
	{
		return obstaclesMemory.getNextDeathDate();
	}

	public boolean isTraversable(int point)
	{
		for(int i = 0; i < 8; i++)
			if(!isTraversable(point, i))
				return false;
		return true;
	}
}
