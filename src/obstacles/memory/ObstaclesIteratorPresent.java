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
		nbTmp = memory.getFirstNotDeadNow() - 1;
	}
	
	/**
	 * Pour parcourir tous ceux qui sont morts (utilisé par le GridSpace)
	 * @return
	 */
	public boolean hasNextDead()
	{
		return nbTmp + 1 < memory.getFirstNotDeadNow();
	}
}
