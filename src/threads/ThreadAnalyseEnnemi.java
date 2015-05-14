package threads;

import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread d'analyse de l'ennemi.
 * En fonction de la position de l'ennemi, on imagine les actions qu'il fait
 * @author pf
 *
 */

public class ThreadAnalyseEnnemi extends RobotThread implements Service {

	// TODO quand/si on a une balise 
	
	public ThreadAnalyseEnnemi(Log log, Config config)
	{
		Thread.currentThread().setPriority(2);
		updateConfig();
	}

	@Override
	public void updateConfig()
	{
	}

}
