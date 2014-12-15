package threads;

import hook.Hook;
import hook.types.HookFactory;

import java.util.ArrayList;

import pathfinding.Pathfinding;
import container.Service;
import enums.ConfigInfo;
import enums.PathfindingNodes;
import enums.ScriptNames;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import exceptions.UnknownScriptException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Decision;
import scripts.Script;
import scripts.ScriptManager;
import strategie.GameState;
import strategie.MemoryManager;
import utils.Config;
import utils.Log;
import utils.Sleep;

public class ThreadStrategy extends AbstractThread implements Service
{

	private Log log;
	private Config config;
	
	// Parcours et appel des scripts
	private ScriptManager scriptmanager;
	
	// Calcul des chemins entre scripts
	private Pathfinding pathfinding;
	
	// Nécessaire aux scripts pour donner les versions disponibles
	private GameState<RobotReal> real_gamestate;
	
	private ArrayList<Hook> hooks_entre_scripts;
	
	private long temps_max_anticipation = 20000;
	private long dateFinMatch;
	private int profondeur_max = 1;

	private Decision[] decisions = null;
	
	public ThreadStrategy(Log log, Config config, ScriptManager scriptmanager, Pathfinding pathfinding, GameState<RobotReal> real_gamestate, HookFactory hookfactory) 
	{
		this.log = log;
		this.config = config;
		this.scriptmanager = scriptmanager;
		this.pathfinding = pathfinding;
		this.real_gamestate = real_gamestate;
		hooks_entre_scripts = hookfactory.getHooksEntreScripts(real_gamestate);
	
		Thread.currentThread().setPriority(4); // TODO
		updateConfig();
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de stratégie", this);
		while(!Config.matchDemarre)
		{
			if(stopThreads)
			{
				log.debug("Stoppage du thread de stratégie", this);
				return;
			}
			Sleep.sleep(50);
		}
		
		while(!finMatch)
		{
			// TODO: appel arbre des possibles
			
			// Tant qu'on n'a pas besoin d'une nouvelle décision
			while(decisions != null)
				Sleep.sleep(50);
			
		}
	}

	public double calculeNote(int temps, int points)
	{
		return ((double)temps) / ((double)points);
	}

	@Override
	public void updateConfig() {
		// temps en secondes dans la config
		temps_max_anticipation = 1000*Integer.parseInt(config.get(ConfigInfo.TEMPS_MAX_ANTICIPATION));	
		dateFinMatch = 1000*Long.parseLong(config.get(ConfigInfo.DUREE_MATCH_EN_S));
	}

	public Decision[] getDecisions()
	{
		synchronized(decisions)
		{
			Decision[] tmp = decisions;
			decisions = null;
			return tmp;
		}
	}
	
	public void setProfondeurMax(int profondeur_max)
	{
		this.profondeur_max = profondeur_max;
	}

}
