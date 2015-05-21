package strategie;

import hook.HookFactory;

import java.util.ArrayList;

import permissions.ReadOnly;
import planification.MemoryManager;
import planification.Pathfinding;
import robot.RobotReal;
import scripts.ScriptManager;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Algorithme de planification qui recherche une stratégie
 * LPA*, dont l'heuristique est un nombre de points
 * @author pf
 *
 */

public class Strategie implements Service {

	protected Log log;
	private ScriptManager scriptmanager;
	private GameState<RobotReal,ReadOnly> real_gamestate;
	private HookFactory hookfactory;
	private Pathfinding pathfinding;
	private MemoryManager memorymanager;
	
	public Strategie(Log log, ScriptManager scriptmanager, GameState<RobotReal,ReadOnly> real_gamestate, HookFactory hookfactory, Pathfinding pathfinding, MemoryManager memorymanager)
	{
		
	}
	
	/**
	 * Mise à jour des coûts après une modification des obstacles
	 */
	public void updateCost()
	{
		// ne met pas à jour le chemin (au cas où ce ne soit pas nécessaire...)
	}
	
	/**
	 * Recalcule un chemin à partir de la position actuelle. A faire après mise à jour.
	 * @param positionActuelle
	 * @return
	 */
	public ArrayList<StrategieArc> getPath(GameState<?,ReadOnly> positionActuelle)
	{
		// met à jour si nécessaire
		return null;
	}
	
	@Override
	public void updateConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void useConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

}
