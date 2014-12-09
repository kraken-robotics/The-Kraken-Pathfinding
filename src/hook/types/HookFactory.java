package hook.types;

import java.util.ArrayList;

import obstacles.GameElement;
import hook.Callback;
import hook.Hook;
import hook.methods.GameElementDone;
import container.Service;
import enums.GameElementType;
import enums.RobotColor;
import enums.Tribool;
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
	
	// spécifie de quelle couleur est le robot (vert ou jaune). Uniquement donné par le fichier de config.
	RobotColor color;
	
	
	
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
		// demande la couleur du robot pour ce match
		color = RobotColor.parse(config.get("couleur"));
		
		// demande avec quelle tolérance sur la précision on déclenche les hooks
		positionTolerancy = Integer.parseInt(this.config.get("hooks_tolerance_mm"));		
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
		return new HookPosition(config, log, state, position, tolerancy, color == RobotColor.YELLOW);
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
		return new HookX(config, log, state, xValue, tolerancy, color == RobotColor.YELLOW);
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
    	// TODO: vérifier si ce if et le color=="yellow" ne font pas double emploi (et su coup s'annulent)
        if(color == RobotColor.YELLOW)
            return new HookXisLesser(config, log, state, xValue, tolerancy, color == RobotColor.YELLOW);
        return new HookXisGreater(config, log, state, xValue, tolerancy, color == RobotColor.YELLOW);
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
    	// TODO: vérifier si ce if et le color=="yellow" ne font pas double emploi (et su coup s'annulent)
        if(color == RobotColor.YELLOW)
            return new HookXisGreater(config, log, state, xValue, tolerancy, color == RobotColor.YELLOW);
        return new HookXisLesser(config, log, state, xValue, tolerancy, color == RobotColor.YELLOW);
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

    public ArrayList<Hook> getHooksEntreScripts(GameState<?> state)
    {
    	ArrayList<Hook> hooks_entre_scripts = new ArrayList<Hook>();
		GameElement[] obstacles = state.table.getObstacles();
		Hook hook;
		GameElementDone action;
		for(GameElement o: obstacles)
		{
			// L'ennemi peut prendre les distributeurs
			if(o.isDone() == Tribool.FALSE && o.getName().getType() == GameElementType.DISTRIBUTEUR)
			{
				hook = newHookDate(20000, state);
				action = new GameElementDone(o, Tribool.MAYBE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}
			else if(o.isDone() == Tribool.FALSE && o.getName().getType() == GameElementType.VERRE)
			{
				hook = newHookDate(20000, state);
				action = new GameElementDone(o, Tribool.MAYBE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}
			// Les éléments de jeu avec un rayon négatif sont ceux qu'on ne peut pas percuter.
			// Exemple: clap, distributeur. 
			if(o.isDone() != Tribool.TRUE && o.getRadius() > 0)
			{
				hook = newHookPosition(o.getPosition(), o.getRadius(), state);
				action = new GameElementDone(o, Tribool.TRUE);
				hook.ajouter_callback(new Callback(action));
				hooks_entre_scripts.add(hook);
			}
		}
		return hooks_entre_scripts;
    }

}
