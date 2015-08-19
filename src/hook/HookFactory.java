package hook;

import java.util.ArrayList;

import permissions.ReadOnly;
import permissions.ReadWrite;
import hook.methods.GameElementDone;
import hook.methods.ThrowScriptRequest;
import hook.types.HookDate;
import hook.types.HookDateFinMatch;
import hook.types.HookPosition;
import container.Service;
import enums.Tribool;
import exceptions.FinMatchException;
import robot.RobotChrono;
import scripts.ScriptHookNames;
import strategie.GameState;
import table.GameElementNames;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;

/**
 * Service fabriquant des hooks à la demande.
 * Les hooks sont soit simulés dans RobotChrono, soit envoyés à la STM
 * @author pf
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
	 *  appelé uniquement par Container.
	 *  Initialise la factory
	 * 
	 * @param log système de log
	 */
	public HookFactory(Log log)
	{
		this.log = log;
	}

	@Override
	public void updateConfig(Config config)
	{}
	
	@Override
	public void useConfig(Config config)
	{
		// demande avec quelle tolérance sur la précision on déclenche les hooks
		dureeMatch = config.getInt(ConfigInfo.DUREE_MATCH_EN_S) * 1000;
		Hook.useConfig(config);
	}

	/**
     * Fournit le hook de fin de match à un chrono gamestate.
     * @param state
     * @param date_limite
     * @return
     */
    private HookDateFinMatch getHooksFinMatchChrono(GameState<RobotChrono,ReadOnly> state)
    {
    	if(hook_fin_match_chrono == null)
    	{
        	hook_fin_match_chrono = new HookDateFinMatch(log, state, dureeMatch);
        	hook_fin_match_chrono.ajouter_callback(new Callback(new ThrowScriptRequest(ScriptHookNames.FUNNY_ACTION, 0)));
    	}
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
    	// On construit le hook s'il n'existe pas déjà
    	getHooksFinMatchChrono(state.getReadOnly());
    	
    	/**
    	 * Mise à jour de la date limite
    	 * Pas besoin de mettre à jour les références, car la méthode
    	 * FinMatchCheck n'en utilise pas.
    	 */
		((HookDateFinMatch)hook_fin_match_chrono).updateDate(date_limite);
	    return hook_fin_match_chrono;
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
    		hooks_table_chrono = getHooksPermanents(state);

    	// on met à jour dans les hooks les références (gridspace, robot, ...)
		// C'est bien plus rapide que de créer de nouveaux hooks
		for(Hook hook: hooks_table_chrono)
			hook.updateGameState(state);

		// Le hook de fin de match est toujours en première position
		((HookDateFinMatch)hooks_table_chrono.get(0)).updateDate(date_limite);

    	return hooks_table_chrono;
    }
    
    /**
     * Donne les hooks valables pendant tout le match à un gamestate
     * @param state
     * @return
     * @throws FinMatchException 
     */
    public ArrayList<Hook> getHooksPermanents(GameState<?,ReadWrite> state)
    {
    	ArrayList<Hook> hooksPermanents = new ArrayList<Hook>();
		Hook hook;
		GameElementDone action;
		
//		// Il faut s'assurer que le hook de fin de match est toujours en première position
//		hooksPermanents.add(getHooksFinMatch(state.getReadOnly()));
    	
		for(GameElementNames n: GameElementNames.values())
		{
			// Ce que l'ennemi peut prendre
			if(n.getType().isInCommon())
			{
				hook = new HookDate(log, state.getReadOnly(), n.getType().getDateEnemyTakesIt());
				action = new GameElementDone(state, n, Tribool.MAYBE);
				hook.ajouter_callback(new Callback(action));
				hooksPermanents.add(hook);
			}

			// Ce qu'on peut shooter
			if(n.getType().canBeShot()) // on ne met un hook de collision que sur ceux qui ont susceptible de disparaître quand on passe dessus
			{
//				hook = new HookCollisionElementJeu(log, state.getReadOnly(), n.getObstacle());
				hook = new HookPosition(log, state.getReadOnly(), n.getObstacle().position, n.getObstacleDilate().getRadius());
				action = new GameElementDone(state, n, Tribool.TRUE);
				hook.ajouter_callback(new Callback(action));
				hooksPermanents.add(hook);
			}
			
			if(n.getType().scriptHookThrown() != null)
			{
//				hook = new HookCollisionElementJeu(log, state.getReadOnly(), n.getObstacleDilate());
				hook = new HookPosition(log, state.getReadOnly(), n.getObstacle().position, n.getObstacleDilate().getRadius());
				ThrowScriptRequest action2 = new ThrowScriptRequest(n.getType().scriptHookThrown(), n.ordinal());
				hook.ajouter_callback(new Callback(action2));
				hooksPermanents.add(hook);				
			}

		}
		return hooksPermanents;
    } 

}
