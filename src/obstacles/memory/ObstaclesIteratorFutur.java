package obstacles.memory;

import utils.Log;

/**
 * Itérator permettant de manipuler facilement les obstacles mobiles dans le GameState
 * @author pf
 *
 */

public class ObstaclesIteratorFutur extends ObstaclesIterator
{
   
    private int firstNotDead = 0;
    private long dateInit = -1;
	
    public ObstaclesIteratorFutur(Log log, ObstaclesMemory memory)
    {
    	super(log, memory);
    }
    
    public ObstaclesIteratorFutur clone()
    {
    	ObstaclesIteratorFutur cloned_manager = new ObstaclesIteratorFutur(log, memory);
		copy(cloned_manager, dateInit);
		return cloned_manager;
    }
    
    /**
     * Nécessaire au fonctionnement du memory manager
     * @param other
     */
    public void copy(ObstaclesIteratorFutur other, long date)
    {
    	other.init(date);
    }

	/**
	 * Réinitialise l'itérateur avec la date donnée à la dernière initialisation
	 */
	public void reinit()
	{
		nbTmp = firstNotDead;
	}

	/**
	 * Calcule l'entrée où commencent les obstacles à cette date.
	 * Se fait à la copie.
	 * @param date
	 */
	public void init(long date)
	{
		// Si on a avancé dans le futur, on sait que firstNotDead ne peut qu'être plus grand
		if(date < dateInit)
			log.critical("Un iterator d'obstacles ne peut pas remonter le temps !");

		while(firstNotDead < memory.size())
		{
			if(memory.isDestructionNecessary(firstNotDead, date))
				firstNotDead++;
			else
				break;
		}
		nbTmp = firstNotDead;		
		dateInit = date;
	}
}
