package table;

import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import container.Service;
import utils.Config;
import utils.Log;
import vec2.ReadOnly;
import vec2.Vec2;
import enums.Tribool;
import exceptions.GridSpaceException;

/**
 * Contient les informations sur le graphe utilisé par le pathfinding.
 * Intègre un mécanisme de cache afin d'accélérer les calculs.
 * @author pf
 *
 */

public class GridSpace implements Service {
	
	private Log log;
	private Config config;
	
	// afin d'éviter les obstacles fixes et mobiles
	private final ObstacleManager obstaclemanager;
		
	// Rempli de ALWAYS_IMPOSSIBLE et null. Ne change pas.
	private static NodesConnection[][] isConnectedModel = null;

	// Rempli de ALWAYS_IMPOSSIBLE, TMP_IMPOSSIBLE, POSSIBLE et null
	// TODO cache gridspace
	private static NodesConnection[][][] isConnectedModelCache = new NodesConnection[2][PathfindingNodes.length][PathfindingNodes.length];

	// Le hash dépend de avoidGameElement
	// Doit-on éviter les éléments de jeux? Ou peut-on foncer dedans?
	private boolean avoidGameElement = true;
	
	/** Ce hash est utilisé afin de vérifier la péremption du cache */
	private long hashTable;
	
	public GridSpace(Log log, Config config, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		hashTable = obstaclemanager.getHashTable();
		// Il est très important de ne faire ce long calcul qu'une seule fois,
		// à la première initialisation
		if(isConnectedModel == null)
		{
			initStatic();
			check_pathfinding_nodes();
		}

		reinitConnections();
		updateConfig();
	}
	
	/**
	 * Vérifie que les noeuds du pathfinding ne sont pas dans un obstacle fixe.
	 */
    public void check_pathfinding_nodes()
    {
    	log.debug("Vérification de l'accessibilité des PathfindingNodes");
    	for(PathfindingNodes i: PathfindingNodes.values)
    	{
    		boolean accessible = false;
        	for(PathfindingNodes j: PathfindingNodes.values)
        		if(!obstaclemanager.obstacleFixeDansSegmentPathfinding(i.getCoordonnees(), j.getCoordonnees()))
        		{
        			accessible = true;
        			break;
        		}
        	if(!accessible)
        		log.critical("Le noeud "+i+" n'est pas accessible!");
    	}
    }
    

    /**
     * Initialisé static de isConnectedModel.
     * En effet, il y a beaucoup d'instanciations de GridSpace... (il fait parti du gamestate)
     */
	private void initStatic()
	{
		log.debug("Calcul de isConnectedModel");
		isConnectedModel = new NodesConnection[PathfindingNodes.length][PathfindingNodes.length];

		for(PathfindingNodes i : PathfindingNodes.values)			
			for(PathfindingNodes j : PathfindingNodes.values)
			{
				if(obstaclemanager.obstacleFixeDansSegmentPathfinding(i.getCoordonnees(), j.getCoordonnees()))
					isConnectedModel[i.ordinal()][j.ordinal()] = NodesConnection.ALWAYS_IMPOSSIBLE;
				else
					isConnectedModel[i.ordinal()][j.ordinal()] = null;
			}
	}
	
	/**
	 * Réinitialise l'état des liaisons.
	 * A faire quand les obstacles mobiles ont changé.
	 */
	public void reinitConnections()
	{
		int max_value = PathfindingNodes.length;
		for(int i = 0; i < max_value; i++)
		{
			System.arraycopy(isConnectedModel[i], 0, isConnectedModelCache[0][i], 0, max_value);
			System.arraycopy(isConnectedModel[i], 0, isConnectedModelCache[1][i], 0, max_value);
		}
	}

