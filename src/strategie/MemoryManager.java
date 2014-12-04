package strategie;

import java.util.Vector;

import robot.RobotChrono;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Config;
import container.Service;
import exceptions.FinMatchException;

/**
 * Classe qui gère les objets utilisés dans l'arbre des possibles de la stratégie
 * @author pf
 */

public class MemoryManager implements Service {

	private Vector<GameState<RobotChrono>> products = new Vector<GameState<RobotChrono>>();
	private GameState<RobotReal> real_state;
	private GameState<RobotChrono> out;
	private Log log;

	private Vec2 obstacle_position = null;
	
	public MemoryManager(Config config, Log log, GameState<RobotReal> real_state) throws FinMatchException
	{
		this.log = log;
	    this.real_state = real_state;
	    products.add(real_state.cloneGameState());
	}
	
	/**
	 * Fournit un clone pour la profondeur donnée. Augmente la taille si besoin est.
	 * @param profondeur. Il s'agit d'un entier entre 0 et beaucoup. Il doit s'agir de la profondeur de l'arbre: l'instance de profondeur 3 est le fils de l'instance de profondeur 2.
	 * @return
	 * @throws FinMatchException 
	 */
	public GameState<RobotChrono> getClone(int profondeur) throws FinMatchException
	{
		// On doit agrandit products
		if(products.size() <= profondeur)
		{
			log.debug("Aggrandissement arbre", this);
			GameState<RobotChrono> dernier = products.get(products.size()-1);
	
		    while(products.size() <= profondeur)
	            products.add(dernier.cloneGameState());
		    return products.get(profondeur);
		    // c'est déjà à jour (vu qu'on vient de faire un clone)
		    // pas besoin de copy en plus
		}
	    
        out = products.get(profondeur);
        
        // Si la profondeur vaut 0, alors l'arbre veut un clone de real_state
        if(profondeur == 0)
        {
        	// ça veut dire qu'on commence un nouvel arbre
        	real_state.commenceRacine();
            real_state.copy(out);
            if(obstacle_position != null)
            {
            	out.gridspace.creer_obstacle(obstacle_position);
            	obstacle_position = null;
            }
        }
        else
            products.get(profondeur-1).copy(out);
        
	    return out;
	}
	
	@Override
	public void updateConfig()
	{}
		

	public void obstacle_dans_prochain_arbre(Vec2 pos)
	{
		obstacle_position = pos;
	}

}
