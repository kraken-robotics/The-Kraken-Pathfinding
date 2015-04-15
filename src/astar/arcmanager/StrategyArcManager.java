package astar.arcmanager;

import java.util.ArrayList;
import java.util.Vector;

import astar.AStar;
import astar.AStarId;
import astar.MemoryManager;
import astar.arc.Arc;
import astar.arc.Decision;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import hook.Hook;
import hook.HookFactory;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Script;
import scripts.ScriptManager;
import scripts.ScriptAnticipableNames;
import strategie.GameState;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import vec2.ReadOnly;
import vec2.ReadWrite;
import exceptions.ArcManagerException;
import exceptions.FinMatchException;
import exceptions.MemoryManagerException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import exceptions.PointSortieException;
import exceptions.ScriptException;
import exceptions.ScriptHookException;
import exceptions.UnableToMoveException;

/**
 * Réalise les calculs sur les scripts et les gamestate pour l'AStar.
 * @author pf
 *
 */

public class StrategyArcManager extends ArcManager {

	protected Log log;
	protected Config config;
	private ScriptManager scriptmanager;
	private AStar<PathfindingArcManager, SegmentTrajectoireCourbe> astar;
	private HookFactory hookfactory;
	
	private ArrayList<Decision> listeDecisions = new ArrayList<Decision>();
	private int iterator;
	private Vector<Long> hashes = new Vector<Long>();
	
	private int dateLimite;
	
	public StrategyArcManager(Log log, Config config, ScriptManager scriptmanager, GameState<RobotReal,ReadOnly> real_gamestate, HookFactory hookfactory, AStar<PathfindingArcManager, SegmentTrajectoireCourbe> astar, MemoryManager memorymanager) throws PointSortieException
	{
		super(AStarId.STRATEGY_ASTAR, memorymanager);
		this.log = log;
		this.config = config;
		this.scriptmanager = scriptmanager;
		this.hookfactory = hookfactory;
		this.astar = astar;
		try {
			checkPointSortie(GameState.cloneGameState(real_gamestate));
		} catch (FinMatchException e) {
			// Impossible
			e.printStackTrace();
		}
		updateConfig();
	}

	/**
	 * On calcule ici la liste des voisins, c'est-à-dire de tous les scripts qu'on peut effectuer depuis la position actuelle.
	 */
	@Override
	public void reinitIterator(GameState<RobotChrono,ReadOnly> gamestate) throws MemoryManagerException
	{
		listeDecisions.clear();
		for(ScriptAnticipableNames s: ScriptAnticipableNames.values())
		{
			if(s.canIDoIt())
			{
				Script script = scriptmanager.getScript(s);
//				if(script.getVersions(gamestate).size() == 0)
//					log.debug("Aucune version pour "+s, this);
				for(PathfindingNodes v: script.getVersions(gamestate))
				{
//					log.debug("Recherche de chemins pour "+s+" "+v, this);
					// On n'ajoute que les versions qui sont accessibles
					try {
						ArrayList<SegmentTrajectoireCourbe> chemin = astar.computePath(GameState.cloneGameState(gamestate), v, true);
//						log.debug("Chemin trouvé en défonçant les éléments de jeux", this);
						listeDecisions.add(new Decision(chemin, s, v));
						try {
							// On ne rajoute la version où on ne shoot pas seulement si le chemin proposé est différent
							ArrayList<SegmentTrajectoireCourbe> chemin2 = astar.computePath(GameState.cloneGameState(gamestate), v, false);
//							log.debug("Chemin trouvé sans défoncer les éléments de jeux", this);
							if(!chemin2.equals(chemin))
								listeDecisions.add(new Decision(chemin2, s, v));
						} catch (PathfindingException
								| PathfindingRobotInObstacleException
								| FinMatchException e) {
						}
					} catch (PathfindingException
							| PathfindingRobotInObstacleException
							| FinMatchException e) {
					}
				}
			}
//			else
//				log.warning("Je ne peux pas faire "+s+"...", this);
		}
		
		iterator = -1;
//		for(Decision d: listeDecisions)
//			log.debug(d, this);
	}