	/**
	 * Surcouche de isConnected qui gère le cache
	 * @return
	 */
	public boolean isTraversable(PathfindingNodes i, PathfindingNodes j, int date)
	{
//		log.debug("avoidGameElement: "+avoidGameElement, this);
		if(isConnectedModelCache[getHashBool()][i.ordinal()][j.ordinal()] != null)
		{
//			log.debug("Trajet entre "+i+" et "+j+": utilisation du cache. Traversable: "+isConnectedModelCache[i.ordinal()][j.ordinal()][getHashBool()].isTraversable(), this);
			return isConnectedModelCache[getHashBool()][i.ordinal()][j.ordinal()].isTraversable();
		}
		else if(avoidGameElement && obstaclemanager.obstacleTableDansSegment(i.getCoordonnees(), j.getCoordonnees()))
		{
//			log.debug("Trajet entre "+i+" et "+j+" impossible à cause d'un élément de jeu", this);
			isConnectedModelCache[getHashBool()][i.ordinal()][j.ordinal()] = NodesConnection.TMP_IMPOSSIBLE;
		}
		else if(obstaclemanager.obstacleProximiteDansSegment(i.getCoordonnees(), j.getCoordonnees(), date))
		{
//			log.debug("Trajet entre "+i+" et "+j+" impossible à cause d'un obstacle de proximité", this);
			isConnectedModelCache[0][i.ordinal()][j.ordinal()] = NodesConnection.TMP_IMPOSSIBLE;
			isConnectedModelCache[1][i.ordinal()][j.ordinal()] = NodesConnection.TMP_IMPOSSIBLE;
		}
		else
		{
//			log.debug("Pas de problème entre "+i+" et "+j, this);
			isConnectedModelCache[getHashBool()][i.ordinal()][j.ordinal()] = NodesConnection.POSSIBLE;
		}

		// symétrie!
		isConnectedModelCache[0][j.ordinal()][i.ordinal()] = isConnectedModelCache[0][i.ordinal()][j.ordinal()];
		isConnectedModelCache[1][j.ordinal()][i.ordinal()] = isConnectedModelCache[1][i.ordinal()][j.ordinal()];
		return isConnectedModelCache[getHashBool()][i.ordinal()][j.ordinal()].isTraversable();
	}
	
	/**
	 * Si le robot est dans un obstacle, cette méthode renverra le PathfindingNode le plus proche
	 * sans qu'il y ait un obstacle de proximité sur le chemin
	 * Si le robot n'est pas dans un obstacle, alors il renverra le PathfindingNode le plus proche
	 * sans qu'il y ait un obstacle de proximité ou un obstacle fixe sur le chemin.
	 * @param point
	 * @return
	 * @throws GridSpaceException 
	 */
	public PathfindingNodes nearestReachableNode(Vec2<ReadOnly> point, int date) throws GridSpaceException
	{
		PathfindingNodes pointPlusProcheAvecObstaclesFixes = null;
		PathfindingNodes pointPlusProcheSansObstaclesFixes = null;
		double distanceMinSansObstaclesFixes = Double.MAX_VALUE;
		double distanceMinAvecObstaclesFixes = Double.MAX_VALUE;
		boolean robotHorsObstacle = false;
		for(PathfindingNodes i : PathfindingNodes.values)
		{
			boolean pasObstacleCetteFois = false;
			double tmp = point.squaredDistance(i.getCoordonnees());
			pasObstacleCetteFois = !obstaclemanager.obstacleFixeDansSegmentPathfinding(point, i.getCoordonnees());
			robotHorsObstacle |= pasObstacleCetteFois;
			
			// si le point est trop loin ou s'il y a un obstacle de proximité entre nous et lui: on passe au point suivant
			if((tmp >= distanceMinSansObstaclesFixes && tmp >= distanceMinAvecObstaclesFixes) || obstaclemanager.obstacleProximiteDansSegment(point, i.getCoordonnees(), date))
				continue;
			
			if(tmp < distanceMinSansObstaclesFixes)
			{
				distanceMinSansObstaclesFixes = tmp;
				pointPlusProcheSansObstaclesFixes = i;
			}
			if(pasObstacleCetteFois && tmp < distanceMinAvecObstaclesFixes)
			{
				distanceMinAvecObstaclesFixes = tmp;
				pointPlusProcheAvecObstaclesFixes = i;			
			}
		}
		if(pointPlusProcheAvecObstaclesFixes == null)
		{
			if(robotHorsObstacle || pointPlusProcheSansObstaclesFixes == null)
				throw new GridSpaceException();
			else
				// On ne renvoie ce point que si le robot est dans un obstacle et
				// qu'il existe un plus proche point sans obstacle de proximité
				return pointPlusProcheSansObstaclesFixes;
		}
		return pointPlusProcheAvecObstaclesFixes;
	}
	
	@Override
	public void updateConfig() {
		obstaclemanager.updateConfig();
		log.updateConfig();
	}

	/**
	 * other devient la copie conforme de this.
	 * @param other
	 * @param date
	 */
	public void copy(GridSpace other, long date)
	{
		long oldHashTable = hashTable;
// 300
		obstaclemanager.supprimerObstaclesPerimes(date);
		hashTable = obstaclemanager.getHashTable();

		// On détruit le cache si les obstacles de proximité ou les éléments de jeux ont changé
// 300
		if(oldHashTable != hashTable || obstaclemanager.getFirstNotDead() != other.obstaclemanager.getFirstNotDead())
			reinitConnections();
// 300
		obstaclemanager.copy(other.obstaclemanager);
		other.avoidGameElement = avoidGameElement;
		other.hashTable = hashTable;
	}
	
