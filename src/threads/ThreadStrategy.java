package threads;

import hook.Hook;
import hook.types.HookFactory;

import java.util.ArrayList;

import pathfinding.Pathfinding;
import container.Service;
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
	
	// Utilisation de l'arbre des possibles, branche avec obstacle
	private MemoryManager memorymanager;
	
	// Parcours et appel des scripts
	private ScriptManager scriptmanager;
	
	// Calcul des chemins entre scripts
	private Pathfinding pathfinding;
	
	// Nécessaire aux scripts pour donner les versions disponibles
	private GameState<RobotReal> real_gamestate;
	
	private ArrayList<Hook> hooks_entre_scripts;
	
	private long temps_max_anticipation;
	private long dureeMatch;
	private int profondeur_max = 1;

	private Decision[] decisions = null;
	
	public ThreadStrategy(Log log, Config config, MemoryManager memorymanager, ScriptManager scriptmanager, Pathfinding pathfinding, GameState<RobotReal> real_gamestate, HookFactory hookfactory) 
	{
		this.log = log;
		this.config = config;
		this.memorymanager = memorymanager;
		this.scriptmanager = scriptmanager;
		this.pathfinding = pathfinding;
		this.real_gamestate = real_gamestate;
		hooks_entre_scripts = hookfactory.getHooksEntreScripts(real_gamestate);
	
		Thread.currentThread().setPriority(4); // TODO
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
			try {
				synchronized(decisions)
				{
					decisions = new Decision[2];
					// Décision optimale
					decisions[0] = findMeilleureDecision();
					try {
						memorymanager.obstacle_dans_prochain_arbre(scriptmanager.getScript(decisions[0].script_name).point_entree(decisions[0].id_version).getCoordonnees());
						// Décision de secours
						decisions[1] = findMeilleureDecision();
					} catch (UnknownScriptException e) {
						e.printStackTrace();
					}
				}
			} catch(FinMatchException e) {
				// fin du match: on arrête le thread
				break;
			}
			
			// Tant qu'on n'a pas besoin d'une nouvelle décision
			while(decisions != null)
				Sleep.sleep(50);
			
		}
	}

	public Decision findMeilleureDecision() throws FinMatchException
	{
		Decision meilleure_decision = new Decision(null, -1, -1);
		for(ScriptNames n: ScriptNames.values())
			if(n.canIDoIt())
				try {
					for(int meta_version : scriptmanager.getScript(n).meta_version(real_gamestate))
					{
						Decision decision_trouvee = parcourtArbre(0, n, meta_version);
						log.debug("Note de "+n+" ("+meta_version+"): "+decision_trouvee.note, this);
						if(decision_trouvee.note > meilleure_decision.note)
							meilleure_decision = decision_trouvee;
					}
				} catch (UnknownScriptException e) {
					// Script inconnu, on le passe
					continue;
				}
		return meilleure_decision;
	}
	
	/**
	 * Renvoie la meilleure décision commençant par un script à une certaine profondeur.
	 * Renvoie null si le temps est écoulé ou s'il n'y a aucun choix possible.
	 * @param profondeur
	 * @return
	 * @throws FinMatchException 
	 */
	private Decision parcourtArbre(int profondeur, ScriptNames nomScript, int id_meta_version) throws FinMatchException
	{
		Decision meilleure_decision = new Decision(null, -1, -1);
		try {
			// Récupération du gamestate
			GameState<RobotChrono> state = memorymanager.getClone(profondeur);
			if(profondeur >= profondeur_max || state.getTempsDepuisRacine() > temps_max_anticipation || state.getTempsDepuisDebut() > dureeMatch)
			{
				log.debug("Profondeur max atteinte", this);
				return meilleure_decision;
			}

			long tempsAvantScript = state.robot.getDate();
			int pointsAvantScript = state.robot.getPointsObtenus();
			
			Script s = scriptmanager.getScript(nomScript);

			// Chemin pour aller au script
			ArrayList<PathfindingNodes> chemin;
			try {
				chemin = pathfinding.computePath(state.robot.getPosition(), s.point_entree(s.version_asso(id_meta_version).get(0)), state.gridspace, false, false);
			} catch (PathfindingException e) {
				// En cas d'erreur, on coupe la branche
				return meilleure_decision;
			} catch (PathfindingRobotInObstacleException e) {
				return meilleure_decision;
			}

			state.robot.suit_chemin(chemin, hooks_entre_scripts);

			// Exécution du script
			try {
				s.execute(s.closest_version(state, id_meta_version), state);
			} catch (UnableToMoveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SerialConnexionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long tempsApresScript = state.robot.getDate();
			int pointsApresScript = state.robot.getPointsObtenus();

			double note = calculeNote((int)(tempsApresScript-tempsAvantScript), pointsApresScript-pointsAvantScript);
			
			Decision tmp_decision;
			
			for(ScriptNames n : ScriptNames.values())
				for(int meta_version : scriptmanager.getScript(n).meta_version(state))
				{
					tmp_decision = parcourtArbre(profondeur+1, n, meta_version);
					if(meilleure_decision == null || meilleure_decision.note < tmp_decision.note)
						meilleure_decision = tmp_decision;
				}
			meilleure_decision.note = meilleure_decision.note + note;
			meilleure_decision.state = state;
		} catch (UnknownScriptException e) {
			// Script inconnu? On annule cette décision.
			e.printStackTrace();
		}
		
		return meilleure_decision;		
	}
	
	private double calculeNote(int temps, int points)
	{
		return ((double)temps) / ((double)points);
	}

	@Override
	public void updateConfig() {
		temps_max_anticipation = Integer.parseInt(config.get("temps_max_anticipation"));	
		dureeMatch = 1000*Long.parseLong(config.get("temps_match"));
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
