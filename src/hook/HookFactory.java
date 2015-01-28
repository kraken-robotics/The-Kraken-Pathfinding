package hook;

import java.util.ArrayList;

import obstacles.ObstacleCircular;
import hook.methods.GameElementDone;
import hook.methods.ThrowsScriptHook;
import hook.types.HookCollisionElementJeu;
import hook.types.HookCollisionObstaclesFixes;
import hook.types.HookDate;
import hook.types.HookDateFinMatch;
import hook.types.HookDemiPlan;
import hook.types.HookPosition;
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
import utils.Vec2;

/**
 * Service fabriquant des hooks à la demande.
 * @author pf, marsu
 *
 */
public class HookFactory implements Service
{
	
	//endroit ou lire la configuration du robot
	private Config config;

	//gestion des log
	private Log log;
	
	// la valeur de 20 est en mm, elle est remplcée par la valeur spécifié dans le fichier de config s'il y en a une
	private int positionTolerancy = 20;	
	private int dureeMatch = 90000;
	
	private ArrayList<Hook> hooks_table_chrono = null;
	private HookDateFinMatch hook_fin_match_chrono = null;
		
	/**
	 *  appellé uniquement par Container.
	 *  Initialise la factory
	 * 
	 * @param config fichier de config du match
	 * @param log système de d log
	 * @param realState état du jeu
	 */
	public HookFactory(Config config, Log log)
	{
		this.config = config;
		this.log = log;
		updateConfig();
	}

	public void updateConfig()
	{
		// demande avec quelle tolérance sur la précision on déclenche les hooks
		positionTolerancy = config.getInt(ConfigInfo.HOOKS_TOLERANCE_MM);		
		dureeMatch = config.getInt(ConfigInfo.DUREE_MATCH_EN_S) * 1000;
	}
	
	/* ======================================================================
	 * 							Hooks de position
	 * ======================================================================
	 */
	
	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine position sur la table
	 * la tolérance sur cette position est ici explicitement demandée et supplante celle du fichier de config
	 * @param position de déclenchement du hook
	 * @param tolerancy le hook sera déclenché si la distance entre le point de déclenchement et la position du robot est inférieure a cette valeur
	 * @return le hook créé
	 */
	public HookPosition newHookPosition(Vec2 position, int tolerancy, GameState<?> state)
	{
		return new HookPosition(config, log, state, position, tolerancy);
	}

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine position sur la table
	 * la tolérance sur cette position est ici celle du fichier de config
	 * @param position de déclenchement du hook
	 * @return le hook créé
	 */
	public HookPosition newHookPosition(Vec2 position, GameState<?> state)
	{
		return newHookPosition(position, positionTolerancy, state);
	}
	
	/* ======================================================================
	 * 							Hooks de collision
	 * ======================================================================
	 */

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot
	 * percute un objet
	 * @param position de déclenchement du hook
	 * @return le hook créé
	 * @throws FinMatchException 
	 */
	public HookCollisionElementJeu newHookCollisionElementJeu(ObstacleCircular o, GameState<?> state) throws FinMatchException
	{
		return new HookCollisionElementJeu(config, log, state, o);
	}

	public HookCollisionObstaclesFixes newHookCollisionObstaclesFixes(ObstacleCircular o, GameState<?> state) throws FinMatchException
	{
		return new HookCollisionObstaclesFixes(config, log, state);
	}

	
	/* ======================================================================
	 * 							Hooks de date
	 * ======================================================================
	 */

	/**
	 * 
	 * @param date
	 * @param state
	 * @return
	 */
	public HookDate newHookDate(long date, GameState<?> state)
	{
		return new HookDate(config, log, state, date);
	}

	public HookDateFinMatch newHookDateFinMatch(long date, GameState<?> state)
	{
		return new HookDateFinMatch(config, log, state, date);
	}

	
	/* ======================================================================
	 * 							Hooks d'abscisse (sur X)
	 * ======================================================================
	 */
	

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot a une abscisse sur la table supérieure à une certaine valeur
	 * la tolérance sur cette absisse est ici explicitement demandée et supplante celle du fichier de config
	 * L'instanciation prends en compte la couleur du robot. La condition sera "X inférieure a" si la couleur et jaune, et "X supérieur a" dans le cas cntraire.
	 * @param xValue de déclenchement du hook
	 * @return le hook créé
	 */
    public HookDemiPlan newHookXisGreater(int xValue, GameState<?> state)
    {
    	return new HookDemiPlan(config, log, state, new Vec2(xValue, 0), new Vec2(1,0));
    }    