	/**
	 * Récupère un clone de ce gridspace.
	 * @param date
	 * @return
	 */
	public GridSpace clone(long date)
	{
		GridSpace cloned_gridspace = new GridSpace(log, config, obstaclemanager.clone());
		copy(cloned_gridspace, date);
		return cloned_gridspace;
	}
    
	/**
	 * Utilisé uniquement pour les tests
	 * @return
	 */
    public int nbObstaclesMobiles()
    {
    	return obstaclemanager.nbObstaclesMobiles();
    }

    /** 
     * A utiliser entre deux points
     * @param pointA
     * @param pointB
     * @return
     */
    public boolean isTraversable(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, int date)
    {
    	// Evaluation paresseuse importante, car obstacle_proximite_dans_segment est bien plus rapide que obstacle_fixe_dans_segment
    	return !obstaclemanager.obstacleProximiteDansSegment(pointA, pointB, date) && !obstaclemanager.obstacleFixeDansSegmentPathfinding(pointA, pointB);
    }
        
    /**
     * Créer un obstacle à une certaine date
     * Utilisé dans l'arbre des possibles.
     * @param position
     * @param date
     */
    public void creer_obstacle(Vec2<ReadOnly> position, int date)
    {
    	obstaclemanager.creerObstacle(position, date);
    	reinitConnections();
    }

    /**
     * Utilisé pour le calcul stratégique de secours uniquement.
     * @param position
     * @param date_actuelle
     */
    public void createHypotheticalEnnemy(Vec2<ReadOnly> position, int date_actuelle)
    {
    	obstaclemanager.createHypotheticalEnnemy(position, date_actuelle);
    }

    /**
     * Doit-on shooter les éléments de jeux? Cela modifie les chemins disponibles.
     * @param avoidGameElement
     */
    public void setAvoidGameElement(boolean avoidGameElement)
    {
    	this.avoidGameElement = avoidGameElement;
    }

    /**
     * Modification de la table.
     * @param element
     * @param done
     */
	public void setDone(GameElementNames element, Tribool done)
	{
		obstaclemanager.setDone(element, done);
	}

	/**
	 * Récupération d'une info de la table.
	 * @param element
	 * @return
	 */
	public Tribool isDone(GameElementNames element)
	{
		return obstaclemanager.isDone(element);
	}

	/**
	 * Utilisé pour les tests uniquement
	 * @return
	 */
	public long getHashTable()
	{
		return obstaclemanager.getHashTable();
	}

	/**
	 * Utilisé par les tests
	 * @return
	 */
	public int getHashObstaclesMobiles()
	{
		return obstaclemanager.getHashObstaclesMobiles();
	}

	public long getHash()
	{
		long hash = obstaclemanager.getHashObstaclesMobiles(); // codé sur autant de bits qu'il le faut puisqu'il est dans les bits de poids forts
		hash = (hash << (2*GameElementNames.valuesLength)) | obstaclemanager.getHashTable(); // codé sur 2 bits par élément de jeux (2 bit par Tribool)
		return hash;
	}

	/**
	 * Debug
	 */
	public void printHash()
	{
		int hash = obstaclemanager.getHashObstaclesMobiles();
		log.debug("Numéro d'obstacle: "+hash/2);
		if(hash%2 == 0)
			log.debug("Pas d'ennemi d'urgence");
		else
			log.debug("Ennemi d'urgence présent");
		obstaclemanager.printHash();
	}
	
	/**
	 * Petite méthode qui permet de fournir un indice pour isConnectedModelCache
	 * @return
	 */
	private int getHashBool()
	{
		return avoidGameElement?1:0;
	}

	/**
	 * Utilisé par le script d'attente
	 * @return
	 */
	public int getDateSomethingChange()
	{
		return obstaclemanager.getDateSomethingChange();
	}

	/**
	 * Pas de cache ici, gridspace fait juste un appel à obstaclemanager.
	 * En effet, ici on traite non pas des segments mais des triplets; les chances de
	 * réutilisations sont nulles
	 * @param intersection
	 * @param directionAvant
	 * @param directionApres
	 * @param tempsDepuisDebutMatch
	 * @return
	 */
	public boolean isTraversableCourbe(PathfindingNodes objectifFinal, PathfindingNodes intersection, Vec2<ReadOnly> directionAvant, int tempsDepuisDebutMatch)
	{
		return obstaclemanager.isTraversableCourbe(objectifFinal, intersection, directionAvant, tempsDepuisDebutMatch);
	}

	public SegmentTrajectoireCourbe getSegment()
	{
		return obstaclemanager.getSegment();
	}
	
}
