package astar.arcmanager;

import java.util.ArrayList;
import java.util.Vector;

import astar.AStar;
import astar.MemoryManager;
import astar.arc.Arc;
import astar.arc.Decision;
import astar.arc.PathfindingNodes;
import hook.Hook;
import hook.HookFactory;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Script;
import scripts.ScriptManager;
import scripts.ScriptAnticipableNames;
import strategie.GameState;
import utils.Log;
import utils.Config;
import container.Service;
import exceptions.ArcManagerException;
import exceptions.FinMatchException;
import exceptions.MemoryManagerException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import exceptions.PointSortieException;
import exceptions.ScriptException;
import exceptions.ScriptHookException;

/**
 * Réalise les calculs sur les scripts et les gamestate pour l'AStar.
 * @author pf
 *
 */

public class StrategyArcManager implements Service, ArcManager {

	protected Log log;
	private ScriptManager scriptmanager;
	private AStar<PathfindingArcManager, PathfindingNodes> astar;
	private HookFactory hookfactory;
	private MemoryManager memorymanager;
	
	private ArrayList<Decision> listeDecisions = new ArrayList<Decision>();
	private int iterator;
	private Vector<Long> hashes = new Vector<Long>();
	
	private int dateLimite = 90000;
	
	public StrategyArcManager(Log log, Config config, ScriptManager scriptmanager, GameState<RobotReal> real_gamestate, HookFactory hookfactory, AStar<PathfindingArcManager, PathfindingNodes> astar, MemoryManager memorymanager) throws PointSortieException
	{
		this.log = log;
		this.scriptmanager = scriptmanager;
		this.hookfactory = hookfactory;
		this.astar = astar;
		this.memorymanager = memorymanager;
		try {
			checkPointSortie(real_gamestate.cloneGameState());
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
	public void reinitIterator(GameState<RobotChrono> gamestate) throws MemoryManagerException
	{
		listeDecisions.clear();
		for(ScriptAnticipableNames s: ScriptAnticipableNames.values())
		{
			if(s.canIDoIt())
			{
				Script script = scriptmanager.getScript(s);
//				if(script.getVersions(gamestate).size() == 0)
//					log.debug("Aucune version pour "+s, this);
				for(Integer v: script.getVersions(gamestate))
				{
//					log.debug("Recherche de chemins pour "+s+" "+v, this);
					// On n'ajoute que les versions qui sont accessibles
					try {
						ArrayList<PathfindingNodes> chemin = astar.computePath(gamestate, script.point_entree(v), true);
//						log.debug("Chemin trouvé en défonçant les éléments de jeux", this);
						listeDecisions.add(new Decision(chemin, s, v));
						try {
							// On ne rajoute la version où on ne shoot pas seulement si le chemin proposé est différent
							ArrayList<PathfindingNodes> chemin2 = astar.computePath(gamestate, script.point_entree(v), false);
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
	public int distanceTo(GameState<RobotChrono> state, Arc arc) throws FinMatchException, ScriptException
	{
		Decision d = (Decision)arc;
		Script s = scriptmanager.getScript(d.script_name);
		int old_temps = (int)state.robot.getTempsDepuisDebutMatch();
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScriptsChrono(state, dateLimite);
		state.robot.suit_chemin(d.chemin, hooks_table);
		try {
			s.agit(d.version, state);
		} catch (ScriptHookException e) {
			// Impossible avec un robotchrono.
			e.printStackTrace();
		}
		state.robot.setPositionPathfinding(s.point_sortie(d.version));
		int new_temps = (int)state.robot.getTempsDepuisDebutMatch();
		return new_temps - old_temps;
	}

	/**
	 * On utilise en fait un Dijsktra pour la stratégie
	 */
	@Override
	public int heuristicCost(GameState<RobotChrono> state)
	{
		return 0;
	}

	/**
	 * Les hash sont créés à la demande et sont contigus (0, 1, 2, 3, ...)
	 */
	@Override
	public int getHashAndCreateIfNecessary(GameState<RobotChrono> state)
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
	public int getHash(GameState<RobotChrono> state) throws ArcManagerException
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
	{}

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
	public void checkPointSortie(GameState<RobotChrono> gamestate) throws PointSortieException
	{
		boolean throw_exception = false;
		for(ScriptAnticipableNames s: ScriptAnticipableNames.values())
		{
			Script script = scriptmanager.getScript(s);
			for(Integer v: script.getVersions(gamestate))
			{
				gamestate.robot.setPositionPathfinding(script.point_entree(v));
				try {
					script.agit(v, gamestate);
				} catch (FinMatchException | ScriptException | ScriptHookException e) {
					e.printStackTrace();
				}
				try {
				script.checkPointSortie(v, gamestate.robot.getPosition());
				}
				catch(PointSortieException e)
				{
					throw_exception = true;
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
	
	public void destroyGameState(GameState<RobotChrono> state) throws MemoryManagerException
	{
		memorymanager.destroyGameState(state, 1);
	}

	public GameState<RobotChrono> getNewGameState() throws FinMatchException
	{
		return memorymanager.getNewGameState(1);
	}

	public void empty()
	{
		memorymanager.empty(1);
	}

}