    public HookDemiPlan newHookXisLess(int xValue, GameState<?> state)
    {
    	return new HookDemiPlan(config, log, state, new Vec2(xValue, 0), new Vec2(-1,0));
    }    


	/* ======================================================================
	 * 							Hook d'ordonnée (sur Y)
	 * ======================================================================
	 */
    
	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot a une ordonnée sur la table supérieure à une certaine valeur
	 * @param yValue de déclenchement du hook
	 * @return le hook créé
	 */
    public HookDemiPlan newHookYisGreater(int yValue, GameState<?> state)
    {
    	return new HookDemiPlan(config, log, state, new Vec2(0, yValue), new Vec2(0,1));
    }

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot a une ordonnée sur la table supérieure à une certaine valeur
	 * @param yValue de déclenchement du hook
	 * @return le hook créé
	 */
    public HookDemiPlan newHookYisLess(int yValue, GameState<?> state)
    {
    	return new HookDemiPlan(config, log, state, new Vec2(0, yValue), new Vec2(0,-1));
    }

    public HookDemiPlan newHookTrajectoireCourbe(ArrayList<Vec2> itineraire, GameState<?> state)
    {
    	return new HookDemiPlan(config, log, state, itineraire);
    }

    
    /**
     * Fournit le hook de fin de match à un chrono gamestate.
     * @param state
     * @param date_limite
     * @return
     */
    public HookDateFinMatch getHooksFinMatchChrono(GameState<?> state)
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
    public HookDateFinMatch updateHooksFinMatch(GameState<RobotChrono> state, int date_limite)
    {
    	if(hook_fin_match_chrono == null)
    		hook_fin_match_chrono = getHooksFinMatch(state, true);
    	
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
    public HookDateFinMatch getHooksFinMatchReal(GameState<?> state)
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
    private HookDateFinMatch getHooksFinMatch(GameState<?> state, boolean isChrono)
    {
        // Cette date est celle demandée à la real gamestate
        // Les chrono gamestate la modifieront si besoin est
    	HookDateFinMatch hook_fin_match = newHookDateFinMatch(dureeMatch, state);
    	hook_fin_match.ajouter_callback(new Callback(new ThrowsScriptHook(ScriptHookNames.FUNNY_ACTION, null)));

    	return hook_fin_match;
    }

    /**
     * Donne les hooks des éléments de jeux à un chrono gamestate
     * @param state
     * @return
     * @throws FinMatchException 
     */
    public ArrayList<Hook> getHooksEntreScriptsChrono(GameState<RobotChrono> state, int date_limite) throws FinMatchException
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
    public ArrayList<Hook> getHooksEntreScriptsReal(GameState<RobotReal> state) throws FinMatchException
    {
    	return getHooksEntreScriptsReal(state, false);
    }

    
    private ArrayList<Hook> getHooksEntreScriptsReal(GameState<?> state, boolean isChrono) throws FinMatchException
    {
    	ArrayList<Hook> hooks_entre_scripts = new ArrayList<Hook>();
		Hook hook;
		GameElementDone action;
		
		// Il faut s'assurer que le hook de fin de match est toujours en première position
		hooks_entre_scripts.add(getHooksFinMatch(state, isChrono));
    	
		for(GameElementNames n: GameElementNames.values())
		{
			// Ce que l'ennemi peut prendre
			if(n.getType().isInCommon())
			{
				hook = newHookDate(n.getType().getDateEnemyTakesIt(), state);
				action = new GameElementDone(state.gridspace, n, Tribool.MAYBE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}

			// Ce qu'on peut shooter
			if(n.getType().canBeShot()) // on ne met un hook de collision que sur ceux qui ont susceptible de disparaître quand on passe dessus
			{
				hook = newHookCollisionElementJeu(n.getObstacle(), state);
				action = new GameElementDone(state.gridspace, n, Tribool.TRUE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}
			
			if(n.getType().scriptHookThrown() != null)
			{
				hook = newHookCollisionElementJeu(n.getObstacleDilate(), state);
				ThrowsScriptHook action2 = new ThrowsScriptHook(n.getType().scriptHookThrown(), n);
				hook.ajouter_callback(new Callback(action2));
				hooks_entre_scripts.add(hook);				
			}

		}
		return hooks_entre_scripts;
    }

}
