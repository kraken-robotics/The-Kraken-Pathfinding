package hook.types;

import hook.Hook;
import container.Service;
import robot.RobotReal;
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
	
	// robot a surveiller pour le déclenchement des hooks
	private GameState<RobotReal> realState;
	
	// la valeur de 20 est en mm, elle est remplcée par la valeur spécifié dans le fichier de config s'il y en a une
	private int positionTolerancy = 20;
	
	// spécifie de quelle couleur est le robot (vert ou jaune). Uniquement donné par le fichier de config. // TODO: en faire une enum
	String color;
	
	
	
	/**
	 *  appellé uniquement par Container.
	 *  Initialise la factory
	 * 
	 * @param config fichier de config du match
	 * @param log système de d log
	 * @param realState état du jeu
	 */
	public HookFactory(Config config, Log log, GameState<RobotReal> realState)
	{
		this.config = config;
		this.log = log;
		this.realState = realState;
		updateConfig();
	}

	/*
	 * (non-Javadoc)
	 * @see container.Service#updateConfig()
	 */
	public void updateConfig()
	{
		// demande la couleur du robot pour ce match
		color = config.get("couleur");
		
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
	public Hook newHookPosition(Vec2 position, int tolerancy)
	{
		return new HookPosition(config, log, realState, position, tolerancy, color=="yellow");
	}

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine position sur la table
	 * la tolérance sur cette position est ici celle du fichier de config
	 * @param position de déclenchement du hook
	 * @return le hook créé
	 */
	public Hook newHookPosition(Vec2 position)
	{
		return newHookPosition(position, positionTolerancy);
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
	public Hook newHookX(float xValue, int tolerancy)
	{
		return new HookX(config, log, realState, xValue, tolerancy, color=="yellow");
	}
	

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine abscisse sur la table
	 * la tolérance sur cette absisse est ici celle du fichier de config
	 * @param xValue de déclenchement du hook
	 * @return le hook créé
	 */
	public Hook newHookX(float xValue)
	{
		return newHookX(xValue, positionTolerancy);
	}
	

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot a une abscisse sur la table supérieure à une certaine valeur
	 * la tolérance sur cette absisse est ici explicitement demandée et supplante celle du fichier de config
	 * L'instanciation prends en compte la couleur du robot. La condition sera "X inférieure a" si la couleur et jaune, et "X supérieur a" dans le cas cntraire.
	 * @param xValue de déclenchement du hook
	 * @param tolerancy le hook sera déclenché si l'écart entre l'abscisse de déclenchement et la position du robot est inférieur a cette valeur
	 * @return le hook créé
	 */
    public Hook newHookXisGreater(float xValue, float tolerancy)
    {
    	// TODO: vérifier si ce if et le color=="yellow" ne font pas double emploi (et su coup s'annulent)
        if(color=="yellow")
            return new HookXisLesser(config, log, realState, xValue, tolerancy, color=="yellow");
        return new HookXisGreater(config, log, realState, xValue, tolerancy, color=="yellow");
    }

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot a une abscisse sur la table inférieur à une certaine valeur
	 * la tolérance sur cette absisse est ici explicitement demandée et supplante celle du fichier de config
	 * L'instanciation prends en compte la couleur du robot. La condition sera "X supérieur a" si la couleur et jaune, et "X inférieur a" dans le cas cntraire.
	 * @param xValue de déclenchement du hook
	 * @param tolerancy le hook sera déclenché si l'écart entre l'abscisse de déclenchement et la position du robot est inférieur a cette valeur
	 * @return le hook créé
	 */
    public Hook newHookXisLesser(float xValue, float tolerancy)
    {
    	// TODO: vérifier si ce if et le color=="yellow" ne font pas double emploi (et su coup s'annulent)
        if(color=="yellow")
            return new HookXisGreater(config, log, realState, xValue, tolerancy, color=="yellow");
        return new HookXisLesser(config, log, realState, xValue, tolerancy, color=="yellow");
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
    public Hook newHookY(float yValue, int tolerancy)
    {
        return new HookY(config, log, realState, yValue, tolerancy);
    }
    

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot atteint une certaine ordonnée sur la table
	 * la tolérance sur cette ordonnée est ici celle du fichier de config
	 * @param yValue de déclenchement du hook
	 * @param tolerancy le hook sera déclenché si l'écart entre l'ordonnée de déclenchement et la position du robot est inférieur a cette valeur
	 * @return le hook créé
	 */
    public Hook newHookY(float yValue)
    {
        return newHookY(yValue, positionTolerancy);
    }

	/**
	 * demande l'instanciation d'un hook se déclenchant si le robot a une ordonnée sur la table supérieure à une certaine valeur
	 * @param yValue de déclenchement du hook
	 * @return le hook créé
	 */
    public Hook newHookYisGreater(float yValue)
    {
        return new HookYisGreater(config, log, realState, yValue);
    }


}