	@Override
	public boolean hasNext()
	{
		iterator++;
		return iterator < listeDecisions.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Decision next()
	{
		return listeDecisions.get(iterator);
	}

	/**
	 * Renvoie le temps. Cela signifie que pour deux situations identiques (position, points, ...),
	 * on préférera celle qui a minimisé le temps pour arriver à cet état de jeu.
	 */
	@Override
	public int distanceTo(GameState<RobotChrono,ReadWrite> state, Arc arc) throws FinMatchException, ScriptException
	{
		Decision d = (Decision)arc;
		Script s = scriptmanager.getScript(d.script_name);
		int old_temps = (int)GameState.getTempsDepuisDebutMatch(state.getReadOnly());
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScriptsChrono(state, dateLimite);
		try {
			GameState.suit_chemin(state, d.chemin, hooks_table);
		} catch (UnableToMoveException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ScriptHookException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			s.agit(d.version, state);
		} catch (ScriptHookException e) {
			// Impossible avec un robotchrono.
			e.printStackTrace();
		}
		GameState.setPositionPathfinding(state, s.point_sortie(d.version));
		int new_temps = (int)GameState.getTempsDepuisDebutMatch(state.getReadOnly());
		return new_temps - old_temps;
	}

	/**
	 * On utilise en fait un Dijsktra pour la stratégie
	 */
	@Override
	public int heuristicCost(GameState<RobotChrono,ReadOnly> state)
	{
		return 0;
	}

	/**
	 * Les hash sont créés à la demande et sont contigus (0, 1, 2, 3, ...)
	 */
	@Override
	public int getHashAndCreateIfNecessary(GameState<RobotChrono,ReadOnly> state)
	{
		long hash = state.getHash();
		int indice = hashes.indexOf(hash);
		if(indice == -1)
		{
//			log.debug("Size: "+hashes.size(), this);
			hashes.add(hash);
			return hashes.size()-1;
		}
		else
			return indice;
	}

	@Override
	public int getHash(GameState<RobotChrono,ReadOnly> state) throws ArcManagerException
	{
		long hash = state.getHash();
		int indice = hashes.indexOf(hash);
		if(indice == -1)
			throw new ArcManagerException();
		else
			return indice;
	}

	@Override
	public void updateConfig()
	{
		log.updateConfig();
		scriptmanager.updateConfig();
		astar.updateConfig();
		hookfactory.updateConfig();
	}

	/**
	 * Doit être appelé à chaque début de calcul de stratégie afin d'éviter une fuite de mémoire.
	 */
	public void reinitHashes()
	{
		hashes.clear();
	}
	
	/**
	 * On est jamais arrivé. Cela signifie simplement que le calcul de stratégie
	 * ne s'arrêtera que par une exception de fin de jeu.
	 */
	@Override
	public boolean isArrive(int hash) {
		return false;
	}

	/**
	 * On renvoie le nombre de points. Ce qui signifie simplement qu'on prend la stratégie qui maximise le nombre de points.
	 */
	@Override
	public int getNoteReconstruct(int hash) {
		return (int)(hashes.get(hash)&511); // la composante "note" du hash (cf gamestate.getHash())
	}
	
	/**
	 * Vérifie que les points de sortie (notés dans l'enum PathfindingNodes) sont bien là où on arrive après l'exécution du script.
	 * Exécuté une fois pour toute à l'instanciation. En cas de problème, on lève une exception.
	 * @param gamestate
	 * @throws PointSortieException
	 */
	public void checkPointSortie(GameState<RobotChrono,ReadWrite> gamestate) throws PointSortieException
	{
		boolean throw_exception = false;
		for(ScriptAnticipableNames s: ScriptAnticipableNames.values())
		{
			if(s.canIDoIt())
			{
				Script script = scriptmanager.getScript(s);
				for(PathfindingNodes v: script.getVersions(gamestate.getReadOnly()))
				{
					GameState.setPositionPathfinding(gamestate, v);
					try {
						script.agit(v, gamestate);
					} catch (ScriptException | ScriptHookException | FinMatchException e) {
						e.printStackTrace();
					}
					try {
					script.checkPointSortie(v, GameState.getPosition(gamestate.getReadOnly()));
					}
					catch(PointSortieException e)
					{
						if(config.getBoolean(ConfigInfo.CHECK_POINTS_SORTIE))
							throw_exception = true;
					} catch (FinMatchException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if(throw_exception)
			throw new PointSortieException();
	}

	public void setDateLimite(int dateLimite)
	{
		this.dateLimite = dateLimite;
	}

}
