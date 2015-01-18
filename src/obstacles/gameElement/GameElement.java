package obstacles.gameElement;

import obstacles.ObstacleCircular;
import utils.Config;
import utils.Log;

/**
 * Un élément de jeu.
 * On demande à ce qu'il soit circulaire afin de faciliter les calculs de collision.
 * @author pf
 *
 */

public class GameElement extends ObstacleCircular
{
	public GameElement(Log log, Config config, GameElementNames nom)
	{
		super(nom.getPosition(), nom.getRadius());
	}
}
