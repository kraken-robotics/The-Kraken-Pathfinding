package table;

import java.util.Iterator;

import obstacles.ObstacleProximity;
import utils.Log;

/**
 * Itérator permettant de manipuler facilement les obstacles mobiles
 * @author pf
 *
 */

public class ObstaclesMobilesIterator implements Iterator<ObstacleProximity>
{
    private Log log;
    private ObstaclesMobilesMemory memory;
    
    private int firstNotDead = 0;
    private int nbTmp;
    private long lastDate = -1;
	
    public ObstaclesMobilesIterator(Log log, ObstaclesMobilesMemory memory)
    {
        this.log = log;
        this.memory = memory;
    }
    
    public ObstaclesMobilesIterator clone(long date)
    {
    	ObstaclesMobilesIterator cloned_manager = new ObstaclesMobilesIterator(log, memory);
		copy(cloned_manager, date);
		return cloned_manager;
    }
    
    /**
     * Nécessaire au fonctionnement du memory manager
     * @param other
     */
    public void copy(ObstaclesMobilesIterator other, long date)
    {
    	other.firstNotDead = firstNotDead;
    	other.init(date);
    }
    
    /**
     * Utilisé afin de calculer la péremption du cache du gridspace
     * @param other
     * @return
     */
    public boolean equals(ObstaclesMobilesIterator other)
    {
        return firstNotDead == other.firstNotDead;
    }

	/**
	 * Quelque chose change si un obstacle disparaît
	 * Si jamais rien ne change, renvoie Long.MAX_VALUE
	 * @return
	 */
	public long getDateSomethingChange()
	{
	    if(firstNotDead < memory.nbMax())
	    	return memory.getObstacle(firstNotDead).getDeathDate();
	    else
	    	return Long.MAX_VALUE;
	}
	
	/**
	 * Réinitialise l'itérateur avec la date donnée à la dernière initialisation
	 */
	public void reinit()
	{
		nbTmp = firstNotDead;
	}
	
	public int getFirstNotDead()
	{
		return firstNotDead;
	}
	
	/**
	 * Calcule l'entrée où commence les obstacles maintenant
	 */
	public void initNow()
	{
		firstNotDead = memory.getFirstNotDeadNow();
		nbTmp = firstNotDead;
	}
	
	/**
	 * Calcule l'entrée où commence les obstacles à cette date.
	 * Se fait à la copie.
	 * @param date
	 */
	private void init(long date)
	{
		// Si on a avancé dans le futur, on sait que firstNotDead ne peut qu'être plus grand
		if(date < lastDate)
			firstNotDead = memory.getFirstNotDeadNow();
		
		ObstacleProximity next;
		while(firstNotDead < memory.nbMax())
		{
			next = memory.getObstacle(firstNotDead);
			if(next.isDestructionNecessary(date))
				firstNotDead++;
			else
				break;
		}
		nbTmp = firstNotDead;		
		lastDate = date;
	}
	
	@Override
	public boolean hasNext()
	{		
		return nbTmp < memory.nbMax();
	}
	
	@Override
	public ObstacleProximity next()
	{
		return memory.getObstacle(nbTmp++);
	}

	@Override
	public void remove()
	{
		// TODO lancer exception
	}

	

}
