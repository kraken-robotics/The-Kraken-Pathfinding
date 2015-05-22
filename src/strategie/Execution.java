package strategie;

import hook.Hook;

import java.util.ArrayList;

import permissions.ReadWrite;
import planification.Chemin;
import planification.Pathfinding;
import container.Service;
import exceptions.FinMatchException;
import exceptions.ScriptException;
import exceptions.UnableToMoveException;
import requete.RequeteSTM;
import requete.RequeteType;
import robot.RobotReal;
import scripts.ScriptManager;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;

/**
 * Exécute ce qu'a décidé la stratégie.
 * Gère en particulier les réponses du système aux différentes erreurs.
 * @author pf
 *
 */

public class Execution implements Service {

	private GameState<RobotReal,ReadWrite> gamestate;
	private Log log;
	private ScriptManager scriptmanager;
	private Strategie strategie;
	private RequeteSTM requete;
	
	private ArrayList<Hook> hooksEntreScripts;
	private volatile Boolean matchDemarre = Boolean.FALSE;
	
	public Execution(Log log, Strategie strategie, ScriptManager scriptmanager, GameState<RobotReal,ReadWrite> gamestate)
	{
		this.log = log;
		this.gamestate = gamestate;
		this.scriptmanager = scriptmanager;
		this.strategie = strategie;
		requete = RequeteSTM.getInstance();
/*		try {
			hooksEntreScripts = hookfactory.getHooksEntreScriptsReal(gamestate);
		} catch (FinMatchException e) {
			// Impossible
			e.printStackTrace();
		}*/
	}

	/**
	 * Appelé par le lanceur. C'est la boucle exécutée par le robot pendant tout le match
	 */
	public void boucleExecution()
	{		
		/**
		 * Boucle d'exécution
		 */
		while(true)
		{
			RequeteType type = null;
			
			synchronized(requete)
			{
				try {
					requete.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				type = requete.type;
			}
			
			if(type == RequeteType.BLOCAGE_MECANIQUE)
			{
				// TODO
			}
			else if(type == RequeteType.TRAJET_FINI)
			{
				// TODO
			}
			else if(type == RequeteType.MATCH_FINI)
			{
				log.debug("Fin du match: arrêt du thread principal");
				break;
			}
		}
	}
	
	/**
	 * Exécute un script et passe sur la stratégie de secours si besoin est
	 * @param decision_actuelle
	 * @throws FinMatchException
	 */
/*	public void executerScript(Decision decision_actuelle) throws FinMatchException
	{
		log.debug("On tente d'exécuter "+decision_actuelle.script_name);
		try {
			tryOnce(decision_actuelle);
		} catch (UnableToMoveException | ScriptException e) {
			// On a rencontré l'ennemi en chemin. On applique la stratégie d'urgence.
			boolean recommence = true;
			do {
				if(GameState.isEnemyHere(gamestate.getReadOnly()))
				{
					log.debug("Stratégie d'urgence avec ennemi: "+decision_actuelle.script_name);
					decision_actuelle = threadstrategy.getEmergencyDecision();
				}
				else
				{
					log.debug("Stratégie d'urgence sans ennemi: "+decision_actuelle.script_name);
					decision_actuelle = threadstrategy.getNormalDecision();
				}
				try {
					tryOnce(decision_actuelle);
					recommence = false;
				} catch (UnableToMoveException | ScriptException e1) {}
			} while(recommence);
		}
	}
	*/
	/**
	 * Exécute un script. Si celui-ci demande à exécuter un script de hook, l'exécuter
	 * @param d
	 * @throws UnableToMoveException
	 * @throws ScriptException
	 * @throws FinMatchException
	 */
/*	private void tryOnce(Decision d) throws UnableToMoveException, ScriptException, FinMatchException
	{
		try {
			GameState.set_vitesse(gamestate, Speed.BETWEEN_SCRIPTS);
			GameState.suit_chemin(gamestate, d.chemin, hooksEntreScripts);
			threadstrategy.computeBestDecisionAfter(d);
			scriptmanager.getScript(d.script_name).agit(d.version, gamestate);
		} catch (ScriptHookException e) {
			scriptmanager.getScript(e.getNomScript()).agit(null, gamestate);
		}
	}*/

	@Override
	public void updateConfig(Config config)
	{
		synchronized(matchDemarre)
		{
			matchDemarre = config.getBoolean(ConfigInfo.MATCH_DEMARRE);
			matchDemarre.notifyAll();
		}
	}
	
	@Override
	public void useConfig(Config config)
	{}

	/**
	 * Attend le début du match
	 */
	public void waitDebutMatch()
	{
		while(!matchDemarre)
		{
			synchronized(matchDemarre)
			{
				try {
					matchDemarre.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}		
		}
	}
	
}
