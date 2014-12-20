package hook.methods;

import pathfinding.GridSpace;
import enums.GameElementNames;
import enums.Tribool;
import hook.Executable;

/**
 * Modifie l'état des éléments de jeux
 * @author pf
 *
 */

public class GameElementDone implements Executable {

	private GridSpace gridspace;
	private Tribool done;
	private GameElementNames element;
	
	public GameElementDone(GridSpace gridspace, GameElementNames element, Tribool done)
	{
		this.gridspace = gridspace;
		this.done = done;
		this.element = element;
	}
	
	@Override
	public void execute()
	{
		// on ne peut faire qu'augmenter l'état d'un élément de jeu.
		// c'est-à-dire qu'on peut passer de FALSE à MAYBE et TRUE
		// et de MAYBE à TRUE.
		// Les autres transitions sont interdites (en particulier passer de TRUE à MAYBE...)
		// TODO: créer une méthode "executable"? ça éviterait de faire les comparaisons de trucs inutiles
		if(gridspace.isDone(element).hash < done.hash)
			gridspace.setDone(element, done);
	}

}
