package obstacles.memory;

import utils.Log;

/**
 * Itérator permettant de manipuler facilement les obstacles mobiles du présent
 * @author pf
 *
 */

public class ObstaclesIteratorPresent extends ObstaclesIterator
{
    public ObstaclesIteratorPresent(Log log, ObstaclesMemory memory)
    {
    	super(log, memory);
    }
    
	/**
	 * Calcule l'entrée où commencent les obstacles maintenant
	 */
	public void reinit()
	{
		nbTmp = memory.getFirstNotDeadNow();
	}
	
	// Pour parcourir tous ceux qui sont morts
	public boolean hasNextDead()
	{
//		log.debug(nbTmp+" "+memory.getFirstNotDeadNow());
		return nbTmp < memory.getFirstNotDeadNow();
	}
}
