package strategie;

import java.util.Vector;

import robot.RobotChrono;
import robot.RobotVrai;
import strategie.GameState;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Classe qui gère les objets utilisés dans l'arbre des possibles de la stratégie
 * Benchmark (eeepc): pour 1000 tables, il y a 35ms d'instanciation et 70ms d'affectation
 * @author pf
 */

public class MemoryManager implements Service {

	private Vector<GameState<RobotChrono>> products = new Vector<GameState<RobotChrono>>();
	private GameState<RobotVrai> real_state;
	private GameState<RobotChrono> out ;
	
	public MemoryManager(Read_Ini config, Log log, GameState<RobotVrai> real_state)
	{
	    this.real_state = real_state;
	    products.add(real_state.clone());
	}
	
	/**
	 * Fournit un clone pour la profondeur donnée. Augmente la taille si besoin est.
	 * @param profondeur. Il s'agit d'un entier entre 0 et beaucoup. Il doit s'agir de la profondeur de l'arbre: l'instance de profondeur 3 est le fils de l'instance de profondeur 2.
	 * @return
	 */
	public GameState<RobotChrono> getClone(int profondeur)
	{
	    GameState<RobotChrono> dernier = products.get(products.size()-1);

	    // On agrandit products si besoin est
	    while(products.size() <= profondeur)
            products.add(dernier.clone());
	    
        out = products.get(profondeur);
        
        // Si la profondeur vaut 0, alors l'arbre veut un clone de real_state
        if(profondeur == 0)
        {
            real_state.time_depuis_debut = System.currentTimeMillis() - ThreadTimer.date_debut;
            real_state.time_depuis_racine = 0;
            real_state.copy(out);
        }
        else
            products.get(profondeur-1).copy(out);
        
	    return out;
	}
	
	@Override
	public void maj_config()
	{}
			
}
