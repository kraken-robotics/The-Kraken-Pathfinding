package hook.types;

import java.util.ArrayList;

import hook.Callback;
import hook.Hook;
import hook.methods.GameElementDone;
import container.Service;
import enums.ConfigInfo;
import enums.GameElementNames;
import enums.GameElementType;
import enums.Tribool;
import robot.RobotChrono;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Config;

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
	
	private ArrayList<Hook> hooks_table_chrono = null;
	
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

	/*
	 * (non-Javadoc)
	 * @see container.Service#updateConfig()
	 */
	public void updateConfig()
	{
		// demande avec quelle tolérance sur la précision on déclenche les hooks
		positionTolerancy = Integer.parseInt(this.config.get(ConfigInfo.HOOKS_TOLERANCE_MM));		
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
	public Hook newHookPosition(Vec2 position, int tolerancy, GameState<?> state)
	{
		return new HookPosition(config, log, state, position, tolerancy);
	}

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine position sur la table
	 * la tolérance sur cette position est ici celle du fichier de config
	 * @param position de déclenchement du hook
	 * @return le hook créé
	 */
	public Hook newHookPosition(Vec2 position, GameState<?> state)
	{
		return newHookPosition(position, positionTolerancy, state);
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
	public Hook newHookDate(long date, GameState<?> state)
	{
		return new HookDate(config, log, state, date);
	}

	
	/* ======================================================================
	 * 							Hooks d'abscisse (sur X)
	 * ======================================================================
	 */
	

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine abscisse sur la table
	 * la tolérance sur cette absisse est ici explicitement demandée et supplante celle du fichier de config
	 * @param xValue de déclenchement du hook
	 * @param tolerancy le hook sera déclenché si l'écart entre l'abscisse de déclenchement et la position du robot est inférieur a cette valeur
	 * @return le hook créé
	 */
	public Hook newHookX(float xValue, int tolerancy, GameState<?> state)
	{
		return new HookX(config, log, state, xValue, tolerancy);
	}
	

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine abscisse sur la table
	 * la tolérance sur cette absisse est ici celle du fichier de config
	 * @param xValue de déclenchement du hook
	 * @return le hook créé
	 */
	public Hook newHookX(float xValue, GameState<?> state)
	{
		return newHookX(xValue, positionTolerancy, state);
	}
	

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot a une abscisse sur la table supérieure à une certaine valeur
	 * la tolérance sur cette absisse est ici explicitement demandée et supplante celle du fichier de config
	 * L'instanciation prends en compte la couleur du robot. La condition sera "X inférieure a" si la couleur et jaune, et "X supérieur a" dans le cas cntraire.
	 * @param xValue de déclenchement du hook
	 * @param tolerancy le hook sera déclenché si l'écart entre l'abscisse de déclenchement et la position du robot est inférieur a cette valeur
	 * @return le hook créé
	 */
    public Hook newHookXisGreater(float xValue, float tolerancy, GameState<?> state)
    {
        return new HookXisGreater(config, log, state, xValue, tolerancy);
    }

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot a une abscisse sur la table inférieur à une certaine valeur
	 * la tolérance sur cette absisse est ici explicitement demandée et supplante celle du fichier de config
	 * L'instanciation prends en compte la couleur du robot. La condition sera "X supérieur a" si la couleur et jaune, et "X inférieur a" dans le cas cntraire.
	 * @param xValue de déclenchement du hook
	 * @param tolerancy le hook sera déclenché si l'écart entre l'abscisse de déclenchement et la position du robot est inférieur a cette valeur
	 * @return le hook créé
	 */
    public Hook newHookXisLesser(float xValue, float tolerancy, GameState<?> state)
    {
        return new HookXisLesser(config, log, state, xValue, tolerancy);
    }
    
    

	/* ======================================================================
	 * 							Hook d'ordonnée (sur Y)
	 * ======================================================================
	 */
    
	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine ordonnée sur la table
	 * la tolérance sur cette ordonnée est ici explicitement demandée et supplante celle du fichier de config
	 * @param yValue de déclenchement du hook
	 * @param tolerancy le hook sera déclenché si l'écart entre l'ordonnée de déclenchement et la position du robot est inférieur a cette valeur
	 * @return le hook créé
	 */
    public Hook newHookY(float yValue, int tolerancy, GameState<?> state)
    {
        return new HookY(config, log, state, yValue, tolerancy);
    }
    

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine ordonnée sur la table
	 * la tolérance sur cette ordonnée est ici celle du fichier de config
	 * @param yValue de déclenchement du hook
	 * @param tolerancy le hook sera déclenché si l'écart entre l'ordonnée de déclenchement et la position du robot est inférieur a cette valeur
	 * @return le hook créé
	 */
    public Hook newHookY(float yValue, GameState<?> state)
    {
        return newHookY(yValue, positionTolerancy, state);
    }

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot a une ordonnée sur la table supérieure à une certaine valeur
	 * @param yValue de déclenchement du hook
	 * @return le hook créé
	 */
    public Hook newHookYisGreater(float yValue, GameState<?> state)
    {
        return new HookYisGreater(config, log, state, yValue);
    }

    public ArrayList<Hook> getHooksEntreScriptsChrono(GameState<RobotChrono> state)
    {
    	if(hooks_table_chrono == null)
    		hooks_table_chrono = getHooksEntreScriptsReal(state);
    	else
    		// on met à jour dans les hooks les références (gridspace, robot, ...)
    		// C'est bien plus rapide que de créer de nouveaux hooks
    		for(Hook hook: hooks_table_chrono)
    			hook.updateGameState(state);

    	return hooks_table_chrono;
    }
    
    public ArrayList<Hook> getHooksEntreScriptsReal(GameState<?> state)
    {
    	ArrayList<Hook> hooks_entre_scripts = new ArrayList<Hook>();
		Hook hook;
		GameElementDone action;
		for(GameElementNames n: GameElementNames.values())
		{
			// L'ennemi peut prendre les distributeurs
			if(/*state.gridspace.isDone(n) == Tribool.FALSE && */n.getType() == GameElementType.DISTRIBUTEUR)
			{
				hook = newHookDate(20000, state);
				action = new GameElementDone(state.gridspace, n, Tribool.MAYBE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}
			else if(/*state.gridspace.isDone(n) == Tribool.FALSE && */n.getType() == GameElementType.VERRE)
			{
				hook = newHookDate(20000, state);
				action = new GameElementDone(state.gridspace, n, Tribool.MAYBE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}
			// Les éléments de jeu avec un rayon négatif sont ceux qu'on ne peut pas percuter.
			// Exemple: clap, distributeur. 
			if(/*state.gridspace.isDone(n) != Tribool.TRUE &&*/ n.getRadius() > 0)
			{
				hook = newHookPosition(n.getPosition(), n.getRadius(), state);
				action = new GameElementDone(state.gridspace, n, Tribool.TRUE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}
		}
		return hooks_entre_scripts;
    }

}
