package hook;

import java.util.ArrayList;

import permissions.ReadOnly;
import permissions.ReadWrite;
import hook.methods.GameElementDone;
import hook.methods.ThrowsScriptHook;
import hook.types.HookCollisionElementJeu;
import hook.types.HookDate;
import hook.types.HookDateFinMatch;
import container.Service;
import enums.Tribool;
import exceptions.FinMatchException;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.ScriptHookNames;
import strategie.GameState;
import table.GameElementNames;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;

/**
 * Service fabriquant des hooks à la demande.
 * @author pf, marsu
 *
 */

public class HookFactory implements Service
{	
	//gestion des log
	private Log log;
	
	private int dureeMatch = 90000;
	
	private ArrayList<Hook> hooks_table_chrono = null;
	private HookDateFinMatch hook_fin_match_chrono = null;
	
	// TODO: créer hooks_table_chrono dès la construction, et maintenir un numéro pour chaque hook
	
	/**
	 *  appellé uniquement par Container.
	 *  Initialise la factory
	 * 
	 * @param config fichier de config du match
	 * @param log système de d log
	 * @param realState état du jeu
	 */
	public HookFactory(Log log)
	{
		this.log = log;
	}

	public void updateConfig(Config config)
	{}
	
	@Override
	public void useConfig(Config config)
	{
		// demande avec quelle tolérance sur la précision on déclenche les hooks
		dureeMatch = config.getInt(ConfigInfo.DUREE_MATCH_EN_S) * 1000;
	}

	/**
     * Fournit le hook de fin de match à un chrono gamestate.
     * @param state
     * @param date_limite
     * @return
     */
    public HookDateFinMatch getHooksFinMatchChrono(GameState<?,ReadOnly> state)
    {
    	if(hook_fin_match_chrono == null)
    		hook_fin_match_chrono = getHooksFinMatch(state, true);
    	return hook_fin_match_chrono;
    }

    /**
     * Met à jour le hook de fin de match d'un chrono gamestate.
     * @param state
     * @param date_limite
     * @return
     */
    public HookDateFinMatch updateHooksFinMatch(GameState<RobotChrono,ReadWrite> state, int date_limite)
    {
    	if(hook_fin_match_chrono == null)
    		hook_fin_match_chrono = getHooksFinMatch(state.getReadOnly(), true);
    	
    	/**
    	 * Mise à jour de la date limite
    	 * Pas besoin de mettre à jour les références, car la méthode
    	 * FinMatchCheck n'en utilise pas.
    	 */
		((HookDateFinMatch)hook_fin_match_chrono).updateDate(date_limite);
	    	return hook_fin_match_chrono;
    }

    /**
     * Fournit le hook de fin de match au gamestate
     * @param state
     * @return
     */
    public HookDateFinMatch getHooksFinMatchReal(GameState<?,ReadOnly> state)
    {
    	return getHooksFinMatch(state, false);
    }

    /**
     * Création du hook qui vérifie la fin du match
     * Ce hook est destiné à être utilisé pendant le script
     * @param state
     * @param isChrono
     * @return
     */
    private HookDateFinMatch getHooksFinMatch(GameState<?,ReadOnly> state, boolean isChrono)
    {
    	HookDateFinMatch hook_fin_match = new HookDateFinMatch(log, state, dureeMatch);
    	hook_fin_match.ajouter_callback(new Callback(new ThrowsScriptHook(ScriptHookNames.FUNNY_ACTION, null)));

    	return hook_fin_match;
    }

    /**
     * Donne les hooks des éléments de jeux à un chrono gamestate
     * @param state
     * @return
     * @throws FinMatchException 
     */
    public ArrayList<Hook> getHooksEntreScriptsChrono(GameState<RobotChrono,ReadWrite> state, int date_limite) throws FinMatchException
    {
    	if(hooks_table_chrono == null)
    		hooks_table_chrono = getHooksEntreScriptsReal(state, true);

    	// on met à jour dans les hooks les références (gridspace, robot, ...)
		// C'est bien plus rapide que de créer de nouveaux hooks
		for(Hook hook: hooks_table_chrono)
			hook.updateGameState(state);

		// Le hook de fin de match est toujours en première position
		((HookDateFinMatch)hooks_table_chrono.get(0)).updateDate(date_limite);

    	return hooks_table_chrono;
    }
    
    /**
     * Donne les hooks des éléments de jeux à un real gamestate
     * @param state
     * @return
     * @throws FinMatchException 
     */
    public ArrayList<Hook> getHooksEntreScriptsReal(GameState<RobotReal,ReadWrite> state) throws FinMatchException
    {
    	return getHooksEntreScriptsReal(state, false);
    }

    
    private ArrayList<Hook> getHooksEntreScriptsReal(GameState<?,ReadWrite> state, boolean isChrono) throws FinMatchException
    {
    	ArrayList<Hook> hooks_entre_scripts = new ArrayList<Hook>();
		Hook hook;
		GameElementDone action;
		
		// Il faut s'assurer que le hook de fin de match est toujours en première position
		hooks_entre_scripts.add(getHooksFinMatch(state.getReadOnly(), isChrono));
    	
		for(GameElementNames n: GameElementNames.values())
		{
			// Ce que l'ennemi peut prendre
			if(n.getType().isInCommon())
			{
				hook = new HookDate(log, state.getReadOnly(), n.getType().getDateEnemyTakesIt());
				action = new GameElementDone(state, n, Tribool.MAYBE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}

			// Ce qu'on peut shooter
			if(n.getType().canBeShot()) // on ne met un hook de collision que sur ceux qui ont susceptible de disparaître quand on passe dessus
			{
				hook = new HookCollisionElementJeu(log, state.getReadOnly(), n.getObstacle());
				action = new GameElementDone(state, n, Tribool.TRUE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}
			
			if(n.getType().scriptHookThrown() != null)
			{
				hook = new HookCollisionElementJeu(log, state.getReadOnly(), n.getObstacleDilate());
				ThrowsScriptHook action2 = new ThrowsScriptHook(n.getType().scriptHookThrown(), n);
				hook.ajouter_callback(new Callback(action2));
				hooks_entre_scripts.add(hook);				
			}

		}
		return hooks_entre_scripts;
    }

	public ArrayList<Hook> getHooksTable() {
		// TODO Auto-generated method stub
		return new ArrayList<Hook>();
	}

}
